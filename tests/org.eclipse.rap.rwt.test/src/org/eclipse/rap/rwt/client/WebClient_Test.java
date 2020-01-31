/*******************************************************************************
 * Copyright (c) 2012, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.ClientFileLoader;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.client.service.ClientInfo;
import org.eclipse.rap.rwt.client.service.ClientService;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.internal.client.BrowserNavigationImpl;
import org.eclipse.rap.rwt.internal.client.ClientFileLoaderImpl;
import org.eclipse.rap.rwt.internal.client.ClientFileUploaderImpl;
import org.eclipse.rap.rwt.internal.client.ClientInfoImpl;
import org.eclipse.rap.rwt.internal.client.ClientMessages;
import org.eclipse.rap.rwt.internal.client.ConnectionMessages;
import org.eclipse.rap.rwt.internal.client.ConnectionMessagesImpl;
import org.eclipse.rap.rwt.internal.client.ExitConfirmationImpl;
import org.eclipse.rap.rwt.internal.client.JavaScriptExecutorImpl;
import org.eclipse.rap.rwt.internal.client.JavaScriptLoaderImpl;
import org.eclipse.rap.rwt.internal.client.StartupParametersImpl;
import org.eclipse.rap.rwt.internal.client.UrlLauncherImpl;
import org.eclipse.rap.rwt.internal.client.WebClientMessages;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.internal.resources.JavaScriptModuleLoader;
import org.eclipse.rap.rwt.internal.resources.JavaScriptModuleLoaderImpl;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class WebClient_Test {

  private WebClient client;

  @Before
  public void setUp() {
    Fixture.setUp();
    client = new WebClient();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testGetInvalidService() {
    assertNull( client.getService( UnsupportedService.class ) );
  }

  @Test
  public void testGetServiveTwice() {
    ClientService service1 = client.getService( JavaScriptExecutor.class );
    ClientService service2 = client.getService( JavaScriptExecutor.class );

    assertSame( service1, service2 );
  }

  @Test
  public void testGetClienInfoService() {
    ClientService service = client.getService( ClientInfo.class );
    assertTrue( service instanceof ClientInfoImpl );
  }

  @Test
  public void testGetUrlLauncherService() {
    ClientService service = client.getService( UrlLauncher.class );
    assertTrue( service instanceof UrlLauncherImpl );
  }

  @Test
  public void testRegistersClienInfoHandlerOnCreate() {
    try {
      ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
      connection.createServiceObject( "rwt.client.ClientInfo" );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }
  }

  @Test
  public void testGetJavaScriptExecutorService() {
    ClientService service = client.getService( JavaScriptExecutor.class );
    assertTrue( service instanceof JavaScriptExecutorImpl );
  }

  @Test
  public void testGetBrowserHistoryService() {
    ClientService service = client.getService( BrowserNavigation.class );
    assertTrue( service instanceof BrowserNavigationImpl );
  }

  @Test
  public void testGetExitConfirmationService() {
    ClientService service = client.getService( ExitConfirmation.class );
    assertTrue( service instanceof ExitConfirmationImpl );
  }

  @Test
  public void testGetConnectionMessagesService() {
    ClientService service = client.getService( ConnectionMessages.class );
    assertTrue( service instanceof ConnectionMessagesImpl );
  }

  @Test
  public void testGetJavaScriptLoaderService() {
    ClientService service = client.getService( JavaScriptLoader.class );
    assertTrue( service instanceof JavaScriptLoaderImpl );
  }

  @Test
  public void testGetJavaScriptModuleLoaderService() {
    ClientService service = client.getService( JavaScriptModuleLoader.class );
    assertTrue( service instanceof JavaScriptModuleLoaderImpl );
  }

  @Test
  public void testGetClientMessagesService() {
    ClientService service = client.getService( ClientMessages.class );
    assertTrue( service instanceof WebClientMessages );
  }

  @Test
  public void testGetClientFileLoaderService() {
    ClientService service = client.getService( ClientFileLoader.class );
    assertTrue( service instanceof ClientFileLoaderImpl );
  }

  @Test
  public void testGetClientFileUploaderService() {
    ClientService service = client.getService( ClientFileUploader.class );
    assertTrue( service instanceof ClientFileUploaderImpl );
  }

  @Test
  public void testGetStartupParametersService() {
    ClientService service = client.getService( StartupParameters.class );
    assertTrue( service instanceof StartupParametersImpl );
  }

  private static class UnsupportedService implements ClientService {
  }

}
