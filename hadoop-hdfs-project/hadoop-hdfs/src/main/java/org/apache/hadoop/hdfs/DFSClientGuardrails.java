package org.apache.hadoop.hdfs;

import java.io.IOException;

/**
 * Created by pranav.agarwal on 05/12/16.
 */
public interface DFSClientGuardrails {
  public static final String FDP_PLATFORM_PROPERTY_PREFIX = "fdp.plat.mr.";
  public static final String DFS_GUARDRAIL_CLASS = FDP_PLATFORM_PROPERTY_PREFIX + "dfs.guradrails.class";
  public static final String DFS_GUARDRAIL_CLASS_VALUE = "com.flipkart.fdp.dfs.DFSClientGuardRailsImpl";
  public static final String BADGER_PROPERTY_PREFIX = "badger.";
  public static final String BADGER_PROCESSID_CONF = BADGER_PROPERTY_PREFIX + "mapred.fact.processId";

  public void canReadFromLocation(String location) throws IOException;
  public void canReadFurther(String location, long alreadyRead) throws IOException;
  public void canWriteFurther(String location, long alreadyWritten) throws IOException;
  public void canWriteToLocation(String location) throws IOException;
}