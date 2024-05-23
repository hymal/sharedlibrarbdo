package com.apigee.loader;

import java.net.MalformedURLException;

/**
 * Used to support bootstrapping for data required for a jenkins pipeline to run
 */
public interface BootLoader {

    void setupConfig(String endpoint) throws MalformedURLException;
    boolean validate();

}
