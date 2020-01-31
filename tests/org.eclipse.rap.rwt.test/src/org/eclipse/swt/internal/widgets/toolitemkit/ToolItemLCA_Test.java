/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.toolitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.registerDataKeys;
import static org.eclipse.rap.rwt.testfixture.internal.Fixture.getProtocolMessage;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getParent;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getStyles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.rap.rwt.testfixture.internal.TestMessage;
import org.eclipse.rap.rwt.testfixture.internal.TestUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ToolItemLCA_Test {

  private Display display;
  private Shell shell;
  private ToolBar toolbar;
  private ToolItem toolitem;
  private ToolItemLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    toolbar = new ToolBar( shell, SWT.NONE );
    toolitem = new ToolItem( toolbar, SWT.PUSH );
    lca = ToolItemLCA.INSTANCE;
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testGetImage() throws IOException {
    ToolItem item = new ToolItem( toolbar, SWT.CHECK );

    Image enabledImage = TestUtil.createImage( display, Fixture.IMAGE1 );
    Image disabledImage = TestUtil.createImage( display, Fixture.IMAGE2 );
    assertNull( ToolItemLCA.getImage( item ) );

    item.setImage( enabledImage );
    assertSame( enabledImage, ToolItemLCA.getImage( item ) );

    item.setImage( enabledImage );
    item.setDisabledImage( disabledImage );
    assertSame( enabledImage, ToolItemLCA.getImage( item ) );

    item.setEnabled( false );
    assertSame( disabledImage, ToolItemLCA.getImage( item ) );

    item.setDisabledImage( null );
    assertSame( enabledImage, ToolItemLCA.getImage( item ) );
  }

  @Test
  public void testRenderCreatePush() throws IOException {
    lca.renderInitialization( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertEquals( "rwt.widgets.ToolItem", operation.getType() );
    assertEquals( 0, operation.getProperties().get( "index" ).asInt() );
    assertTrue( getStyles( operation ).contains( "PUSH" ) );
  }

  @Test
  public void testRenderCreateCheck() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.CHECK );

    lca.renderInitialization( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertEquals( "rwt.widgets.ToolItem", operation.getType() );
    assertEquals( 1, operation.getProperties().get( "index" ).asInt() );
    assertTrue( getStyles( operation ).contains( "CHECK" ) );
  }

  @Test
  public void testRenderCreateRadio() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.RADIO );

    lca.renderInitialization( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertEquals( "rwt.widgets.ToolItem", operation.getType() );
    assertEquals( 1, operation.getProperties().get( "index" ).asInt() );
    assertTrue( getStyles( operation ).contains( "RADIO" ) );
  }

  @Test
  public void testRenderCreateDropDown() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.DROP_DOWN );

    lca.renderInitialization( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertEquals( "rwt.widgets.ToolItem", operation.getType() );
    assertEquals( 1, operation.getProperties().get( "index" ).asInt() );
    assertTrue( getStyles( operation ).contains( "DROP_DOWN" ) );
  }

  @Test
  public void testRenderCreateSeparator() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.SEPARATOR );

    lca.renderInitialization( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertEquals( "rwt.widgets.ToolItem", operation.getType() );
    assertEquals( 1, operation.getProperties().get( "index" ).asInt() );
    assertTrue( getStyles( operation ).contains( "SEPARATOR" ) );
  }

  @Test
  public void testRenderInitialization_setsOperationHandler() throws IOException {
    String id = getId( toolitem );
    lca.renderInitialization( toolitem );

    OperationHandler handler = RemoteObjectRegistry.getInstance().get( id ).getHandler();
    assertTrue( handler instanceof ToolItemOperationHandler );
  }

  @Test
  public void testRenderParent() throws IOException {
    lca.renderInitialization( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertEquals( getId( toolitem.getParent() ), getParent( operation ) );
  }

  @Test
  public void testRenderInitialEnabled() throws IOException {
    lca.render( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertFalse( operation.getProperties().names().contains( "enabled" ) );
  }

  @Test
  public void testRenderEnabled() throws IOException {
    toolitem.setEnabled( false );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( toolitem, "enabled" ) );
  }

  @Test
  public void testRenderEnabledUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setEnabled( false );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "enabled" ) );
  }

  @Test
  public void testRenderIntialToolTipMarkupEnabled() throws IOException {
    toolitem.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );

    lca.renderChanges( toolitem );

    TestMessage message = getProtocolMessage();
    assertTrue( "foo", message.findSetProperty( toolitem, "toolTipMarkupEnabled" ).asBoolean() );
  }

  @Test
  public void testRenderToolTipMarkupEnabled() throws IOException {
    toolitem.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
    Fixture.markInitialized( toolitem );

    lca.renderChanges( toolitem );

    TestMessage message = getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "toolTipMarkupEnabled" ) );
  }

  @Test
  public void testRenderInitialToolTip() throws IOException {
    lca.render( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertFalse( operation.getProperties().names().contains( "toolTip" ) );
  }

  @Test
  public void testRenderToolTip() throws IOException {
    toolitem.setToolTipText( "foo" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( toolitem, "toolTip" ).asString() );
  }

  @Test
  public void testRenderToolTipUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setToolTipText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "toolTip" ) );
  }

  @Test
  public void testRenderCustomVariant() throws IOException {
    toolitem.setData( RWT.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( toolitem, "customVariant" ).asString() );
  }

  @Test
  public void testRenderInitialVisible() throws IOException {
    toolbar.setSize( 20, 25 );

    lca.render( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertFalse( operation.getProperties().names().contains( "visible" ) );
  }

  @Test
  public void testRenderVisible() throws IOException {
    toolbar.setSize( 20, 25 );

    toolitem.setText( "foo bar" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( toolitem, "visible" ) );
  }

  @Test
  public void testRenderVisibleUnchanged() throws IOException {
    toolbar.setSize( 20, 25 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setText( "foo bar" );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "visible" ) );
  }

  @Test
  public void testRenderInitialText() throws IOException {
    lca.render( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertFalse( operation.getProperties().names().contains( "text" ) );
  }

  @Test
  public void testRenderText() throws IOException {
    toolitem.setText( "foo" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( toolitem, "text" ).asString() );
  }

  @Test
  public void testRenderText_WithMnemonic() throws IOException {
    toolitem.setText( "fo&o" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( toolitem, "text" ).asString() );
  }

  @Test
  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "text" ) );
  }

  @Test
  public void testRenderInitialImage() throws IOException {
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "image" ) );
  }

  @Test
  public void testRenderImage() throws IOException {
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );

    toolitem.setImage( image );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    JsonArray expected = new JsonArray().add( imageLocation ).add( 100 ).add( 50 );
    assertEquals( expected, message.findSetProperty( toolitem, "image" ) );
  }

  @Test
  public void testRenderImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );

    toolitem.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "image" ) );
  }

  @Test
  public void testRenderInitialHotImage() throws IOException {
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "hotImage" ) );
  }

  @Test
  public void testRenderHotImage() throws IOException {
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );

    toolitem.setHotImage( image );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    JsonArray expected = new JsonArray().add( imageLocation ).add( 100 ).add( 50 );
    assertEquals( expected, message.findSetProperty( toolitem, "hotImage" ) );
  }

  @Test
  public void testRenderHotImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );

    toolitem.setHotImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "hotImage" ) );
  }

  @Test
  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );
    toolitem.setImage( image );

    Fixture.preserveWidgets();
    toolitem.setImage( null );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( toolitem, "image" ) );
  }

  @Test
  public void testRenderInitialControl() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.SEPARATOR );

    lca.render( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertFalse( operation.getProperties().names().contains( "control" ) );
  }

  @Test
  public void testRenderControl() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.SEPARATOR );
    Composite control = new Composite( toolbar, SWT.NONE );
    String controlId = WidgetUtil.getId( control );

    toolitem.setControl( control );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( controlId, message.findSetProperty( toolitem, "control" ).asString() );
  }

  @Test
  public void testRenderControlUnchanged() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.SEPARATOR );
    Composite control = new Composite( toolbar, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setControl( control );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "control" ) );
  }

  @Test
  public void testRenderInitialSelection() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.CHECK );

    lca.render( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( toolitem );
    assertFalse( operation.getProperties().names().contains( "selection" ) );
  }

  @Test
  public void testRenderSelection() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.CHECK );

    toolitem.setSelection( true );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( toolitem, "selection" ) );
  }

  @Test
  public void testRenderSelectionUnchanged() throws IOException {
    toolitem = new ToolItem( toolbar, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setSelection( true );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "selection" ) );
  }

  @Test
  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Fixture.preserveWidgets();

    toolitem.addListener( SWT.Selection, mock( Listener.class ) );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( toolitem, "Selection" ) );
  }

  @Test
  public void testRenderRemoveSelectionListener() throws Exception {
    Listener listener = mock( Listener.class );
    toolitem.addListener( SWT.Selection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Fixture.preserveWidgets();

    toolitem.removeListener( SWT.Selection, listener );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( toolitem, "Selection" ) );
  }

  @Test
  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Fixture.preserveWidgets();

    toolitem.addSelectionListener( mock( SelectionListener.class ) );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( toolitem, "Selection" ) );
  }

  @Test
  public void testRenderSelectionListener_onSeparator() throws Exception {
    toolitem = new ToolItem( toolbar, SWT.SEPARATOR );
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );
    Fixture.preserveWidgets();

    toolitem.addListener( SWT.Selection, mock( Listener.class ) );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( toolitem, "Selection" ) );
  }

  @Test
  public void testRenderInitialMnemonicIndex() throws IOException {
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "mnemonicIndex" ) );
  }

  @Test
  public void testRenderMnemonicIndex() throws IOException {
    toolitem.setText( "te&st" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 2, message.findSetProperty( toolitem, "mnemonicIndex" ).asInt() );
  }

  @Test
  public void testRenderMnemonic_OnTextChange() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setText( "te&st" );
    Fixture.preserveWidgets();
    toolitem.setText( "aa&bb" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 2, message.findSetProperty( toolitem, "mnemonicIndex" ).asInt() );
  }

  @Test
  public void testRenderMnemonicIndexUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setText( "te&st" );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "mnemonicIndex" ) );
  }

  @Test
  public void testRenderData() throws IOException {
    registerDataKeys( new String[]{ "foo", "bar" } );
    toolitem.setData( "foo", "string" );
    toolitem.setData( "bar", Integer.valueOf( 1 ) );

    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    JsonObject data = ( JsonObject )message.findSetProperty( toolitem, "data" );
    assertEquals( "string", data.get( "foo" ).asString() );
    assertEquals( 1, data.get( "bar" ).asInt() );
  }

  @Test
  public void testRenderDataUnchanged() throws IOException {
    registerDataKeys( new String[]{ "foo" } );
    toolitem.setData( "foo", "string" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderInitialBadge() throws IOException {
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "badge" ) );
  }

  @Test
  public void testRenderBadge() throws IOException {
    toolitem.setData( RWT.BADGE, "11" );
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "11", message.findSetProperty( toolitem, "badge" ).asString() );
  }

  @Test
  public void testRendeBadgeUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( toolitem );

    toolitem.setData( RWT.BADGE, "11" );
    Fixture.preserveWidgets();
    lca.renderChanges( toolitem );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( toolitem, "badge" ) );
  }

}
