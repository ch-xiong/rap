/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.tableitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.registerDataKeys;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getParent;
import static org.eclipse.rap.rwt.testfixture.internal.TestUtil.createImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.rap.rwt.testfixture.internal.TestMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.ITableAdapter;
import org.eclipse.swt.internal.widgets.buttonkit.ButtonOperationHandler;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TableItemLCA_Test {

  private Display display;
  private Shell shell;
  private Table table;
  private TableItem item;
  private TableItemLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    table = new Table( shell, SWT.NONE );
    item = new TableItem( table, SWT.NONE );
    lca = TableItemLCA.INSTANCE;
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testPreserveValues() throws IOException {
    new TableColumn( table, SWT.CENTER );
    new TableColumn( table, SWT.CENTER );
    new TableColumn( table, SWT.CENTER );
    Fixture.markInitialized( display );
    Fixture.preserveWidgets();
    RemoteAdapter adapter = WidgetUtil.getAdapter( item );
    Image[] images = ( Image[] )adapter.getPreserved( TableItemLCA.PROP_IMAGES );
    assertNull( images );
    assertNull( adapter.getPreserved( "background" ) );
    assertNull( adapter.getPreserved( "foreground" ) );
    assertNull( adapter.getPreserved( "font" ) );
    Color[] preservedCellBackgrounds
      = ( Color[] )adapter.getPreserved( TableItemLCA.PROP_CELL_BACKGROUNDS );
    assertNull( preservedCellBackgrounds );
    Color[] preservedCellForegrounds
      = ( Color[] )adapter.getPreserved( TableItemLCA.PROP_CELL_FOREGROUNDS );
    assertNull( preservedCellForegrounds );
    Font[] preservedCellFonts = ( Font[] )adapter.getPreserved( TableItemLCA.PROP_CELL_FONTS );
    assertNull( preservedCellFonts );
    Fixture.clearPreserved();
    item.setText( 0, "item11" );
    item.setText( 1, "item12" );
    item.setText( 2, "item13" );
    Font font1 = new Font( display, "font1", 10, 1 );
    item.setFont( 0, font1 );
    Font font2 = new Font( display, "font2", 8, 1 );
    item.setFont( 1, font2 );
    Font font3 = new Font( display, "font3", 6, 1 );
    item.setFont( 2, font3 );
    Image image1 = createImage( display, Fixture.IMAGE1 );
    Image image2 = createImage( display, Fixture.IMAGE2 );
    Image image3 = createImage( display, Fixture.IMAGE3 );
    item.setImage( new Image[] {
      image1, image2, image3
    } );
    Color background1 =new Color( display, 234, 230, 54 );
    item.setBackground( 0, background1 );
    Color background2 =new Color( display, 145, 222, 134 );
    item.setBackground( 1, background2 );
    Color background3 =new Color( display, 143, 134, 34 );
    item.setBackground( 2, background3 );
    Color foreground1 =new Color( display, 77, 77, 54 );
    item.setForeground( 0, foreground1 );
    Color foreground2 =new Color( display, 156, 45, 134 );
    item.setForeground( 1, foreground2 );
    Color foreground3 =new Color( display, 88, 134, 34 );
    item.setForeground( 2, foreground3 );
    table.setSelection( 0 );
    ITableAdapter tableAdapter = table.getAdapter( ITableAdapter.class );
    tableAdapter.setFocusIndex( 0 );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( item );
    images = ( Image[] )adapter.getPreserved( TableItemLCA.PROP_IMAGES );
    assertEquals( image1, images[ 0 ] );
    assertEquals( image2, images[ 1 ] );
    assertEquals( image3, images[ 2 ] );
    preservedCellFonts = ( Font[] )adapter.getPreserved( TableItemLCA.PROP_CELL_FONTS );
    assertEquals( font1, preservedCellFonts[ 0 ] );
    assertEquals( font2, preservedCellFonts[ 1 ] );
    assertEquals( font3, preservedCellFonts[ 2 ] );
    preservedCellBackgrounds
      = ( Color[] )adapter.getPreserved( TableItemLCA.PROP_CELL_BACKGROUNDS );
    assertEquals( background1, preservedCellBackgrounds[ 0 ] );
    assertEquals( background2, preservedCellBackgrounds[ 1 ] );
    assertEquals( background3, preservedCellBackgrounds[ 2 ] );
    preservedCellForegrounds
      = ( Color[] )adapter.getPreserved( TableItemLCA.PROP_CELL_FOREGROUNDS );
    assertEquals( foreground1, preservedCellForegrounds[ 0 ] );
    assertEquals( foreground2, preservedCellForegrounds[ 1 ] );
    assertEquals( foreground3, preservedCellForegrounds[ 2 ] );
    Fixture.clearPreserved();
  }

  @Test
  public void testCheckPreserveValues() {
    Table table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.preserveWidgets();
    RemoteAdapter adapter = WidgetUtil.getAdapter( item );
    Object checked = adapter.getPreserved( TableItemLCA.PROP_CHECKED );
    assertEquals( Boolean.FALSE, checked );
    Object grayed = adapter.getPreserved( TableItemLCA.PROP_GRAYED );
    assertEquals( Boolean.FALSE, grayed );
    Fixture.clearPreserved();
    item.setChecked( true );
    item.setGrayed( true );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( item );
    checked = adapter.getPreserved( TableItemLCA.PROP_CHECKED );
    grayed = adapter.getPreserved( TableItemLCA.PROP_GRAYED );
    assertEquals( Boolean.TRUE, checked );
    assertEquals( Boolean.TRUE, grayed );
    Fixture.clearPreserved();
  }

  @Test
  public void testItemTextWithoutColumn() throws IOException {
    // Ensure that even though there are no columns, the first text of an item
    // will be rendered
    Fixture.fakeResponseWriter();
    Fixture.markInitialized( item );
    lca.preserveValues( item );
    item.setText( "newText" );
    lca.renderChanges( item );
    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray().add( "newText" );
    assertEquals( expected, message.findSetProperty( item, "texts" ) );
  }

  @Test
  public void testDisposeSelected() {
    final boolean[] executed = { false };
    table = new Table( shell, SWT.CHECK );
    new TableItem( table, SWT.NONE );
    new TableItem( table, SWT.NONE );
    new TableItem( table, SWT.NONE );
    table.setSelection( 2 );
    Button button = new Button( shell, SWT.PUSH );
    getRemoteObject( button ).setHandler( new ButtonOperationHandler( button ) );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        table.remove( 1, 2 );
        executed[ 0 ] = true;
      }
    } );

    Fixture.fakeNotifyOperation( getId( button ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( display );

    assertTrue( executed[ 0 ] );
  }

  @Test
  public void testDispose() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );
    WidgetLCA<TableItem> itemLCA = WidgetUtil.getLCA( item );
    Fixture.markInitialized( table );
    Fixture.markInitialized( item );
    Fixture.fakeResponseWriter();

    item.dispose();
    itemLCA.renderDispose( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNotNull( message.findDestroyOperation( item ) );
  }

  @Test
  public void testDisposeTable() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );
    WidgetLCA<TableItem> itemLCA = WidgetUtil.getLCA( item );
    Fixture.markInitialized( table );
    Fixture.markInitialized( item );
    Fixture.fakeResponseWriter();

    table.dispose();
    itemLCA.renderDispose( item );

    // when the whole table is disposed of, the tableitem's dispose must not be rendered
    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findDestroyOperation( item ) );
    assertTrue( item.isDisposed() );
  }

  @Test
  public void testWriteChangesForVirtualItem() throws IOException {
    Table table = new Table( shell, SWT.VIRTUAL );
    table.setItemCount( 100 );
    // Ensure that nothing is written for an item that is virtual and whose
    // cached was false and remains unchanged while processing the life cycle
    TableItem item = table.getItem( 0 );
    table.clear( 0 );
    Fixture.fakeResponseWriter();
    Fixture.markInitialized( item );
    // Ensure that nothing else than the 'index' and 'cached' property gets preserved
    lca.preserveValues( item );
    RemoteAdapter itemAdapter = WidgetUtil.getAdapter( item );

    assertEquals( Boolean.FALSE, itemAdapter.getPreserved( TableItemLCA.PROP_CACHED ) );
    assertEquals( Integer.valueOf( 0 ), itemAdapter.getPreserved( TableItemLCA.PROP_INDEX ) );
    assertNull( itemAdapter.getPreserved( TableItemLCA.PROP_TEXTS ) );
    assertNull( itemAdapter.getPreserved( TableItemLCA.PROP_IMAGES ) );
    assertNull( itemAdapter.getPreserved( TableItemLCA.PROP_CHECKED ) );

    // ... and no markup is generated for a uncached item that was already
    // uncached when entering the life cycle
    lca.renderChanges( item );

    assertEquals( 0, Fixture.getProtocolMessage().getOperationCount() );
  }

  @Test
  public void testDynamicColumns() {
    new TableColumn( table, SWT.NONE );
    item.setBackground( 0, display.getSystemColor( SWT.COLOR_BLACK ) );
    // Create another column after setting a cell background
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=277089
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.preserveWidgets();
  }

  @Test
  public void testRenderCreate() throws IOException {
    lca.renderInitialization( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( "rwt.widgets.GridItem", operation.getType() );
  }

  @Test
  public void testRenderCreate_setsOperationHandler() throws IOException {
    String id = getId( item );

    lca.renderInitialization( item );

    OperationHandler handler = RemoteObjectRegistry.getInstance().get( id ).getHandler();
    assertTrue( handler instanceof TableItemOperationHandler );
  }

  @Test
  public void testRenderParent() throws IOException {
    lca.renderInitialization( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( getId( item.getParent() ), getParent( operation ) );
  }

  @Test
  public void testRenderInitialIndex() throws IOException {
    TableItem item1 = new TableItem( table, SWT.NONE );

    lca.render( item1 );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item1 );
    assertEquals( 1, operation.getProperties().get( "index" ).asInt() );
  }

  @Test
  public void testRenderIndex() throws IOException {
    new TableItem( table, SWT.NONE, 0 );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 1, message.findSetProperty( item, "index" ).asInt() );
  }

  @Test
  public void testRenderIndex_VirtualAfterClear() throws IOException {
    table = new Table( shell, SWT.VIRTUAL );
    item = new TableItem( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    new TableItem( table, SWT.NONE, 0 );
    table.clear( 1 );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 1, message.findSetProperty( item, "index" ).asInt() );
  }

  @Test
  public void testRenderIndexUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    new TableItem( table, SWT.NONE, 0 );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "index" ) );
  }

  @Test
  public void testRenderInitialTexts() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "texts" ) );
  }

  @Test
  public void testRenderTexts() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    item.setText( new String[] { "item 0.0", "item 0.1" } );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray().add( "item 0.0" ).add( "item 0.1" );
    assertEquals( expected, message.findSetProperty( item, "texts" ) );
  }

  @Test
  public void testRenderTextsUnchanged() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setText( new String[] { "item 0.0", "item 0.1" } );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "texts" ) );
  }

  @Test
  public void testRenderTextsReset() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    item.setText( 1, "item 0.1" );
    Fixture.preserveWidgets();

    item.setText( 1, "" );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( item, "texts" ) );
  }

  @Test
  public void testRenderInitialImages() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "images" ) );
  }

  @Test
  public void testRenderImages() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Image image = createImage( display, Fixture.IMAGE1 );

    item.setImage( new Image[] { null, image } );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray();
    expected.add( JsonValue.NULL );
    expected.add( new JsonArray().add( "rwt-resources/generated/90fb0bfe.gif" ).add( 58 ).add( 12 ) );
    assertEquals( expected, message.findSetProperty( item, "images" ) );
  }

  @Test
  public void testRenderImagesUnchanged() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Image image = createImage( display, Fixture.IMAGE1 );

    item.setImage( new Image[] { null, image } );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "images" ) );
  }

  @Test
  public void testRenderImagesReset() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Image image = createImage( display, Fixture.IMAGE1 );
    item.setImage( 1, image );
    Fixture.preserveWidgets();

    item.setImage( 1, null );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( item, "images" ) );
  }

  @Test
  public void testRenderInitialBackground() throws IOException {
    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "background" ) );
  }

  @Test
  public void testRenderBackground() throws IOException {
    item.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );

    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[0,255,0,255]" );
    assertEquals( expected, message.findSetProperty( item, "background" ) );
  }

  @Test
  public void testRenderBackgroundUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "background" ) );
  }

  @Test
  public void testRenderInitialForeground() throws IOException {
    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "foreground" ) );
  }

  @Test
  public void testRenderForeground() throws IOException {
    item.setForeground( display.getSystemColor( SWT.COLOR_GREEN ) );

    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[0, 255, 0, 255]" );
    assertEquals( expected, message.findSetProperty( item, "foreground" ) );
  }

  @Test
  public void testRenderForegroundUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setForeground( display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "foreground" ) );
  }

  @Test
  public void testRenderInitialFont() throws IOException {
    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "font" ) );
  }

  @Test
  public void testRenderFont() throws IOException {
    item.setFont( new Font( display, "Arial", 20, SWT.BOLD ) );

    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[[\"Arial\"], 20, true, false]" );
    assertEquals( expected, message.findSetProperty( item, "font" ) );
  }

  @Test
  public void testRenderFontUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "font" ) );
  }

  @Test
  public void testRenderInitialCellBackgrounds() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "cellBackgrounds" ) );
  }

  @Test
  public void testRenderCellBackgrounds() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    item.setBackground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[null, [0, 255, 0, 255]]" );
    assertEquals( expected, message.findSetProperty( item, "cellBackgrounds" ) );
  }

  @Test
  public void testRenderCellBackgroundsUnchanged() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setBackground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellBackgrounds" ) );
  }

  @Test
  public void testRenderCellBackgroundsReset() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    item.setBackground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();

    item.setBackground( 1, null );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( item, "cellBackgrounds" ) );
  }

  @Test
  public void testRenderInitialCellForegrounds() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "cellForegrounds" ) );
  }

  @Test
  public void testRenderCellForegrounds() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    item.setForeground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[null, [0, 255, 0, 255]]" );
    assertEquals( expected, message.findSetProperty( item, "cellForegrounds" ) );
  }

  @Test
  public void testRenderCellForegroundsUnchanged() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setForeground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellForegrounds" ) );
  }

  @Test
  public void testRenderCellForegroundsReset() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    item.setForeground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();

    item.setForeground( 1, null );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( item, "cellForegrounds" ) );
  }

  @Test
  public void testRenderInitialCellFonts() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "cellFonts" ) );
  }

  @Test
  public void testRenderCellFonts() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );

    item.setFont( 1, new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[null, [[\"Arial\"], 20, true, false]]" );
    assertEquals( expected, message.findSetProperty( item, "cellFonts" ) );
  }

  @Test
  public void testRenderCellFontsUnchanged() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setFont( 1, new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellFonts" ) );
  }

  @Test
  public void testRenderCellFontsReset() throws IOException {
    new TableColumn( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    item.setFont( 1, new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();

    item.setFont( 1, null );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( item, "cellFonts" ) );
  }

  @Test
  public void testRenderInitialChecked() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "checked" ) );
  }

  @Test
  public void testRenderChecked() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );

    item.setChecked( true );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( item, "checked" ) );
  }

  @Test
  public void testRenderCheckedUnchanged() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setChecked( true );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "checked" ) );
  }

  @Test
  public void testRenderInitialGrayed() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );

    lca.render( item );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertFalse( operation.getProperties().names().contains( "grayed" ) );
  }

  @Test
  public void testRenderGrayed() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );

    item.setGrayed( true );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( item, "grayed" ) );
  }

  @Test
  public void testRenderGrayedUnchanged() throws IOException {
    table = new Table( shell, SWT.CHECK );
    item = new TableItem( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setGrayed( true );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "grayed" ) );
  }

  @Test
  public void testRenderCustomVariant() throws IOException {
    item.setData( RWT.CUSTOM_VARIANT, "blue" );

    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( item, "customVariant" ).asString() );
  }

  @Test
  public void testRenderClear() throws IOException {
    table = new Table( shell, SWT.VIRTUAL );
    table.setItemCount( 1 );
    TableItem item = table.getItem( 0 );

    table.clear( 0 );
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertNotNull( message.findCallOperation( item, "clear" ) );
  }

  @Test
  public void testRenderData() throws IOException {
    registerDataKeys( new String[]{ "foo", "bar" } );
    item.setData( "foo", "string" );
    item.setData( "bar", Integer.valueOf( 1 ) );

    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    JsonObject data = ( JsonObject )message.findSetProperty( item, "data" );
    assertEquals( "string", data.get( "foo" ).asString() );
    assertEquals( 1, data.get( "bar" ).asInt() );
  }

  @Test
  public void testRenderDataUnchanged() throws IOException {
    registerDataKeys( new String[]{ "foo" } );
    item.setData( "foo", "string" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    Fixture.preserveWidgets();
    lca.renderChanges( item );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

}
