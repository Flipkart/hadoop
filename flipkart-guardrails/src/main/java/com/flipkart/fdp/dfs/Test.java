package com.flipkart.fdp.dfs;

import com.flipkart.fdp.bagder.BadgerConfiguration;
import com.flipkart.fdp.bagder.BadgerHttpClient;
import com.flipkart.fdp.bagder.Uri;
import com.flipkart.fdp.bagder.response.BadgerProcessDataResponse;
import com.flipkart.fdp.utils.cfg.ConfigServiceImpl;
import org.apache.hadoop.conf.Configuration;

public class Test
{
    public static void main(String[] args)
    {
        ConfigServiceImpl impl = new ConfigServiceImpl();
        impl.initialize("prod-fdpflow-mrsrvc-a");
        impl.getAllKeys();
        BadgerConfiguration configuration = impl.getConfig(BadgerConfiguration.class);
        System.out.println(configuration.getBadgerHostPort());
        BadgerProcessDataResponse response = BadgerHttpClient.get(Uri.getProcessData(configuration.getBadgerHostPort(),
                11192l), BadgerProcessDataResponse.class);
        DefaultDfsClientConfigurationProvider pr = new DefaultDfsClientConfigurationProvider(11192l);
        pr.loadDefaultDfsConfiguration(new Configuration());
        System.out.println("aa");
    }
}
