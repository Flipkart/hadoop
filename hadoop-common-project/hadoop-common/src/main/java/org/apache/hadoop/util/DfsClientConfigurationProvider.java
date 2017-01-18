package org.apache.hadoop.util;

import org.apache.hadoop.conf.Configuration;

/**
 * interface to provide configurations which are required even before job.submit()
 * like what all directories job can read and write.
 * this is required as input paths {Job.setInputPaths()} and output paths {Job.setOutputPaths()}
 * are set before Job.submit()
 */
public interface DfsClientConfigurationProvider
{
    public static final String DFS_DEFAULT_CONF_CLASS_VALUE = "com.flipkart.fdp.dfs.DefaultDfsClientConfigurationProvider";
    public static final String BADGER_PROPERTY_PREFIX = "badger.";
    public static final String BADGER_PROCESSID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.processId";
    public static final String BADGER_EXECUTIONID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.executionId";

    /**
     * Method to add badger configs into {@link Configuration }
     * @param jobConf
     */
    public void loadDefaultDfsConfiguration(Configuration jobConf);
}
