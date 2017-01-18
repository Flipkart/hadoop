package com.flipkart.fdp;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

/**
 * Created by pranav.agarwal on 02/12/16.
 */
public class TestFDPJobConfig {
  public static void main(String [] args) {
    AdjustJobConfigurationImpl fc = new AdjustJobConfigurationImpl((Long) 11192l, 11192l);
    try {
      fc.adjustJobConfiguration((new Job(new Configuration(true))).getConfiguration());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
