import CICDEnvUtils
import com.apigee.boot.ConfigType
import com.apigee.boot.Pipeline
import com.apigee.cicd.service.AssetService
import com.apigee.cicd.service.CIEnvInfoService
import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.FunctionalTestService
import com.apigee.cicd.service.OrgInfoService
import com.apigee.loader.BootStrapConfigLoad
import com.apigee.cicd.service.DeploymentInfoService
import groovy.json.JsonSlurper
import hudson.AbortException
import hudson.model.Cause
import pom
import Maven
import JenkinsUserUtils
import shell

/*
This pipeline is used to perform CD on apiproxies
 */
def call(String build_number, String repoProjectName) {
    node {
        deleteDir()
        def shell = new shell()

        try {

            stage('init') {


                // TeamService service = new TeamService();

                if (!params.projectName) {
                    error "Project Name is Required "
                }

                if (!params.buName) {
                    error "buName is Required "
                }

                if (!params.Service_functionality) {
                    error "Service_functionality Name is Required "
                }
                if (!params.Versions) {
                    error "Version Name is Required "
                }

                if (!params.version) {
                    error "Version is Required "
                }
            }

            withCredentials([
                    [$class          : 'UsernamePasswordMultiBinding',
                     credentialsId   : "bb-scm-cred",
                     usernameVariable: 'scmUser',
                     passwordVariable: 'scmPassword'],
                    [$class          : 'UsernamePasswordMultiBinding',
                     credentialsId   : "bb-scm-oauth-cred",
                     usernameVariable: 'scmClient',
                     passwordVariable: 'scmSecret'],
            ])
                    {

                        withFolderProperties {

                            BootStrapConfigLoad configLoad = new BootStrapConfigLoad();
                            try {
                                scmAPILocation = env.API_SCM_LOCATION
                                scmOauthServerLocation = env.API_SCM_OAUTH_SERVER
                                configLoad.setupConfig("${env.API_SERVER_LOCATION}")
                                configLoad.setupAssetConfiguration("${env.API_SERVER_LOCATION}", "${buName}-${projectName}-${Service_functionality}-${Versions}")
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }

                        // stage("get scm token") {
                        //     def userDetails = "${env.scmClient}:${env.scmSecret}";
                        //     def encodedUser = userDetails.bytes.encodeBase64().toString()
                        //     def response = httpRequest httpMode: 'POST',
                        //             customHeaders: [[name: "Authorization", value: "Basic ${encodedUser}"], [name: "content-type", value: "application/x-www-form-urlencoded"]],
                        //             url: "${scmOauthServerLocation}",
                        //             requestBody: "grant_type=client_credentials"
                        //     def responseJson = new JsonSlurper().parseText(response.content)
                        //     scmAccessToken = responseJson.access_token
                        // }

                        stage('Checkout') {
                                shell.pipe("git clone https://${scmUser}:${scmPassword}@bitbucket.org/${repoProjectName}/${buName}-${projectName}-${Service_functionality}-${Versions}.git")
                        }
                    }
            dir("${buName}-${projectName}-${Service_functionality}-${Versions}") {
                shell.pipe("git checkout tags/${params.version} -b ${params.version}")
                Maven maven = new Maven()
                JenkinsUserUtils jenkinsUserUtils = new JenkinsUserUtils()
                Npm npm = new Npm()
                def pom = new pom(),
                    proxyRootDirectory = "edge",
                    artifactId = pom.artifactId("./${proxyRootDirectory}/pom.xml"),
                    version = pom.version("./${proxyRootDirectory}/pom.xml"),
                    entityDeploymentInfos


                /*
                    Populating asset deployment data
                */
                AssetService.instance.data.branchMappings.each { branchMapping ->
                    if (branchMapping.branchPattern.contains("RC")) {
                        entityDeploymentInfos = branchMapping.deploymentInfo
                    }
                }

                dir(proxyRootDirectory) {

                    stage('build-proxy') {
                        maven.runCommand("mvn package -Phybrid-apiproxy")
                    }

                    entityDeploymentInfos.each {

                        stage('pre-deploy-prep') {


                            if (fileExists("resources/edge/env/${it.env}/targetServers.json"))
                             withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                                echo "load configs "
                                //maven.runCommand("mvn -X package apigee-config:targetservers -Phybrid-apiproxy -Dapigee.config.options=update -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount}")
                            }

                        }

                        stage('deploy-proxy') {

                            withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                                echo "deploying apiproxy"
                                maven.runCommand("mvn -X package apigee-enterprise:deploy -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount}")
                            }
                            DeploymentInfoService.instance.setApiName(artifactId)
                            DeploymentInfoService.instance.setApiVersion(version)
                            DeploymentInfoService.instance.setEdgeEnv("${it.env}")
                            DeploymentInfoService.instance.saveDeploymentStatus("DEPLOYMENT-SUCCESS", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())

                        }


                        /*stage('post-deploy') {

                            if (integrationTestFlag == "true") {


                                withCredentials([file(credentialsId: 'bdo-uat-external-apigeex', variable: 'serviceAccount')]) {
                                    if (fileExists("resources/edge/org/apiProducts.json")) {
                                        echo "load api product for integration init"
                                        maven.runCommand("mvn -X apigee-config:apiproducts -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                                    }
                                    if (fileExists("resources/edge/org/developers.json")) {
                                        echo "load api developer for integration init"
                                        maven.runCommand("mvn -X apigee-config:developers -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                                    }
                                    if (fileExists("resources/edge/org/developerApps.json")) {
                                        echo "load api developer app for integration init"
                                        maven.runCommand("mvn -X apigee-config:apps -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                                        echo "export app key for integration init"
                                        maven.runCommand("mvn -X apigee-config:exportAppKeys -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                                    }
                                }

                            } else {
                                echo "This stage was skipped due to integration testing not running."
                            }
                        }*/


                    if (DefaultConfigService.instance.steps.functionalTest) {

                        // stage('integration-tests') {

                        //     if (integrationTestFlag == "true") {
                        //         if (tagsFlag == "tags") {
                        //             entityDeploymentInfos.each {
                        //                 if (fileExists("test/integration")) {
                        //                     echo "run integration init"
                        //                     npm.runCommand("npm install")
                        //                     if (fileExists("node_modules/cucumber/bin/cucumber-js")) {
                        //                         echo "run integration init"
                        //                         /*
                        //                         Setting test-config and auth server for integration test cases
                        //                          */
                        //                         def env=it.env
                        //                         def org=it.org
                        //                         def setApiDomain = readJSON file: "target/test/integration/test-config.json"
                        //                         def setAuthDomain = readJSON file: "target/test/integration/auth-server.json"
                        //                         setApiDomain.domains[env]=it.host
                        //                         setApiDomain[artifactId].product=artifactId+"Product-"+env
                        //                         setApiDomain[artifactId].app=artifactId+"App-"+env
                        //                         setApiDomain[artifactId].org=org
                        //                         setApiDomain[artifactId].env=env
                        //                         setAuthDomain.domain=it.host
                        //                         writeJSON file: 'test/integration/test-config.json', json: setApiDomain
                        //                         writeJSON file: 'test/integration/auth-server.json', json: setAuthDomain
                        //                         npm.runCommand("node ./node_modules/cucumber/bin/cucumber-js target/test/integration/features -format json:target/reports.json --tags @${it.env}")
                        //                         step([
                        //                                 $class             : 'CucumberReportPublisher',
                        //                                 fileExcludePattern : '',
                        //                                 fileIncludePattern : "*reports.json",
                        //                                 ignoreFailedTests  : false,
                        //                                 jenkinsBasePath    : '',
                        //                                 jsonReportDirectory: "target",
                        //                                 missingFails       : false,
                        //                                 parallelTesting    : false,
                        //                                 pendingFails       : false,
                        //                                 skippedFails       : false,
                        //                                 undefinedFails     : false
                        //                         ])
                        //                     }
                        //                     DeploymentInfoService.instance.saveDeploymentStatus("INTEG-TEST-EXECUTED", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())
                        //                 }
                        //             }
                        //         }
                        //     } else if (tagsFlag == "default") {
                        //         if (fileExists("test/integration")) {
                        //             echo "run integration init"
                        //             npm.runCommand("npm install")
                        //             if (fileExists("node_modules/cucumber/bin/cucumber-js")) {
                        //                 echo "run integration init"
                        //                 /*
                        //                 Setting test-config and auth server for integration test cases
                        //                  */
                        //                 def env=it.env
                        //                 def org=it.org
                        //                 def setApiDomain = readJSON file: "target/test/integration/test-config.json"
                        //                 def setAuthDomain = readJSON file: "target/test/integration/auth-server.json"
                        //                 setApiDomain.domains[env]=it.host
                        //                 setApiDomain[artifactId].product=artifactId+"Product-"+env
                        //                 setApiDomain[artifactId].app=artifactId+"App-"+env
                        //                 setApiDomain[artifactId].org=org
                        //                 setApiDomain[artifactId].env=env
                        //                 setAuthDomain.domain=it.host
                        //                 writeJSON file: 'test/integration/test-config.json', json: setApiDomain
                        //                 writeJSON file: 'test/integration/auth-server.json', json: setAuthDomain
                        //                 npm.runCommand("node ./node_modules/cucumber/bin/cucumber-js target/test/integration/features -format json:target/reports.json")
                        //                 step([
                        //                         $class             : 'CucumberReportPublisher',
                        //                         fileExcludePattern : '',
                        //                         fileIncludePattern : "*reports.json",
                        //                         ignoreFailedTests  : false,
                        //                         jenkinsBasePath    : '',
                        //                         jsonReportDirectory: "target",
                        //                         missingFails       : false,
                        //                         parallelTesting    : false,
                        //                         pendingFails       : false,
                        //                         skippedFails       : false,
                        //                         undefinedFails     : false
                        //                 ])
                        //             }
                        //             DeploymentInfoService.instance.saveDeploymentStatus("INTEG-TEST-EXECUTED", env.BUILD_URL, jenkinsUserUtils.getUsernameForBuild())
                        //         }
                        //     } else {
                        //         echo "Integration testing was not selected to run."
                        //     }
                        // }
                    }

                        /*stage('undeploy-org-config') {

                            if (integrationTestFlag == "true") {

                                withCredentials([file(credentialsId: 'bdo-uat-external-apigeex', variable: 'serviceAccount')]) {
                                    if (fileExists("resources/edge/org/developerApps.json")) {
                                        echo "delete developer app"
                                        maven.runCommand("mvn -X apigee-config:apps -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=delete")
                                    }
                                    if (fileExists("resources/edge/org/apiProducts.json")) {
                                        echo "delete api product"
                                        maven.runCommand("mvn -X apigee-config:apiproducts -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=delete")
                                    }
                                    if (fileExists("resources/edge/org/developers.json")) {
                                        echo "delete developer"
                                        maven.runCommand("mvn -X apigee-config:developers -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=delete")
                                    }
                                }

                            } else {
                                echo "This stage was skipped due to integration testing not running."
                            }
                        }*/
                    }

                   if (DefaultConfigService.instance.steps.release) {

                       stage('upload-artifact') {
                           if ((version as String).trim().endsWith("-SNAPSHOT")) {
                        withCredentials([file(credentialsId: 'gcp-service-account-key', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                               maven.runMaven(" -Ddeployment.suffix=cicd ", "package deploy")
                           }
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