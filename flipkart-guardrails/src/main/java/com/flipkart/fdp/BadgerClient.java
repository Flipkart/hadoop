package com.flipkart.fdp;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.util.Map;
import java.util.Random;


/**
 * Created by pranav.agarwal on 01/12/16.
 */

public class BadgerClient {
  private static final Log LOGGER = LogFactory.getLog(BadgerClient.class);
  public static class BadgerProcessData {
    @JsonProperty("type")
    String type;

    @JsonProperty("jobConfig")
    String jobConfig;
    public String getJobConfig() {
      return jobConfig;
    }
    public String getType() {
      return type;
    }
  }

  private static class ExponentialBackoffRetry {

    private final int  baseSleepTimeMs;
    private final int maxRetries;
    private final int maxSleepMs;
    private final Random random;

    public ExponentialBackoffRetry(int baseSleepTimeMs, int maxRetries, int maxSleepMs) {
      this.baseSleepTimeMs = baseSleepTimeMs;
      this.maxRetries = maxRetries;
      this.maxSleepMs = maxSleepMs;
      this.random = new Random();
    }

    public boolean allowRetry(int retryCount, Exception e) {

      if ( retryCount <= maxRetries ) {
        try {
          Thread.sleep(getSleepTimeMs(retryCount));
        } catch ( InterruptedException ie ) {
          Thread.currentThread().interrupt();
          return false;
        }
        return true;
      }
      return false;
    }

    private int getSleepTimeMs(int retryCount)
    {
      // copied from Curator's RetryPolicies.java
      int sleepMs = baseSleepTimeMs * Math.max(1, random.nextInt(1 << (retryCount + 1)));
      if ( sleepMs > maxSleepMs ) {
        LOGGER.warn("Sleep extension too large " + sleepMs + ". Pinning to " + maxSleepMs);
        sleepMs = maxSleepMs;
      }
      return sleepMs;
    }
  }

  private static RestServiceClient s_rsc = null;

  private final String BADGER_HOST_PORT_KEY = "badgerHostPort";
  private static final String HTTP = "http://";
  private static final String PATH = "/view/";
  public static final int DEFAULT_REQUEST_TIMEOUT_MILLI_SECONDS = 60000;
  private static final String EMPTY_RESPONSE_STRING = "\"EMPTY_RESPONSE\"";

  private String bucketName;
  private String badgerURL;
  private ExponentialBackoffRetry retryPolicy;
  private int requestTimeoutMillis;

  public BadgerClient(Configuration conf) {
    bucketName = conf.get(Constants.CONFIG_BUCKET_NAME);
    if (bucketName == null) {
      throw new RuntimeException("config bucket name not found");
    }
    //cs.initialize(bucketName);
    //badgerURL = HTTP + (String) cs.getAllKeys().get(BADGER_HOST_PORT_KEY);
    //TODO: There is no method to close the configServiceImpl so not using it for now...
    //TODO: OR is it maybe cause of the Sl4j thing.. thats been used inside configServiceImpl
    //TODO: Issue is even after the last line if executed in main, the application is still running

    badgerURL = HTTP + "10.34.57.183:28220";

    if (badgerURL == null) {
      throw new RuntimeException(BADGER_HOST_PORT_KEY + " key not found in " + bucketName + " config bucket");
    }

    int baseSleepTimeMs = conf.getInt("TODO_CONFIG_NAME", 1000);
    int baseMaxSleepTimeMs = conf.getInt("TODO_CONFIG_NAME", 3600000);
    int maxRetry = conf.getInt("TODO_CONFIG_NAME", 1);
    retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetry, baseMaxSleepTimeMs);
    requestTimeoutMillis = conf.getInt("TODO_CONFIG_NAME", DEFAULT_REQUEST_TIMEOUT_MILLI_SECONDS);
    if (s_rsc == null) {
      synchronized (BadgerClient.class) {
        s_rsc = instantiateRestServiceClient(badgerURL);
      }
    }
  }

  static private RestServiceClient instantiateRestServiceClient(String URL) {
      return new RestServiceClient(URL);
  }

  public BadgerProcessData getProcessDataDetails(Long processDataID) {
    BadgerProcessData badgerProcessData =  executesSync("GET", PATH + processDataID.toString(), null, null,
      BadgerProcessData.class);
    return badgerProcessData;
  }

  public <R> R executesSync(String method, String path, Map<String, String> queryParams, Object requestEntity,
                            Class<R> clss) {
    int retryCount = 0;
    Exception exception;
    do {
      try {
        if (method.equals("GET")) {
          return s_rsc.getRESTGetResponse(String.format( "%s", path), queryParams, requestEntity, clss);
        } else {
          throw new RuntimeException ("method other than GET not supported");
        }
      } catch (RuntimeException e) {
        exception = e;
        LOGGER.info("Retry count " + retryCount + "Caught exception" + e);
        retryCount++;
      } catch (IOException e) {
        exception = e;
        LOGGER.info("Retry count " + retryCount + "Caught exception" + e);
        //TODO change to logger.ERROR
        retryCount++;
      }
    } while (retryPolicy.allowRetry(retryCount, exception));
    throw new RuntimeException(exception);
  }

}
