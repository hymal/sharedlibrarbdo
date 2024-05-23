
package com.apigee.cicd.model


public class Org implements Serializable  {

     String orgName;
     List<Env> env = new ArrayList<>();
     String credential  ;
}
