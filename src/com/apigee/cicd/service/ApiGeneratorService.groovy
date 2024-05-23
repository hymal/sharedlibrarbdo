package com.apigee.cicd.service


import com.cloudbees.groovy.cps.NonCPS


/**
 * This Service is used to generate API
 */
class ApiGeneratorService {

  private static ApiGeneratorService ins = new ApiGeneratorService()
  @NonCPS
  /**
   * Used to generate an archetype in the current workspace
   */
  static ApiGeneratorService getInstance(){
    return ins
  }

  def slurper = new groovy.json.JsonSlurperClassic()
  List data = []

  void createEdgeProxy (String groupName, String teamName, String apiName) {



  }

}
