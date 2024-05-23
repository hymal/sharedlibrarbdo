package com.apigee.boot

import com.apigee.cicd.service.*
import com.cloudbees.groovy.cps.NonCPS

// import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted
class Pipeline implements PipelineBootstrapper {

    private static Pipeline ins


    private Pipeline() {
    }

    @NonCPS
    public static Pipeline getInstance() {
        if (ins == null) ins = new Pipeline()
        return ins
    }

    //@Whitelisted
    String namespace;

    //@Whitelisted
    Map<String, String> map = new HashMap<>();

    ////@Whitelisted
    @NonCPS
    public void initServices() {
        println "Initializing Services"
        println "Getting orgInfo from Map " + map.get(ConfigType.ORG_INFO.toString())
        try {
            // Initializing the services with data needed about Apigee and CiPipeline
            println "Initializing Services - OrgInfo"
            assert OrgInfoService.instance != null
            OrgInfoService.instance.init(map.get(ConfigType.ORG_INFO.toString()))

            println "Initializing Services - CIEnvInfoService"
            CIEnvInfoService.instance.init(map.get(ConfigType.ENV_CI_INFO.toString()))

            println "Initializing Services - VHostInfoService"
            VHostInfoService.instance.init(map.get(ConfigType.VHOST.toString()))

            println "Initializing Services - ApigeeMavenPluginConfigService"
            ApigeeMavenPluginConfigService.instance.init(map.get(ConfigType.APIGEE_MAVEN_OPS.toString()))

            println "Initializing Services - ApigeeMavenPluginConfigService"
            DeployGroupService.instance.init(map.get(ConfigType.ENV_GROUP_INFO.toString()))

            println "Initializing Services - JenkinsJobCreatorService"
            JenkinsJobCreatorService.instance.init(map.get(ConfigType.API_MULTIBRANCH_CONFIG.toString()))

            if (map.containsKey(ConfigType.OTHER.toString())) {
                println "Initializing Services - DefaultConfigService"
                DefaultConfigService.instance.init(map.get(ConfigType.OTHER.toString()))
            }

        } catch (Exception e) {
            e.printStackTrace()
            throw new Exception("Unable to Initialize Services", e)
        }

    }

    public void init(String namespace) {
        this.namespace = namespace
    }

    private void addCIEnvInfo(String info) {
        map.put(ConfigType.ENV_CI_INFO, info)
    }

    public void addOrgInfo(String info) {
        map.put(ConfigType.ORG_INFO, info)
    }

    public void addVhost(String info) {
        map.put(ConfigType.VHOST, info)
    }

    public void addXMLConfig(String info) {
        map.put(ConfigType.API_MULTIBRANCH_CONFIG, info)
    }

    public String getInfo(ConfigType infoType) {
        return map.get(infoType)
    }

    public String getInfo(String configType) {
        return map.get(configType);
    }

    public void addInfo(String configType, String info) {
        map.put(configType, info)
    }

    /**
     * Used to boot strap / load the configuration required for
     * the pipeline
     */
    @NonCPS
    void bootstrap() {

        //TODO

        /*
        1. Get Location of the micro service
        2. Get credentials / Bearer Token
        3. Get global info as string and add as ConfigType.OTHER
        4. Get env info
        5. ...

         */

        initServices();
    }
}
