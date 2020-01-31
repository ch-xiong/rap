/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class WidgetTreeUtil_Test {

  private Display display;
  private Shell shell;
  private List<Widget> visited;
  private WidgetTreeVisitor loggingVisitor;

  @Rule
  public TestContext context = new TestContext();

  @Before
  public void setUp() {
    display = new Display();
    shell = new Shell( display , SWT.NONE );
    visited = new ArrayList<>();
    loggingVisitor = new WidgetTreeVisitor() {
      @Override
      public boolean visit( Widget widget ) {
        visited.add( widget );
        return true;
      }
    };
  }

  @Test
  public void testAccept_withWidgetHierarchy() {
    Control control1 = new Button( shell, SWT.PUSH );
    Composite composite = new Composite( shell, SWT.NONE );
    Control control2 = new Button( composite, SWT.PUSH );
    Control control3 = new Button( composite, SWT.PUSH );
    Tree tree = new Tree( composite, SWT.NONE );
    TreeColumn treeColumn = new TreeColumn( tree, SWT.NONE );
    TreeItem treeItem1 = new TreeItem( tree, SWT.NONE );
    TreeItem treeItem2 = new TreeItem( tree, SWT.NONE );
    TreeItem subTreeItem1 = new TreeItem( treeItem1, SWT.NONE );
    ScrollBar hScroll = tree.getHorizontalBar();
    ScrollBar vScroll = tree.getVerticalBar();

    WidgetTreeUtil.accept( shell, loggingVisitor );

    assertEquals( asList( shell,
                          control1,
                          composite,
                          control2,
                          control3,
                          tree,
                          treeColumn,
                          treeItem1,
                          subTreeItem1,
                          treeItem2,
                          hScroll,
                          vScroll ),
                  visited );
  }

  @Test
  public void testAccept_withTable() {
    // Ensure that table item are visited in this order: first TableColumn,
    // then TableItem; regardless in which order they were constructed
    Table table = new Table( shell, SWT.NONE );
    TableItem item1 = new TableItem( table, SWT.NONE );
    TableColumn column1 = new TableColumn( table, SWT.NONE );
    TableItem item2 = new TableItem( table, SWT.NONE );
    TableColumn column2 = new TableColumn( table, SWT.NONE );
    Control child = new Button( table, SWT.PUSH );

    WidgetTreeUtil.accept( table, loggingVisitor );

    ScrollBar hScroll = table.getHorizontalBar();
    ScrollBar vScroll = table.getVerticalBar();
    assertEquals( asList( table, column1, column2, item1, item2, hScroll, vScroll, child ),
                  visited );
  }

  @Test
  public void testAccept_withList() {
    org.eclipse.swt.widgets.List list
      = new org.eclipse.swt.widgets.List( shell, SWT.H_SCROLL | SWT.V_SCROLL );

    WidgetTreeUtil.accept( list, loggingVisitor );

    ScrollBar hScroll = list.getHorizontalBar();
    ScrollBar vScroll = list.getVerticalBar();
    assertEquals( asList( list, hScroll, vScroll ), visited );
  }

  @Test
  public void testAccept_withToolBar() {
    ToolBar toolBar = new ToolBar( shell, SWT.NONE );
    ToolItem toolItem = new ToolItem( toolBar, SWT.NONE );

    WidgetTreeUtil.accept( toolBar, loggingVisitor );

    assertEquals( asList( toolBar, toolItem ), visited );
  }

  @Test
  public void testAccept_withMenus() {
    Menu menuBar = new Menu( shell, SWT.BAR );
    shell.setMenuBar( menuBar );
    Menu shellMenu = new Menu( shell );
    Text text = new Text( shell, SWT.NONE );
    Menu textMenu = new Menu( text );

    WidgetTreeUtil.accept( shell, loggingVisitor );

    assertEquals( asList( shell, menuBar, shellMenu, textMenu, text ), visited );
  }

  @Test
  public void testAccept_withDecoration() {
    Control control1 = new Button( shell, SWT.PUSH );
    Decorator decoration1 = new Decorator( control1, SWT.RIGHT );
    Composite composite = new Composite( shell, SWT.NONE );
    Control control2 = new Button( composite, SWT.PUSH );
    Decorator decoration2 = new Decorator( control2, SWT.RIGHT );

    WidgetTreeUtil.accept( shell, loggingVisitor );

    assertEquals( asList( shell, control1, decoration1, composite, control2, decoration2 ),
                  visited );
  }

  @Test
  public void testAccept_withDragSource() {
    DragSource compositeDragSource = new DragSource( shell, SWT.NONE );
    Text text = new Text( shell, SWT.NONE );
    DragSource controlDragSource = new DragSource( text, SWT.NONE );

    WidgetTreeUtil.accept( shell, loggingVisitor );

    assertEquals( asList( shell, compositeDragSource, text, controlDragSource ), visited );
  }

  @Test
  public void testAccept_withToolTip() {
    Control control = new Label( shell, SWT.NONE );
    ToolTip toolTip = new ToolTip( shell, SWT.NONE );

    WidgetTreeUtil.accept( shell, loggingVisitor );

    assertEquals( asList( shell, control, toolTip ), visited );
  }

  @Test
  public void testAccept_withCustomWidget() {
    // Custom widgets may override getChildren, see bug 363844
    Composite customWidget = new Composite( shell, SWT.NONE ) {
      @Override
      public Control[] getChildren() {
        return new Control[ 0 ];
      }
    };
    Control innerLabel = new Label( customWidget, SWT.NONE );

    WidgetTreeUtil.accept( customWidget, loggingVisitor );

    assertEquals( asList( customWidget, innerLabel ), visited );
  }

  @Test
  public void testAccept_withCustomWidget_withDelegatedScrollbars() {
    CustomWidgetWithDelegatedScrollbars customWidget
      = new CustomWidgetWithDelegatedScrollbars( shell, SWT.NONE );
    Canvas innerScrollable = customWidget.getInnerScrollable();

    WidgetTreeUtil.accept( shell, loggingVisitor );

    assertEquals( asList( shell,
                          customWidget,
                          innerScrollable,
                          innerScrollable.getHorizontalBar(),
                          innerScrollable.getVerticalBar() ),
                  visited );
  }

  @Test
  public void testAccept_stopsAtTable() {
    final Table table = new Table( shell, SWT.NONE );
    new TableItem( table, SWT.NONE );
    new TableColumn( table, SWT.NONE );
    new Button( table, SWT.PUSH );

    WidgetTreeUtil.accept( shell, new WidgetTreeVisitor() {
      @Override
      public boolean visit( Widget widget ) {
        visited.add( widget );
        return widget != table;
      }
    } );

    assertEquals( asList( shell, table ), visited );
  }

  @Test
  public void testAccept_stopsAtTreeItem() {
    final Tree tree = new Tree( shell, SWT.NO_SCROLL );
    final TreeItem item1 = new TreeItem( tree, SWT.NONE );
    new TreeItem( item1, SWT.NONE ); // <- skipped
    TreeItem item2 = new TreeItem( tree, SWT.NONE );
    TreeItem item2a = new TreeItem( item2, SWT.NONE );
    Button child = new Button( tree, SWT.PUSH );

    WidgetTreeUtil.accept( shell, new WidgetTreeVisitor() {
      @Override
      public boolean visit( Widget widget ) {
        visited.add( widget );
        return widget != item1;
      }
    } );

    assertEquals( asList( shell, tree, item1, item2, item2a, child ), visited );
  }

  private class CustomWidgetWithDelegatedScrollbars extends Composite {

    private Canvas innerScrollable;

    public CustomWidgetWithDelegatedScrollbars( Composite parent, int style ) {
      super( parent, style );
      innerScrollable = new Canvas( this, SWT.H_SCROLL | SWT.V_SCROLL );
    }

    public Canvas getInnerScrollable() {
      return innerScrollable;
    }

    @Override
    public ScrollBar getHorizontalBar() {
      return innerScrollable.getHorizontalBar();
    }

    @Override
    public ScrollBar getVerticalBar() {
      return innerScrollable.getVerticalBar();
    }

  }

}
