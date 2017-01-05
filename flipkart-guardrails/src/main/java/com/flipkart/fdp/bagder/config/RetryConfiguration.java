package com.flipkart.fdp.bagder.config;

import com.flipkart.fdp.utils.cfg.ConfigBucketKey;
import com.flipkart.fdp.utils.cfg.PropertySetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ConfigBucketKey
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RetryConfiguration implements PropertySetter
{
    @ConfigBucketKey(usePropertySetter = true)
    private int maxRetries;

    @ConfigBucketKey(usePropertySetter = true)
    private Long maxSleepInMs;

    @ConfigBucketKey(usePropertySetter = true)
    private Long baseSleepInMs;

    @Override
    public void setProperty(String property, Object propertyValue)
    {
        switch (property) {
            case "maxRetries" :
                maxRetries = Integer.parseInt(propertyValue.toString());
                break;
            case "maxSleepInMs" :
                maxSleepInMs = Long.parseLong(propertyValue.toString());
                break;
            case "baseSleepInMs" :
                baseSleepInMs = Long.parseLong(propertyValue.toString());
                break;
        }
    }
}
