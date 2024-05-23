import com.apigee.cicd.service.DefaultConfigService
import groovy.transform.Field

@Field private CICDEnvUtils envUtils = new CICDEnvUtils()
@Field private LogUtils log = new LogUtils()

def getShell() {
  return new shell()
}

String getRepoUrl(String projectName, String teamName, String apiName) {

  Map scmConfig = DefaultConfigService.instance.config.scm
  println "SCM Config == ${scmConfig}"
  String repoUrl = (scmConfig.repoUrl != null )? scmConfig.repoUrl : scmConfig.repo_url
  println "Repo URL == ${repoUrl}"
  String gitUrl = (scmConfig.git_url != null )? scmConfig.git_url : scmConfig.gitUrl

  repoUrl = repoUrl.replace("#projectName", projectName)
          .replace("#teamName", teamName)
          .replace("#apiName", apiName)
          .replace("#git_url", gitUrl)

  //log.info(" repoUrl : ${repoUrl}")

  return repoUrl
}

boolean userHasCreateFeaturePrivs(teamName, api) {

  return true

}

private String getRepoCredential() {
  Map scmConfig = envUtils.getConfig().scm
  String repoCred = scmConfig.credential
  return repoCred
}

def authenticatedUrl(url, username, password) {
  encodedUsername = URLEncoder.encode(username as String, "UTF-8")
  encodedPassword = URLEncoder.encode(password as String, "UTF-8")
  def bits = (url as String).split("://")
  if (bits.length == 2) {
    return bits[0] + "://${encodedUsername}:${encodedPassword}@" + bits[1] as String
  }
  return "${encodedUsername}:${encodedPassword}@" + bits[0] as String
}

void checkOutRepo(def repositoryUrl, def branch) {
  String gitAuthUrl;

  //getShell().pipe( "git clone ${repositoryUrl}")
  git branch: branch, credentialsId: getRepoCredential(), url: repositoryUrl
}

/**
 * Check if a branch exists. Note this will only work when the git repo has alredy been checked out
 * @param branch
 * @return
 */
boolean existsBranch(branch) {

  def branches = getShell().pipe("git branch -r").tokenize()

  def result = []

  for (int i = 0; i < branches.size(); i++) {
    if (branches[i].toString()
            .replace("origin/", "")
            .contains(branch)) return true
  }

  return false;
}

def listBranches(repositoryUrl, branch, credentialsId) {
  ws {
    git poll: false, changelog: false, url: repositoryUrl, branch: branch, credentialsId: credentialsId
    def branches = getShell().pipe("git branch -r").tokenize()
    def result = []
    for (int i = 0; i < branches.size(); i++) {
      result.add(branches[i].toString().replace("origin/", ""))
    }
    return result
  }
}


return this;
