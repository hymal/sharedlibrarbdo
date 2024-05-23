package com.apigee.cicd.service

import com.apigee.cicd.dao.DataSource
import com.apigee.cicd.model.ApigeePluginDeployConfiguration
import com.apigee.cicd.model.EdgeCiConfiguration
import com.apigee.cicd.model.EnvInfo
import com.apigee.cicd.model.Vhost
import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS
// import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted

class CIEnvInfoService implements ConfigLoader{


  private static CIEnvInfoService ins = new CIEnvInfoService()


  @NonCPS
  static CIEnvInfoService getInstance() {
    return ins
  }

  private CIEnvInfoService() {}

  List data  ;

  String jsonData ;

  //@Whitelisted
  List<EdgeCiConfiguration> getEdgeCiConfigurations() {
    List<EdgeCiConfiguration> environments = new ArrayList<>()
    this.data.each {
      EdgeCiConfiguration cfg = it as EdgeCiConfiguration
      environments << cfg
    }
    return environments
  }

  def slurper = new groovy.json.JsonSlurperClassic()



  @NonCPS
  //@Whitelisted
  public void init (String info) {

    jsonData = info

    data = slurper.parseText(info)
    println "CIEnv Config Load ->> " + data.toString()
    println "Init CIEnv Config - ";

//      println "INFO:-->"+ info
//      println "DATA:-->"+ data
//
//      data.each { e->
//          println("iterating on cienv:"+e);
//      }

    data.each { e ->
      EdgeCiConfiguration edgeCiEnvironments1 = e as EdgeCiConfiguration
        println "e->:"+e
      println "EdgeCiConfiguration parsed:"+ edgeCiEnvironments1
      DataSource.edgeCiEnvironmentsList.add(edgeCiEnvironments1)

    }

  }

  /**
   * Get a list of environments that CiPipeline pipe line should deploy for a change in SCM
   * @param scmBranchType
   * @return
   */
  //@Whitelisted
  List<String> findEdgeEnviromentsForBranch( String scmBranchType ) {
    List environmentNames = []
    edgeCiConfigurations.each  {
      println("iteration:"+ it);
      println("scmBranchType:"+ it.scmBranchType);
      if ( it.scmBranchType.equalsIgnoreCase(scmBranchType)) {
        it.edgeCiEnv.each { name ->
          environmentNames << name.toString()
        }
      }
    }
    return environmentNames
  }


    /**
     * Get the Apigee Maven Configs for a given EdgeEnv
     * @param env
     * @return
     */

    protected String getApigeeMavenOptions(String env) {
        // Environment Info
        EnvInfo envinfo
        envinfo = OrgInfoService.ins.getEnvInfo(env);


    // Vhost info for the Environment
    Vhost vhost = VHostInfoService.ins.getVhostInfoForEnv(envinfo.envName)

    ApigeePluginDeployConfiguration config = ApigeeMavenPluginConfigService.ins.getConfig(envinfo.envName)

    if ( true ) {
      def apigeeOptions = " -Denv=${envinfo.envName} " +
        "-Dorg=${envinfo.orgname} " +
        "-DvhostProtocol=${vhost.protocol} " +
        "-DvhostDomainName=${vhost.cname} " +
        "-DvhostDomainPort=${vhost.port} " +
        "-DvhostEdgeName=${vhost.name} " +
        "-Dapigee.config.dir=${config.configDir} " +
        "-Dapigee.config.options=${config.configOptions} " +
        "-Dapigee.override.delay=15 " +
        "-Dapigee.config.exportDir=${config.configExportDir} ";

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
      println "unable to find env [${env}]"
      return ""
    }
  }

    protected String getApigeeMavenOptionsForDelete(String env) {
        // Environment Info
        EnvInfo envinfo
        envinfo = OrgInfoService.ins.getEnvInfo(env);


        // Vhost info for the Environment
        Vhost vhost = VHostInfoService.ins.getVhostInfoForEnv(envinfo.envName)

        ApigeePluginDeployConfiguration config = ApigeeMavenPluginConfigService.ins.getConfig(envinfo.envName)

        if ( true ) {
            def apigeeOptions = " -Denv=${envinfo.envName} " +
                    "-Dorg=${envinfo.orgname} " +
                    "-DvhostProtocol=${vhost.protocol} " +
                    "-DvhostDomainName=${vhost.cname} " +
                    "-DvhostDomainPort=${vhost.port} " +
                    "-DvhostEdgeName=${vhost.name} " +
                    "-Dapigee.config.dir=target/resources/edge " +
                    "-Dapigee.config.options=delete " +
                    "-Dapigee.config.exportDir=${config.configExportDir} ";

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
            println "unable to find env [${env}]"
            return ""
        }
    }


    //@Whitelisted
  public String getProxyDeployOptions( String env) {
     return "-Papigee ${getApigeeMavenOptions(env)}  -Doptions=override "
  }

  //@Whitelisted
  public String getProxyUnDeployOptions( String env) {
      return "-Papigee-prep ${getApigeeMavenOptionsForDelete(env)} -Doptions=clean "

  }


  String get_branch_type(String branch_name) {
    //Must be specified according to <flowInitContext> configuration of jgitflow-Maven-plugin in pom.xml
    config = getEnviromentConfigs();
//    def dev_pattern = ".*develop"
//    def release_pattern = ".*release/.*"
//    def feature_pattern = ".*feature/.*"
//    def hotfix_pattern = ".*hotfix/.*"
//    def master_pattern = ".*master"
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

}
