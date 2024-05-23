import com.apigee.boot.ConfigType
import com.apigee.boot.Pipeline
import com.apigee.cicd.model.EnvInfo
import com.apigee.cicd.model.Org
import com.apigee.cicd.service.ApigeeMavenPluginConfigService
import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.OrgInfoService
import com.apigee.cicd.service.VHostInfoService
import groovy.transform.Field

@Field private static Map configMap = null;
@Field private static boolean initialized = false;
//@Field def static instance = new CICDEnvUtils()

def getTemplate() {
  return new template()
}

def getConfigSluper() {
  return new configslurper();
}

/**
 * Get the configuration for the CICD jobs
 * @return
 */
Map getEnviromentConfigs() {
  return getConfig()
}

Map getConfig() {
  return  DefaultConfigService.instance.config
}

/**
 * get the USERNAME  and PASSWORD
 * Map Contains {user:"", password:"}* @param credId
 * @return
 */
Map<String, String> getCredentials(String credId) {
  def cred = [:]
  if (credId == null) {
    echo "credId is null "
    return credId
  }
  withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                    credentialsId   : credId,
                    usernameVariable: 'user',
                    passwordVariable: 'password']]) {
    if (env.user == null || env.password == null) {
      echo "Credential ${credId} is not configured correctly. Missing Username/ Password. The build will fail "
      error "Credential Configuration Error : ${credId}"
      throw new Exception("Credential Configuration Error : ${credId}")
    }
    cred.username = env.user
    cred.password = env.password

    println("--->> " + cred)
    echo "Credential ${cred} is used"
  }

  return cred
}

/**
 * given the name jenkins managed file, gets the location of the location of the file on jenkins.
 * @return
 */
String getMavenSettingsFileId() {
  def mavenConfigs = getEnviromentConfigs().maven
  if (mavenConfigs && mavenConfigs.settingsFileId) {
    return mavenConfigs.settingsFileId as String
  }
  return null
}

/**
 * Get the Apigee Maven Configs for a given EdgeEnv
 * @param env
 * @return
 */
String getApigeeMavenOptions(String env) {
  if (env == null) {
    cicdInitNamespace()
  }

  // Environment Info
  EnvInfo envinfo
  envinfo = OrgInfoService.ins.getEnvInfo(env);

  def vhost = VHostInfoService.instance.getVhostInfoForEnv(envinfo.envName)
  def apigeeCfg = ApigeeMavenPluginConfigService.instance.getConfig(envinfo.envName)


  if (vhost.envName) {
    def apigeeOptions = "-Papigee -Doptions=override -Denv=${envinfo.envName} " +
            "-Dorg=${envinfo.orgname} " +
            "-DvhostProtocol=${vhost.protocol} " +
            "-DvhostDomainName=${vhost.cname} " +
            "-DvhostDomainPort=${vhost.port} " +
            "-DvhostEdgeName=${vhost.name} " +
            "-Dapigee.config.dir=${apigeeCfg.configDir} " +
            "-Dapigee.config.options=${apigeeCfg.configOptions} " +
            "-Dapigee.override.delay=15 " +
            "-Dapigee.config.exportDir=${apigeeCfg.configExportDir} ";

    URL url = new URL (envinfo.msUrl)

    if (url.port) {
      apigeeOptions += " -Dapigee.api.port=${url.port} ";
    }

    if (url.host) {
      apigeeOptions += " -Dapigee.api.host=${url.host} ";
    }

    if (url.protocol) {
      apigeeOptions += " -Dapigee.api.protocol=${url.protocol} ";
    }

    return apigeeOptions
  } else {
    echo "unable to find env [${env}]"
    return ""
  }
}


/**
 * The branch type is defined based on the global-config.json file used
 * @param branch_name
 * @return
 */
String get_branch_type(String branch_name) {
  //Must be specified according to <flowInitContext> configuration of jgitflow-Maven-plugin in pom.xml
  config = getEnviromentConfigs();

  if (branch_name =~ config.scm.branch_pattern.dev) {
    return "dev"
  } else if (branch_name =~ config.scm.branch_pattern.release) {
    return "release"
  } else if (branch_name =~ config.scm.branch_pattern.master) {
    return "master"
  } else if (branch_name =~ config.scm.branch_pattern.feature) {
    return "feature"
  } else if (branch_name =~ config.scm.branch_pattern.hotfix) {
    return "hotfix"
  } else {
    return null;
  }
}


String get_org_type(String branch_name) {
  //Must be specified according to <flowInitContext> configuration of jgitflow-Maven-plugin in pom.xml
  def dev_pattern = ".*develop"
  def release_pattern = ".*release/.*"
  def feature_pattern = ".*feature/.*"
  def hotfix_pattern = ".*hotfix/.*"
  def master_pattern = ".*master"
  if (branch_name =~ dev_pattern) {
    return "dev"
  } else if (branch_name =~ release_pattern) {
    return "release"
  } else if (branch_name =~ master_pattern) {
    return "master"
  } else if (branch_name =~ feature_pattern) {
    return "feature"
  } else if (branch_name =~ hotfix_pattern) {
    return "hotfix"
  } else {
    return null;
  }
}

/**
 * Apigee Deployment Environments
 * @param branch_type
 * @return
 */
String get_branch_deployment_environment(String branch_type) {
  if (branch_type == "dev" || branch_type == "feature") {
    return "dev"
  } else if (branch_type == "release") {
    return "integration"
  } else {
    return null;
  }
}

def mvn(String goals) {
  def mvnHome = tool "Maven-3.2.3"
  def javaHome = tool "JDK1.8.0_102"

  withEnv(["JAVA_HOME=${javaHome}", "PATH+MAVEN=${mvnHome}/bin"]) {
    sh "mvn -B ${goals}"
  }
}

def version() {
  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
  return matcher ? matcher[0][1] : null
}

void setConfig(String configFileJson) {
  try {
    println "DEBUG: Getting config File " + configFileJson.trim()
    def envConfig = new configslurper().slurpJsonResourceToMap(configFileJson.trim() as String, [:])
    configMap = envConfig as Map

  } catch (any) {
    println(any)
    error any.getLocalizedMessage()
  }
}

