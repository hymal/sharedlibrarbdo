package com.apigee.cicd.service

import com.apigee.cicd.model.Jenkins
import com.apigee.cicd.model.JenkinsTools
import com.apigee.cicd.model.PipelineSteps
import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS

/**
 * Loads Global Configuration
 */
class DefaultConfigService implements ConfigLoader {

  static private DefaultConfigService ins = new DefaultConfigService()

  @NonCPS
  static DefaultConfigService getInstance() {
    return ins
  }

  def slurper = new groovy.json.JsonSlurperClassic()


  def config = [:]

  PipelineSteps steps;
  JenkinsTools tools;
  Jenkins jenkins;

  void load() {

  }

  @NonCPS
  public void init(String info) {

    config = slurper.parseText(info)

    println "Global Configs Load ->>>  Start"

    try {

      //DataSource.configs = slurper.parseText(info)
      //echo " ###### : ${DataSource.configs}"


      println "Global Configs ->>> " + config.toString()

      if (getConfig()["steps"])
        steps = new PipelineSteps(config["steps"])
      if (getConfig()["tools"]) {
        tools = new JenkinsTools(config["tools"])
      }
      if (getConfig()["jenkins"]) {

        jenkins = new Jenkins()
        jenkins.server_url=getConfig().jenkins.serverUrl
        jenkins.credential=getConfig().jenkins.credential
      }

    } catch (Exception e) {
      e.printStackTrace()
      println(e.getMessage())
    }
    println("Initialized DefaultConfigService")
  }


  public PipelineSteps getSteps() {
    return getConfig().steps as PipelineSteps
  }

  public JenkinsTools getTools() {
    return getConfig().tools as JenkinsTools
  }

  public String getMavenSettingsFile() {
    return config.maven.settingsFileId
  }

  public String getNodejsSettingsFileId() {
    return config.nodeJs.settingsFileId
  }

  public Jenkins getJenkins(){
    return getConfig().jenkins as Jenkins
  }

}
