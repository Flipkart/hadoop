package com.flipkart.fdp;

import java.io.IOException;
import java.util.*;

import com.flipkart.fdp.bagder.Uri;
import com.flipkart.fdp.bagder.config.BadgerConfiguration;
import com.flipkart.fdp.bagder.config.BadgerConfigurationFactory;
import com.flipkart.fdp.bagder.http.BadgerHttpClient;
import com.flipkart.fdp.bagder.http.ExponentialBackoffRetryPolicy;
import com.flipkart.fdp.bagder.response.BadgerProcessDataResponse;
import com.flipkart.fdp.util.JobConfigParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.AdjustJobConfiguration;
import org.apache.hadoop.mapreduce.Job;

/**
 * Created by pranav.agarwal on 29/11/16.
 */


public class AdjustJobConfigurationImpl implements AdjustJobConfiguration {
  private static final Log LOGGER = LogFactory.getLog(AdjustJobConfigurationImpl.class);
  private final BadgerHttpClient badgerHttpClient;
  private final Long badgerProcessID;
  private final Configuration fdpConf;
  private final Map<String, String> processDataProperties = new LinkedHashMap<String, String>();
  private BadgerProcessDataResponse badgerProcessData = null;


  private Map<String, String> fetchProperties(long processID) throws IOException {
    BadgerProcessDataResponse response = badgerHttpClient.get(Uri.getProcessData(processID), BadgerProcessDataResponse.class);
    if (!JobConfigParser.validateJobConfig(response)) {
      throw new RuntimeException("Received invalid job config from badger");
    }
    return JobConfigParser.getConfigMap(response);
  }

  public AdjustJobConfigurationImpl(Long badgerProcessID)  {
    this.badgerProcessID = badgerProcessID;
    Job j;
    try {
      j = new Job(new Configuration());
    } catch (IOException e) {
      throw new RuntimeException("unable to create new Job instance");
    }
    fdpConf = j.getConfiguration();

    Configuration conf = new Configuration(false);
    conf.addResource("fk-fdp-mr-default-site.xml");
    conf.addResource("fk-fdp-mr-site.xml");

    BadgerConfiguration configuration = BadgerConfigurationFactory.getBadgerConfiguration();
    String badgerUrl = "http://" + configuration.getBadgerHostPort();
    ExponentialBackoffRetryPolicy retryConfig = new ExponentialBackoffRetryPolicy(configuration.getRetryConfig().getMaxRetries(),
            configuration.getRetryConfig().getMaxSleepInMs(), configuration.getRetryConfig().getBaseSleepInMs());
    badgerHttpClient = new BadgerHttpClient(badgerUrl, retryConfig);

    badgerProcessData = badgerHttpClient.get(Uri.getProcessData(badgerProcessID), BadgerProcessDataResponse.class);

    for (Map.Entry<String, String> property : conf) {
      processDataProperties.put(property.getKey(), property.getValue());
    }
    try {
      processDataProperties.putAll(fetchProperties(this.badgerProcessID));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (processDataProperties.get(Constants.WHITE_LIST_CONF) != null) {
      for (String allowedConfig : processDataProperties.get(Constants.WHITE_LIST_CONF).split(",")) {
        Constants.WHITE_LISTED_PROPERTIES.add(allowedConfig);
      }
    }
  }

  private void ensureAllPropertiesAreAvailable(Map<String, String> properties) {
    Set<String> missingProperties = new LinkedHashSet<String>();
    Set<String> invalidProperties = new LinkedHashSet<String> ();
    for (Map.Entry<String, PropertyDetails> property : Constants.FDP_PLATFORM_PROPERTIES.entrySet()) {
      if (property.getValue().getPropertyType() == PropertyDetails.PropertyType.Mandatory &&
      properties.get(property.getKey()) == null) {
        missingProperties.add(property.getKey());
        continue;
      } else if (properties.get(property.getKey()) == null) {
        LOGGER.info(String.format("Non-Mandatory property %s is not set by Badger", property.getKey()));
        continue;
      }
      switch(property.getValue().getPropertyDataType()) {
        case Number:
          try {
            Long.parseLong(properties.get(property.getKey()));
          } catch (NumberFormatException e) {
            invalidProperties.add(property.getKey());
          }
          break;
        case String:
          if (properties.get(property.getKey()) == null || properties.get(property.getKey()).equals("")) {
            invalidProperties.add(property.getKey());
          }
          break;
      }
    }
    for (String propName : missingProperties) {
      LOGGER.error(String.format("Mandatory property %s is not set by Badger", propName));
    }
    for (String propName : invalidProperties) {
      LOGGER.error(String.format("Mandatory property %s is incorrectly set by Badger", propName));
    }
    if (missingProperties.size() > 0 || invalidProperties.size() > 0) {
      throw new RuntimeException("Mandatory properties missing");
    }
  }

  @Override
  public void adjustJobConfiguration(Configuration jobConf) {
    if (!badgerProcessData.getType().equals(Constants.SUPPORTED_BADGER_JOB_TYPE)) {
      LOGGER.info(String.format("Job is of type %s, skipping Job Configuration Adjustment...",
        badgerProcessData.getType()));
      jobConf.set(Constants.PASS_THROUGH, "true");
      return;
    }

    ensureAllPropertiesAreAvailable(processDataProperties);

    Map<String, String> unknownProperties = new LinkedHashMap<String, String>();
    Map<String, String> mismatchProperties = new LinkedHashMap<String, String>();

    for (Map.Entry<String, String> inputProperty : jobConf) {
      // value returned by Configuration iterator is different from the get call,
      // so don't use inputProperty.getValue() instead fetch the value and then use
      String inputProperyValue = jobConf.get(inputProperty.getKey());
      if (Constants.WHITE_LISTED_PROPERTIES.contains(inputProperty.getKey())){
        LOGGER.info(String.format("Whitelisted property found %s=%s", inputProperty.getKey(), inputProperyValue));
      }
      if (inputProperty.getKey().startsWith(Constants.FDP_PLATFORM_PROPERTY_PREFIX) &&
              !processDataProperties.containsKey(inputProperty.getKey())) {

        throw new RuntimeException(String.format("Exiting.. Unexpected property starting with %s found. %s=%s",
          Constants.FDP_PLATFORM_PROPERTY_PREFIX, inputProperty.getKey(), inputProperty.getValue() ));
      }

      if (fdpConf.get(inputProperty.getKey()) != null
        && fdpConf.get(inputProperty.getKey()).equals(inputProperyValue)) {
        continue;
      }
      if (inputProperty.getKey().startsWith(Constants.FDP_CLIENT_PROPERTY_PREFIX) ||
        inputProperty.getKey().startsWith(Constants.BADGER_PROPERTY_PREFIX) ||
        //TODO: get away with BADGER_PROPERTY_PREFIX, as everything should come as FDP_PLATFORM_PROPERTY_PREFIX
        Constants.WHITE_LISTED_PROPERTIES.contains(inputProperty.getKey())) {
        LOGGER.info(String.format("Found configuration: %s=%s", inputProperty.getKey(), inputProperyValue));
        continue;
      }
      if (processDataProperties.get(inputProperty.getKey()) != null &&
        processDataProperties.get(inputProperty.getKey()).equals(inputProperyValue)) {
        LOGGER.info(String.format("Persisting processData configuration: %s=%s", inputProperty.getKey(),
            inputProperyValue));
          continue;
      }
      if (processDataProperties.get(inputProperty.getKey()) != null &&
        !processDataProperties.get(inputProperty.getKey()).equals(inputProperyValue)) {
        inputProperty.setValue(processDataProperties.get(inputProperty.getKey()));
        LOGGER.info(String.format("ProcessData configuration override for: %s from %s ***** TO ***** %s", inputProperty.getKey(),
          inputProperyValue, processDataProperties.get(inputProperty.getKey())));
        continue;
      }
      if (fdpConf.get(inputProperty.getKey()) == null) {
        unknownProperties.put(inputProperty.getKey(), inputProperyValue);
        continue;
      }
      if (!fdpConf.get(inputProperty.getKey()).equals(inputProperyValue)) {
        mismatchProperties.put(inputProperty.getKey(), inputProperyValue +
          "***** , **** expected value is:" + fdpConf.get(inputProperty.getKey()));
        continue;
      }
    }
    if (unknownProperties.size() > 0 || mismatchProperties.size() > 0) {
      for (Map.Entry<String, String> property : unknownProperties.entrySet()) {
        LOGGER.info(String.format("Unknown Configuration: %s=%s", property.getKey(), property.getValue()));
      }
      for (Map.Entry<String, String> property : mismatchProperties.entrySet()) {
        LOGGER.info(String.format("Configuration Violation: %s=%s", property.getKey(), property.getValue()));
      }
      throw new RuntimeException("Invalid properties set in Job Config. Exiting...");
    }
    // set all processdata properties (properties set in process data job config + fk-fdp-mr-site.xml)
    for (Map.Entry<String, String> property : processDataProperties.entrySet()) {
        if (jobConf.get(property.getKey()) != null &&
          jobConf.get(property.getKey()).equals(property.getValue())) {
          continue;
        }
        LOGGER.info(String.format("Enforcing processData configuration: %s=%s", property.getKey(), property.getValue()));
        jobConf.set(property.getKey(), property.getValue(), "Badger processData property");
    }
    jobConf.set(Constants.PASS_THROUGH, "false");
  }
  public static void main(String[] args) throws IOException {

    AdjustJobConfigurationImpl fc = new AdjustJobConfigurationImpl((Long) 72760l);
    try {
      fc.adjustJobConfiguration((new Job(new Configuration(true))).getConfiguration());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
