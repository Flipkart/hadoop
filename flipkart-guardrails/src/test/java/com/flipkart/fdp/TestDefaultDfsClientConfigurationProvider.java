package com.flipkart.fdp;

import com.flipkart.fdp.bagder.config.BadgerConfiguration;
import com.flipkart.fdp.bagder.config.BadgerConfigurationFactory;
import com.flipkart.fdp.bagder.http.BadgerHttpClient;
import com.flipkart.fdp.bagder.http.ExponentialBackoffRetryPolicy;
import com.flipkart.fdp.bagder.Uri;
import com.flipkart.fdp.bagder.response.BadgerProcessDataResponse;

public class TestDefaultDfsClientConfigurationProvider
{
    public static void main(String[] args)
    {
        BadgerConfiguration configuration = BadgerConfigurationFactory.getBadgerConfiguration();
        System.out.println(configuration.getBadgerHostPort());
        String badgerUrl = "http://" + configuration.getBadgerHostPort();
        ExponentialBackoffRetryPolicy retryPolicy = new ExponentialBackoffRetryPolicy(configuration.getRetryConfig().getMaxRetries(),
                configuration.getRetryConfig().getMaxSleepInMs(), configuration.getRetryConfig().getBaseSleepInMs());
        BadgerHttpClient instance =  new BadgerHttpClient(badgerUrl, retryPolicy);
        BadgerProcessDataResponse response = instance.get(Uri.getProcessData(
                11192l), BadgerProcessDataResponse.class);
        System.out.println("aa");
    }
}
