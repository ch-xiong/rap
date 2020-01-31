/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.util.Util;
import org.eclipse.ui.tests.harness.util.TweakletCheckTest;
//import org.eclipse.ui.tests.markers.MarkersViewColumnSizeTest;
//import org.eclipse.ui.tests.statushandlers.StatusHandlerConfigurationSuite;
//import org.eclipse.ui.tests.statushandlers.StatusHandlingConfigurationTest;

/**
 * @since 3.1
 */
public class SessionTests extends TestSuite {

	/**
	 * @return
	 */
	public static Test suite() {
		return new SessionTests();
	}

	/**
	 *
	 */
	public SessionTests() {
		addTweakletCheck();
		addHandlerStateTests();
		addIntroTests();
//		addEditorTests();
		addViewStateTests();
		addThemeTests();
//		addStatusHandlingTests();
//		addRestoredSessionTest();
		addWindowlessSessionTest();
	}

	/**
	 *
	 */
	private void addWindowlessSessionTest() {
		// Windowless apps are available only on Cocoa
		if(Util.isCocoa()) {
			Map arguments = new HashMap(2);
			arguments.put("product", null);
			arguments.put("testApplication", "org.eclipse.ui.tests.windowLessRcpApplication");
			addTest(new WorkbenchSessionTest("windowlessSessionTests",WindowlessSessionTest.class, arguments));
		}
	}

//	/**
//	 *
//	 */
//	private void addStatusHandlingTests() {
//		//actually we do not care which workspace is used
//		addTest(new StatusHandlerConfigurationSuite("themeSessionTests",
//				StatusHandlingConfigurationTest.class));
//	}

	/**
	 *
	 */
	private void addTweakletCheck() {
		addTest(new TweakletCheckTest());
	}

	/**
	 *
	 */
	private void addThemeTests() {
		addTest(new WorkbenchSessionTest("themeSessionTests",
				ThemeStateTest.class));

	}

//	private void addRestoredSessionTest() {
//		Map arguments = new HashMap(2);
//		arguments.put("product", null);
//		arguments.put("testApplication", "org.eclipse.ui.tests.rcpSessionApplication");
//		addTest(new WorkbenchSessionTest("introSessionTests",RestoreSessionTest.class, arguments));
//	}

//	/**
//	 * Add editor tests that involve starting and stopping sessions.
//	 */
//	private void addEditorTests() {
//		addTest(new WorkbenchSessionTest("editorSessionTests",
//				Bug95357Test.class));
//		addTest(new WorkbenchSessionTest("editorSessionTests",
//				EditorWithStateTest.class));
//		addTest(new WorkbenchSessionTest("editorSessionTests",
//				ArbitraryPropertiesEditorTest.class));
//	}

	/**
	 * Adds tests related to command and handler state.
	 *
	 * @since 3.2
	 */
	private void addHandlerStateTests() {
		addTest(new WorkbenchSessionTest("editorSessionTests",
				HandlerStateTest.class));
	}

	/**
	 * Adds intro related session tests.
	 */
	private void addIntroTests() {
		addTest(new WorkbenchSessionTest("introSessionTests",
				IntroSessionTests.class));
	}

	/**
	 * Add a view state test that involves state from one session to the other.
	 *
	 * BTW: the <b>editorSessionTests</b> is the zip file to grab the default
	 * workspace for these particular session tests.
	 */
	private void addViewStateTests() {
		addTest(new WorkbenchSessionTest("editorSessionTests",
				Bug98800Test.class));
		addTest(new WorkbenchSessionTest("editorSessionTests",
				Bug108033Test.class));
		addTest(new WorkbenchSessionTest("editorSessionTests",
				ArbitraryPropertiesViewTest.class));
		addTest(new WorkbenchSessionTest("editorSessionTests",
				NonRestorableViewTest.class));
		addTest(new WorkbenchSessionTest("editorSessionTests",
				NonRestorablePropertySheetTest.class));
//		addTest(new WorkbenchSessionTest("editorSessionTests",
//				MarkersViewColumnSizeTest.class));
	}
}
