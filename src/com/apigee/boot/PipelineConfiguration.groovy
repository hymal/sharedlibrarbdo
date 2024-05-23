package com.apigee.boot

import com.apigee.cicd.model.Env
import com.apigee.cicd.model.EnvInfo
import com.apigee.cicd.model.Org
import com.apigee.cicd.model.Planet

import javax.annotation.PostConstruct

@Singleton
class PipelineConfiguration {

  public Map sysProps = null
  def tempMessage = ""

  String planetInfoJson = null  ;
  private List<Planet> planetInfo = null ;

  @PostConstruct
  public void init ()  {

    if ( sysProps == null ) {
      sysProps = System.getenv()
    }

    if ( planetInfoJson != null ) {

      def planetInfoMap  = new ConfigSlurper().parseText(planetInfoJson)
      planetInfo = new ArrayList<Planet>(planetInfoMap)

    }


  }

  // Return list of all planets
  public List<Planet> getPlanets() {
    return planetInfo;
  }

  // Env Names are unique ; i.e Env name is not used across planets
  public EnvInfo getEnvInfo (String envName ) {

    for ( Planet p : planetInfo ) {
      for ( Org o : p.orgs ) {
        for ( Env e: o.env ){
          if (e.name.equalsIgnoreCase(envName))
          {
            String cred ;
            if ( e.getCredential()!= null) {
              cred = e.getCredential()
            } else {
              if ( o.getCredential() != null ) {
                cred = o.getCredential()
              } else {
                if ( p.getCredential() != null ) {
                  cred = p.getCredential()
                }  else {
                  // This is the default Cred
                  cred = "edge-${o.getOrgName()}-${e.getName()}-cred"
                }
              }
            }
            return new EnvInfo(e.name, o.orgName, p.mgmtServerLocation, cred);
          }
        }
      }
    }

    throw new Exception("Environment ${envName} was not recognized as a valid Edge Enviroment for CICD ")
  }

  /**
   * Get all External Envs
   * @return
   */

  public  List<EnvInfo> getExternalEnviromnment  (    ) {
    return  getEnviromnmentByType("external|both")
  }

  /**
   * Get all Internal Envs
   * @return
   */
  public  List<EnvInfo> getInternalEnviromnment  (    ) {
    return  getEnviromnmentByType("internal|both")
  }

    protected List<EnvInfo> getEnviromnmentByType  (String type   ) {
    List <EnvInfo> envInfoList = new ArrayList<>();

    for ( Planet p : planetInfo ) {
      for ( Org o : p.orgs ) {
        for ( Env e: o.env ){
          if (type.toLowerCase().contains(e.getVisibility().toLowerCase().trim()))
          {
            String cred ;
            if ( e.getCredential()!= null) {
              cred = e.getCredential()
            } else {
              if ( o.getCredential() != null ) {
                cred = o.getCredential()
              } else {
                if ( p.getCredential() != null ) {
                  cred = p.getCredential()
                }  else {
                  // This is the default Cred
                  cred = "edge-${o.getOrgName()}-${e.getName()}-cred"
                }
              }
            }
            envInfoList.add(new EnvInfo(e.name, o.orgName, p.mgmtServerLocation, cred)) ;
          }
        }
      }
    }

  }
}
