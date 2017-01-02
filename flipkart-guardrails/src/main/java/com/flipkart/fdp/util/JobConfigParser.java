package com.flipkart.fdp.util;

import com.flipkart.fdp.bagder.response.BadgerProcessDataResponse;

import java.util.HashMap;
import java.util.Map;

public class JobConfigParser
{
    private static final String CONF_SEPERATOR = ";";
    private static final String KEY_VALUE_SEPERATOR = "=";

    public static Map<String, String> getConfigMap(BadgerProcessDataResponse processData) {
        Map<String, String> configMap = new HashMap<String, String>();
        String jobConfig = processData.getJobConfig();
        String[] confs = jobConfig.split(CONF_SEPERATOR);
        for (String conf : confs) {
            String key = conf.split(KEY_VALUE_SEPERATOR)[0].trim();
            String value = conf.split(KEY_VALUE_SEPERATOR)[1].trim();
            configMap.put(key,value);
        }
        return configMap;
    }

    public static boolean validateJobConfig(BadgerProcessDataResponse processData) {
        String jobConfig = processData.getJobConfig();

        if (jobConfig == null || jobConfig.length() == 0 ||
                !jobConfig.contains(CONF_SEPERATOR) || !jobConfig.contains(KEY_VALUE_SEPERATOR)) {
            return false;
        }
        return true;
    }
}
