/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.ui.tests.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.service.IServiceHandler;
import org.eclipse.rap.ui.tests.ServiceHandlerExtensionTest;


public class ServiceHandler2 implements IServiceHandler {

  public void service( HttpServletRequest request, HttpServletResponse response ) {
    ServiceHandlerExtensionTest.log = this.getClass().getName();
  }
}
