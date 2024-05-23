package com.apigee.cicd.service

import com.apigee.cicd.model.EntityDeploymentInfo
import com.apigee.cicd.model.Planet;
import com.apigee.loader.ConfigLoader;
import com.cloudbees.groovy.cps.NonCPS;

class AssetService implements ConfigLoader {

    private static AssetService ins = new AssetService();
    def data;

    @NonCPS
    static AssetService getInstance() {
        return ins;
    }

    private AssetService() {
    }


    @Override
    public void init(String info) {
        assert info != null;
        data = new groovy.json.JsonSlurperClassic().parseText(info);
    }

}
