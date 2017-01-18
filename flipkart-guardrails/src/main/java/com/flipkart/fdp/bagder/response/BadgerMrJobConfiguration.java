package com.flipkart.fdp.bagder.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BadgerMrJobConfiguration implements BadgerResponse
{
    private String command;
    private Map<String, String> env;
}
