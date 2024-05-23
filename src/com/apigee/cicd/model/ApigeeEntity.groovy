package com.apigee.cicd.model;

import java.util.ArrayList;
import java.util.List;

public class ApigeeEntity implements  Serializable {
    String name;
    String type;
    List<BranchMapping> branchMappings=new ArrayList<>();
}
