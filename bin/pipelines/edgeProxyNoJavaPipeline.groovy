#!groovy


properties([[$class  : 'BuildDiscarderProperty',
             strategy: [$class: 'LogRotator', numToKeepStr: '10']]])


//JenkinsUserUtils jenkinsUserUtil = new JenkinsUserUtils();

def branch_type = get_branch_type "${env.BRANCH_NAME}"
dir("edge") {
    if (branch_type == "dev") {
        edgeProxyCiModelOnePipeline "dev", env.BUILD_NUMBER

    } else if (branch_type == "release") {
        edgeProxyCiModelOnePipeline "release", env.BUILD_NUMBER

    } else if (branch_type == "feature") {
        edgeProxyCiModelOnePipeline "feature", env.BUILD_NUMBER

    } else if (branch_type == "hotfix") {
        edgeProxyCiModelOnePipeline "hotfix", env.BUILD_NUMBER

    } else {
        node {
            stage ("build-setup"){
                echo "Unknown Branch Type. Pipeline existing and has taken no action"
            }
        }
    }
}

// Utility functions
def get_branch_type(String branch_name) {
    //Must be specified according to <flowInitContext> configuration of jgitflow-Maven-plugin in pom.xml
    def dev_pattern = ".*develop"
    def release_pattern = ".*rel-.*"
    def feature_pattern = ".*feat-.*"
    def hotfix_pattern = ".*hf-.*"
    def master_pattern = ".*master"
    if (branch_name =~ dev_pattern) {
        return "dev"
    } else if (branch_name =~ release_pattern) {
        return "release"
    } else if (branch_name =~ master_pattern) {
        return "master"
    } else if (branch_name =~ feature_pattern) {
        return "feature"
    } else if (branch_name =~ hotfix_pattern) {
        return "hotfix"
    } else {
        return null;
    }
}


