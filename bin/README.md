
## Shared Library

Instructions for devops-shared_library

[TOC levels=1-6]: # "# Table of Contents"

## Table of Contents
- [Description](#description)
- [Pre-Requisites](#pre-requisites)
- [Directory Structure](#Directory-Structure)
    - [pipelines](#pipelines)
    - [src](#src)
    - [test](#test)
    - [Vars](#Vars) 
- [How to Configure shared library globally in Jenkins](#How-to-Configure-shared-library-globally-in-Jenkins)
- [How to load the shared library in Jenkins jobs](#How-to-load-the-shared-library-in-Jenkins-jobs)
- [Debugging Instructions](#Debugging-Instructions)
- [Additional References](#Additional-References)

## Description 
A shared Library is a collection of independent **Groovy** scripts which can be use in our Jenkins pipeline execution.
We use it to avoid repeating the same common code across different pipelines. Shared library follows the "DRY" principle. 

## Prerequisites

### OS
      - Windows
      - Linux
### Jenkins
       - Version : > 2.121+    
### SCM
       - Bitbucket
### Plugins
       - Git
       - Maven
       - NodeJS
       - Folder properties 
       - Git plugin 
       - NodeJs plugin 
       - Pipeline 
       - Pipeline maven integration plugin 
       - Extended choice parameters 
       - HTML Publisher plugin 
       - Bitbucket branch source plugin
       - Mask Password


## Directory Structure
   
The directory structure of a Shared Library repository is as follows:

   1. pipelines
   2. src
   3. test
   4. vars
   
```bash

    (root)
    +- pipelines               # place holder for developed pipeline scripts for Jenkins
    +- src                     # Groovy source files
    |   +- org
    |       +- foo
    |           +- Bar.groovy  # for org.foo.Bar class
    +- test                    # test cases to validate the data from microservice
    +- vars
    |   +- foo.groovy          # for global 'foo' variable
    |   +- foo.txt             # help for 'foo' variable   
```
   
   - /pipelines
     
   Place holder for keeping all the pipeline scripts are present in this directory. All the pipeline scripts can be copied from here and paste in Jenkins job directly. 

   - /src
   
   A regular groovy source directory. This directory is added to the classpath when executing Pipelines.
    
   - /test
      
   Test cases are live here in this directory. 
   
   - /vars 
   
   Global variables and functions will be kept here.

   
## How to Configure shared library globally in Jenkins

1. Log in to Jenkins
2. Go to > Manage Jenkins -> Configure System
3. Under Global Pipeline Libraries, select > Add and add a library with the following settings:
    - Library name: `cicd`
    - Default version: `develop`
    - Load implicitly: tick the option (Help for feature Load implicitly) 
    - Allow default version to be overridden: tick this option (Help for feature: Allow default version to be overridden) 
    - Include @Library changes in job recent changes : tick the option 
    - Retrieval method: Modern SCM
    - Select the Git type
    - Project repository: specify the shared library url (use `HTTPS` version, not `SSH`)
    - Credential: provide the credential id to connect with SCM
4. Apply and Save the configurations

##  How to load the shared library in Jenkins jobs

1. Configure the server location for the microservice at folder level properties. 
    - Goto root folder in Jenkins "ferguson" -> Configure -> Folder Properties -> Property List
    - Provide "Name" : API_SERVER_LOCATION
    - Provide "value": https://IP_Address:Port_No
    - if "Folder Properties" is missing, install the Folder Properties Plugin for Jenkins 
    - If you are running the CICD configurator mirco-service in developer local environment, you should provide the localhost path along with the port number as part of "value". Ex. http://localhost:8080
   
    
2. Copy the script of a particular feature you are creating from shared library pipelines element. Example: if you are creating a feature branch and executing a feature-pipeline in Jenkins. 
    - Goto shared library repo in Bitbucket: https://bitbucket.org/feidsi/devops-shared_library/src/develop/
    - pipelines -> createFeature.groovy 
    - copy the script 
    - Create a feature-pipeline job by following provided branch management user guide doc: https://bitbucket.org/feidsi/team_docs/src/master/FEI-branch-mgmt-guidelines.docx
    - Paste the copied script in "Pipeline" -> Definition -> Choose "Pipeline script" -> paste the copied script.
    - Note: Make sure your bitbucket repo must have "develop" branch before you create feature branch using above process. 
3. Build the Job now. If no conflicts branch will be created.
   
## Debugging Instructions   

   - /test
   
   1. This directory has all the test cases.
   2. Any developer can create test case to debug any particular issue for loading the expected data from the micro service.
   3. Creating test cases under this directory will not effect any Jenkins pipeline execution process. 
    
## Additional References

https://jenkins.io/doc/book/pipeline/shared-libraries/

