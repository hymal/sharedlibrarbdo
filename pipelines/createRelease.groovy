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


  if ((params.operation as String).equals("release-candidate-create")) {
    stage("release-candidate") {
      new BranchManagerService().createReleaseCandidate(params.team, params.project, params.api)
    }
  }


  if ((params.operation as String).equals("release-start")) {
    stage("release-start") {
      new BranchManagerService().createRelease(params.team, params.project, params.api)
    }
  }

  if ((params.operation as String).equals("release-close")) {
    stage("release-close") {
      new BranchManagerService().finishRelease(params.team, params.project, params.api)
    }
  }

}

