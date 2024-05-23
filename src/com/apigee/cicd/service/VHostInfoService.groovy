package com.apigee.cicd.service

import com.apigee.cicd.dao.DataSource
import com.apigee.cicd.model.EdgeCiConfiguration
import com.apigee.cicd.model.Vhost
import com.apigee.cicd.util.PipelineUtils
import com.apigee.loader.ConfigLoader
import com.cloudbees.groovy.cps.NonCPS


class VHostInfoService implements ConfigLoader{

    private static VHostInfoService ins = new VHostInfoService()
    List<Vhost> vhosts = new ArrayList<>()


    @NonCPS
    static VHostInfoService getInstance() {
        return ins
    }

    def slurper = new groovy.json.JsonSlurperClassic()
    List data

    @NonCPS
    List<Vhost> getVhosts() {
        List<Vhost> vhostList = new ArrayList<>()
        data.each { v ->
            vhostList.add(v as Vhost)
        }
        return vhostList
    }

    @NonCPS
    public void init (String info) {
        data = slurper.parseText(info)
        println "Vhost Configs Load->>> " + data.toString()
        println "Init VHostInfoService - "

        vhosts = getVhosts()
    }

    @NonCPS
    public Vhost getVhostInfoForEnv(String envName ) {
        for (Vhost v : vhosts) {
            //println "vhost details for env " + v.envName
            if ( v.envName.equalsIgnoreCase(envName)) {
                return v ;
            }
        }
    }

}
