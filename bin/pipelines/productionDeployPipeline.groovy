#!groovy

/**
 * Jenkinsfile to be used for Production Deployment for Ferguson API's
 * */



properties([[$class  : 'BuildDiscarderProperty',
             strategy: [$class: 'LogRotator', numToKeepStr: '10']]])


if ( !params.environment ) error ("EdgeEnv Name is Required")

productionDeliveryPipeline params.environment , env.BUILD_NUMBER

