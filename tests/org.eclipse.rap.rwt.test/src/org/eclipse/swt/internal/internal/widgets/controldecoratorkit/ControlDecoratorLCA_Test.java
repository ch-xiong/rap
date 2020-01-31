/*******************************************************************************
 * Copyright (c) 2009, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.internal.widgets.controldecoratorkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getParent;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getStyles;
import static org.eclipse.rap.rwt.testfixture.internal.TestUtil.createImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.DestroyOperation;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.rap.rwt.testfixture.internal.TestMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.internal.widgets.ControlDecorator;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ControlDecoratorLCA_Test {

  private Display display;
  private Shell shell;
  private Button control;
  private ControlDecorator decorator;
  private ControlDecoratorLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    control = new Button( shell, SWT.PUSH );
    decorator = new ControlDecorator( control, SWT.NONE, null );
    lca = ControlDecoratorLCA.INSTANCE;
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testRenderCreate() throws IOException {
    lca.renderInitialization( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    assertEquals( "rwt.widgets.ControlDecorator", operation.getType() );
    List<String> styles = getStyles( operation );
    assertTrue( styles.contains( "LEFT" ) );
    assertTrue( styles.contains( "CENTER" ) );
  }

  @Test
  public void testRenderCreateWithRightAndBottom() throws IOException {
    decorator = new ControlDecorator( control, SWT.RIGHT | SWT.BOTTOM, null );

    lca.renderInitialization( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    List<String> styles = getStyles( operation );
    assertTrue( styles.contains( "RIGHT" ) );
    assertTrue( styles.contains( "BOTTOM" ) );
  }

  @Test
  public void testRenderCreateWithLeftAndTop() throws IOException {
    decorator = new ControlDecorator( control, SWT.LEFT | SWT.TOP, null );

    lca.renderInitialization( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    List<String> styles = getStyles( operation );
    assertTrue( styles.contains( "LEFT" ) );
    assertTrue( styles.contains( "TOP" ) );
  }

  @Test
  public void testRenderCreate_setsOperationHandler() throws IOException {
    String id = getId( decorator );

    lca.renderInitialization( decorator );

    OperationHandler handler = RemoteObjectRegistry.getInstance().get( id ).getHandler();
    assertTrue( handler instanceof ControlDecoratorOperationHandler );
  }

  @Test
  public void testRenderParent() throws IOException {
    lca.renderInitialization( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    assertEquals( getId( decorator.getParent() ), getParent( operation ) );
  }

  @Test
  public void testRenderCreateWithMarkupEnabled() throws IOException {
    decorator.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );

    lca.renderInitialization( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    assertEquals( JsonValue.TRUE, operation.getProperties().get( "markupEnabled" ) );
  }

  @Test
  public void testRenderDispose() throws IOException {
    lca.renderDispose( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( getId( decorator ), operation.getTarget() );
  }

  @Test
  public void testRenderDispose_withDisposedControl() throws IOException {
    control.dispose();

    lca.renderDispose( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( getId( decorator ), operation.getTarget() );
  }

  @Test
  public void testRenderInitialBounds() throws IOException {
    lca.render( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray bounds = ( JsonArray )message.findCreateProperty( decorator, "bounds" );
    assertEquals( 0, bounds.get( 2 ).asInt() );
    assertEquals( 0, bounds.get( 3 ).asInt() );
  }

  @Test
  public void testRenderBounds() throws IOException {
    decorator.setImage( createImage( display, Fixture.IMAGE_100x50 ) );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray bounds = ( JsonArray )message.findSetProperty( decorator, "bounds" );
    assertTrue( bounds.get( 2 ).asInt() > 0 );
    assertTrue( bounds.get( 3 ).asInt() > 0 );
  }

  @Test
  public void testRenderBoundsUnchanged() throws IOException {
    decorator.setImage( createImage( display, Fixture.IMAGE_100x50 ) );
    Fixture.markInitialized( display );
    Fixture.markInitialized( decorator );

    Fixture.preserveWidgets();
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( decorator, "bounds" ) );
  }

  @Test
  public void testRenderInitialText() throws IOException {
    lca.render( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    assertTrue( operation.getProperties().names().indexOf( "text" ) == -1 );
  }

  @Test
  public void testRenderText() throws IOException {
    decorator.setText( "foo" );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( decorator, "text" ).asString() );
  }

  @Test
  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( decorator );

    decorator.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( decorator, "text" ) );
  }

  @Test
  public void testRenderInitialImage() throws IOException {
    lca.render( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( decorator, "image" ) );
  }

  @Test
  public void testRenderImage() throws IOException {
    Image image = createImage( display, Fixture.IMAGE_100x50 );

    decorator.setImage( image );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    JsonArray expected = JsonArray.readFrom( "[\"" + imageLocation + "\", 100, 50 ]" );
    assertEquals( expected, message.findSetProperty( decorator, "image" ) );
  }

  @Test
  public void testRenderImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( decorator );
    Image image = createImage( display, Fixture.IMAGE_100x50 );

    decorator.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( decorator, "image" ) );
  }

  @Test
  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( decorator );
    Image image = createImage( display, Fixture.IMAGE_100x50 );
    decorator.setImage( image );

    Fixture.preserveWidgets();
    decorator.setImage( null );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( decorator, "image" ) );
  }

  @Test
  public void testRenderInitialVisible() throws IOException {
    lca.render( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    assertFalse( operation.getProperties().names().contains( "visible" ) );
  }

  @Test
  public void testRenderVisible() throws IOException {
    shell.open();
    decorator.setImage( createImage( display, Fixture.IMAGE_100x50 ) );

    decorator.show();
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    decorator.isVisible();
    assertEquals( JsonValue.TRUE, message.findSetProperty( decorator, "visible" ) );
  }

  @Test
  public void testRenderVisibleUnchanged() throws IOException {
    shell.open();
    Fixture.markInitialized( display );
    Fixture.markInitialized( decorator );
    decorator.setImage( createImage( display, Fixture.IMAGE_100x50 ) );

    decorator.show();
    Fixture.preserveWidgets();
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( decorator, "visible" ) );
  }

  @Test
  public void testRenderInitialShowHover() throws IOException {
    lca.render( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( decorator );
    assertFalse( operation.getProperties().names().contains( "showHover" ) );
  }

  @Test
  public void testRenderShowHover() throws IOException {
    decorator.setShowHover( false );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( decorator, "showHover" ) );
  }

  @Test
  public void testRenderShowHoverUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( decorator );

    decorator.setShowHover( false );
    Fixture.preserveWidgets();
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( decorator, "showHover" ) );
  }

  @Test
  public void testRenderListen_Selection() throws Exception {
    Fixture.markInitialized( decorator );
    Fixture.clearPreserved();

    decorator.addListener( SWT.Selection, mock( Listener.class ) );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( decorator, "Selection" ) );
  }

  @Test
  public void testRenderListen_DefaultSelection() throws Exception {
    Fixture.markInitialized( decorator );
    Fixture.clearPreserved();

    decorator.addListener( SWT.DefaultSelection, mock( Listener.class ) );
    lca.renderChanges( decorator );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( decorator, "DefaultSelection" ) );
  }

}
