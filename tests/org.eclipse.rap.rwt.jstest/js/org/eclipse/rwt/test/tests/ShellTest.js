/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function() {

var TestUtil = org.eclipse.rwt.test.fixture.TestUtil;
var MessageProcessor = rwt.remote.MessageProcessor;
var ObjectRegistry = rwt.remote.ObjectRegistry;

rwt.qx.Class.define( "org.eclipse.rwt.test.tests.ShellTest", {
  extend : rwt.qx.Object,

  members : {

    testShellHandlerEventsList : function() {
      var handler = rwt.remote.HandlerRegistry.getHandler( "rwt.widgets.Shell" );

      assertEquals( [ "Activate", "Close", "Resize", "Move" ], handler.events );
    },

    testDisplayOverlayBackground : function() {
      // first check that the default theme for overlay has no background set
      var tv = new rwt.theme.ThemeValues( {} );
      var backgroundImage = tv.getCssImage( "Shell-DisplayOverlay",
                                            "background-image" );
      assertNull( backgroundImage );
// [if] This is not testable with the new default theme as we currently can't
// fake the ThemeStore. Reactivate when the ThemeStore fixture is available.
//    assertEquals( "undefined", backgroundColor );
      // create shell like the LCA would do:
      TestUtil.fakeResponse( true );
      var shell = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      shell.addState( "rwt_APPLICATION_MODAL" );
      shell.initialize();
      shell.open();
      shell.setActive( true );
      shell.setSpace( 50, 300, 50, 200 );
      shell.setVisibility( true );
      TestUtil.flush();
      TestUtil.fakeResponse( false );
      // Check for overlay background-image to be "blank.gif", as IE needs
      // this to capture mouse events.
      var overlay = rwt.widgets.base.ClientDocument.getInstance()._getBlocker();
      assertTrue( overlay.isSeeable() );
// [if] This is not testable with the new default theme as we currently can't
// fake the ThemeStore. Reactivate when the ThemeStore fixture is available.
//    assertEquals( "static/image/blank.gif", overlay.getBackgroundImage() );
      shell.doClose();
      overlay.hide(); // not done by doClose because this is the only shell
      TestUtil.flush();
      shell.destroy();
      TestUtil.flush();
    },

    testDisplayOverlayCopyStates : function() {
      TestUtil.fakeResponse( true );
      var shell = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      shell.addState( "rwt_APPLICATION_MODAL" );
      shell.addState( "rwt_myTest" );
      shell.initialize();
      shell.open();
      shell.setActive( true );
      shell.setSpace( 50, 300, 50, 200 );
      shell.setVisibility( true );
      TestUtil.flush();
      TestUtil.fakeResponse( false );
      // Check for overlay to have the same states as the shell
      var overlay = rwt.widgets.base.ClientDocument.getInstance()._getBlocker();
      assertTrue( overlay.isSeeable() );
      assertTrue( overlay.hasState( "rwt_APPLICATION_MODAL" ) );
      assertTrue( overlay.hasState( "rwt_myTest" ) );
      shell.doClose();
      overlay.hide(); // not done by doClose because this is the only shell
      TestUtil.flush();
      shell.destroy();
      TestUtil.flush();
    },

    testDisplayOverlayAddStates : function() {
      TestUtil.fakeResponse( true );
      var shell = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      shell.addState( "rwt_APPLICATION_MODAL" );
      shell.initialize();
      shell.open();
      shell.setActive( true );
      shell.setSpace( 50, 300, 50, 200 );
      shell.setVisibility( true );
      TestUtil.flush();
      TestUtil.fakeResponse( false );
      // Check for overlay to have the same states as the shell
      var overlay = rwt.widgets.base.ClientDocument.getInstance()._getBlocker();
      assertTrue( overlay.isSeeable() );
      assertTrue( overlay.hasState( "rwt_APPLICATION_MODAL" ) );
      shell.addState( "rwt_myTest" );
      assertTrue( overlay.hasState( "rwt_myTest" ) );
      shell.removeState( "rwt_myTest" );
      assertFalse( overlay.hasState( "rwt_myTest" ) );
      shell.doClose();
      overlay.hide(); // not done by doClose because this is the only shell
      TestUtil.flush();
      shell.destroy();
      TestUtil.flush();
    },

    testDisplayOverlayMultipleShells : function() {
      var overlay = rwt.widgets.base.ClientDocument.getInstance()._getBlocker();
      var visibilityChanges = 0;
      overlay.addEventListener( "changeVisibility", function( event) {
        visibilityChanges++;
      } );
      TestUtil.fakeResponse( true );
      var shell = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      var handler = rwt.remote.HandlerRegistry.getHandler( "rwt.widgets.Shell" );
      rwt.remote.ObjectRegistry.add( "w222", shell, handler );
      shell.addState( "rwt_APPLICATION_MODAL" );
      shell.initialize();
      shell.open();
      shell.setActive( true );
      shell.setSpace( 50, 300, 50, 200 );
      shell.setVisibility( true );
      TestUtil.flush();
      var shell2 = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      shell2.addState( "rwt_APPLICATION_MODAL" );
      shell2.initialize();
      shell2.addState( "rwt_myTest2" );
      shell2.open();
      shell2.setActive( true );
      shell2.setSpace( 100, 300, 50, 200 );
      shell2.setVisibility( true );
      TestUtil.flush();
      TestUtil.fakeResponse( false );
      shell.addState( "rwt_myTest1" );
      shell2.addState( "rwt_myTest2b" );
      // check for Z-index and states for shell2
      assertTrue( overlay.isSeeable() );
      assertTrue( overlay.getZIndex() > shell.getZIndex() );
      assertTrue( overlay.getZIndex() < shell2.getZIndex() );
      assertTrue( shell2.getZIndex() < 1e7 );
      assertFalse( overlay.hasState( "rwt_myTest1" ) );
      assertTrue( overlay.hasState( "rwt_myTest2" ) );
      assertTrue( overlay.hasState( "rwt_myTest2b" ) );
      // close shell2, check for Z-index and states for shell1
      shell2.doClose();
      TestUtil.flush();
      shell.addState( "rwt_myTest1b" );
      assertTrue( overlay.isSeeable() );
      assertTrue( overlay.getZIndex() < shell.getZIndex() );
      assertTrue( overlay.hasState( "rwt_myTest1" ) );
      assertTrue( overlay.hasState( "rwt_myTest1b" ) );
      assertFalse( overlay.hasState( "rwt_myTest2" ) );
      assertFalse( overlay.hasState( "rwt_myTest2b" ) );
      shell.doClose();
      overlay.hide(); // not done by doClose because this is the only shell
      assertEquals( 2, visibilityChanges ); // to prevent unwanted animations
      TestUtil.flush();
      shell.destroy();
      TestUtil.flush();
    },

    testCustomVariant : function() {
      var shell = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      var variant = "variant_myCustomVariant";
      shell.addState( variant );
      assertTrue( shell._captionBar.hasState( variant) );
      assertTrue( shell._captionTitle.hasState( variant) );
      assertTrue( shell._minimizeButton.hasState( variant) );
      assertTrue( shell._maximizeButton.hasState( variant) );
      assertTrue( shell._restoreButton.hasState( variant) );
      assertTrue( shell._closeButton.hasState( variant) );
      shell.removeState( variant );
      assertFalse( shell._captionBar.hasState( variant) );
      assertFalse( shell._captionTitle.hasState( variant) );
      assertFalse( shell._minimizeButton.hasState( variant) );
      assertFalse( shell._maximizeButton.hasState( variant) );
      assertFalse( shell._restoreButton.hasState( variant) );
      assertFalse( shell._closeButton.hasState( variant) );
    },

    testMinimizeRestore : function() {
      var shell = TestUtil.createShellByProtocol( "w3" );

      shell.setActive(false);
      shell.minimize();
      shell.restore();
      shell.setActive(true);

      assertTrue(shell.getElement().style.zIndex < 1e7);
      assertTrue(shell.getElement().style.zIndex >= 1e5);
      assertTrue(shell.isSeeable());
      shell.destroy();
    },

    testDefaultButtonState_addedAndRemovedWithSetter : function() {
      var shell = new rwt.widgets.Shell( [ "APPLICATION_MODAL" ] );
      var button = new rwt.widgets.Button( "push" );
      button.setParent( shell );
      assertFalse( button.hasState( "default") );
      shell.setDefaultButton( button );
      assertTrue( button.hasState( "default") );
      shell.setDefaultButton( null );
      assertFalse( button.hasState( "default") );
      button.destroy();
      shell.destroy();
    },

    testDefaultButtonState_movedToFocusedButton : function() {
      var shell = TestUtil.createShellByProtocol( "w3" );
      shell.open();
      var button1 = new rwt.widgets.Button( "push" );
      button1.setParent( shell );
      var button2 = new rwt.widgets.Button( "push" );
      rwt.remote.ObjectRegistry.add( "w4", button2, rwt.remote.HandlerRegistry.getHandler( "rwt.widgets.Button" ) );
      button2.setParent( shell );
      shell.setDefaultButton( button1 );
      TestUtil.flush();

      TestUtil.click( button2 );

      assertFalse( button1.hasState( "default") );
      assertTrue( button2.hasState( "default") );
      shell.destroy();
    },

    testDefaultButtonState_notMovedToFocusedCheckBox : function() {
        var shell = TestUtil.createShellByProtocol( "w3" );
        shell.open();
        var button1 = new rwt.widgets.Button( "push" );
        button1.setParent( shell );
        var button2 = new rwt.widgets.Button( "check" );
        rwt.remote.ObjectRegistry.add( "w4", button2, rwt.remote.HandlerRegistry.getHandler( "rwt.widgets.Button" ) );
        button2.setParent( shell );
        shell.setDefaultButton( button1 );
        TestUtil.flush();

        TestUtil.click( button2 );

        assertTrue( button1.hasState( "default" ) );
        assertFalse( button2.hasState( "default" ) );
        shell.destroy();
    },

    testDefaultButtonGainFocusOnExecute : function() {
      var shell = TestUtil.createShellByProtocol( "w3" );
      shell.open();
      rwt.event.EventHandler.setFocusRoot( shell );
      MessageProcessor.processOperation( {
        "target" : "w4",
        "action" : "create",
        "type" : "rwt.widgets.Button",
        "properties" : {
          "style" : [ "PUSH" ],
          "parent" : "w3"
        }
      } );
      var button = ObjectRegistry.getObject( "w4" );
      shell.setDefaultButton( button );
      TestUtil.flush();
      assertFalse( button.getFocused() );

      TestUtil.keyDown( shell.getElement(), "Enter", 0 );

      assertTrue( button.getFocused() );
      button.destroy();
      shell.destroy();
    },

    testDefaultButtonExecute : function() {
      var shell = TestUtil.createShellByProtocol( "w3" );
      rwt.event.EventHandler.setFocusRoot( shell );
      MessageProcessor.processOperation( {
        "target" : "w4",
        "action" : "create",
        "type" : "rwt.widgets.Button",
        "properties" : {
          "style" : [ "PUSH" ],
          "parent" : "w3"
        }
      } );
      TestUtil.protocolListen( "w4", { "Selection" : true } );
      var button = ObjectRegistry.getObject( "w4" );
      shell.setDefaultButton( button );
      TestUtil.flush();
      TestUtil.clearRequestLog();

      TestUtil.keyDown( shell.getElement(), "Enter", 0 );

      assertNotNull( TestUtil.getMessageObject().findNotifyOperation( "w4", "Selection" ) );
      button.destroy();
      shell.destroy();
    },

    testDefaultButtonExecute_onDisabledButton : function() {
      var shell = TestUtil.createShellByProtocol( "w3" );
      rwt.event.EventHandler.setFocusRoot( shell );
      MessageProcessor.processOperation( {
        "target" : "w4",
        "action" : "create",
        "type" : "rwt.widgets.Button",
        "properties" : {
          "style" : [ "PUSH" ],
          "parent" : "w3",
          "enabled" : false
        }
      } );
      TestUtil.protocolListen( "w4", { "Selection" : true } );
      var button = ObjectRegistry.getObject( "w4" );
      shell.setDefaultButton( button );
      TestUtil.flush();
      TestUtil.clearRequestLog();

      TestUtil.keyDown( shell.getElement(), "Enter", 0 );
      rwt.remote.Connection.getInstance().send();

      assertNull( TestUtil.getMessageObject().findNotifyOperation( "w4", "Selection" ) );
      button.destroy();
      shell.destroy();
    },

    testFiresParentShellChangedEvent : function() {
      var shell = this._createDefaultShell( {} );
      var parentShell = this._createDefaultShell( {} );
      var log = 0;
      shell.addEventListener( "parentShellChanged", function() {
        log++;
      } );

      shell.setParentShell( parentShell );

      assertTrue( log > 0 );
      shell.destroy();
      parentShell.destroy();
    },

    testSendBounds : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      TestUtil.protocolListen( "w2", { "Move" : true, "Resize" : true } );

      shell.setLeft( 51 );
      shell.setTop( 52 );
      shell.setWidth( 53 );
      shell.setHeight( 54 );
      TestUtil.forceInterval( shell._sendBoundsTimer );

      var message = TestUtil.getMessageObject();
      assertEquals( [ 51, 52, 53, 54 ], message.findSetProperty( "w2", "bounds" ) );
      shell.destroy();
    },

    testSendResize : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      TestUtil.protocolListen( "w2", { "Resize" : true } );

      shell.setLeft( 51 );
      shell.setTop( 52 );
      shell.setWidth( 53 );
      shell.setHeight( 54 );
      TestUtil.forceInterval( shell._sendBoundsTimer );

      var message = TestUtil.getMessageObject();
      assertEquals( [ 51, 52, 53, 54 ], message.findSetProperty( "w2", "bounds" ) );
      assertNotNull( message.findNotifyOperation( "w2", "Resize" ) );
      assertNull( message.findNotifyOperation( "w2", "Move" ) );
      shell.destroy();
    },

    testSendResize_onMaximizedShell : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      TestUtil.protocolListen( "w2", { "Resize" : true } );
      shell.setMode( "maximized" );
      TestUtil.clearRequestLog();

      rwt.widgets.base.ClientDocument.getInstance().createDispatchEvent( "windowresize" );
      TestUtil.forceInterval( rwt.remote.Connection.getInstance()._delayTimer );

      var message = TestUtil.getMessageObject();
      var width = window.innerWidth;
      var height = window.innerHeight;
      assertEquals( [ 0, 0, width, height ], message.findSetProperty( "w2", "bounds" ) );
      assertNotNull( message.findNotifyOperation( "w2", "Resize" ) );
      shell.destroy();
    },

    testSendMove : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      TestUtil.protocolListen( "w2", { "Move" : true } );

      shell.setLeft( 51 );
      shell.setTop( 52 );
      shell.setWidth( 53 );
      shell.setHeight( 54 );
      TestUtil.forceInterval( shell._sendBoundsTimer );

      var message = TestUtil.getMessageObject();
      assertEquals( [ 51, 52, 53, 54 ], message.findSetProperty( "w2", "bounds" ) );
      assertNull( message.findNotifyOperation( "w2", "Resize" ) );
      assertNotNull( message.findNotifyOperation( "w2", "Move" ) );
      shell.destroy();
    },

    testNotifyActivateDeactivate_WithListeners : function() {
      var shell = this._createWidgetTree();
      shell.setActiveChild( ObjectRegistry.getObject( "w8" ) );
      TestUtil.protocolListen( "w7", { "Activate" : true } );
      TestUtil.protocolListen( "w8", { "Deactivate" : true } );
      TestUtil.clearRequestLog();

      shell.setActiveChild( ObjectRegistry.getObject( "w7" ) );

      var messages = TestUtil.getMessages();
      assertEquals( 2, messages.length );
      assertNull( messages[ 0 ].findSetOperation( "w3", "activeControl" ) );
      assertNotNull( messages[ 0 ].findNotifyOperation( "w8", "Deactivate" ) );
      assertEquals( "w7", messages[ 1 ].findSetProperty( "w3", "activeControl" ) );
      assertNotNull( messages[ 1 ].findNotifyOperation( "w7", "Activate" ) );
      shell.destroy();
    },

    testNotifyActivateDeactivate_WithoutListeners : function() {
      var shell = this._createWidgetTree();
      shell.setActiveChild( ObjectRegistry.getObject( "w8" ) );
      TestUtil.clearRequestLog();

      shell.setActiveChild( ObjectRegistry.getObject( "w7" ) );
      rwt.remote.Connection.getInstance().send();

      var messages = TestUtil.getMessages();
      assertEquals( 1, messages.length );
      assertNull( messages[ 0 ].findNotifyOperation( "w8", "Deactivate" ) );
      assertEquals( "w7", messages[ 0 ].findSetProperty( "w3", "activeControl" ) );
      assertNull( messages[ 0 ].findNotifyOperation( "w7", "Activate" ) );
      shell.destroy();
    },

    testNotifyActivateDeactivate_WithActivateListenerOnSameParent : function() {
      var shell = this._createWidgetTree();
      shell.setActiveChild( ObjectRegistry.getObject( "w8" ) );
      TestUtil.protocolListen( "w6", { "Activate" : true } );
      TestUtil.clearRequestLog();

      shell.setActiveChild( ObjectRegistry.getObject( "w9" ) );
      rwt.remote.Connection.getInstance().send();

      var messages = TestUtil.getMessages();
      assertEquals( 1, messages.length );
      assertNull( messages[ 0 ].findNotifyOperation( "w8", "Deactivate" ) );
      assertEquals( "w9", messages[ 0 ].findSetProperty( "w3", "activeControl" ) );
      assertNull( messages[ 0 ].findNotifyOperation( "w6", "Activate" ) );
      shell.destroy();
    },

    testNotifyActivateDeactivate_WithDeactivateListenerOnSameParent : function() {
      var shell = this._createWidgetTree();
      shell.setActiveChild( ObjectRegistry.getObject( "w8" ) );
      TestUtil.protocolListen( "w6", { "Deactivate" : true } );
      TestUtil.clearRequestLog();

      shell.setActiveChild( ObjectRegistry.getObject( "w9" ) );
      rwt.remote.Connection.getInstance().send();

      var messages = TestUtil.getMessages();
      assertEquals( 1, messages.length );
      assertNull( messages[ 0 ].findNotifyOperation( "w8", "Deactivate" ) );
      assertEquals( "w9", messages[ 0 ].findSetProperty( "w3", "activeControl" ) );
      assertNull( messages[ 0 ].findNotifyOperation( "w6", "Deactivate" ) );
      shell.destroy();
    },

    testNotifyActivateDeactivate_WithActivateListenerOnDifferentParent : function() {
      var shell = this._createWidgetTree();
      shell.setActiveChild( ObjectRegistry.getObject( "w7" ) );
      TestUtil.protocolListen( "w6", { "Activate" : true } );
      TestUtil.clearRequestLog();

      shell.setActiveChild( ObjectRegistry.getObject( "w9" ) );

      var messages = TestUtil.getMessages();
      assertEquals( 1, messages.length );
      assertEquals( "w9", messages[ 0 ].findSetProperty( "w3", "activeControl" ) );
      assertNotNull( messages[ 0 ].findNotifyOperation( "w6", "Activate" ) );
      shell.destroy();
    },

    testNotifyActivateDeactivate_WithDectivateListenerOnDifferentParent : function() {
      var shell = this._createWidgetTree();
      shell.setActiveChild( ObjectRegistry.getObject( "w7" ) );
      TestUtil.protocolListen( "w5", { "Deactivate" : true } );
      TestUtil.clearRequestLog();

      shell.setActiveChild( ObjectRegistry.getObject( "w9" ) );
      rwt.remote.Connection.getInstance().send();

      var messages = TestUtil.getMessages();
      assertEquals( 2, messages.length );
      assertNotNull( messages[ 0 ].findNotifyOperation( "w5", "Deactivate" ) );
      assertEquals( "w9", messages[ 1 ].findSetProperty( "w3", "activeControl" ) );
      shell.destroy();
    },

    testNotifyShellActivate : function() {
      rwt.remote.EventUtil.setSuspended( true );
      var shell = new rwt.widgets.Shell( {} );
      var handler = rwt.remote.HandlerRegistry.getHandler( "rwt.widgets.Shell" );
      rwt.remote.ObjectRegistry.add( "w222", shell, handler );
      shell.initialize();
      shell.open();
      TestUtil.fakeListener( shell, "Activate", true );
      rwt.remote.EventUtil.setSuspended( false );

      shell.setActive( true );

      assertNotNull( TestUtil.getMessageObject().findNotifyOperation( "w222", "Activate" ) );
      shell.destroy();
    },

    testNotifyShellClose : function() {
      rwt.remote.EventUtil.setSuspended( true );
      var shell = new rwt.widgets.Shell( {} );
      var handler = rwt.remote.HandlerRegistry.getHandler( "rwt.widgets.Shell" );
      rwt.remote.ObjectRegistry.add( "w222", shell, handler );
      shell.initialize();
      shell.open();
      TestUtil.fakeListener( shell, "Close", true );
      rwt.remote.EventUtil.setSuspended( false );

      shell.close();

      assertNotNull( TestUtil.getMessageObject().findNotifyOperation( "w222", "Close" ) );
      shell.destroy();
    },

    testApplicationModal : function() {
      var shell = new rwt.widgets.Shell( {} );
      shell.addState( "rwt_APPLICATION_MODAL" );
      shell.initialize();

      assertTrue( shell._appModal );
      shell.destroy();
    },

    testPrimaryModal : function() {
      var shell = new rwt.widgets.Shell( {} );
      shell.addState( "rwt_PRIMARY_MODAL" );
      shell.initialize();

      assertTrue( shell._appModal );
      shell.destroy();
    },

    testShellIsFocusRoot : function() {
      var shell = new rwt.widgets.Shell( {} );

      assertTrue( shell.isFocusRoot() );
      assertIdentical( shell, shell.getFocusRoot() );
      shell.destroy();
    },

    testSetDirection_mirrorsChildControlPosition : function() {
      var shell = this._createDefaultShell( {} );
      var child = new rwt.widgets.base.Terminator();
      child.setParent( shell );
      child.setSpace( 1, 2, 3, 4 );

      shell.setDirection( "rtl" );
      TestUtil.flush();

      assertEquals( "1px", child.getElement().style.right );
      shell.destroy();
    },

    testSetDirection_setsCaptionBarReverseChildrenOrder : function() {
      var shell = new rwt.widgets.Shell( {} );
      shell.addToDocument();

      shell.setDirection( "rtl" );
      TestUtil.flush();

      assertTrue( shell.getCaptionBar().getReverseChildrenOrder() );
      shell.destroy();
    },

    testSetDirection_setsCaptionBarHorizontalChildrenAlign : function() {
      var shell = new rwt.widgets.Shell( {} );
      shell.addToDocument();

      shell.setDirection( "rtl" );
      TestUtil.flush();

      assertEquals( "right", shell.getCaptionBar().getHorizontalChildrenAlign() );
      shell.destroy();
    },

    testDisposeShell : function() {
      var shell = new rwt.widgets.Shell( {} );
      var timer = shell._sendBoundsTimer;

      shell.destroy();
      TestUtil.flush();

      assertTrue( shell.isDisposed() );
      assertTrue( timer.isDisposed() );
    },
      /////////
    // Helper

    _createDefaultShell : function( styles, noFlush ) {
      TestUtil.fakeResponse( true );
      var shell = new rwt.widgets.Shell( styles );
      shell.initialize();
      shell.open();
      shell.setActive( true );
      shell.setSpace( 50, 300, 50, 200 );
      shell.setVisibility( true );
      TestUtil.fakeResponse( false );
      if( !noFlush ) {
        TestUtil.flush();
      }
      return shell;
    },

    _createWidgetTree : function() {
      var shell = TestUtil.createShellByProtocol( "w3" );
      MessageProcessor.processOperation( {
        "target" : "w4",
        "action" : "create",
        "type" : "rwt.widgets.Composite",
        "properties" : {
          "style" : [ "BORDER" ],
          "parent" : "w3"
        }
      } );
      MessageProcessor.processOperation( {
        "target" : "w5",
        "action" : "create",
        "type" : "rwt.widgets.Composite",
        "properties" : {
          "style" : [ "BORDER" ],
          "parent" : "w4"
        }
      } );
      MessageProcessor.processOperation( {
        "target" : "w7",
        "action" : "create",
        "type" : "rwt.widgets.Button",
        "properties" : {
          "style" : [ "PUSH" ],
          "parent" : "w5"
        }
      } );
      MessageProcessor.processOperation( {
        "target" : "w6",
        "action" : "create",
        "type" : "rwt.widgets.Composite",
        "properties" : {
          "style" : [ "BORDER" ],
          "parent" : "w4"
        }
      } );
      MessageProcessor.processOperation( {
        "target" : "w8",
        "action" : "create",
        "type" : "rwt.widgets.Button",
        "properties" : {
          "style" : [ "PUSH" ],
          "parent" : "w6"
        }
      } );
      MessageProcessor.processOperation( {
        "target" : "w9",
        "action" : "create",
        "type" : "rwt.widgets.Button",
        "properties" : {
          "style" : [ "PUSH" ],
          "parent" : "w6"
        }
      } );
      TestUtil.flush();
      return shell;
    }

  }

} );

}() );
