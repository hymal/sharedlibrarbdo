package com.apigee.cicd.service

import com.cloudbees.groovy.cps.NonCPS

class SCMRepoService {

  /**
   * Create a new
   * @param groupName
   * @param teamName
   * @param apiName
   */
  @NonCPS
  void createRepo (String groupName, String teamName, String apiName) {

  }


  protected boolean repoExists ( String repoName) {
    //TODO
    return false ;
  }


  /**
   * Create a new trigger and register the jenkins job
   * @param groupName
   * @param teamName
   * @param apiName
   */
  @NonCPS
  void registerTrigger (String groupName, String teamName, String apiName, String branchName) {

  }

}
