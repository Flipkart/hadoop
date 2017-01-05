package com.flipkart.fdp.bagder.config;

import com.flipkart.fdp.utils.cfg.ConfigServiceImpl;

/**
 * Factory class for getting {@link BadgerConfiguration}
 *
 */
public class BadgerConfigurationFactory
{
    private static BadgerConfiguration instance = null;

    public static BadgerConfiguration getBadgerConfiguration()
    {
        if (instance == null) {
            synchronized (BadgerConfiguration.class) {
                if (instance == null) {
                    instance = initializeBadgerConfiguration();
                }
            }
        }
        return instance;
    }

    /**
     * Connects to config service and gets back with {@link BadgerConfiguration}
     * @return
     */
    private static BadgerConfiguration initializeBadgerConfiguration()
    {
        ConfigServiceImpl impl = new ConfigServiceImpl();
        //TODO - config bucket name to be made configurable
        impl.initialize("prod-fdpflow-mrsrvc-a");
        return impl.getConfig(BadgerConfiguration.class);
    }
}
