package com.apigee.loader

import com.apigee.cicd.service.ApigeeMavenPluginConfigService
import com.apigee.cicd.service.AssetService
import com.apigee.cicd.service.CIEnvInfoService
import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.DeploymentInfoService

//import com.apigee.cicd.service.DeploymentInfoService
import com.apigee.cicd.service.OrgInfoService
import com.apigee.cicd.service.VHostInfoService
import com.cloudbees.groovy.cps.NonCPS;

import java.net.MalformedURLException;
import java.net.URL;

class BootStrapConfigLoad implements BootLoader {
    /**
     * Fetch the Global Configuration and store as a map
     * @param endpoint
     * @throws MalformedURLException
     */
    @NonCPS
    void setupConfig(String endpoint) throws MalformedURLException {

        def connection = new URL(endpoint + "/globalConfig")
                .openConnection() as HttpURLConnection
        connection.setRequestProperty('Accept', 'application/json')
        DefaultConfigService.instance.init(connection.inputStream.text)

        connection = new URL(endpoint + "/ciEnv")
                .openConnection() as HttpURLConnection
        connection.setRequestProperty('Accept', 'application/json')
        CIEnvInfoService.instance.init(connection.inputStream.text)

        connection = new URL(endpoint + "/planet")
                .openConnection() as HttpURLConnection
        connection.setRequestProperty('Accept', 'application/json')
        OrgInfoService.instance.init(connection.inputStream.text)

        connection = new URL(endpoint + "/vhost")
                .openConnection() as HttpURLConnection
        connection.setRequestProperty('Accept', 'application/json')
        VHostInfoService.instance.init(connection.inputStream.text)

        connection = new URL(endpoint + "/mavenDeploy")
                .openConnection() as HttpURLConnection
        connection.setRequestProperty('Accept', 'application/json')
        ApigeeMavenPluginConfigService.instance.init(connection.inputStream.text)

        DeploymentInfoService.instance.init("${endpoint}/report")
    }

    /**
     * Fetch asset configuration for deploying the apigee entity
     * @param endpoint
     * @param assetName
     * @throws MalformedURLException
     */
    @NonCPS
    void setupAssetConfiguration(String endpoint,String assetName) throws MalformedURLException {
        def connection = new URL(endpoint + "/assets/"+assetName)
                .openConnection() as HttpURLConnection
        connection.setRequestProperty('Accept', 'application/json')
        AssetService.instance.init(connection.inputStream.text)
    }

    /**
     * Returns True when Ready
     * @return
     */
    @NonCPS
    boolean validate() {
        return true
    }

    String getBearerToken() {
        return "dummy token"
    }

}


