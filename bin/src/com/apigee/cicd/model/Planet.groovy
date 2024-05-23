
package com.apigee.cicd.model


public class Planet implements Serializable {

     String planetId;
     String planetDesc;
     String mgmtServerLocation;
     String credential = null ;
     List<Org> orgs = new ArrayList<>();


}
