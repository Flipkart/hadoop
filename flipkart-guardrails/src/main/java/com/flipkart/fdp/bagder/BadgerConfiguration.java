package com.flipkart.fdp.bagder;

import com.flipkart.fdp.utils.cfg.ConfigBucketKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigBucketKey
public class BadgerConfiguration
{
    private String badgerHostPort;

    public BadgerConfiguration()
    {
    }
}
