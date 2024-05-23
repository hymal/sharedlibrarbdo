package com.apigee.cicd.service

import com.apigee.cicd.dao.DataSource
import com.apigee.cicd.model.ApigeePluginDeployConfiguration
import com.apigee.cicd.util.PipelineUtils
import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS


class ApigeeMavenPluginConfigService implements ConfigLoader {

  private static ApigeeMavenPluginConfigService ins = new ApigeeMavenPluginConfigService() ;

  @NonCPS
  static ApigeeMavenPluginConfigService getInstance() {
    return ins
  }

  def slurper = new groovy.json.JsonSlurperClassic()
  List data = []

  List<ApigeePluginDeployConfiguration> getApigeePluginDeployConfigurations() {
    List<ApigeePluginDeployConfiguration> list = new ArrayList<>()
    data.each { d ->
        ApigeePluginDeployConfiguration deployConfiguration = new ApigeePluginDeployConfiguration()
        deployConfiguration.envname = d.envname
        deployConfiguration.configDir =d.configDir
        deployConfiguration.configExportDir = d.configExportDir
        deployConfiguration.configOptions = d.configOptions
        deployConfiguration.deploy = d.deploy
        list.add(deployConfiguration)

    }
    return list
  }

  @NonCPS
  public void init (String info) {
    println " Apigee Maven Plugin Config Load ->>"
    data = slurper.parseText(info )
    println data.toString()
    println "Init Apigee Maven Config - "
  }

//  @NonCPS
  public ApigeePluginDeployConfiguration getConfig (String env) {

    ApigeePluginDeployConfiguration configuration = null

    for  ( ApigeePluginDeployConfiguration c : getApigeePluginDeployConfigurations() ) {
      if ( c.envname.equalsIgnoreCase(env)) {
        return c
      }
    }

    // Return default if envrionment was not found
    for  ( ApigeePluginDeployConfiguration c : getApigeePluginDeployConfigurations() ) {
      if ( c.envname.equalsIgnoreCase("default")) {
        return c
      }
    }

  }

}
