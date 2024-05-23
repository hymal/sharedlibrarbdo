package com.apigee.cicd.model


public class EdgeCiConfiguration implements Serializable {

  int id;
  String scmBranchType;
  String edgeCiEnvList ;
  String[] edgeEnvs ;

  public String[] getEdgeCiEnv() {
    return edgeCiEnvList.split(",");
  }

//
//  @Override
//  public String toString() {
//    return "EdgeCiConfiguration{" +
//            "scmBranchType='" + scmBranchType + '\'' +
//            ", edgeCiEnvList='" + edgeCiEnvList + '\'' +
//            ", edgeEnvs=" + edgeEnvs +
//            '}';
//  }
}


