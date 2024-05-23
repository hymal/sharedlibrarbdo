import CICDEnvUtils
import com.apigee.cicd.service.AssetService
import com.apigee.loader.BootStrapConfigLoad
import groovy.json.JsonSlurper
import groovy.transform.Field
import hudson.AbortException
import hudson.model.Cause
import pom
import com.apigee.boot.Pipeline
import com.apigee.cicd.service.CIEnvInfoService
import com.apigee.cicd.service.DeploymentInfoService
import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.FunctionalTestService
import com.apigee.cicd.service.OrgInfoService
import Maven
import JenkinsUserUtils


/*
This pipeline is used for performing CI on apirpoxies
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

                    stage('build-proxy') {
                        maven.runCommand("mvn package -Phybrid-apiproxy")
                    }

                    if (DefaultConfigService.instance.steps.lint) {
                        stage('proxy-lint') {
                            echo "testing apigee-lint"
                            npm.runCommand("npm install")
                            npm.runCommand("apigeelint -s apiproxy -e BN001 -f html.js > target/apigeelint.html")
                            if (true) {
                                echo "publish html report"
                                publishHTML(target: [
                                        allowMissing         : false,
                                        alwaysLinkToLastBuild: false,
                                        keepAll              : true,
                                        reportDir            : 'target',
                                        reportFiles          : 'apigeelint.html',
                                        reportName           : 'Apigeelint HTML Report'
                                ])
                            }
                        }

                    }

                entityDeploymentInfos.each {
                        stage('pre-deploy-prep') {
                            withCredentials([file(credentialsId: it.org, variable: 'file')]) {
                                echo "load api product for integration init"
                                //maven.runCommand("mvn -X package apigee-config:targetservers -Phybrid-apiproxy -Dapigee.config.options=update -Dorg=${it.org} -Denv=${it.env} -Dfile=${file}")
                            }
                        }

                        stage('deploy-proxy') {

                            withCredentials([file(credentialsId: it.org, variable: 'file')]) {
                                echo "deploying apirpoxy"
                                maven.runCommand("mvn -X package apigee-enterprise:deploy -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${file}")
                            }
                            DeploymentInfoService.instance.setApiName(artifactId)
                            DeploymentInfoService.instance.setApiVersion(version)
                            DeploymentInfoService.instance.setEdgeEnv("${it.env}")
                            DeploymentInfoService.instance.saveDeploymentStatus("DEPLOYMENT-SUCCESS", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())

                        }


                        // stage('post-deploy') {

                        //     withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                        //         if (fileExists("resources/edge/org/apiProducts.json")) {
                        //             echo "load api product for integration init"
                        //             maven.runCommand("mvn -X apigee-config:apiproducts -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                        //         }
                        //         if (fileExists("resources/edge/org/developers.json")) {
                        //             echo "load api developer for integration init"
                        //             maven.runCommand("mvn -X apigee-config:developers -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                        //         }
                        //         if (fileExists("resources/edge/org/developerApps.json")) {
                        //             echo "load api developer app for integration init"
                        //             maven.runCommand("mvn -X apigee-config:apps -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                        //             echo "export app key for integration init"
                        //             maven.runCommand("mvn -X apigee-config:exportAppKeys -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                        //         }
                        //     }

                        // }

//
//
//                    if (DefaultConfigService.instance.steps.functionalTest) {
//                        stage('integration-tests') {
//                            if (fileExists("test/integration")) {
//                                if (new FunctionalTestService().isFunctionalTestEnabled(branchType)) {
//                                    if (fileExists("node_modules/cucumber/bin/cucumber-js")) {
////                                        dir("target/test/integration") {
//                                        /*
//                                        Setting test-config and auth server for integration test cases
//                                         */
//                                            def env=it.env
//                                            def org=it.org
//                                            def setApiDomain = readJSON file: "target/test/integration/test-config.json"
//                                            def setAuthDomain = readJSON file: "target/test/integration/auth-server.json"
//                                            setApiDomain.domains[env]=it.host
//                                            //setApiDomain[artifactId].org=org
//                                            //setApiDomain[artifactId].product=artifactId+"Product-"+env
//                                            //setApiDomain[artifactId].app=artifactId+"App-"+env
//                                            //setApiDomain[artifactId].env=env
//                                            setAuthDomain.domain=it.host
//                                            writeJSON file: 'target/test/integration/test-config.json', json: setApiDomain
//                                            writeJSON file: 'target/test/integration/auth-server.json', json: setAuthDomain
////                                        }
//                                        echo "run integration init"
//                                        npm.runCommand("node ./node_modules/cucumber/bin/cucumber-js target/test/integration/features --format json:target/reports.json")
//                                        echo "generate integration init report "
//                                        step([
//                                                $class             : 'CucumberReportPublisher',
//                                                fileExcludePattern : '',
//                                                fileIncludePattern : "*reports.json",
//                                                ignoreFailedTests  : false,
//                                                jenkinsBasePath    : '',
//                                                jsonReportDirectory: "target",
//                                                missingFails       : false,
//                                                parallelTesting    : false,
//                                                pendingFails       : false,
//                                                skippedFails       : false,
//                                                undefinedFails     : false
//                                        ])
//
//                                    }
//                                } else {
//                                    echo "Functional Tests are not enabled for ${branchType}"
//                                }
//                                DeploymentInfoService.instance.saveDeploymentStatus("INTEG-TEST-EXECUTED", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())
//                            }
//                        }
//                    }


//                    stage('undeploy-org-config') {
//                        withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
//                            if (fileExists("resources/edge/org/developerApps.json")) {
//                                echo "delete developer app"
//                                maven.runCommand("mvn -X apigee-config:apps -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=delete")
//                            }
//                            if (fileExists("resources/edge/org/apiProducts.json")) {
//                                echo "delete api product"
//                                maven.runCommand("mvn -X apigee-config:apiproducts -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=delete")
//                            }
//                            if (fileExists("resources/edge/org/developers.json")) {
//                                echo "delete developer"
//                                maven.runCommand("mvn -X apigee-config:developers -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=delete")
//                            }
//                        }
//                    }
                }



            if (DefaultConfigService.instance.steps.release) {
                        stage('upload-artifact') {
                            withCredentials([file(credentialsId: 'artifact-registry', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                            maven.runCommand("mvn -X deploy")
                        }
                        }

                    }
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