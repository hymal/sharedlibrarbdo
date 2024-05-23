package com.apigee.cicd.model

class ApigeePluginDeployConfiguration implements Serializable {

  public String envname = "" ;
  public String configDir = "target/resources/edge";
  public String configOptions = "create" ;
  public String configExportDir = "target/config.test/integration";
  public boolean deploy = true
  public String options ="";
  public String configType ;


}
