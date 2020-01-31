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

import static org.eclipse.rap.rwt.testfixture.internal.SerializationTestUtil.serializeAndDeserialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RequestCounter_Test {

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testGetInstance_returnsSame() {
    RequestCounter result1 = RequestCounter.getInstance();
    RequestCounter result2 = RequestCounter.getInstance();

    assertSame( result1, result2 );
  }

  @Test
  public void testGetInstance_returnsNotSameForDifferentUISessions() {
    RequestCounter result1 = RequestCounter.getInstance();

    createNewSession();
    RequestCounter result2 = RequestCounter.getInstance();

    assertNotSame( result1, result2 );
  }

  @Test
  public void testInitialValueIsZero() {
    int requestId = RequestCounter.getInstance().currentRequestId();

    assertEquals( 0, requestId );
  }

  @Test
  public void testNextRequestId_returnsIncrementedValue() {
    int requestId = RequestCounter.getInstance().nextRequestId();

    assertEquals( 1, requestId );
  }

  @Test
  public void testNextCurrentRequestId_doesNotModifyValue() {
    RequestCounter.getInstance().currentRequestId();
    int requestId = RequestCounter.getInstance().currentRequestId();

    assertEquals( 0, requestId );
  }

  @Test
  public void testSerialization() throws Exception {
    RequestCounter instance = RequestCounter.getInstance();
    instance.nextRequestId(); // ensure counter differs from zero
    int currentRequestId = instance.nextRequestId();

    RequestCounter deserialized = serializeAndDeserialize( instance );

    assertEquals( currentRequestId, deserialized.currentRequestId() );
  }

  private static void createNewSession() {
    Fixture.disposeOfServiceContext();
    Fixture.createServiceContext();
  }

}
