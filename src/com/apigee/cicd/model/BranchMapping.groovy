package com.apigee.cicd.model;

import java.util.ArrayList;
import java.util.List;

public class BranchMapping implements  Serializable{
    String branchPattern;
    List<EntityDeploymentInfo> deploymentInfo=new ArrayList<>();
}
