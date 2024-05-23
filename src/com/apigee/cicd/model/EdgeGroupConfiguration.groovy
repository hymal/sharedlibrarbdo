package com.apigee.cicd.model

class EdgeGroupConfiguration implements Serializable {

    String name;
    String envs ;
    String[] edgeEnvs ;

    public String[] getEdgeEnv() {
      return envs.split(",");
    }



}
