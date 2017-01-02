package com.flipkart.fdp.bagder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.fdp.bagder.response.BadgerResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class BadgerHttpClient
{
    public static <T extends BadgerResponse> T get(final String uri, final Class<T> responseClazz)
    {
        try {
            HttpResponse<String> response = Unirest.get(uri)
                    .asString();
            T obj = new ObjectMapper().readValue(response.getBody(), responseClazz);
            return obj;
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Exception occurred while contacting badger service %s",
                    e.getStackTrace()));
        }
    }
}
