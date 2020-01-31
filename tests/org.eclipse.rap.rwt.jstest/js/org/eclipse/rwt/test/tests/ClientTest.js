/*******************************************************************************
 * Copyright (c) 2011, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function() {

var Client = rwt.client.Client;

rwt.qx.Class.define( "org.eclipse.rwt.test.tests.ClientTest", {

  extend : rwt.qx.Object,

  members : {

    testEngine : function() {
      var engines = [ "gecko", "blink", "webkit", "trident" ];
      var currentEngine = Client.getEngine();
      assertTrue( engines.indexOf( currentEngine ) != -1 );
      var isEngine = 0;
      isEngine = Client.isGecko() ? isEngine + 1 : isEngine;
      isEngine = Client.isBlink() ? isEngine + 1 : isEngine;
      isEngine = Client.isWebkit() ? isEngine + 1 : isEngine;
      isEngine = Client.isTrident() ? isEngine + 1 : isEngine;
      assertEquals( 1, isEngine );
      assertEquals( "string", typeof Client.getBrowser() );
      // NOTE: No check for specific browsers since there many clones that are
      // technically almost identical to the major ones and should run RAP.
    },

    testVersions : function() {
      assertEquals( "number", typeof Client.getVersion() );
      assertEquals( "number", typeof Client.getMajor() );
      assertEquals( "number", typeof Client.getMinor() );
      assertEquals( "number", typeof Client.getRevision() );
      assertEquals( "number", typeof Client.getBuild() );
    },

    testPlatform : function() {
      var platforms = [ "win", "mac", "unix", "ios", "android" ];
      var currentPlatform = Client.getPlatform();
      assertTrue( platforms.indexOf( currentPlatform ) != -1 );
    },

    testSupportsFileDrop : function() {
      assertTrue( typeof rwt.client.Client.supportsFileDrop() === "boolean" );
    },

    testLocale : function() {
      assertEquals( "string", typeof Client.getLocale() );
      assertEquals( "string", typeof Client.getLanguage() );
      assertEquals( "string", typeof Client.getTerritory() );
      assertEquals( "string", typeof Client.getDefaultLocale() );
      assertEquals( "boolean", typeof Client.usesDefaultLocale() );
    },

    testBoxSizingAttributes : function() {
      var attr = Client.getEngineBoxSizingAttributes();
      assertTrue( attr instanceof Array );
      assertEquals( "string", typeof attr[ 0 ] );
    },

    testMobile : function() {
      var msafari = Client.isMobileSafari();
      var androidb = Client.isAndroidBrowser();
      assertEquals( "boolean", typeof msafari );
      assertEquals( "boolean", typeof androidb );
      assertTrue( !( msafari && androidb ) );
      // NOTE: Since the android browser indentifies itself as safari,
      //       there is a certain risk of confusing the two.
    },

    testGetBasePath : function() {
      var compute = Client._computeBasePath;
      assertEquals( compute( document.location.href ), Client.getBasePath() );
      assertEquals( "http://eclipse.org/", compute( "http://eclipse.org/" ) );
      assertEquals( "http://eclipse.org/", compute( "http://eclipse.org/foo" ) );
      assertEquals( "http://eclipse.org/foo/", compute( "http://eclipse.org/foo/" ) );
      assertEquals( "http://eclipse.org/foo/", compute( "http://eclipse.org/foo/" ) );
      assertEquals( "http://eclipse.org/foo/", compute( "http://eclipse.org/foo/?a=b/b" ) );
      assertEquals( "http://eclipse.org/foo/", compute( "http://eclipse.org/foo/#b/b" ) );
      assertEquals( "http://eclipse.org/foo/", compute( "http://eclipse.org/foo/?a=b/b#c/c" ) );
    }

  }

} );

}() );
