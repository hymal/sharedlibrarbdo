package com.apigee.cicd.service
import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS

class FunctionalTestService implements ConfigLoader {

  @NonCPS
  public boolean isFunctionalTestEnabled(String branchType) {

    if ( !DefaultConfigService.instance.config.functional) return true ;

    if ( DefaultConfigService.instance.config.functional[branchType]) return true ;
    return false ;
  }

  void init(String info) {

  }
}
