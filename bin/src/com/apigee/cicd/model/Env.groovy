
package com.apigee.cicd.model


public class Env implements Serializable  {

    private String name;
    private String vhost;
    private String visibility;
    // Credential ID in Jenkins
    private String credential ;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}
