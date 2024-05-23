import com.apigee.cicd.service.DefaultConfigService

def getEnvUtils() {
  return new CICDEnvUtils()
}

private String getDefaultMavenSettingsFile() {
  if ( DefaultConfigService.instance.getMavenSettingsFile() )   {
    return DefaultConfigService.instance.getMavenSettingsFile()
  }
  return null
}

def getMavenSettingsFile(String configFileId) {

  if (configFileId == null) {
    configFileId = "cicd-settings-file"
  }
  String mavenSettingsFile;
  configFileProvider(
    [configFile(fileId: configFileId, variable: 'MAVEN_SETTINGS')]) {
    //    sh 'mvn -s $MAVEN_SETTINGS clean package'
    mavenSettingsFile = env.MAVEN_SETTINGS

  }
  return mavenSettingsFile
}

/**
 *
 * @param mvnCommands
 * @param mvnGoalOrPhase
 * @return
 */
def runMaven(String mvnCommands, String mvnGoalOrPhase) {
  runMaven(null, mvnCommands, mvnGoalOrPhase, null)

}
/**
 *
 * @param mvnCommands
 * @param mvnGoalOrPhase
 * @param mavenDir
 * @return
 */
def runMaven(String mvnCommands, String mvnGoalOrPhase, String mavenDir) {
  runMaven(null, mvnCommands, mvnGoalOrPhase, mavenDir)
}

/**
 * Nore the SettingFileId is always ignored.
 * @param settingFileID
 * @param mvnCommands
 * @param mvnGoalOrPhase
 * @param mavenDir
 * @return
 */
def runMaven(String settingFileID, String mvnCommands, String mvnGoalOrPhase, String mavenDir) {
  def commands = [:]
  // add commands
  if (mvnCommands != null) commands.options = mvnCommands;
  // add directory where Maven should be run
  if (mavenDir != null && mavenDir.trim().length() == 0) {
    commands.directory = mavenDir
  }
  // setting file as needed
  if (settingFileID != null) {
    commands.configFileId = settingFileID
  } else {
    // is there a settings file defined as default
    String defaultSettingsFileId = DefaultConfigService.instance.getMavenSettingsFile()
    if (defaultSettingsFileId != null) commands.configFileId = defaultSettingsFileId
    // use this settings  file
  }
  // goals to run
  commands.goals = mvnGoalOrPhase
  runMavenCommands(commands)
}

private def runMavenCommands(java.util.Map commands) {

  String mvnCommands = "";
  if (commands.options) mvnCommands = commands.options as String;
  def mvnGoalOrPhase = commands.goals as String;
  if (commands.directory) {
    dir(commands.directory as String) {
      mavenexec mvnCommands , mvnGoalOrPhase
    }
  } else {
    mavenexec mvnCommands , mvnGoalOrPhase
  }
}

private mavenexec(String mvnCommands, String mvnGoalOrPhase) {
  withMaven(maven: DefaultConfigService.instance.tools.maven,
      globalMavenSettingsConfig:DefaultConfigService.instance.getMavenSettingsFile()) {
    if (isUnix()) {

      sh "mvn " + mvnCommands + " " + mvnGoalOrPhase
    } else {
      bat "mvn " + mvnCommands + " " + mvnGoalOrPhase
    }
  }
}

def runCommand(String command) {
  withMaven(maven: DefaultConfigService.instance.tools.maven,
          globalMavenSettingsConfig:DefaultConfigService.instance.getMavenSettingsFile()) {
    if (!isUnix()) {
      println command
      bat returnStdout: true, script: "${command}"
    } else {
      println command
      sh returnStdout: true, script: command
    }
  }
}

return this;