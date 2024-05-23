import CICDEnvUtils

// This Model is used for non java callout Proxies
def call ( String apigee_env) {

  envUtils = new CICDEnvUtils();
  def config = envUtils.getEnviromentConfigs()
  String apigeeMavenGlobalOptions = envUtils.getApigeeMavenOptions(apigee_env);
  String proxyRootDirectory = ".";
  def mavenSettingFileId = envUtils.getMavenSettingsFileId()

  stage('lib-validate') {
    node {
//          checkout scm
      echo "config.com.cicd.apigee.edge.envs =  ${config.apigee.edge.envs}"
      echo " Build Name reset ${currentBuild.displayName}"
      echo " Apigee Maven Options : ${apigeeMavenGlobalOptions}"

      echo "Validate SCM Credential Setup"
      try {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : config.scm.credential,
                          usernameVariable: 'edgeUser',
                          passwordVariable: 'edgePassword']]) {}

      } catch (Exception e) {
        error "Missing SCM Credentials : - " + e.getMessage()
      }

      try {
        echo "Validate Edge Management Credential Setup"
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : config.apigee.mgmt.credential,
                          usernameVariable: 'edgeUser',
                          passwordVariable: 'edgePassword']]) {
          apigeeMavenOptions = apigeeMavenGlobalOptions +
            "-Dusername=${env.edgeUser} " +
            "-Dpassword=${env.edgePassword} ";
        }
      } catch (Exception e) {
        error "Missing Edge Management Credentials : - " + e.getMessage()
      }

      try {
        echo "Validate Edge Management Credential Setup for undeploy"
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : config.apigee.mgmt.credential,
                          usernameVariable: 'edgeUser',
                          passwordVariable: 'edgePassword']]) {
          apigeeMavenOptionsPrep = apigeeMavenGlobalOptionsPrep +
                  "-Dusername=${env.edgeUser} " +
                  "-Dpassword=${env.edgePassword} ";
        }
      } catch (Exception e) {
        error "Missing Edge Management Credentials : - " + e.getMessage()
      }

      echo "Validating that Maven settings file is specificed in configuration "
      if ( mavenSettingFileId == null ) {
        error "Missing Maven Settings File maven.settingsFileId for CICD in the config.json file/ resource "
      } else {
        try {
          configFileProvider(
            [configFile(fileId: mavenSettingFileId, variable: "mvnSettings")]) {
            //    sh 'mvn -s $MAVEN_SETTINGS clean package'
            echo "Maven Settings ${env.mvnSettings}"
            mavenSettingsFile = env.mvnSettings as String

          }
        } catch (Exception e) {
          echo e.getMessage()
          e.printStackTrace()
          error "Maven Settings file  json property maven.settingsFileId : ${mavenSettingFileId }  in config.json is not setup in Jenkins"
        }
      }

    }
  }
}
