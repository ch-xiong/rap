/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.cluster.testfixture.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rap.rwt.cluster.testfixture.server.IServletEngine;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushServiceHandler;
import org.eclipse.rap.rwt.internal.service.ServiceManagerImpl;
import org.eclipse.swt.dnd.Transfer;


@SuppressWarnings("restriction")
public class RWTClient {
  // Keep "text" in sync with TextTransfer#TYPE_NAME
  private static final String TEXT_TRANSFER_DATA_TYPE
    = String.valueOf( Transfer.registerType( "text" ) );
  private static final String CONNECTION_ID = "cid";

  private IServletEngine servletEngine;
  private final IConnectionProvider connectionProvider;
  private final long startTime;
  private String sessionId;
  private int requestCounter;
  private String connectionId;

  public RWTClient( IServletEngine servletEngine ) {
    this( servletEngine, new DefaultConnectionProvider() );
  }

  RWTClient( IServletEngine servletEngine, IConnectionProvider connectionProvider ) {
    this.servletEngine = servletEngine;
    this.connectionProvider = connectionProvider;
    startTime = System.currentTimeMillis();
    sessionId = "";
    requestCounter = 0;
  }

  public void changeServletEngine( IServletEngine servletEngine ) {
    this.servletEngine = servletEngine;
  }

  public Object getServletEngine() {
    return servletEngine;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getConnectionId() {
    return connectionId;
  }

  public Response sendStartupRequest() throws IOException {
    return sendGetRequest( new HashMap<String, String>() );
  }

  public Response sendInitializationRequest() throws IOException {
    JsonMessage message = new JsonMessage();
    message.setRequestCounter( 0 );
    message.addOperation( "[\"set\",\"w1\",{\"bounds\":[0,0,800,600],\"dpi\":[96,96],\"colorDepth\":32}]" );
    message.addOperation( "[\"set\",\"w1\",{\"cursorLocation\":[0,0]}]" );
    return sendPostRequest( message );
  }

  public Response sendDisplayResizeRequest( int width, int height ) throws IOException {
    JsonMessage message = new JsonMessage();
    message.addOperation( "[\"set\",\"w1\",{\"bounds\":[0,0," + width + "," + height + "]}]" );
    return sendPostRequest( message );
  }

  public Response sendWidgetSelectedRequest( String widgetId ) throws IOException {
    JsonMessage message = new JsonMessage();
    message.addOperation( "[\"notify\",\"" + widgetId + "\",\"Selection\",{}]" );
    return sendPostRequest( message );
  }

  public Response sendShellCloseRequest( String shellId ) throws IOException {
    JsonMessage message = new JsonMessage();
    message.addOperation( "[\"notify\",\"" + shellId + "\",\"Close\",{}]" );
    return sendPostRequest( message );
  }

  public Response sendDragStartRequest( String dragSourceId ) throws IOException {
    JsonMessage message = new JsonMessage();
    message.addOperation( "[\"notify\",\""
                          + dragSourceId
                          + "\",\"DragStart\",{\"x\":100,\"y\":100,\"time\":"
                          + createTimeParam()
                          + "}]" );
    return sendPostRequest( message );
  }

  public Response sendDragFinishedRequest( String dragSourceId,
                                           String dropTargetId,
                                           String sourceControlId )
    throws IOException
  {
    JsonMessage message = new JsonMessage();
    message.addOperation( "[\"notify\",\""
                          + dragSourceId
                          + "\",\"DragEnd\",{\"x\":100,\"y\":100,\"time\":"
                          + createTimeParam()
                          + "}]" );
    message.addOperation( "[\"notify\",\""
                          + dropTargetId
                          + "\",\"DropAccept\",{\"x\":100,\"y\":100,\"item\":null,"
                          + "\"operation\":\"move\",\"feedback\":0,\"dataType\":"
                          + TEXT_TRANSFER_DATA_TYPE
                          + ",\"source\":\""
                          + sourceControlId
                          + "\",\"time\":"
                          + createTimeParam()
                          + "}]" );
    return sendPostRequest( message );
  }

  public Response sendResourceRequest( String resourceLocation ) throws IOException {
    URL url = createUrl( resourceLocation );
    HttpURLConnection connection = createGetConnection( url, 0 );
    return new Response( connection );
  }

  public Response sendServerPushRequest( int timeout ) throws IOException {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put( ServiceManagerImpl.REQUEST_PARAM, ServerPushServiceHandler.HANDLER_ID );
    parameters.put( CONNECTION_ID, connectionId );
    URL url = createUrl( IServletEngine.SERVLET_NAME, parameters );
    HttpURLConnection connection = createGetConnection( url, timeout );
    return new Response( connection );
  }

  Response sendPostRequest( JsonMessage message ) throws IOException {
    if( requestCounter >= 0 ) {
      message.setRequestCounter( requestCounter );
    }
    HashMap<String, String> parameters = new HashMap<String, String>();
    if( connectionId != null ) {
      parameters.put( CONNECTION_ID, connectionId );
    }
    URL url = createUrl( IServletEngine.SERVLET_NAME, parameters );
    HttpURLConnection connection = createPostConnection( url, message.toString(), 0 );
    parseSessionId( connection );
    requestCounter++;
    Response response = new Response( connection );
    if( connectionId == null ) {
      connectionId = parseConnectionId( response.getContentText() );
    }
    return response;
  }

  Response sendGetRequest( Map<String, String> parameters ) throws IOException {
    URL url = createUrl( IServletEngine.SERVLET_NAME, parameters );
    HttpURLConnection connection = createGetConnection( url, 0 );
    parseSessionId( connection );
    return new Response( connection );
  }

  private void addSessionCookie( HttpURLConnection connection ) {
    if( sessionId != null && sessionId.length() > 0 ) {
      connection.setRequestProperty( "Cookie", "JSESSIONID=" + sessionId );
    }
  }

  private URL createUrl( String path ) {
    return createUrl( path, new HashMap<String, String>() );
  }

  private URL createUrl( String path, Map<String, String> parameters ) {
    int port = servletEngine.getPort();
    HttpUrlBuilder urlBuilder = new HttpUrlBuilder( "localhost", port, path );
    urlBuilder.addParameters( parameters );
    return urlBuilder.toUrl();
  }

  private HttpURLConnection createGetConnection( URL url, int timeout )
    throws IOException
  {
    HttpURLConnection connection = ( HttpURLConnection )connectionProvider.createConnection( url );
    connection.setInstanceFollowRedirects( false );
    connection.setAllowUserInteraction( false );
    connection.setRequestMethod( "GET" );
    connection.setConnectTimeout( timeout );
    connection.setReadTimeout( timeout );
    addSessionCookie( connection );
    connection.connect();
    return connection;
  }

  private HttpURLConnection createPostConnection( URL url, String content, int timeout )
    throws IOException
  {
    byte[] bytes = content.getBytes();
    HttpURLConnection connection = ( HttpURLConnection )connectionProvider.createConnection( url );
    connection.setRequestMethod( "POST" );
    connection.setRequestProperty( "Content-Type", "application/json" );
    connection.setRequestProperty( "Content-Length", Integer.toString( bytes.length ) );
    connection.setUseCaches( false );
    connection.setDoInput( true );
    connection.setDoOutput( true );
    connection.setConnectTimeout( timeout );
    connection.setReadTimeout( timeout );
    addSessionCookie( connection );
    OutputStream outputStream = connection.getOutputStream();
    outputStream.write( content.getBytes() );
    outputStream.flush();
    outputStream.close();
    return connection;
  }

  private String createTimeParam() {
    return String.valueOf( System.currentTimeMillis() - startTime );
  }

  private void parseSessionId( HttpURLConnection connection ) {
    String cookieField = connection.getHeaderField( "Set-Cookie" );
    if( cookieField != null ) {
      String[] parts = cookieField.split( ";" );
      sessionId = parts[ 0 ].split( "=" )[ 1 ];
    }
  }

  static String parseConnectionId( String content ) {
    Pattern pattern = Pattern.compile( "\"cid\"\\s*:\\s*\"(\\w+)\"" );
    Matcher matcher = pattern.matcher( content );
    if( matcher.find() ) {
      return matcher.group(1);
    }
    return null;
  }

  private static class DefaultConnectionProvider implements IConnectionProvider {
    public URLConnection createConnection( URL url ) throws IOException {
      return url.openConnection();
    }
  }

}
