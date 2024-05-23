#!groovy
import com.apigee.loader.BootStrapConfigLoad

node {

  CICDEnvUtils utils = new CICDEnvUtils()
  SCMUtils scmUtils = new SCMUtils()
  withFolderProperties{

    BootStrapConfigLoad configLoad = new BootStrapConfigLoad();
    try {
      echo "API_SERVER_LOCATION: ${env.API_SERVER_LOCATION}"
      configLoad.setupConfig("${env.API_SERVER_LOCATION}");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  stage("init") {
    deleteDir()
  }


  if ((params.operation as String).equals("hotfix-start")) {
    stage("hotfix-start") {
      new BranchManagerService().createHotFix(params.team, params.project, params.api)
    }
  }

  if ((params.operation as String).equals("hotfix-close")) {
    stage("hotfix-close") {
      new BranchManagerService().finishHotFix(params.team, params.project, params.api)
    }
  }
}
