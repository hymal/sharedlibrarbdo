#!groovy

/**
 * Jenkinsfile to be used for functional testing for Ferguson API's
 * */

properties([[$class  : 'BuildDiscarderProperty',
             strategy: [$class: 'LogRotator', numToKeepStr: '10']]])


if ( !params.environment ) error ("EdgeEnv Name is Required")

edgeProxyIntegrationTestingPipeline params.environment

