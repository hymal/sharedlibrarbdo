import com.apigee.cicd.model.EntityDeploymentInfo
import com.apigee.cicd.service.AssetService
import com.apigee.cicd.service.CIEnvInfoService
import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.DeploymentInfoService
import com.apigee.loader.BootStrapConfigLoad
import Maven
import JenkinsUserUtils
import GenerateProject
import shell
import groovy.json.JsonSlurper
import hudson.model.Cause

/**
 * This pipeline is used for onboarding a new entity (both apiproxy and sharedflow) to apigee
 *
 * 1. create a project from template
 * 2. Create a repo
 * 3. Commit project to repo
 * 4. Deploy it to edge
 */
def call(String build_number) throws Exception{
    node {
    deleteDir()
    try {
        def scmAPILocation,scmOauthServerLocation,entityDeploymentInfos,scmProjectName,entityType
        Maven maven = new Maven()
        JenkinsUserUtils jenkinsUserUtils = new JenkinsUserUtils()
        GenerateProject generateProject = new GenerateProject()
        def shell = new shell()
        echo " Starting Onboard pipeline"
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
                    configLoad.setupAssetConfiguration("${env.API_SERVER_LOCATION}","${params.buName}-${params.teamName}-${params.artifactId}")
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            /*
                Populating asset deployment data
             */
            entityType=AssetService.instance.data.type
            AssetService.instance.data.branchMappings.each{branchMapping ->
                if(branchMapping.branchPattern == "default")
                {
                    entityDeploymentInfos=branchMapping.deploymentInfo
                }
            }


            stage("generate project") {
                dir('target') {
                    echo "generating template project"
                    generateProject.generateProject("apigee-hybrid","${entityType}")
                }
            }

            if(entityType == "apiproxy") {
                stage("pre-deploy") {
                    entityDeploymentInfos.each {
                        withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                            dir("target/${params.artifactId}/edge") {
                                if (fileExists("resources/edge/env/${it.env}/targetServers.json")) {
                                    withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                                        maven.runCommand("mvn -X apigee-config:targetservers -Phybrid-apiproxy -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount} -Dapigee.config.options=update")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            stage("deploy to apigee organization") {
                    entityDeploymentInfos.each {
                        withCredentials([file(credentialsId: it.org, variable: 'serviceAccount')]) {
                            dir("target/${params.artifactId}/edge") {
                                maven.runCommand("mvn -X package apigee-enterprise:deploy -Phybrid-${entityType} -Dorg=${it.org} -Denv=${it.env} -Dfile=${serviceAccount}")
                        }
                    }
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

            stage("create repo") {
                if(entityType == "apiproxy"){
                    scmProjectName="apiplatform-${params.buName}-${params.teamName}-${params.artifactId}"
                }
                else{
                    scmProjectName="apiplatform-${params.buName}-${params.teamName}-sf_${params.artifactId}"
                }

                echo "Create repo  $scmProjectName at ${scmAPILocation}/${scmProjectName}"
                def response = httpRequest httpMode: 'POST',
                        customHeaders: [[name: "Authorization", value: "Bearer ${scmAccessToken}"], [name: "content-type", value: "application/json"]],
                        url: "${scmAPILocation}/${scmProjectName}",
                        requestBody: "{\"scm\":\"git\", \"project\":{\"key\":\"AP\"},\"is_private\" : \"true\"}"
                def responseJson = new JsonSlurper().parseText(response.content)
                def scmCloneURLs = responseJson.links.clone
                scmCloneURLs.each {
                    if (it.name == "https") {
                        scmCloneURL = it.href
                    }
                }

            }

            stage("push template") {
                dir("target/${params.artifactId}") {
                    def defRepURL= scmCloneURL.split("@")[1]
                    def scmCloneURLFinal="https://${env.scmUser}:${env.scmPassword}@${defRepURL}"
                    shell.pipe "git init"
                    shell.pipe "git add ."
                    shell.pipe "git config --global user.email ${params.email}"
                    shell.pipe "git config --global user.name ${params.name}"
                    shell.pipe "git commit -m \"${params.CommitMessage}\""
                    shell.pipe "git remote add origin ${scmCloneURLFinal}"
                    shell.pipe "git push -u origin master"
                    shell.pipe "git branch develop master"
                    shell.pipe "git checkout develop"
                    shell.pipe "git push --set-upstream origin develop"
                }
                dir("target"){
                    echo "Cleaning directory after commit"
                    shell.pipe "rm -rf ./*"
                }
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


