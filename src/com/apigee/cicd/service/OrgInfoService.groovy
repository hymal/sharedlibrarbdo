package com.apigee.cicd.service
import com.apigee.cicd.model.EnvInfo
import com.apigee.cicd.model.Planet
import com.apigee.cicd.model.Vhost
import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS

class OrgInfoService implements ConfigLoader {

  def slurper = new groovy.json.JsonSlurperClassic()
  def data;

  private static OrgInfoService ins = new OrgInfoService()

  @NonCPS
  static OrgInfoService getInstance() {
    return ins
  }

  @NonCPS
  public void init(String info) {
    assert info != null
    println("OrgInfoService data = ${info}")
    data = new groovy.json.JsonSlurperClassic().parseText(info)
    //data = slurper.parseText(info)
    println "Init OrgInfoService - 2"

  }

  @NonCPS
  public List<Planet> getPlanets() {
    List<Planet> planetList = new ArrayList<>()
    data.each {
      Planet p = it as Planet
      planetList.add p
    }
    return planetList
  }

  // Return list of all planets
  @NonCPS
  public List<EnvInfo> getEnvInfoList() {
    List<EnvInfo> envInfos = new ArrayList<>()
    data.each { p ->
      p.orgs.each { o ->
        o.env.each { e ->
          e.vhost.each { v ->
            println "Building a Env Info  "
            EnvInfo info = new EnvInfo()
            info.envName = e.name
            info.orgname = o.orgName
            info.vhost = v.name
            info.msUrl = p.mgmtServerLocation
            if (o.credential) info.cred = o.credential
            else if (p.credential) info.cred = p.cred.toString()
            else info.cred = "edge-${o.orgName}-${e.name}"
            envInfos << info
          }
        }
      }
    }
    return envInfos;
  }

  //  Env Names are unique ; i.e Env name is not used across planets
//  @NonCPS
  public EnvInfo getEnvInfo(String envName) {
    EnvInfo envInfo = null;

    getEnvInfoList().each {
      if (it.envName.toLowerCase().equalsIgnoreCase(envName)) {
        envInfo = it as EnvInfo
      };
    }

    return envInfo
  }


  protected List<EnvInfo> getEnviromnmentByType(String type) {
    List<EnvInfo> envInfoList1 = new ArrayList<>();

    getEnvInfoList().each {
      if (type.contains(it.getEnvType())) envInfoList1 << it
    }

    return envInfoList1


  }

  @NonCPS
  List<Vhost> getVhosts() {
    List<Vhost> vhostList = new ArrayList<>()
    data.each { v ->
      vhostList.add(v as Vhost)
    }
    return vhostList
  }
  @NonCPS
  public Vhost getVhostInfoForEnv(String envName ) {
    for (Vhost v : vhosts) {
      if ( v.envName && v.envName.equalsIgnoreCase(envName)) {
        return v ;
      }
    }
  }

}