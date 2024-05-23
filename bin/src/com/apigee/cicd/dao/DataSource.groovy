package com.apigee.cicd.dao

import com.apigee.cicd.model.ApigeePluginDeployConfiguration
import com.apigee.cicd.model.EdgeCiConfiguration
import com.apigee.cicd.model.EnvInfo
import com.apigee.cicd.model.Planet
import com.apigee.cicd.model.Vhost
import groovy.transform.Field


@Field static Map configs = [:]
//configs.put('name', 'John')

@Field static List<Planet> planets = new ArrayList<>()
@Field static List<EnvInfo> envInfoList = new ArrayList<>()
@Field static List<Vhost> vhostList = new ArrayList<>()
@Field static List<ApigeePluginDeployConfiguration> apigeePluginDeployConfigurationList = new ArrayList<>()
@Field static List<EdgeCiConfiguration> edgeCiEnvironmentsList = new ArrayList<>()

return this
