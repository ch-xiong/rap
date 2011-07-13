/*******************************************************************************
 * Copyright (c) 2007, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rwt.internal.lifecycle;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rwt.SessionSingletonBase;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.internal.util.SerializableLock;
import org.eclipse.rwt.service.*;
import org.eclipse.swt.internal.SerializableCompatibility;


public final class UICallBackManager implements SerializableCompatibility {

  
  private static final int DEFAULT_REQUEST_CHECK_INTERVAL = 30000;

  private static final String FORCE_UI_CALLBACK
    = UICallBackManager.class.getName() + "#forceUICallBack";
  
  class DuplicateCallBackRequestException extends RuntimeException {
  }
  
  private static class UnblockSessionStoreListener
    implements SessionStoreListener, SerializableCompatibility
  {
    private transient final Thread currentThread;

    private UnblockSessionStoreListener( Thread currentThread ) {
      this.currentThread = currentThread;
    }

    public void beforeDestroy( SessionStoreEvent event ) {
      currentThread.interrupt();
    }
  }

  public static UICallBackManager getInstance() {
    return ( UICallBackManager )SessionSingletonBase.getInstance( UICallBackManager.class );
  }
  
  private final IdManager idManager;

  // synchronization object to control access to the runnables List
  final SerializableLock lock;
  // contains a reference to the callback request thread that is currently
  // blocked.
  private transient Thread activeCallBackRequest;
  // Flag that indicates whether a request is processed. In that case no
  // notifications are sent to the client.
  private boolean uiThreadRunning;
  // indicates whether the display has runnables to execute
  private boolean hasRunnables;
  private boolean wakeCalled;
  private int requestCheckInterval;

  private UICallBackManager() {
    lock = new SerializableLock();
    idManager = new IdManager();
    uiThreadRunning = false;
    wakeCalled = false;
    requestCheckInterval = DEFAULT_REQUEST_CHECK_INTERVAL;
  }

  public boolean isCallBackRequestBlocked() {
    synchronized( lock ) {
      return activeCallBackRequest != null;
    }
  }

  public void wakeClient() {
    synchronized( lock ) {
      if( !uiThreadRunning ) {
        releaseBlockedRequest();
      }
    }
  }

  public void releaseBlockedRequest() {
    synchronized( lock ) {
      wakeCalled = true;
      lock.notifyAll();
    }
  }

  public void setHasRunnables( boolean hasRunnables ) {
    synchronized( lock ) {
      this.hasRunnables = hasRunnables;
    }
    if( hasRunnables && isUICallBackActive() ) {
      ContextProvider.getStateInfo().setAttribute( FORCE_UI_CALLBACK, Boolean.TRUE );
    }
  }
  
  public void setRequestCheckInterval( int requestCheckInterval ) {
    this.requestCheckInterval = requestCheckInterval;
  }

  void notifyUIThreadStart() {
    synchronized( lock ) {
      uiThreadRunning = true;
    }
  }

  void notifyUIThreadEnd() {
    synchronized( lock ) {
      uiThreadRunning = false;
      if( hasRunnables ) {
        wakeClient();
      }
    }
  }

  boolean hasRunnables() {
    synchronized( lock ) {
      return hasRunnables;
    }
  }

  void blockCallBackRequest( HttpServletResponse response ) {
    synchronized( lock ) {
      if( activeCallBackRequest != null ) {
        throw new DuplicateCallBackRequestException();
      }
      if( mustBlockCallBackRequest() ) {
        Thread currentThread = Thread.currentThread();
        SessionStoreListener listener = new UnblockSessionStoreListener( currentThread );
        ISessionStore sessionStore = ContextProvider.getSession();
        sessionStore.addSessionStoreListener( listener );
        activeCallBackRequest = Thread.currentThread();
        try {
          boolean keepWaiting = true;
          wakeCalled = false;
          while( !wakeCalled && keepWaiting ) {
            lock.wait( requestCheckInterval );
            keepWaiting = mustBlockCallBackRequest() && isConnectionAlive( response );
          }
        } catch( InterruptedException ie ) {
          Thread.interrupted(); // Reset interrupted state, see bug 300254
        } finally {
          activeCallBackRequest = null;
          sessionStore.removeSessionStoreListener( listener );
        }
      }
    }
  }

  private static boolean isConnectionAlive( HttpServletResponse response ) {
    boolean result;
    try {
      JavaScriptResponseWriter responseWriter = new JavaScriptResponseWriter( response );
      responseWriter.write( " " );
      result = !responseWriter.checkError();
    } catch( IOException ioe ) {
      result = false;
    }
    return result;
  }

  boolean mustBlockCallBackRequest() {
    return isUICallBackActive() && ( uiThreadRunning || !hasRunnables );
  }

  boolean isUICallBackActive() {
    return !idManager.isEmpty();
  }

  public void activateUICallBacksFor( final String id ) {
    idManager.add( id );
  }

  public void deactivateUICallBacksFor( final String id ) {
    int size = idManager.remove( id );
    if( size == 0 ) {
      releaseBlockedRequest();
    }
  }

  public boolean needsActivation() {
    boolean result;
    if( isCallBackRequestBlocked() ) {
      result = false;
    } else {
      result = isUICallBackActive() || forceUICallBackForPendingRunnables();
    }
    return result;
  }

  private static boolean forceUICallBackForPendingRunnables() {
    return Boolean.TRUE.equals( ContextProvider.getStateInfo().getAttribute( FORCE_UI_CALLBACK ) );
  }
}
