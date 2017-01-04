package com.flipkart.fdp.bagder;

import com.flipkart.fdp.RestServiceClient;
import com.flipkart.fdp.bagder.response.BadgerResponse;

public class BadgerHttpClient
{
    private static RestServiceClient s_rsc = null;
    private String url;

    public BadgerHttpClient(String url)
    {
        if(s_rsc == null) {
            synchronized (BadgerHttpClient.class) {
                s_rsc = instantiateRestServiceClient(url);
            }
        }
    }

    static private RestServiceClient instantiateRestServiceClient(String URL) {
        return new RestServiceClient(URL);
    }

    public <T extends BadgerResponse> T get(final String uri, final Class<T> responseClazz)
    {
        try {
            return s_rsc.getRESTGetResponse(uri, responseClazz);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Exception occurred while contacting badger service %s",
                    e.getStackTrace()));
        }
    }
}
