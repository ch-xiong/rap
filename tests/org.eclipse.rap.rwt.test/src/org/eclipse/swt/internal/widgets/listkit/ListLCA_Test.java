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
package org.eclipse.swt.internal.widgets.listkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getParent;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getStyles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.rap.rwt.testfixture.internal.TestMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.internal.widgets.IListAdapter;
import org.eclipse.swt.internal.widgets.controlkit.ControlLCATestUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListLCA_Test {

  private Display display;
  private Shell shell;
  private ListLCA lca;
  private List list;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display, SWT.NONE );
    list = new List( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    lca = ListLCA.INSTANCE;
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testCommonControlProperties() throws IOException {
    ControlLCATestUtil.testCommonControlProperties( list );
  }

  @Test
  public void testRenderCreate() throws IOException {
    lca.renderInitialization( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertEquals( "rwt.widgets.List", operation.getType() );
    assertTrue( getStyles( operation ).contains( "SINGLE" ) );
  }

  @Test
  public void testRenderCreate_setsOperationHandler() throws IOException {
    String id = getId( list );

    lca.renderInitialization( list );

    OperationHandler handler = RemoteObjectRegistry.getInstance().get( id ).getHandler();
    assertTrue( handler instanceof ListOperationHandler );
  }

  @Test
  public void testRenderCreateWithMulti() throws IOException {
    List list = new List( shell, SWT.MULTI );

    lca.renderInitialization( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertTrue( getStyles( operation ).contains( "MULTI" ) );
  }

  @Test
  public void testRenderParent() throws IOException {
    lca.renderInitialization( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertEquals( getId( list.getParent() ), getParent( operation ) );
  }

  @Test
  public void testRenderInitialItems() throws IOException {
    lca.render( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertFalse( operation.getProperties().names().contains( "items" ) );
  }

  @Test
  public void testRenderItems() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray().add( "Item 1" ).add( "Item 2" ).add( "Item 3" );
    assertEquals( expected, message.findSetProperty( list, "items" ) );
  }

  @Test
  public void testRenderItemsUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );

    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    Fixture.preserveWidgets();
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( list, "items" ) );
  }

  @Test
  public void testRenderInitialSelectionIndices() throws IOException {
    lca.render( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertFalse( operation.getProperties().names().contains( "selectionIndices" ) );
  }

  @Test
  public void testRenderSelectionIndices() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );

    list.select( 1 );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray().add( 1 );
    assertEquals( expected, message.findSetProperty( list, "selectionIndices" ) );
  }

  @Test
  public void testRenderSelectionIndicesWithMulti() throws IOException {
    List list = new List( shell, SWT.MULTI );
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );

    list.setSelection( new int[] { 1, 2 } );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray exected = new JsonArray().add( 1 ).add( 2 );
    assertEquals( exected, message.findSetProperty( list, "selectionIndices" ) );
  }

  @Test
  public void testRenderSelectionIndicesUnchanged() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );

    list.select( 1 );
    Fixture.preserveWidgets();
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( list, "selectionIndices" ) );
  }

  @Test
  public void testRenderInitialTopIndex() throws IOException {
    lca.render( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertFalse( operation.getProperties().names().contains( "topIndex" ) );
  }

  @Test
  public void testRenderTopIndex() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );

    list.setTopIndex( 2 );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 2, message.findSetProperty( list, "topIndex" ).asInt() );
  }

  @Test
  public void testRenderTopIndexUnchanged() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );

    list.setTopIndex( 2 );
    Fixture.preserveWidgets();
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( list, "topIndex" ) );
  }

  @Test
  public void testRenderInitialFocusIndex() throws IOException {
    lca.render( list );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( list );
    assertFalse( operation.getProperties().names().contains( "focusIndex" ) );
  }

  @Test
  public void testRenderFocusIndex() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );

    setFocusIndex( list, 2 );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 2, message.findSetProperty( list, "focusIndex" ).asInt() );
  }

  @Test
  public void testRenderFocusIndexUnchanged() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );

    setFocusIndex( list, 2 );
    Fixture.preserveWidgets();
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( list, "focusIndex" ) );
  }

  @Test
  public void testRenderInitialItemDimensions() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );

    lca.render( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNotNull( message.findCreateOperation( list ).getProperties().get( "itemDimensions" ) );
  }

  @Test
  public void testRenderItemDimensions() throws IOException {
    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray actual = ( JsonArray )message.findSetProperty( list, "itemDimensions" );
    assertEquals( list.getItemHeight(), actual.get( 1 ).asInt() );
  }

  @Test
  public void testRenderItemDimensionsUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );

    list.setItems( new String[] { "Item 1", "Item 2", "Item 3" } );
    Fixture.preserveWidgets();
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( list, "itemDimensions" ) );
  }

  @Test
  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );
    Fixture.preserveWidgets();

    list.addListener( SWT.Selection, mock( Listener.class ) );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( list, "Selection" ) );
    assertNull( message.findListenOperation( list, "DefaultSelection" ) );
  }

  @Test
  public void testRenderRemoveSelectionListener() throws Exception {
    Listener listener = mock( Listener.class );
    list.addListener( SWT.Selection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );
    Fixture.preserveWidgets();

    list.removeListener( SWT.Selection, listener );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( list, "Selection" ) );
    assertNull( message.findListenOperation( list, "DefaultSelection" ) );
  }

  @Test
  public void testRenderAddDefaultSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );
    Fixture.preserveWidgets();

    list.addListener( SWT.DefaultSelection, mock( Listener.class ) );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( list, "DefaultSelection" ) );
    assertNull( message.findListenOperation( list, "Selection" ) );
  }

  @Test
  public void testRenderRemoveDefaultSelectionListener() throws Exception {
    Listener listener = mock( Listener.class );
    list.addListener( SWT.DefaultSelection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );
    Fixture.preserveWidgets();

    list.removeListener( SWT.DefaultSelection, listener );
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( list, "DefaultSelection" ) );
    assertNull( message.findListenOperation( list, "Selection" ) );
  }

  @Test
  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( list );
    Fixture.preserveWidgets();

    list.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( list, "Selection" ) );
    assertNull( message.findListenOperation( list, "DefaultSelection" ) );
  }

  @Test
  public void testRenderMarkupEnabled() throws IOException {
    list.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );

    lca.render( list );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findCreateProperty( list, "markupEnabled" ) );
  }

  private static void setFocusIndex( List list, int focusIndex ) {
    list.getAdapter( IListAdapter.class ).setFocusIndex( focusIndex );
  }

}
