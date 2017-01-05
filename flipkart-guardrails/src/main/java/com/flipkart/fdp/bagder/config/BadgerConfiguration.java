package com.flipkart.fdp.bagder.config;

import com.flipkart.fdp.utils.cfg.ConfigBucketKey;
import lombok.Getter;
import lombok.Setter;

/**
 *  Class to store configs related to badger service
 *  Badger service will provide us the configs related to MR jobs
 */
@ConfigBucketKey(name = "app")
public class BadgerConfiguration
{
    /**
     * host and port to connect to badger service
     */
    @Getter
    @Setter
    private String badgerHostPort;

    /**
     * Configs for retrying API calls
     */
    @Getter
    @Setter
    @ConfigBucketKey(name = "retryConfig")
    private RetryConfiguration retryConfig;

    public BadgerConfiguration()
    {

    }
}
