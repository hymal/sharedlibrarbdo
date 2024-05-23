package cicd

class Jenkins {
    static Map getGlobalEnv() {
        def envProps = jenkins.model.Jenkins.getConfigs().globalNodeProperties.get(hudson.slaves.EnvironmentVariablesNodeProperty)
        return envProps ? envProps.envVars : [:]
    }
}
