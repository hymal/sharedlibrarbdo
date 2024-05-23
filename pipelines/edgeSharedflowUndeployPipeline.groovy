#!groovy
node {

    stage("init") {
        deleteDir()
    }

    if ((params.operation as String).equals("undeploy")) {
        stage("undeploy") {
            edgeSharedflowModelUndeployPipeline params.environment, params.project, params.team, params.api
        }
    }
}
