/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ControlThemeAdapter_Test {

  private Display display;
  private Shell shell;
  private Label control;
  private ControlThemeAdapter themeAdapter;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
    display = new Display();
    shell = new Shell( display );
    control = new Label( shell, SWT.BORDER );
    themeAdapter = getControlThemeAdapter( control );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testValues() {

    assertEquals( new BoxDimensions( 1, 1, 1, 1 ), themeAdapter.getBorder( control ) );
    assertEquals( new Color( display, 74, 74, 74 ), themeAdapter.getForeground( control ) );
    assertEquals( new Color( display, 255, 255, 255 ), themeAdapter.getBackground( control ) );
  }

  @Test
  public void testForegroundWhenDisabled() {
    control.setEnabled( false );

    assertEquals( new Color( display, 207, 207, 207 ), themeAdapter.getForeground( control ) );
  }

  @Test
  public void testFontWhenDisabled() throws IOException {
    String css = "Label:disabled { font: bold 16px Arial }";
    ThemeTestUtil.registerTheme( "custom", css, null );
    ThemeTestUtil.setCurrentThemeId( "custom" );
    control.setEnabled( false );

    Font expected = new Font( display, "Arial", 16, SWT.BOLD);
    assertEquals( expected, control.getFont() );
  }

  @Test
  public void testGetBorderWidth_forCompositeSubclass() throws IOException {
    String css = "Composite.special { border: 23px solid gray }";
    ThemeTestUtil.registerTheme( "custom", css, null );
    ThemeTestUtil.setCurrentThemeId( "custom" );

    Composite subclassedComposite = new Composite( shell, SWT.BORDER ) {
      // empty subclass
    };
    subclassedComposite.setData( RWT.CUSTOM_VARIANT, "special" );

    assertEquals( 23, subclassedComposite.getBorderWidth() );
  }

  @Test
  public void testGetBorder() throws IOException {
    String css = "Composite { border: 1px solid black; border-top: 3px solid black; }";
    ThemeTestUtil.registerTheme( "custom", css, null );
    ThemeTestUtil.setCurrentThemeId( "custom" );

    Composite composite = new Composite( shell, SWT.BORDER );

    BoxDimensions expected = new BoxDimensions( 3, 1, 1, 1 );
    assertEquals( expected , getControlThemeAdapter( composite ).getBorder( composite ) );
  }

  private ControlThemeAdapter getControlThemeAdapter( Control control ) {
    return ( ControlThemeAdapterImpl )control.getAdapter( ThemeAdapter.class );
  }

}
