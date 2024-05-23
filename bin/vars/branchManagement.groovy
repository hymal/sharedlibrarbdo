import com.apigee.loader.BootStrapConfigLoad
import groovy.json.JsonSlurper
import CICDEnvUtils
import com.apigee.boot.ConfigType
import com.apigee.boot.Pipeline
import com.apigee.loader.BootStrapConfigLoad
import com.apigee.cicd.service.DeploymentInfoService
import groovy.json.JsonSlurper
import hudson.AbortException
import hudson.model.Cause
import pom
import Maven
import JenkinsUserUtils
import shell
import BranchManagerService

/*
This pipeline is used for handling branch management on the repos
1. Feature branchches
2. Hotfix branches
3. Release branches
4. Release candidates
 */
def call(String operation,String repoProjectName) {

    node {
        deleteDir()
        def shell = new shell()
        try {

            withCredentials([
                    [$class          : 'UsernamePasswordMultiBinding',
                     credentialsId   : "svc-jenkins-scm-cred",
                     usernameVariable: 'scmUser',
                     passwordVariable: 'scmPassword'],
                    [$class          : 'UsernamePasswordMultiBinding',
                     credentialsId   : "svc-jenkins-scm-oauth-cred",
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
                            }
                            catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }

                        stage("get scm token") {
                            def userDetails = "${env.scmClient}:${env.scmSecret}";
                            def encodedUser = userDetails.bytes.encodeBase64().toString()
                            def response = httpRequest httpMode: 'POST',
                                    customHeaders: [[name: "Authorization", value: "Basic ${encodedUser}"], [name: "content-type", value: "application/x-www-form-urlencoded"]],
                                    url: "${scmOauthServerLocation}",
                                    requestBody: "grant_type=client_credentials"
                            def responseJson = new JsonSlurper().parseText(response.content)
                            scmAccessToken = responseJson.access_token
                        }

                        stage('Checkout') {
                            wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: scmAccessToken, var: 'SECRET']]]) {
                                shell.pipe("git clone https://x-token-auth:${scmAccessToken}@bitbucket.org/${repoProjectName}/${projectName}-${params.buName}-${teamName}-${artifactId}.git")

                            }
                         }
                    }
            dir("${projectName}-${params.buName}-${teamName}-${artifactId}") {
                BranchManagerService branchManagerService = new BranchManagerService()
                def updatedProjectName=params.projectName+"-"+params.buName
                switch (operation) {
                    case "release-create":
                        branchManagerService.createRelease(params.teamName, updatedProjectName, params.artifactId)
                        break
                    case "release-close":
                        branchManagerService.finishRelease(params.teamName, updatedProjectName, params.artifactId)
                        break
                    case "release-candidate-create":
                        branchManagerService.createReleaseCandidate(params.teamName, updatedProjectName, params.artifactId)
                        break
                    case "feature-create":
                        branchManagerService.createFeature(params.feature,params.teamName, updatedProjectName, params.artifactId)
                        break
                    case "feature-close":
                        branchManagerService.finishFeature(params.feature,params.teamName, updatedProjectName, params.artifactId)
                        break
                    case "hotfix-create":
                        branchManagerService.createHotFix(params.teamName, updatedProjectName, params.artifactId)
                        break
                    case "hotfix-close":
                        branchManagerService.finishHotFix(params.teamName, updatedProjectName, params.artifactId)
                        break
                    default:
                        echo "invalid operation"
                        break
                }
            }
        }
            catch (any) {
                JenkinsUserUtils jenkinsUserUtils = new JenkinsUserUtils()
                println any.toString()
                currentBuild.result = 'FAILURE'
                DeploymentInfoService.instance.saveDeploymentStatus("FAILURE", env.BUILD_URL,jenkinsUserUtils.getUsernameForBuild())
            }
        }
    }

