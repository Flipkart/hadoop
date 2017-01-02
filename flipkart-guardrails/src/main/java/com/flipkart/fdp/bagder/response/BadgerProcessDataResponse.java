package com.flipkart.fdp.bagder.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BadgerProcessDataResponse implements BadgerResponse
{
    @JsonProperty
    private String type;

    @JsonProperty
    private String jobConfig;
}
