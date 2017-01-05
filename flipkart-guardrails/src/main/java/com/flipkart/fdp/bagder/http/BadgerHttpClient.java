package com.flipkart.fdp.bagder.http;

import com.flipkart.fdp.bagder.response.BadgerResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HttpClient to connect to badger service and make API calls
 */
public class BadgerHttpClient
{
    private static final Log LOG = LogFactory.getLog(BadgerHttpClient.class);
    private static RestServiceClient s_rsc = null;
    private String url;
    private ExponentialBackoffRetryPolicy retryPolicy;

    public BadgerHttpClient(String url, ExponentialBackoffRetryPolicy retryPolicy)
    {
        this.url = url;
        this.retryPolicy = retryPolicy;
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
        return executeWithRetry(HttpMethod.GET, uri, responseClazz);
    }


    public  <T> T executeWithRetry(HttpMethod method, String url, Class<T> responseClazz) {
        int retryCount = 0;

        Exception exception;
        do {
            if(!method.equals(HttpMethod.GET)) {
                throw new RuntimeException("Only GET method is supported");
            }
            try {
                LOG.info(String.format("Retry count : %s",retryCount));
                return s_rsc.getRESTGetResponse(url, responseClazz);
            }
            catch (Exception ex) {
                exception = ex ;
                retryCount++;
                LOG.info(String.format("Caught Exception %s",ex));
            }
        } while (retryPolicy.isRetryAllowed(retryCount));
        throw new RuntimeException(String.format("Retry budget of %s expired.Got Exception : %s",
                retryPolicy.getMaxRetries(), exception));
    }
}
