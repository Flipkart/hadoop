package org.apache.hadoop.mapreduce;

import org.apache.hadoop.conf.Configuration;

/**
 * Created by pranav.agarwal on 02/12/16.
 */
public interface AdjustJobConfiguration {
  public static final String ADJUST_JOBCONFIG_CLASS_VALUE = "com.flipkart.fdp.AdjustJobConfigurationImpl";
  public static final String BADGER_PROPERTY_PREFIX = "badger.";
  public static final String BADGER_PROCESSID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.processId";
  public static final String BADGER_EXECUTIONID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.executionId";
  public void adjustJobConfiguration(Configuration jobConf);
}
