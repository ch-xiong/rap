/*******************************************************************************
 * Copyright (c) 2008, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.datetimekit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getParent;
import static org.eclipse.rap.rwt.testfixture.internal.TestMessage.getStyles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.rap.rwt.testfixture.internal.TestMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.controlkit.ControlLCATestUtil;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DateTimeLCA_Test {

  private Display display;
  private Shell shell;
  private DateTimeLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display, SWT.NONE );
    lca = DateTimeLCA.INSTANCE;
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testCommonControlProperties() throws IOException {
    ControlLCATestUtil.testCommonControlProperties( new DateTime( shell, SWT.DATE ) );
    ControlLCATestUtil.testCommonControlProperties( new DateTime( shell, SWT.TIME ) );
    ControlLCATestUtil.testCommonControlProperties( new DateTime( shell, SWT.CALENDAR ) );
  }

  // 315950: [DateTime] method getDay() return wrong day in particular circumstances
  @Test
  public void testDateTimeDate_Bug315950() {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN );
    getRemoteObject( dateTime ).setHandler( new DateTimeOperationHandler( dateTime ) );
    dateTime.setDay( 15 );
    dateTime.setMonth( 5 );
    dateTime.setYear( 2010 );
    Fixture.markInitialized( display );

    Fixture.fakeNotifyOperation( getId( dateTime ), ClientMessageConst.EVENT_SELECTION, null );
    JsonObject properties = new JsonObject().add( "year", 2010 ).add( "month", 4 ).add( "day", 31 );
    Fixture.fakeSetOperation( getId( dateTime ), properties );
    Fixture.readDataAndProcessAction( dateTime );

    assertEquals( 31, dateTime.getDay() );
    assertEquals( 4, dateTime.getMonth() );
    assertEquals( 2010, dateTime.getYear() );
  }

  @Test
  public void testRenderCreateDate() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.SHORT | SWT.DROP_DOWN );

    lca.renderInitialization( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertEquals( "rwt.widgets.DateTime", operation.getType() );
    List<String> styles = getStyles( operation );
    assertTrue( styles.contains( "DATE" ) );
    assertTrue( styles.contains( "SHORT" ) );
    assertTrue( styles.contains( "DROP_DOWN" ) );
  }

  @Test
  public void testRenderCreateDate_InitalParameters() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    lca.renderInitialization( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    java.util.List<String> propertyNames = operation.getProperties().names();
    assertTrue( propertyNames.contains( "cellSize" ) );
    assertTrue( propertyNames.contains( "monthNames" ) );
    assertTrue( propertyNames.contains( "weekdayNames" ) );
    assertTrue( propertyNames.contains( "weekdayShortNames" ) );
    assertTrue( propertyNames.contains( "dateSeparator" ) );
    assertTrue( propertyNames.contains( "datePattern" ) );
  }

  @Test
  public void testRenderInitialization_Date_setsOperationHandler() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    String id = getId( dateTime );
    lca.renderInitialization( dateTime );

    OperationHandler handler = RemoteObjectRegistry.getInstance().get( id ).getHandler();
    assertTrue( handler instanceof DateTimeOperationHandler );
  }

  @Test
  public void testReadData_Date_usesOperationHandler() {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    DateTimeOperationHandler handler = spy( new DateTimeOperationHandler( dateTime ) );
    getRemoteObject( getId( dateTime ) ).setHandler( handler );

    Fixture.fakeNotifyOperation( getId( dateTime ), "Help", new JsonObject() );
    lca.readData( dateTime );

    verify( handler ).handleNotifyHelp( dateTime, new JsonObject() );
  }

  @Test
  public void testRenderCreateTime() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME | SWT.MEDIUM );

    lca.renderInitialization( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertEquals( "rwt.widgets.DateTime", operation.getType() );
    List<String> styles = getStyles( operation );
    assertTrue( styles.contains( "TIME" ) );
    assertTrue( styles.contains( "MEDIUM" ) );
  }

  @Test
  public void testReadData_Time_usesOperationHandler() {
    DateTime dateTime = new DateTime( shell, SWT.TIME );
    DateTimeOperationHandler handler = spy( new DateTimeOperationHandler( dateTime ) );
    getRemoteObject( getId( dateTime ) ).setHandler( handler );

    Fixture.fakeNotifyOperation( getId( dateTime ), "Help", new JsonObject() );
    lca.readData( dateTime );

    verify( handler ).handleNotifyHelp( dateTime, new JsonObject() );
  }

  @Test
  public void testRenderCreateCalendar() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.CALENDAR | SWT.LONG );

    lca.renderInitialization( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertEquals( "rwt.widgets.DateTime", operation.getType() );
    List<String> styles = getStyles( operation );
    assertTrue( styles.contains( "CALENDAR" ) );
    assertTrue( styles.contains( "LONG" ) );
  }

  @Test
  public void testReadData_Calendar_usesOperationHandler() {
    DateTime dateTime = new DateTime( shell, SWT.CALENDAR );
    DateTimeOperationHandler handler = spy( new DateTimeOperationHandler( dateTime ) );
    getRemoteObject( getId( dateTime ) ).setHandler( handler );

    Fixture.fakeNotifyOperation( getId( dateTime ), "Help", new JsonObject() );
    lca.readData( dateTime );

    verify( handler ).handleNotifyHelp( dateTime, new JsonObject() );
  }

  @Test
  public void testRenderCreateCalendar_InitalParameters() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.CALENDAR );

    lca.renderInitialization( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    java.util.List<String> propertyNames = operation.getProperties().names();
    assertTrue( propertyNames.contains( "cellSize" ) );
    assertTrue( propertyNames.contains( "monthNames" ) );
    assertTrue( propertyNames.contains( "weekdayShortNames" ) );
  }

  @Test
  public void testRenderParent() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );

    lca.renderInitialization( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertEquals( getId( dateTime.getParent() ), getParent( operation ) );
  }

  @Test
  public void testRenderAddSelectionListener() throws Exception {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );
    Fixture.preserveWidgets();

    dateTime.addListener( SWT.Selection, mock( Listener.class ) );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( dateTime, "Selection" ) );
  }

  @Test
  public void testRenderRemoveSelectionListener() throws Exception {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );
    Listener listener = mock( Listener.class );
    dateTime.addListener( SWT.Selection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );
    Fixture.preserveWidgets();

    dateTime.removeListener( SWT.Selection, listener );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( dateTime, "Selection" ) );
  }

  @Test
  public void testRenderSelectionListenerUnchanged() throws Exception {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );
    Fixture.preserveWidgets();

    dateTime.addListener( SWT.Selection, mock( Listener.class ) );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( dateTime, "Selection" ) );
  }

  @Test
  public void testRenderAddDefaultSelectionListener() throws Exception {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );
    Fixture.preserveWidgets();

    dateTime.addListener( SWT.DefaultSelection, mock( Listener.class ) );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( dateTime, "DefaultSelection" ) );
  }

  @Test
  public void testRenderRemoveDefaultSelectionListener() throws Exception {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );
    Listener listener = mock( Listener.class );
    dateTime.addListener( SWT.DefaultSelection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );
    Fixture.preserveWidgets();

    dateTime.removeListener( SWT.DefaultSelection, listener );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( dateTime, "DefaultSelection" ) );
  }

  @Test
  public void testRenderDefaultSelectionListenerUnchanged() throws Exception {
    DateTime dateTime = new DateTime( shell, SWT.DATE | SWT.MEDIUM );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );
    Fixture.preserveWidgets();

    dateTime.addListener( SWT.DefaultSelection, mock( Listener.class ) );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( dateTime, "DefaultSelection" ) );
  }

  @Test
  public void testRenderInitialDate() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "date" ) );
  }

  @Test
  public void testRenderDate() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    dateTime.setDate( 1974, 5, 6 );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray().add( 1974 ).add( 5 ).add( 6 );
    assertEquals( expected, message.findSetProperty( dateTime, "date" ).asArray() );
  }

  @Test
  public void testRenderYearUnchanged() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setDate( 1974, 5, 6 );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "date" ) );
  }

  @Test
  public void testRenderInitialMinimum() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertFalse( operation.getProperties().names().contains( "minimum" ) );
  }

  @Test
  public void testRenderMinimum() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Date date = new Date();

    dateTime.setMinimum( date );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( date.getTime(), message.findSetProperty( dateTime, "minimum" ).asLong() );
  }

  @Test
  public void testRenderMinimumUnchanged() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setMinimum( new Date() );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "minimum" ) );
  }

  @Test
  public void testRenderMinimumReset() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setMinimum( new Date() );
    Fixture.preserveWidgets();
    dateTime.setMinimum( null );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( dateTime, "minimum" ) );
  }

  @Test
  public void testRenderInitialMaximum() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertFalse( operation.getProperties().names().contains( "maximum" ) );
  }

  @Test
  public void testRenderMaximum() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Date date = new Date();

    dateTime.setMaximum( date );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( date.getTime(), message.findSetProperty( dateTime, "maximum" ).asLong() );
  }

  @Test
  public void testRenderMaximumUnchanged() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setMaximum( new Date() );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "maximum" ) );
  }

  @Test
  public void testRenderMaximumReset() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setMaximum( new Date() );
    Fixture.preserveWidgets();
    dateTime.setMaximum( null );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.NULL, message.findSetProperty( dateTime, "maximum" ) );
  }

  @Test
  public void testRenderInitialSubWidgetsBounds_Date() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "subWidgetsBounds" ) );
  }

  @Test
  public void testRenderSubWidgetsBounds_Date() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );

    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray actual = ( JsonArray )message.findSetProperty( dateTime, "subWidgetsBounds" );
    assertEquals( 9, actual.size() );
  }

  @Test
  public void testRenderSubWidgetsBoundsUnchanged_Date() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.DATE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "subWidgetsBounds" ) );
  }

  @Test
  public void testRenderInitialHours() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "hours" ) );
  }

  @Test
  public void testRenderHours() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    dateTime.setHours( 10 );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 10, message.findSetProperty( dateTime, "hours" ).asInt() );
  }

  @Test
  public void testRenderHoursUnchanged() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setHours( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "hours" ) );
  }

  @Test
  public void testRenderInitialMinutes() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "minutes" ) );
  }

  @Test
  public void testRenderMinutes() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    dateTime.setMinutes( 10 );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 10, message.findSetProperty( dateTime, "minutes" ).asInt() );
  }

  @Test
  public void testRenderMinutesUnchanged() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setMinutes( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "minutes" ) );
  }

  @Test
  public void testRenderInitialSeconds() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "seconds" ) );
  }

  @Test
  public void testRenderSeconds() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    dateTime.setSeconds( 10 );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertEquals( 10, message.findSetProperty( dateTime, "seconds" ).asInt() );
  }

  @Test
  public void testRenderSecondsUnchanged() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setSeconds( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "seconds" ) );
  }

  @Test
  public void testRenderInitialSubWidgetsBounds_Time() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "subWidgetsBounds" ) );
  }

  @Test
  public void testRenderSubWidgetsBounds_Time() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );

    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray actual = ( JsonArray )message.findSetProperty( dateTime, "subWidgetsBounds" );
    assertEquals( 6, actual.size() );
  }

  @Test
  public void testRenderSubWidgetsBoundsUnchanged_Time() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.TIME );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "subWidgetsBounds" ) );
  }

  @Test
  public void testRenderInitialDate_Calendar() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.CALENDAR );

    lca.render( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( dateTime );
    assertTrue( operation.getProperties().names().contains( "date" ) );
  }

  @Test
  public void testRenderYear_Calendar() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.CALENDAR );

    dateTime.setDate( 1974, 5, 6 );
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray().add( 1974 ).add( 5 ).add( 6 );
    assertEquals( expected, message.findSetProperty( dateTime, "date" ).asArray() );
  }

  @Test
  public void testRenderYearUnchanged_Calendar() throws IOException {
    DateTime dateTime = new DateTime( shell, SWT.CALENDAR );
    Fixture.markInitialized( display );
    Fixture.markInitialized( dateTime );

    dateTime.setDate( 1974, 5, 6 );
    Fixture.preserveWidgets();
    lca.renderChanges( dateTime );

    TestMessage message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( dateTime, "date" ) );
  }

}
