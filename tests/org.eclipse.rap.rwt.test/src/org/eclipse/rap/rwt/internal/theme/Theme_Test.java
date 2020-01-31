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
package org.eclipse.rap.rwt.internal.theme;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;
import static org.eclipse.rap.rwt.internal.theme.ThemeTestUtil.RESOURCE_LOADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.theme.css.ConditionalValue;
import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.swt.widgets.Button;
import org.junit.Test;


public class Theme_Test {

  private static final String TEST_SYNTAX_CSS = "TestExample.css";

  @Test
  public void testCreateWithNullId() {
    try {
      new Theme( null, "Test", null );
      fail();
    } catch( NullPointerException e ) {
      assertTrue( e.getMessage().contains( "id" ) );
    }
  }

  @Test
  public void testCreateEmpty() {
    Theme theme = new Theme( "some.id", "Test", null );
    assertEquals( "some.id", theme.getId() );
    assertEquals( "Test", theme.getName() );
    ThemeableWidget buttonWidget = createSimpleButtonWidget();
    theme.initialize( new ThemeableWidget[] { buttonWidget } );
    CssValuesMap valuesMap = theme.getValuesMap();
    assertEquals( 0, valuesMap.getAllValues().length );
  }

  @Test
  public void testCreate() throws Exception {
    StyleSheet styleSheet = ThemeTestUtil.getStyleSheet( TEST_SYNTAX_CSS );
    Theme theme = new Theme( "some.id", "Test", styleSheet );
    assertEquals( "some.id", theme.getId() );
    assertEquals( "Test", theme.getName() );
  }

  @Test
  public void testAddStyleSheet() throws Exception {
    String defaultCss = "Button { background-color: black; color: #aaaaaa; }\n";
    StyleSheet defaultStyleSheet = ThemeTestUtil.createStyleSheet( defaultCss );
    Theme theme = new Theme( "some.id", "Test", defaultStyleSheet );
    String addedCss = "Button.SPECIAL { color: #bbbbbb; }\n";
    StyleSheet addedStyleSheet = ThemeTestUtil.createStyleSheet( addedCss );
    theme.addStyleSheet( addedStyleSheet );
    ThemeableWidget buttonWidget = createSimpleButtonWidget();
    theme.initialize( new ThemeableWidget[] { buttonWidget } );
    CssValuesMap valuesMap = theme.getValuesMap();
    ConditionalValue[] values = valuesMap.getValues( "Button", "color" );
    assertEquals( 2, values.length );
    assertEquals( "#bbbbbb", values[ 0 ].value.toDefaultString() );
    assertEquals( "#aaaaaa", values[ 1 ].value.toDefaultString() );
  }

  @Test( expected = IllegalStateException.class )
  public void test_Uninitialized() throws Exception {
    StyleSheet styleSheet = ThemeTestUtil.getStyleSheet( TEST_SYNTAX_CSS );
    Theme theme = new Theme( "some.id", "Test", styleSheet );

    theme.getValuesMap();
  }

  @Test
  public void testInitialize() throws Exception {
    StyleSheet styleSheet = ThemeTestUtil.getStyleSheet( TEST_SYNTAX_CSS );
    Theme theme = new Theme( "some.id", "Test", styleSheet );
    theme.initialize( new ThemeableWidget[ 0 ] );
    assertNotNull( theme.getValuesMap() );
  }

  @Test( expected = IllegalStateException.class )
  public void testInitializeTwice() throws Exception {
    StyleSheet styleSheet = ThemeTestUtil.getStyleSheet( TEST_SYNTAX_CSS );
    Theme theme = new Theme( "some.id", "Test", styleSheet );
    theme.initialize( new ThemeableWidget[ 0 ] );

    theme.initialize( new ThemeableWidget[ 0 ] );
  }

  @Test
  public void testGetJsIdForDefaultTheme() throws Exception {
    StyleSheet styleSheet = ThemeTestUtil.getStyleSheet( TEST_SYNTAX_CSS );
    String defaultThemeId = RWT.DEFAULT_THEME_ID;
    Theme defaultTheme = new Theme( defaultThemeId, "Default", styleSheet );
    assertEquals( "rwt.theme.Default", defaultTheme.getJsId() );
  }

  @Test
  public void testGetJsId() throws Exception {
    StyleSheet styleSheet = ThemeTestUtil.getStyleSheet( TEST_SYNTAX_CSS );
    Theme theme1 = new Theme( "custom.id1", "Custom 1", styleSheet );
    assertTrue( theme1.getJsId().startsWith( "rwt.theme.Custom_" ) );
    Theme theme2 = new Theme( "custom.id2", "Custom 2", styleSheet );
    assertFalse( theme2.getJsId().equals( theme1.getJsId() ) );
  }

  @Test
  public void testRegisterResources() throws Exception {
    Fixture.setUp();
    try {
      String css = "Button {"
                   + "  background-image: url( " + Fixture.IMAGE1 + ");\n"
                   + "  cursor: url( " + Fixture.IMAGE2 + " );\n"
                   + "}";
      StyleSheet styleSheet = ThemeTestUtil.createStyleSheet( css, RESOURCE_LOADER );
      Theme defaultTheme = new Theme( "custom.theme.id", "Custom", styleSheet );
      ThemeableWidget[] widgets = new ThemeableWidget[] { createSimpleButtonWidget() };
      defaultTheme.initialize( widgets );

      ApplicationContext applicationContext = getApplicationContext();
      ResourceManager resourceManager = applicationContext.getResourceManager();
      defaultTheme.registerResources( applicationContext );

      assertTrue( resourceManager.isRegistered( "themes/images/9e78c44e.gif" ) );
      assertTrue( resourceManager.isRegistered( "themes/cursors/ccb7e1a.gif" ) );
    } finally {
      Fixture.tearDown();
    }
  }

  private static ThemeableWidget createSimpleButtonWidget() {
    ThemeableWidget buttonWidget = new ThemeableWidget( Button.class.getName(), null );
    CssElement buttonElement = new CssElementImpl( "Button" )
      .addProperty( "color" )
      .addProperty( "cursor" )
      .addProperty( "background-color" )
      .addProperty( "background-image" );
    buttonWidget.elements = new CssElement[] { buttonElement };
    return buttonWidget;
  }

}
