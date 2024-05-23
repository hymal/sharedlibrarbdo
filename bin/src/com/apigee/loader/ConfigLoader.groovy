package com.apigee.loader;

/**
 * Used to support loading and setup of a configuration needed for CiPipeline env
 */
public interface ConfigLoader {

    // load all the configurations
    public void init(String info);

}
