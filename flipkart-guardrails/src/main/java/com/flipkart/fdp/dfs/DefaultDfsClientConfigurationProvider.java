package com.flipkart.fdp.dfs;

import com.flipkart.fdp.bagder.BadgerConfiguration;
import com.flipkart.fdp.bagder.BadgerHttpClient;
import com.flipkart.fdp.bagder.Uri;
import com.flipkart.fdp.bagder.response.BadgerProcessDataResponse;
import com.flipkart.fdp.util.JobConfigParser;
import com.flipkart.fdp.utils.cfg.ConfigServiceImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.DfsClientConfigurationProvider;

import java.util.Map;

public class DefaultDfsClientConfigurationProvider implements DfsClientConfigurationProvider
{
    private Long badgerProcessId;

    public DefaultDfsClientConfigurationProvider(Long badgerProcessId)
    {
        this.badgerProcessId = badgerProcessId;
    }

    @Override
    public void loadDefaultDfsConfiguration(Configuration jobConf)
    {
        ConfigServiceImpl impl = new ConfigServiceImpl();
        //TODO - config bucket name to be made configurable
        impl.initialize("prod-fdpflow-mrsrvc-a");
        BadgerConfiguration configuration = impl.getConfig(BadgerConfiguration.class);
        BadgerProcessDataResponse response = BadgerHttpClient.get(Uri.getProcessData(configuration.getBadgerHostPort(),
                badgerProcessId), BadgerProcessDataResponse.class);
        if (!JobConfigParser.validateJobConfig(response)) {
            throw new RuntimeException("Received invalid job config from badger");
        }
        Map<String,String> confMap = JobConfigParser.getConfigMap(response);
        populateBadgerConfs(jobConf,confMap);

    }
    private void populateBadgerConfs(Configuration jobConf, Map<String, String> confMap) {
        for (Map.Entry<String, String> entry : confMap.entrySet()) {
            jobConf.set(entry.getKey(), entry.getValue());
        }

    }

}
