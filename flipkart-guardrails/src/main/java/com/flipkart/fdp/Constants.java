package com.flipkart.fdp;

import java.util.*;

import org.apache.hadoop.mapreduce.MRJobConfig;

/**
 * Created by pranav.agarwal on 01/12/16.
 */
public class Constants {
  public static final String FDP_CLIENT_PROPERTY_PREFIX = "fdp.client.mr.";
  public static final String FDP_PLATFORM_PROPERTY_PREFIX = "fdp.plat.mr.";
  public static final String BADGER_PROPERTY_PREFIX = "badger.";

  public static final String CONFIG_BUCKET_NAME = FDP_PLATFORM_PROPERTY_PREFIX + "configbucket.name";
  public static final String PASS_THROUGH = FDP_PLATFORM_PROPERTY_PREFIX + "jobconfig.adjust.passthrough";
  public static final String WHITE_LIST_CONF = FDP_PLATFORM_PROPERTY_PREFIX + "allow.properties";
  public static final String INPUT_DIRS = FDP_PLATFORM_PROPERTY_PREFIX + "input.dirs";
  public static final String OUTPUT_DIRS = FDP_PLATFORM_PROPERTY_PREFIX + "output.dirs";
  public static final String READ_THRESHOLD = FDP_PLATFORM_PROPERTY_PREFIX + "read.bytes.threshold";
  public static final String WRITE_THRESHOLD = FDP_PLATFORM_PROPERTY_PREFIX + "write.bytes.threshold";
  public static final String INPUT_CONTENT_THRESHOLD = FDP_PLATFORM_PROPERTY_PREFIX + "input.content.threshold";

  public static final String BADGER_PROCESSID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.processId";


  static final Map<String, PropertyDetails> FDP_PLATFORM_PROPERTIES = new LinkedHashMap<String, PropertyDetails>() {
    {
      put(MRJobConfig.MAPREDUCE_APPLICATION_FRAMEWORK_PATH, PropertyDetails.createProperty(
        PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.String));
      put(MRJobConfig.MAPREDUCE_APPLICATION_CLASSPATH, PropertyDetails.createProperty(
        PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.String));
      put(MRJobConfig.QUEUE_NAME, PropertyDetails.createProperty(PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.String));
      put(INPUT_DIRS, PropertyDetails.createProperty(PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.StringList));
      put(OUTPUT_DIRS, PropertyDetails.createProperty(PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.StringList));
      put(READ_THRESHOLD, PropertyDetails.createProperty(PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.Number));
      put(WRITE_THRESHOLD, PropertyDetails.createProperty(PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.Number));
      put(INPUT_CONTENT_THRESHOLD, PropertyDetails.createProperty(PropertyDetails.PropertyType.Mandatory,
        PropertyDetails.PropertyDataType.Number));
    }
  };

  public static final String SUPPORTED_BADGER_JOB_TYPE = "MR";

  static final Set<String> WHITE_LISTED_PROPERTIES = new HashSet<String>() {
    {
      add(MRJobConfig.INPUT_FORMAT_CLASS_ATTR);
      add(MRJobConfig.MAP_CLASS_ATTR);
      add(MRJobConfig.MAP_OUTPUT_COLLECTOR_CLASS_ATTR);
      add(MRJobConfig.COMBINE_CLASS_ATTR);
      add(MRJobConfig.REDUCE_CLASS_ATTR);
      add(MRJobConfig.OUTPUT_FORMAT_CLASS_ATTR);
      add(MRJobConfig.PARTITIONER_CLASS_ATTR);
      add(MRJobConfig.OUTPUT_KEY_CLASS);
      add(MRJobConfig.OUTPUT_VALUE_CLASS);
      add(MRJobConfig.KEY_COMPARATOR);
      add(MRJobConfig.COMBINER_GROUP_COMPARATOR_CLASS);
      add(MRJobConfig.GROUP_COMPARATOR_CLASS);
      add(MRJobConfig.JAR);
      add(MRJobConfig.JOB_NAME);
      add(MRJobConfig.CLASSPATH_ARCHIVES);
      add(MRJobConfig.CLASSPATH_FILES);
      add(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST);
      add(MRJobConfig.MAPREDUCE_JOB_CLASSLOADER);
      add(MRJobConfig.MAPREDUCE_JOB_CLASSLOADER_SYSTEM_CLASSES);
      add(MRJobConfig.MAP_OUTPUT_KEY_CLASS);
      add(MRJobConfig.MAP_OUTPUT_VALUE_CLASS);
      add(MRJobConfig.COMBINER_GROUP_COMPARATOR_CLASS);
      /**
       * adding this as it gets automatically set when we use {@link org.apache.hadoop.util.ToolRunner
       */
      add("mapreduce.client.genericoptionsparser.used");
      add("config.bucket.name");
    }
  };
}
