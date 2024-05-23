package com.apigee.cicd.util

class PipelineUtils {

  public static Map getMapFromJsonString (String info) {
    def slurper = new groovy.json.JsonSlurperClassic()
    return slurper.parseText(info);
    //return configObject
  }

  public static List getListFromJsonString (String info) {
    def slurper = new groovy.json.JsonSlurperClassic()
    return slurper.parseText(info);
  }
}
