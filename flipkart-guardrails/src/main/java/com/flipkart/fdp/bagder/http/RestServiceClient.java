package com.flipkart.fdp.bagder.http;

/**
 * Created by pranav.agarwal on 01/12/16.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;


import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;


/**
 * Generic RestService client to connect to a specific url address and make api calls
 *
 */
public class RestServiceClient {

  private static final Log LOGGER = LogFactory.getLog(RestServiceClient.class);

  static private volatile Client jerseyClient;

  static private int connectTimeout = 30000;
  static private int readTimeout = 180000;

  static private ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.configure( DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false );
  }

  private String serviceBaseUrl;

  private Level requestLogLevel = Level.FINE;
  private Level responseLogLevel = Level.FINE;

  public RestServiceClient( String serviceBaseUrl ) {
    this.serviceBaseUrl = serviceBaseUrl;
  }


  public void setRequestLogLevel( Level value ) { requestLogLevel = value; }
  public void setResponseLogLevel( Level value ) { responseLogLevel = value; }


  /**
   * Convert an object to a JSON String
   */
  public String toJSONString( Object object ) {
    StringWriter stringWriter = new StringWriter();
    try {
      objectMapper.writeValue( stringWriter, object );
    } catch ( Exception e ) {
      LOGGER.error("Error transforming to JSON:\n" + object, e );
    }
    return stringWriter.toString();
  }

  public <T>T getRESTGetResponse( String urlPath, Class<T> c ) throws IOException {
    return getRESTGetResponse( urlPath, null, null, c );
  }

  /**
   * Calls the REST Service and returns the result.
   */
  public <T>T getRESTGetResponse(
    String urlPath,
    Map<String,String> queryParams,
    Object requestEntity,
    Class<T> c ) throws IOException {
    long startTime = System.currentTimeMillis();
    try {
      // WebRequest
      WebResource webResource = getWebResource( urlPath );
      if ( queryParams != null ) {
        for ( Map.Entry<String,String>  entry : queryParams.entrySet() ) {
          webResource = webResource.queryParam( entry.getKey(), entry.getValue() );
        }
      }

      // WebResource.Builder
      WebResource.Builder wrBuilder = webResource.getRequestBuilder();
      if ( requestEntity != null ) {
        wrBuilder = wrBuilder.entity( requestEntity );
      }
      wrBuilder = wrBuilder.accept( MediaType.APPLICATION_JSON );

      ClientResponse response = wrBuilder.get( ClientResponse.class );

      return String.class.isAssignableFrom( c ) ?
        response.getEntity( c ) : processClientResponse( response, c );
    } finally {
      StringBuilder sb = new StringBuilder();
      sb.append( "REST GET Call Summary [Endpoint: " ).append( serviceBaseUrl ).append( "]" );
      sb.append( "[Service: " ).append( urlPath ).append( "]" );
      sb.append( "[Duration: " ).append( System.currentTimeMillis() - startTime ).append( " ms]" );

      LOGGER.info( sb.toString() );
    }
  }

  public <T>T getRESTPostResponse(
    String urlPath,
    Object requestEntity,
    Class<T> c ) throws IOException {
    return getRESTPostResponse( urlPath, null, requestEntity, c );
  }

  public <T>T getRESTPostResponse(
    String urlPath,
    Map<String,String> queryParams,
    Object requestEntity,
    Class<T> c ) throws IOException {


    long startTime = System.currentTimeMillis();
    T response = null;
    try {
      WebResource webResource = getWebResource( urlPath );
      if ( queryParams != null ) {
        for ( Map.Entry<String,String>  entry : queryParams.entrySet() ) {
          webResource = webResource.queryParam( entry.getKey(), entry.getValue() );
        }
      }
      ClientResponse clientResponse = webResource
        .accept( MediaType.APPLICATION_JSON )
        .type( MediaType.APPLICATION_JSON )
        .post( ClientResponse.class, requestEntity );
      response = processClientResponse( clientResponse, c );
      clientResponse.close();
      return response;
    } finally {
    }
  }

  private WebResource getWebResource( String urlPath ) {
    String url = serviceBaseUrl + urlPath;
    return getJerseyClient().resource( url );
  }

  private Client getJerseyClient() {
    if ( jerseyClient == null ) {
      synchronized ( RestServiceClient.class ) {
        if ( jerseyClient == null ) {
          LOGGER.info( "Creating Jersey Client." );
          Client client = Client.create();
          if ( connectTimeout >= 0 ) {
            LOGGER.info( "Initializing Connect Timeout to " + connectTimeout );
            client.setConnectTimeout( new Integer( connectTimeout ) );
          }
          if ( readTimeout >= 0 ) {
            LOGGER.info( "Initializing Read Timeout to " + readTimeout );
            client.setReadTimeout( new Integer( readTimeout ) );
          }
          jerseyClient = client;
        }
      }
    }

    Map<String,Object> clientProperties = jerseyClient.getProperties();

    if ( connectTimeout != getIntValue( clientProperties, ClientConfig.PROPERTY_CONNECT_TIMEOUT ) ||
      readTimeout != getIntValue( clientProperties, ClientConfig.PROPERTY_READ_TIMEOUT ) ) {
      LOGGER.info( "Setting Connect Timeout to " + connectTimeout );
      LOGGER.info( "Setting Read Timeout to " + readTimeout );
      synchronized ( RestServiceClient.class ) {
        // Revert to default value of null if the Site Parameter
        // setting is negative.
        jerseyClient.setConnectTimeout( connectTimeout < 0 ? null : new Integer( connectTimeout ) );
        jerseyClient.setReadTimeout( readTimeout < 0 ? null : new Integer( readTimeout ) );
      }
    }

    return jerseyClient;
  }

  private int getIntValue( Map<String, Object> map, String propertyName ) {
    Object obj = map.get( propertyName );
    if ( obj != null && obj instanceof Number ) {
      return ((Number)obj).intValue();
    }
    return -1;
  }

  private <T>T processClientResponse( ClientResponse response, Class<T> c ) throws IOException {
    if ( response.getStatus() != 200 ) {
      //ErrorResponse err = response.getEntity(ErrorResponse.class);
      //System.out.println(err);
      throw new IOException( "Failed : HTTP error code : " + response.getStatus() );
    }

      InputStream resultInputStream = response.getEntity( InputStream.class );
      try {
        return objectMapper.readValue( resultInputStream, c );
      } finally {
        if ( resultInputStream != null ) {
          try {
            resultInputStream.close();
          } catch ( Exception e ) {
            LOGGER.error("Error closing REST Service response InputStream", e );
          }
        }
      }
    }
  //}
}
