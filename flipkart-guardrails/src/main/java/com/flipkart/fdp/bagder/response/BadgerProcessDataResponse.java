package com.flipkart.fdp.bagder.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response class for storing configurations of a badger processData
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BadgerProcessDataResponse implements BadgerResponse
{
    /**
     * Type of job like MR, HIVE, CMD
     */
    @JsonProperty
    private String type;

    /**
     * configurations related to MR job
     */
    @JsonProperty
    private String jobConfig;
}
