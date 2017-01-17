package com.flipkart.fdp.dfs;

import com.flipkart.fdp.Constants;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSClientGuardrails;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by pranav.agarwal on 05/12/16.
 */
public class DFSClientGuardRailsImpl implements DFSClientGuardrails, Runnable {
  private static final Log LOGGER = LogFactory.getLog(DFSClientGuardRailsImpl.class);
  private static final String JOBHISTORY_DIR_INTERMEDIATE = "mapreduce.jobhistory.intermediate-done-dir";
  private static final String JOBHISTORY_DIR_DONE = "mapreduce.jobhistory.done-dir";
  private static final String MR_APP_FRAMEWORK_PATH = "mapreduce.application.framework.path";
  private static final String YARN_APP_STAGING_DIR = "yarn.app.mapreduce.am.staging-dir";
  private static final String STAGING_DIR_SUFFIX = "/.staging";

  private long writeThreshHold;
  private long readThreshold;
  private List<String> inputDirs;
  private List<String> outputDirs;
  private List<String> whiteList;
  private boolean raiseException = false;
  private Configuration conf;
  private boolean ignoreGuardrails;

  public DFSClientGuardRailsImpl(Configuration conf) throws IOException {
    // DFSClient instance is created by processes such as JobHistoryUtils, and those don't use Driver's JobConfig
    if (conf.get(DFSClientGuardrails.BADGER_PROCESSID_CONF) == null) {
      ignoreGuardrails = true;
      return;
    } else {
      ignoreGuardrails = false;
    }
    whiteList = new ArrayList<String>();
    this.conf = conf;
    writeThreshHold = conf.getLong(Constants.WRITE_THRESHOLD, -1l);
    if (writeThreshHold == -1) {
      throw new RuntimeException(String.format("%s property not set", Constants.WRITE_THRESHOLD));
    }
    readThreshold = conf.getLong(Constants.READ_THRESHOLD, -1l);
    if (readThreshold == -1) {
      throw new RuntimeException(String.format("%s property not set", Constants.READ_THRESHOLD));
    }
    inputDirs = Arrays.asList(conf.getStrings(Constants.INPUT_DIRS,""));
    if (inputDirs.size() == 1 && inputDirs.get(0).equals("")) {
      throw new RuntimeException(String.format("%s property not set", Constants.INPUT_DIRS));
    }
    outputDirs = Arrays.asList(conf.getStrings(Constants.OUTPUT_DIRS,""));
    if (outputDirs.size() == 1 && outputDirs.get(0).equals("")) {
      throw new RuntimeException(String.format("%s property not set", Constants.OUTPUT_DIRS));
    }

    if (conf.get(JOBHISTORY_DIR_INTERMEDIATE) != null) {
      whiteList.add(conf.get(JOBHISTORY_DIR_INTERMEDIATE));
    }
    if (conf.get(JOBHISTORY_DIR_DONE) != null) {
      whiteList.add(conf.get(JOBHISTORY_DIR_DONE));
    }
    if (conf.get(MR_APP_FRAMEWORK_PATH) != null) {
      whiteList.add(conf.get(MR_APP_FRAMEWORK_PATH));
    }
    String appStagingDIR = conf.get(YARN_APP_STAGING_DIR);
    UserGroupInformation user = UserGroupInformation.getCurrentUser();

    if (appStagingDIR != null && user != null) {
      if (appStagingDIR.endsWith("/")) {
        appStagingDIR = appStagingDIR.substring(0, appStagingDIR.length() -1);
      }
      String stagingDir = appStagingDIR + "/" + user.getUserName() + STAGING_DIR_SUFFIX;
      whiteList.add(stagingDir);
      LOGGER.info(String.format("Adding staging dir to whitelist %s", stagingDir));
    }



//    ses = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("DFSClientGuradrailImpl ").build());
//    ses.scheduleAtFixedRate(this, 60L, 300L, TimeUnit.SECONDS);

  }
  @Override public void run() {
    FileSystem fs = null;
    try {
      fs = FileSystem.get(conf);
      for (String s : outputDirs) {
        Path path = new Path(s);
        if (fs.getContentSummary(path).getSpaceConsumed() > writeThreshHold) {
          raiseException = false;
        }
      }
    } catch (IOException e) {
//      raiseExceptionCounter++;
//      if (raiseExceptionCounter > 3) {
        raiseException = true;
      //}
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch(Exception e) {}
      }
    }
  }
  private boolean locationAccessible(String src, List<String> dirs) {
    LOGGER.info(String.format("locationAccessible invoked for :%s", src));

    if (ignoreGuardrails == true) {
      return true;
    }
    if (raiseException == true) {
      return false;
    }
    for (String dir : whiteList) {
      if (src.startsWith(dir)) {
        return true;
      }
    }
    for (String dir : dirs) {
      if (src.startsWith(dir)) {
        return true;
      }
    }

    return false;
  }
  @Override
  public void canReadFromLocation(String location) throws IOException {
    if (!locationAccessible(location, inputDirs)) {
      throw new IOException("Not authorized to read from " + location);
    }
  }
  @Override
  public void canWriteToLocation(String location) throws IOException {
    if (!locationAccessible(location, outputDirs)) {
      throw new IOException("Not authorized to write to " + location);
    }
  }

  @Override
  public void canReadFurther(String location, long alreadyRead) throws IOException {
    LOGGER.debug(String.format("canReadFurther validation call for %s, alreadyRead %s", location, alreadyRead));
    if (ignoreGuardrails == true) {
      return;
    }
    if (alreadyRead > readThreshold) {
      throw new IOException(String.format("Location %s, Already read %s, threshold is %s",
        location, alreadyRead, readThreshold));
    }
  }
  @Override
  public void canWriteFurther(String location, long alreadyWritten) throws IOException {
    LOGGER.debug(String.format("canWriteFurther validation call for %s, alreadywritten %s", location, alreadyWritten));
    if (ignoreGuardrails == true) {
      return;
    }
  }
}
