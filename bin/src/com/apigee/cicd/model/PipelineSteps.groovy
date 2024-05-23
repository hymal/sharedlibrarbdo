package com.apigee.cicd.model

class PipelineSteps implements Serializable {

  public boolean unitTest, lint, functionalTest, deploy, release ;

}
