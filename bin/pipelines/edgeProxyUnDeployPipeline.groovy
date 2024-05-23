#!groovy
node {

    stage("init") {
        deleteDir()
    }

    if ((params.operation as String).equals("undeploy")) {
        stage("undeploy") {
            edgeProxyModelUnDeployPipeline params.environment, params.project, params.team, params.api
        }
    }
}
