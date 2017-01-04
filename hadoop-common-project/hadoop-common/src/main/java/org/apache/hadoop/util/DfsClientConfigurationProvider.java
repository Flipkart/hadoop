package org.apache.hadoop.util;

import org.apache.hadoop.conf.Configuration;

public interface DfsClientConfigurationProvider
{
    public static final String DFS_DEFAULT_CONF_CLASS_VALUE = "com.flipkart.fdp.dfs.DefaultDfsClientConfigurationProvider";
    public static final String BADGER_PROPERTY_PREFIX = "badger.";
    public static final String BADGER_PROCESSID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.processId";

    public void loadDefaultDfsConfiguration(Configuration jobConf);
}
