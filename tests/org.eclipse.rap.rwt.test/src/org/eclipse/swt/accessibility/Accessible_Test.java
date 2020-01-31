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
package org.eclipse.swt.accessibility;

import static org.eclipse.rap.rwt.testfixture.internal.SerializationTestUtil.serializeAndDeserialize;
import static org.junit.Assert.assertNotNull;

import java.io.NotSerializableException;

import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class Accessible_Test {

  @Before
  public void setUp() {
    Fixture.setUp();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testIsNotSerializable() throws Exception {
    Display display = new Display();
    Control control = new Shell( display );
    Accessible accessible = new Accessible( control );

    try {
      serializeAndDeserialize( accessible );
    } catch( NotSerializableException expected ) {
    }
  }

  @Test
  public void testAccessibleAfterDeserialization() throws Exception {
    Display display = new Display();
    Control control = new Shell( display );

    Control deserializedControl = serializeAndDeserialize( control );

    assertNotNull( deserializedControl.getAccessible() );
  }

}
