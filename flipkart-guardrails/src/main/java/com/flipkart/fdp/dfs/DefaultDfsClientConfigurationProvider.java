package com.flipkart.fdp.dfs;

import com.flipkart.fdp.bagder.config.BadgerConfiguration;
import com.flipkart.fdp.bagder.config.BadgerConfigurationFactory;
import com.flipkart.fdp.bagder.http.BadgerHttpClient;
import com.flipkart.fdp.bagder.http.ExponentialBackoffRetryPolicy;
import com.flipkart.fdp.bagder.Uri;
import com.flipkart.fdp.bagder.response.BadgerMrJobConfiguration;
import com.flipkart.fdp.bagder.response.BadgerProcessDataResponse;
import com.flipkart.fdp.util.JobConfigParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.DfsClientConfigurationProvider;

import java.util.Map;

/**
 * Implementation of {@link DfsClientConfigurationProvider}
 */
public class DefaultDfsClientConfigurationProvider implements DfsClientConfigurationProvider
{
    private final Long badgerProcessId;
    private final Long badgerExecutionId;

    public DefaultDfsClientConfigurationProvider(Long badgerProcessId, Long badgerExecutionId)
    {
        this.badgerProcessId = badgerProcessId;
        this.badgerExecutionId = badgerExecutionId;
    }

    /**
     * Method to add {@link BadgerMrJobConfiguration.env} into current {@link Configuration}
     * This calls badger service , gets the jobConfig , validates it and finally populates current
     * {@link Configuration}
     * @param jobConf
     */
    @Override
    public void loadDefaultDfsConfiguration(Configuration jobConf)
    {

        BadgerConfiguration configuration = BadgerConfigurationFactory.getBadgerConfiguration();
        String badgerUrl = "http://" + configuration.getBadgerHostPort();
        ExponentialBackoffRetryPolicy retryConfig = new ExponentialBackoffRetryPolicy(configuration.getRetryConfig().getMaxRetries(),
                configuration.getRetryConfig().getMaxSleepInMs(), configuration.getRetryConfig().getBaseSleepInMs());
        BadgerHttpClient instance = new BadgerHttpClient(badgerUrl, retryConfig);
        BadgerMrJobConfiguration mrJobConfiguration = instance.get(Uri.getJobConfig(badgerExecutionId, badgerProcessId),
                BadgerMrJobConfiguration.class);
        Map<String, String> confMap = mrJobConfiguration.getEnv();
        populateBadgerConfs(jobConf, confMap);

    }

    private void populateBadgerConfs(Configuration jobConf, Map<String, String> confMap)
    {
        for (Map.Entry<String, String> entry : confMap.entrySet()) {
            jobConf.set(entry.getKey(), entry.getValue());
        }

    }

}
