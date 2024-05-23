import groovy.transform.Field


/**
 * Created by ayeluri on 1/1/2018.
 */

@Field CICDEnvUtils utils = new CICDEnvUtils()
@Field SCMUtils scmUtils = new SCMUtils()
@Field LogUtils log = new LogUtils()


private runGitflowCommands(String commands, String goal) {

  def cred = utils.getConfig().scm.credential
  echo "Using Credential : ${cred.toString()}"
  if (cred) {
    withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                      credentialsId   : cred.toString(),
                      usernameVariable: 'user',
                      passwordVariable: 'password']]) {
      if (env.user == null || env.password == null) {
        echo "Credential ${cred} is not configured correctly. Missing Username/ Password. The build will fail "
        error "Credential Configuration Error : ${cred}"
        throw new Exception("Credential Configuration Error : ${cred}")
      } else {
        echo "Using Username : ${env.user}  and password : ${env.password}"
      }

      //     echo "setting up the gitflow command"
      echo "Credential ${cred} , user: ${env.user} "

      String commonCommands = "-B -P gitflow " +
              "-DpushFeatures=true " +
              "-DscmUsername=${env.user} " +
              "-DscmPassword=${env.password}"

      Maven maven = new Maven()
      maven.runMaven(" ${commands} ${commonCommands} ", goal)
    }

  } else {
    log.error("SCM Credential is not configured")
    throw new Exception("SCM Credential is not configured")
  }
}

private init(String team, String project, String api, String branch) {
  def url = scmUtils.getRepoUrl(project, team, api)
  scmUtils.checkOutRepo(url, branch)
}

void createFeature(String name, String team, String project, String api) {
//  init(team, project, api, "develop")
  if (scmUtils.existsBranch("${name}")) {
    log.fatal "Feature : ${name} exist. Stopping Job "
  }

  runGitflowCommands("-DallowSnapshots=true -DfeatureName=${name}  ",
          " jgitflow:feature-start")
}

void finishFeature(String name, String team, String project, String api) {
//  init(team, project, api, "develop")
  if (!scmUtils.existsBranch("${name}")) {
    log.fatal "Feature : ${name} does not exist. Stopping Job "
  }
  runGitflowCommands("-DfeatureName=${name}", "jgitflow:feature-finish")
}

/**
 * This will update the maven pom version. CI will take care of the running the required CI Jobs
 * @param team
 * @param project
 * @param api
 */
void createReleaseCandidate(String team, String project, String api) {


  def cred = utils.getConfig().scm.credential
  def url = scmUtils.getRepoUrl(project, team, api)
  def shell = new shell()


  //def workspace = pwd

//  shell.pipe("git config --global credential.helper cache ")
//  shell.pipe(" git config --global push.default simple")

//  checkout([$class           : 'GitSCM', branches: [[name: 'develop']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [],
//            userRemoteConfigs: [[credentialsId: "${cred}", url: "${url}"]]])

  if (!scmUtils.existsBranch("rel-")) {
    log.fatal "A current Release doesn't exist. " +
            "Stopping Job "
  }

  def branches = shell.pipe("git branch -r").tokenize()
  String releaseBranchName = null
  for (int i = 0; i < branches.size(); i++) {
    if (branches[i].toString()
            .replace("origin/", "")
            .startsWith("rel-")) {
      releaseBranchName = branches[i].toString().replace("origin/", "")
    }
  }

  log.info("git checkout ${releaseBranchName}  ")
  shell.pipe("git checkout ${releaseBranchName} ")


  def pom = new pom()
  String currentVersion = pom.version("pom.xml")
  String rcVersion = currentVersion.replace("-SNAPSHOT", "")
  String nextVersion = getNextVersion(currentVersion)


  maven = new Maven()
  maven.runMaven("-DnewVersion=${rcVersion}", "versions:set versions:commit")
//TODO change this to with maven
//  dir("edge") {
//    maven.runMaven("", "compile")
//    maven.runMaven("-Papigee -Ddeployment.suffix=cicd", "package")
//    // preforming an artifact release
//    // activate the release profile
//    maven.runMaven("-Prelease", "deploy")
//  }

//  maven.runMaven("", "clean")


  log.info "testing... ${rcVersion}"


  shell.pipe "git config user.email ${utils.getConfig().scm.user.email} "
  shell.pipe "git config user.name ${utils.getConfig().scm.user.name} "

  shell.pipe("git tag ${rcVersion} -f ")


  sh script: "git commit -m \" Releasing ${rcVersion}\" . ", returnStatus: true
  echo "committing"
  log.info " Push changes to Git Server "
  shell.pipe "git push origin --tags -f"
  log.info "Done Pushing Changes to Git Server "

  // Note the push above will result in triggering the release branch which in
  // turn will result in creating the tag and the release deployment


  maven.runMaven("-DnewVersion=${nextVersion}", "versions:set versions:commit")


//    shell.pipe "git config user.email \"jenkin@sidgs.com\" "
//    shell.pipe "git config user.name \"jenkin@sidgs.com\" "

  shell.pipe "git commit -m \" Starting new RC ${nextVersion}\" . "
  shell.pipe "git push origin"

}

/**
 *
 * @param currentVersion
 * @return
 */
def getNextVersion(String currentVersion) {
  log.info("Current Version is : ${currentVersion}")
  if (currentVersion.endsWith("-SNAPSHOT")) {
    currentVersion = currentVersion.replace("-SNAPSHOT", "");
  }
  log.info("Current Version Sans SNAPSHOT is : ${currentVersion}")

  def rcVersion = currentVersion.split("-RC")[1]
  def newVersion = "${currentVersion.split('-RC')[0]}-RC${(rcVersion as int) + 1}-SNAPSHOT"
  return newVersion
  log.info " new version of pom ${newVersion} "
}

void createRelease(String team, String project, String api) {
//  init(team, project, api, "develop")
  def pom = new pom()
  if (scmUtils.existsBranch("rel-")) {
    log.fatal "A current Release exist. Finish the Release before starting a new one. " +
            "Stopping Job "
  }
  runGitflowCommands("-DallowSnapshots=true -DautoVersionSubmodules=true " +
          "-DreleaseBranchVersionSuffix=RC1 -DupdateDependencies=true", "jgitflow:release-start")

}

void finishRelease(String team, String project, String api) {
//  init(team, project, api, "develop")
  if (!scmUtils.existsBranch("rel-")) {
    log.fatal "A current Release does not exist. Create a Release before finishing one. " +
            "Stopping Job "
  }
  runGitflowCommands("-DallowSnapshots=true -DautoVersionSubmodules=true", "jgitflow:release-finish")

}


void createHotFix(String team, String project, String api) {
//  init(team, project, api, "master")
  if (scmUtils.existsBranch("hf-")) {
    log.fatal "A current HotFix branch exists. Finish the Hotfix before starting a new one. " +
            "Stopping Job "
  }
  runGitflowCommands("-DallowSnapshots=true -DreleaseBranchVersionSuffix=RC -DupdateDependencies=true", "jgitflow:hotfix-start")

}


void finishHotFix(String team, String project, String api) {
//  init(team, project, api, "master")
  if (!scmUtils.existsBranch("hf-")) {
    log.fatal "A current HotFix branch does not exist. Create a Hotfix before finishing one. " +
            "Stopping Job "
  }
  runGitflowCommands("-DallowSnapshots=true", "jgitflow:hotfix-finish")
}


return this;
