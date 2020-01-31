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
package org.eclipse.rap.rwt.internal.lifecycle;

import static org.junit.Assert.*;

import java.util.Locale;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings( "deprecation" )
public class LifeCycleAdapter_Test {

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeResponseWriter();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testWidgetAdapter() {
    Display display = new Display();
    Widget widget = new Shell( display );

    Object adapter = widget.getAdapter( WidgetLCA.class );

    assertNotNull( adapter );
  }

  @Test
  public void testWidgetAdapterReturnsSameAdapterForEachInvocation() {
    Display display = new Display();
    Widget widget = new Shell( display );

    Object adapter1 = widget.getAdapter( WidgetLCA.class );
    Object adapter2 = widget.getAdapter( WidgetLCA.class );

    assertSame( adapter1, adapter2 );
  }

  @Test
  public void testWidgetAdapterReturnsSameAdapterForDifferentInstancesOfSameType() {
    Display display = new Display();
    Widget widget1 = new Shell( display );
    Object adapter1 = widget1.getAdapter( WidgetLCA.class );
    Widget widget2 = new Shell( display );

    Object adapter2 = widget2.getAdapter( WidgetLCA.class );

    assertSame( adapter1, adapter2 );
  }

  @Test
  public void testWidgetAdaptreReturnsDistinctAdapterForEachWidgetType() {
    Display display = new Display();
    Shell shell = new Shell( display );
    Button button = new Button( shell, SWT.PUSH );

    Object shellAdapter = shell.getAdapter( WidgetLCA.class );
    Object buttonAdapter = button.getAdapter( WidgetLCA.class );

    assertNotNull( shellAdapter );
    assertNotNull( buttonAdapter );
    assertNotSame( shellAdapter, buttonAdapter );
  }

  @Test
  public void testWidgetAdapterIsApplicationScoped() {
    Display display1 = new Display();
    Widget widget1 = new Shell( display1 );
    Object adapter1 = widget1.getAdapter( WidgetLCA.class );
    newSession();
    Display display2 = new Display();
    Widget widget2 = new Shell( display2 );
    Object adapter2 = widget2.getAdapter( WidgetLCA.class );

    assertSame( adapter1, adapter2 );
  }

  @Test
  public void testGetAdapterWithMissingWidgetLCA() {
    Display display = new Display();
    Shell shell = new Shell( display );
    Widget widget = new TestWidget( shell );

    assertNull( widget.getAdapter( WidgetLCA.class ) );
  }

  @Test
  public void testTreeItemLifeCycleAdapter() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault( new Locale( "tr", "TR" ) );
      Display display = new Display();
      Composite shell = new Shell( display, SWT.NONE );
      Tree tree = new Tree( shell, SWT.NONE );
      TreeItem treeItem = new TreeItem( tree, SWT.NONE );

      Object treeItemLCA = treeItem.getAdapter( WidgetLCA.class );

      assertTrue( treeItemLCA instanceof WidgetLCA );
    } finally {
      Locale.setDefault( originalLocale );
    }
  }

  private static void newSession() {
    ContextProvider.disposeContext();
    Fixture.createServiceContext();
  }

  public static class CustomComposite extends Composite {
    public CustomComposite( Composite parent ) {
      super( parent, SWT.NONE );
    }
  }

  public static class TestControl extends Control {
    public TestControl( Composite parent ) {
      super( parent, SWT.NONE );
    }
  }

  public static class TestWidget extends Widget {
    public TestWidget( Widget parent ) {
      super( parent, SWT.NONE );
    }
  }

}
