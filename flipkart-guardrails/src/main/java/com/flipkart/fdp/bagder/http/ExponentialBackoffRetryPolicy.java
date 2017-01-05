package com.flipkart.fdp.bagder.http;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;

@Getter
@Setter
public class ExponentialBackoffRetryPolicy
{
    private Log LOG = LogFactory.getLog(ExponentialBackoffRetryPolicy.class);
    private final int maxRetries;
    private final Long maxSleepInMs;
    private final Long baseSleepInMs;
    private final Random random;

    public ExponentialBackoffRetryPolicy(int maxRetries, Long maxSleepInMs, Long baseSleepInMs)
    {
        this.maxRetries = maxRetries;
        this.maxSleepInMs = maxSleepInMs;
        this.baseSleepInMs = baseSleepInMs;
        this.random = new Random();
    }

    public Long getSleepTimeMs(int retryCount)
    {
        // copied from Curator's RetryPolicies.java
        Long sleepMs = baseSleepInMs * Math.max(1, random.nextInt(1 << (retryCount + 1)));
        if ( sleepMs > maxSleepInMs ) {
            LOG.warn("Sleep extension too large " + sleepMs + ". Pinning to " + maxSleepInMs);
            sleepMs = maxSleepInMs;
        }
        return sleepMs;
    }

    public boolean isRetryAllowed(int numOfRetries) {
        if(numOfRetries < maxRetries) {
            try {
                Thread.currentThread().sleep(getSleepTimeMs(numOfRetries));
                return true;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

}
