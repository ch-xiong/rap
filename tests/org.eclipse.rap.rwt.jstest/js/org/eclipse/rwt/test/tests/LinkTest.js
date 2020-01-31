/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
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
var ObjectManager = rwt.remote.ObjectRegistry;
var Processor = rwt.remote.MessageProcessor;

rwt.qx.Class.define( "org.eclipse.rwt.test.tests.LinkTest", {

  extend : rwt.qx.Object,

  members : {

    testCreateLinkByProtocol : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      Processor.processOperation( {
        "target" : "w3",
        "action" : "create",
        "type" : "rwt.widgets.Link",
        "properties" : {
          "style" : [],
          "parent" : "w2"
        }
      } );
      var widget = ObjectManager.getObject( "w3" );
      assertTrue( widget instanceof rwt.widgets.Link );
      assertIdentical( shell, widget.getParent() );
      assertTrue( widget.getUserData( "isControl") );
      assertEquals( "link", widget.getAppearance() );
      assertEquals( "", widget._text );
      assertEquals( 0, widget._linksCount );
      shell.destroy();
      widget.destroy();
    },

    testSetTextByProtocol : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      Processor.processOperation( {
        "target" : "w3",
        "action" : "create",
        "type" : "rwt.widgets.Link",
        "properties" : {
          "style" : [],
          "parent" : "w2",
          "text" : [ [ "text1 ", null ], [ "link1", 0 ], [ " text2 ", null ], [ "link2", 1 ] ]
        }
      } );
      var widget = ObjectManager.getObject( "w3" );
      var expected
        = "text1 "
        + "<span tabIndex=\"1\" id=\"w3#0\">link1</span>"
        + " text2 "
        + "<span tabIndex=\"1\" id=\"w3#1\">link2</span>";
      assertEquals( expected, widget._text );
      assertEquals( 2, widget._linksCount );
      shell.destroy();
      widget.destroy();
    },

    testSetTextWithLineBreaksByProtocol : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      Processor.processOperation( {
        "target" : "w3",
        "action" : "create",
        "type" : "rwt.widgets.Link",
        "properties" : {
          "style" : [],
          "parent" : "w2",
          "text" : [ [ "text\ntext ", null ], [ "link\nlink", 0 ] ]
        }
      } );
      var widget = ObjectManager.getObject( "w3" );
      var expected
        = "text<br/>text "
        + "<span tabIndex=\"1\" id=\"w3#0\">"
        + "link<br/>link"
        + "</span>";
      assertEquals( expected, widget._text );
      assertEquals( 1, widget._linksCount );
      shell.destroy();
      widget.destroy();
    },

    testSetTextEscapedByProtocol : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      Processor.processOperation( {
        "target" : "w3",
        "action" : "create",
        "type" : "rwt.widgets.Link",
        "properties" : {
          "style" : [],
          "parent" : "w2",
          "text" : [ [ "foo && <> \" bar ", null ], [ "foo && <> \" bar", 0 ] ]
        }
      } );
      var widget = ObjectManager.getObject( "w3" );
      var expected
        = "foo &amp;&amp; &lt;&gt; &quot; bar "
        + "<span tabIndex=\"1\" id=\"w3#0\">"
        + "foo &amp;&amp; &lt;&gt; &quot; bar"
        + "</span>";
      assertEquals( expected, widget._text );
      shell.destroy();
      widget.destroy();
    },

    testSetSelectionListenerByProtocol : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      Processor.processOperation( {
        "target" : "w3",
        "action" : "create",
        "type" : "rwt.widgets.Link",
        "properties" : {
          "style" : [],
          "parent" : "w2"
        }
      } );
      var widget = ObjectManager.getObject( "w3" );

      TestUtil.protocolListen( "w3", { "Selection" : true } );

      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( widget );
      assertTrue( remoteObject.isListening( "Selection" ) );
      shell.destroy();
      widget.destroy();
    },

    testSendSelectionEvent : function() {
      var shell = TestUtil.createShellByProtocol( "w2" );
      Processor.processOperation( {
        "target" : "w3",
        "action" : "create",
        "type" : "rwt.widgets.Link",
        "properties" : {
          "style" : [],
          "parent" : "w2",
          "text" : [ [ "text1 ", null ], [ "link1", 0 ], [ " text2 ", null ], [ "link2", 1 ] ]
        }
      } );
      TestUtil.protocolListen( "w3", { "Selection" : true } );
      TestUtil.flush();
      var widget = ObjectManager.getObject( "w3" );

      //TestUtil.clickDOM( widget.getElement().lastChild );
      widget._sendChanges( 1 ); // Can not use fixture in this case

      assertEquals( 1, TestUtil.getRequestsSend() );
      var message = TestUtil.getLastMessage();
      assertEquals( 1, message.findNotifyProperty( "w3", "Selection", "index" ) );
      shell.destroy();
    },

    testLinkStyleProperties : function() {
      var link = new rwt.widgets.Link();
      link.addToDocument();
      link.addLink( "foo", 0 );

      link.applyText();
      TestUtil.flush();

      var style = link._getHyperlinkElements()[ 0 ].style;
      assertEquals( "underline", style.textDecoration );
      assertEquals( "pointer", style.cursor );
      assertEquals( [0, 0, 255], rwt.util.Colors.stringToRgb( style.color ) );
      link.destroy();
    },

    // bug 436494
    testHyperlinkElementDoesNotChangeAfterAppearsAgain : function() {
      var link = new rwt.widgets.Link();
      link.addToDocument();
      link.addLink( "foo", 0 );
      link.applyText();
      TestUtil.flush();
      var lastChild = link._getTargetNode().lastChild;

      link.hide();
      link.show();

      assertTrue( lastChild === link._getTargetNode().lastChild );
      link.destroy();
    },

    testTextDecorationOnHover : function() {
      TestUtil.fakeAppearance( "link-hyperlink", {
        "style" : function( states ) {
          return {
            textDecoration : states.over ? "underline" : "none"
          };
        }
      } );
      var link = new rwt.widgets.Link();
      link.addToDocument();
      link.addLink( "foo", 0 );
      link.applyText();
      TestUtil.flush();
      var hyperlink = link._getHyperlinkElements()[ 0 ];

      // TestUtil#fakeMouseEventDOM does not work in this case.
      // Call _onMouseOver/Out directly by faking the event target
      link._onMouseOver( { target : hyperlink } );

      assertEquals( "underline", hyperlink.style.textDecoration );

      link._onMouseOut( { target : hyperlink } );

      assertEquals( "none", hyperlink.style.textDecoration );
      link.destroy();
    }

  }

} );

}() );
