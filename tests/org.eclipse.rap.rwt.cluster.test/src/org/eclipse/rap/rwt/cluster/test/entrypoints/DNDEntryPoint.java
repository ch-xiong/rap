/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.cluster.test.entrypoints;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings( "restriction" )
public class DNDEntryPoint implements EntryPoint {

  public static final String ID_DRAG_SOURCE = "dragSourceId";
  public static final String ID_DROP_TARGET = "dropTargetId";
  public static final String ID_SOURCE_LABEL = "sourceLabelId";

  private static final String TRANSFER_DATA = "transfer data";
  private static final String ATTR_DRAG_FINISHED = "dragFinished";
  private static final String ATTR_DROP_FINISHED = "dropFinished";

  public static boolean isDragFinished( UISession uiSession ) {
    return Boolean.TRUE.equals( uiSession.getAttribute( ATTR_DRAG_FINISHED ) );
  }

  public static boolean isDropFinished( UISession uiSession ) {
    return Boolean.TRUE.equals( uiSession.getAttribute( ATTR_DROP_FINISHED ) );
  }

  public int createUI() {
    Display display = new Display();
    Shell shell = new Shell( display );
    Label sourceLabel = new Label( shell, SWT.NONE );
    assignWidgetId( sourceLabel, ID_SOURCE_LABEL );
    sourceLabel.setText( "source label" );
    DragSource dragSource = new DragSource( sourceLabel, DND.DROP_MOVE );
    assignWidgetId( dragSource, ID_DRAG_SOURCE );
    dragSource.setTransfer( new Transfer[] { TextTransfer.getInstance() } );
    dragSource.addDragListener( new LabelDragSourceListener() );
    Label targetLabel = new Label( shell, SWT.NONE );
    targetLabel.setText( "target label" );
    DropTarget dropTarget = new DropTarget( targetLabel, DND.DROP_MOVE );
    assignWidgetId( dropTarget, ID_DROP_TARGET );
    dropTarget.setTransfer( new Transfer[] { TextTransfer.getInstance() } );
    dropTarget.addDropListener( new LabelDropTargetListener() );
    shell.open();
    return 0;
  }

  private static void assignWidgetId( Widget widget, String id ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )widget.getAdapter( RemoteAdapter.class );
    try {
      Field field = WidgetRemoteAdapter.class.getDeclaredField( "id" );
      field.setAccessible( true );
      field.set( adapter, id );
    } catch( Exception exception ) {
      throw new RuntimeException( exception );
    }
  }

  private static class LabelDragSourceListener extends DragSourceAdapter implements Serializable {

    @Override
    public void dragSetData( DragSourceEvent event ) {
      event.data = TRANSFER_DATA;
    }

    @Override
    public void dragFinished( DragSourceEvent event ) {
      RWT.getUISession().setAttribute( ATTR_DRAG_FINISHED, Boolean.TRUE );
    }
  }

  private static class LabelDropTargetListener extends DropTargetAdapter implements Serializable {

    @Override
    public void drop( DropTargetEvent event ) {
      if( TRANSFER_DATA.equals( event.data ) ) {
        RWT.getUISession().setAttribute( ATTR_DROP_FINISHED, Boolean.TRUE );
      }
    }
  }

}
