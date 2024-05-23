import CICDEnvUtils
import com.apigee.boot.ConfigType
import com.apigee.boot.Pipeline
import com.apigee.cicd.service.AssetService
import com.apigee.cicd.service.CIEnvInfoService
import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.DeploymentInfoService
import com.apigee.cicd.service.OrgInfoService
import com.apigee.loader.BootStrapConfigLoad
import groovy.json.JsonSlurper
import groovy.transform.Field
import hudson.AbortException
import hudson.model.Cause
import pom

/*
This pipeline is used to perform CI on sharedflows
 */

def call(String branchType, String build_number) {

  node {
    deleteDir()
    try {

      stage('Checkout') {
        checkout scm
      }
      echo " Stating CiPipeline for branchType = ${branchType}"

      Maven maven = new Maven()
      JenkinsUserUtils jenkinsUserUtils = new JenkinsUserUtils()
      Npm npm= new Npm()
      def pom = new pom(),
          proxyRootDirectory = "edge",
          artifactId = pom.artifactId("./${proxyRootDirectory}/pom.xml"),
          version =pom.version("./${proxyRootDirectory}/pom.xml"),
          entityDeploymentInfos

      echo artifactId


      withFolderProperties {
        BootStrapConfigLoad configLoad = new BootStrapConfigLoad();
        try {
          configLoad.setupConfig("${env.API_SERVER_LOCATION}")
          configLoad.setupAssetConfiguration("${env.API_SERVER_LOCATION}","${artifactId}")
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }

      /*
          Populating asset deployment data
       */
      AssetService.instance.data.branchMappings.each{ branchMapping ->
        if(branchMapping.branchPattern =~ branchType)
        {
          entityDeploymentInfos=branchMapping.deploymentInfo
        }
      }

      dir(proxyRootDirectory) {

        if (DefaultConfigService.instance.steps.unitTest) {
          stage('unit-init') {
            if (fileExists("test/unit")) {
              echo "run unit tests "
              npm.runCommand("npm install")
              npm.runCommand("node node_modules/istanbul/lib/cli.js cover --dir target/unit-init-coverage node_modules/mocha/bin/_mocha test/unit")
              if (true) {
                echo "publish html report"
                publishHTML(target: [
                        allowMissing         : false,
                        alwaysLinkToLastBuild: false,
                        keepAll              : true,
                        reportDir            : "target/unit-init-coverage/lcov-report",
                        reportFiles          : 'index.html',
                        reportName           : 'Code Coverage HTML Report'
                ])
              }
            }
          }
        }

        if (DefaultConfigService.instance.steps.deploy) {

          stage('build-sharedflow') {
            maven.runCommand("mvn package -Phybrid-sharedflow")
          }

          stage('deploy-sharedflow') {
            entityDeploymentInfos.each {
              withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                echo "deploying sharedflow"
                maven.runCommand("mvn -X package apigee-enterprise:deploy -Phybrid-sharedflow -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount}")
              }

              DeploymentInfoService.instance.setApiName(artifactId)
              DeploymentInfoService.instance.setApiVersion(version)
              DeploymentInfoService.instance.setEdgeEnv("${it.env}")
              DeploymentInfoService.instance.saveDeploymentStatus("DEPLOYMENT-SUCCESS", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())
            }
          }

//          if (DefaultConfigService.instance.steps.release) {
//            stage('upload-artifact') {
//              if ((version as String).trim().endsWith("-SNAPSHOT")) {
//                maven.runMaven(" -Ddeployment.suffix=cicd ", "package deploy")
//              }
//            }
//
//          }
        }
      }

    } catch (any) {
      println any.toString()
      JenkinsUserUtils jenkinsUserUtils = new JenkinsUserUtils()
      currentBuild.result = 'FAILURE'
      DeploymentInfoService.instance.saveDeploymentStatus("FAILURE", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())
    }
  }
}






