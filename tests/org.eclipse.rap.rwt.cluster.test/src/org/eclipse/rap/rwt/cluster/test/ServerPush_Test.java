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
package org.eclipse.rap.rwt.cluster.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.cluster.test.entrypoints.ServerPushEntryPoint;
import org.eclipse.rap.rwt.cluster.test.entrypoints.SessionTimeoutEntryPoint;
import org.eclipse.rap.rwt.cluster.testfixture.ClusterTestHelper;
import org.eclipse.rap.rwt.cluster.testfixture.client.RWTClient;
import org.eclipse.rap.rwt.cluster.testfixture.client.Response;
import org.eclipse.rap.rwt.cluster.testfixture.server.IServletEngine;
import org.eclipse.rap.rwt.cluster.testfixture.server.IServletEngineFactory;
import org.eclipse.rap.rwt.cluster.testfixture.server.JettyFactory;
import org.eclipse.rap.rwt.cluster.testfixture.server.TomcatFactory;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@SuppressWarnings("restriction")
@RunWith( Parameterized.class )
public class ServerPush_Test {

  private final IServletEngineFactory servletEngineFactory;
  private IServletEngine servletEngine;
  private RWTClient client;

  @Parameters
  public static Collection<Object[]> getParameters() {
    return Arrays.asList( new Object[][] { { new JettyFactory() }, { new TomcatFactory() } } );
  }

  public ServerPush_Test( IServletEngineFactory servletEngineFactory ) {
    this.servletEngineFactory = servletEngineFactory;
  }

  @Before
  public void setUp() throws Exception {
    servletEngine = servletEngineFactory.createServletEngine();
    client = new RWTClient( servletEngine );
  }

  @After
  public void tearDown() throws Exception {
    servletEngine.stop();
  }

  @Test
  public void testServerPushRequestResponse() throws Exception {
    servletEngine.start( ServerPushEntryPoint.class );
    client.sendStartupRequest();
    client.sendInitializationRequest();
    HttpSession session = ClusterTestHelper.getFirstHttpSession( servletEngine );
    final Display display = ClusterTestHelper.getSessionDisplay( session, client.getConnectionId() );

    Thread thread = new Thread( new Runnable() {
      public void run() {
        sleep( 2000 );
        RWT.getUISession( display ).exec( new Runnable() {
          public void run() {
            ServerPushManager pushManager = ServerPushManager.getInstance();
            pushManager.setRequestCheckInterval( 500 );
            pushManager.setHasRunnables( true );
            pushManager.releaseBlockedRequest();
          }
        } );
      }
    } );
    thread.setDaemon( true );
    thread.start();

    Response response = client.sendServerPushRequest( 0 );
    thread.join();

    String contentText = response.getContentText();
    assertEquals( "", contentText.trim() );
  }

  @Test
  public void testAbortConnectionDuringServerPushRequest() throws Exception {
    servletEngine.start( ServerPushEntryPoint.class );
    client.sendStartupRequest();
    client.sendInitializationRequest();
    configureServerPushRequestCheckInterval( 400 );

    try {
      client.sendServerPushRequest( 200 );
      fail();
    } catch( IOException expected ) {
      assertEquals( "Read timed out", expected.getMessage() );
    }

    Thread.sleep( 800 );

    ServerPushManager pushManager = getServerPushManager();
    assertFalse( pushManager.isCallBackRequestBlocked() );
  }

  @Test
  public void testServerPushRequestDoesNotKeepSessionAlive() throws Exception {
    servletEngine.start( SessionTimeoutEntryPoint.class );
    client.sendStartupRequest();
    client.sendInitializationRequest();
    getServerPushManager().setRequestCheckInterval( 100 );

    asyncSendServerPushRequest();
    
    final long startToWait = System.currentTimeMillis();
    final short checkInterval = 100; 
    final short maxWaitPeriod = 30000;
    boolean invalidated = SessionTimeoutEntryPoint.isSessionInvalidated(); 
    // expect the session to be invalidated within 30 Seconds
    while( System.currentTimeMillis() - startToWait < maxWaitPeriod && !invalidated ) {
      Thread.sleep( checkInterval );
      invalidated = SessionTimeoutEntryPoint.isSessionInvalidated();
    } 


    assertTrue( "The session was not invalidated after " + maxWaitPeriod +" ms", invalidated );
  }

  @Test
  public void testServerPushRequestDoesNotPreventEngineShutdown() throws Exception {
    servletEngine.start( SessionTimeoutEntryPoint.class );
    client.sendStartupRequest();
    client.sendInitializationRequest();
    ServerPushManager pushManager = getServerPushManager();
    asyncSendServerPushRequest();
    while( !pushManager.isCallBackRequestBlocked() ) {
      Thread.yield();
    }

    servletEngine.stop( 2000 );

    assertTrue( SessionTimeoutEntryPoint.isSessionInvalidated() );
  }

  private ServerPushManager getServerPushManager() {
    final ServerPushManager[] result = { null };
    HttpSession session = ClusterTestHelper.getFirstHttpSession( servletEngine );
    Display display = ClusterTestHelper.getSessionDisplay( session, client.getConnectionId() );
    RWT.getUISession( display ).exec( new Runnable() {
      public void run() {
        result[ 0 ] = ServerPushManager.getInstance();
      }
    } );
    return result[ 0 ];
  }

  private void sleep( int duration ) {
    try {
      Thread.sleep( duration );
    } catch( InterruptedException ie ) {
      throw new RuntimeException( ie );
    }
  }

  private void asyncSendServerPushRequest() {
    Thread thread = new Thread( new Runnable() {
      public void run() {
        try {
          client.sendServerPushRequest( 0 );
        } catch( IOException ignore ) {
        }
      }
    } );
    thread.setDaemon( true );
    thread.start();
  }

  private void configureServerPushRequestCheckInterval( final int interval ) {
    HttpSession session = ClusterTestHelper.getFirstHttpSession( servletEngine );
    Display display = ClusterTestHelper.getSessionDisplay( session, client.getConnectionId() );
    RWT.getUISession( display ).exec( new Runnable() {
      public void run() {
        ServerPushManager.getInstance().setRequestCheckInterval( interval );
      }
    } );
  }

}
