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
package org.eclipse.rap.rwt.internal.serverpush;

import static org.eclipse.rap.rwt.testfixture.internal.SerializationTestUtil.serializeAndDeserialize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class CallBackActivationTracker_Test {

  private ServerPushActivationTracker serverPushActivationTracker;

  @Before
  public void setUp() {
    serverPushActivationTracker = new ServerPushActivationTracker();
  }

  @Test
  public void testInitialActiveState() {
    assertFalse( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testActivate() {
    serverPushActivationTracker.activate( "x" );
    assertTrue( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testActivateTwice() {
    String id = "id";
    serverPushActivationTracker.activate( id );
    serverPushActivationTracker.activate( id );
    assertTrue( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testIsActiveAfterActivate() {
    serverPushActivationTracker.activate( "x" );
    assertTrue( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testDeactivateWithNonExistingId() {
    serverPushActivationTracker.deactivate( "does.not.exist" );
    assertFalse( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testDeactivateExistingActivation() {
    String id = "id";
    serverPushActivationTracker.activate( id );
    serverPushActivationTracker.deactivate( id );
    assertFalse( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testDeactivateSameActivationTwice() {
    String id = "id";
    serverPushActivationTracker.activate( id );
    serverPushActivationTracker.deactivate( id );
    serverPushActivationTracker.deactivate( id );
    assertFalse( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testActivateMultipleTimes() {
    serverPushActivationTracker.activate( "id1" );
    serverPushActivationTracker.activate( "id2" );
    assertTrue( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testDeactivateOneFromMultipleActivations() {
    serverPushActivationTracker.activate( "id1" );
    serverPushActivationTracker.activate( "id2" );
    serverPushActivationTracker.deactivate( "id2" );
    assertTrue( serverPushActivationTracker.isActive() );
  }

  @Test
  public void testSerializeWhenEmpty() throws Exception {
    ServerPushActivationTracker deserializedTracker
      = serializeAndDeserialize( serverPushActivationTracker );

    assertTrue( serverPushActivationTracker.isActive() == deserializedTracker.isActive() );
  }

  @Test
  public void testSerializeWhenHoldingIds() throws Exception {
    String id = "id";
    serverPushActivationTracker.activate( id );

    ServerPushActivationTracker deserializedTracker
      = serializeAndDeserialize( serverPushActivationTracker );

    assertTrue( serverPushActivationTracker.isActive() == deserializedTracker.isActive() );
    deserializedTracker.deactivate( id );
    assertFalse( deserializedTracker.isActive() );
  }

}
