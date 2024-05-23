package com.apigee.cicd.service

import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS

class DeploymentInfoService implements ConfigLoader{

    private static DeploymentInfoService ins = new DeploymentInfoService();

    String apiName, apiVersion ;
    String serviceLocation = null ;
    String edgeEnv ;



    @NonCPS
    static DeploymentInfoService getInstance() {
        return ins;
    }

    @NonCPS
    void setEdgeEnv(String edgeEnv) {
        this.edgeEnv = edgeEnv
    }

    @NonCPS
    void setApiName(String apiName) {
        this.apiName = apiName
    }

    @NonCPS
    void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion
    }

    @NonCPS
    public void saveDeploymentStatus( String status, String buildInfo, String deployedBy) {

        if ( serviceLocation ==null ) {
            println "Skipping Save Deployment Info : ServiceLocation is not set "
            return
        }

        def post = new URL(serviceLocation).openConnection() as HttpURLConnection;

        def report_input = "    \"apiVersion\" : \"${apiVersion}\",\n" +
                "                   \"apiName\" : \"${apiName}\",\n" +
                "                   \"apiEdgeEnv\" : \"${edgeEnv}\",\n" +
                "                   \"buildInfo\" : \"${buildInfo}\",\n" +
                "                   \"deployedBy\" : \"${deployedBy}\",\n" +
                "                   \"deploymentStatus\" : \"${status}\" "

        def deployment_report = "{ "  + report_input + " } "

        println "deployment_report : $deployment_report"
        post.setRequestMethod("POST")
        post.setRequestProperty("Content-Type", "application/json")
        post.setRequestProperty("Accept", "application/json")
        post.setDoOutput(true)
        post.getOutputStream().write(deployment_report.getBytes("UTF-8"));

        OutputStreamWriter writer = new OutputStreamWriter(post.getOutputStream());
        writer.write(deployment_report)
        writer.flush()

        def postRC = post.getResponseCode();
        println "post req: ${post}"
        println(postRC);
        if(postRC.equals(200)) {
            println(post.getInputStream().getText());
        }
        writer.close()

    }

    @NonCPS
    void init(String locationOfReportingService ) {

        serviceLocation = "${locationOfReportingService}"

     //   println "Service Location : "+ serviceLocation

    }

}
