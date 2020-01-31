/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.helpers;

import java.util.ArrayList;

import junit.framework.Assert;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.FastViewBar;
import org.eclipse.ui.internal.FastViewBarContextMenuContribution;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.SlavePageService;
import org.eclipse.ui.internal.SlavePartService;
import org.eclipse.ui.internal.SlaveSelectionService;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.tests.PerspectiveState;

/**
 * @since 3.5
 * 
 */
public class TestFacadeImpl extends TestFacade {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#assertActionSetFound(org.eclipse
	 * .ui.IWorkbenchPage, java.lang.String)
	 */
	public void assertActionSetId(IWorkbenchPage page, String id,
			boolean condition) {
		IActionSetDescriptor[] sets = ((WorkbenchPage) page).getActionSets();
		boolean found = false;
		for (int i = 0; i < sets.length && !found; i++) {
			if (id.equals(sets[i].getId())) {
				found = true;
			}
		}
		Assert.assertEquals("Failed for " + id, condition, found);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#getActionSetCount(org.eclipse
	 * .ui.IWorkbenchPage)
	 */
	public int getActionSetCount(IWorkbenchPage page) {
		return ((WorkbenchPage) page).getActionSets().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.helpers.TestFacade#addFastView(org.eclipse.ui.
	 * IWorkbenchPage, org.eclipse.ui.IViewReference)
	 */
	public void addFastView(IWorkbenchPage page, IViewReference ref) {
		((WorkbenchPage) page).addFastView(ref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.helpers.TestFacade#saveState(org.eclipse.ui.
	 * IWorkbenchPage, org.eclipse.ui.IMemento)
	 */
	public IStatus saveState(IWorkbenchPage page, IMemento memento) {
		return ((WorkbenchPage) page).saveState(memento);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.helpers.TestFacade#getFastViews(org.eclipse.ui.
	 * IWorkbenchPage)
	 */
	public IViewReference[] getFastViews(IWorkbenchPage page) {
		return ((WorkbenchPage) page).getFastViews();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#getPerspectivePartIds(org.eclipse
	 * .ui.IWorkbenchPage, java.lang.String)
	 */
	public ArrayList getPerspectivePartIds(IWorkbenchPage page, String folderId) {
		PerspectiveState state = new PerspectiveState(page);
		return state.getPartIds(folderId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.helpers.TestFacade#isFastView(org.eclipse.ui.
	 * IWorkbenchPage, org.eclipse.ui.IViewReference)
	 */
	public boolean isFastView(IWorkbenchPage page, IViewReference ref) {
		return ((WorkbenchPage) page).isFastView(ref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#testSetAutomatedResponse(int)
	 */
	public void saveableHelperSetAutomatedResponse(int response) {
		SaveableHelper.testSetAutomatedResponse(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isSlavePageService(org.eclipse
	 * .ui.IPageService)
	 */
	public void isSlavePageService(IPageService slaveService) {
		Assert.assertTrue(slaveService instanceof SlavePageService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#getFVBContribution(org.eclipse
	 * .ui.IWorkbenchPage)
	 */
	public IContributionItem getFVBContribution(IWorkbenchPage page) {
		FastViewBar fastViewBar = ((WorkbenchWindow) page.getWorkbenchWindow())
				.getFastViewBar();
		return fastViewBar.testContextMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#setFVBTarget(org.eclipse.jface
	 * .action.IContributionItem, org.eclipse.ui.IViewReference)
	 */
	public void setFVBTarget(IContributionItem menuContribution,
			IViewReference viewRef) {
		((FastViewBarContextMenuContribution) menuContribution)
				.setTarget(viewRef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isViewPaneVisible(org.eclipse
	 * .ui.IViewReference)
	 */
	public boolean isViewPaneVisible(IViewReference viewRef) {
		return ((PartSite) viewRef.getPart(true).getSite()).getPane()
				.isVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isViewToolbarVisible(org.eclipse
	 * .ui.IViewReference)
	 */
	public boolean isViewToolbarVisible(IViewReference viewRef) {
		Control toolBar = ((PartSite) viewRef.getPart(true).getSite())
				.getPane().getToolBar();
		Assert.assertNotNull("This view must have a toolbar", toolBar);
		return toolBar.isVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isSlavePartService(org.eclipse
	 * .ui.IPartService)
	 */
	public boolean isSlavePartService(IPartService slaveService) {
		return slaveService instanceof SlavePartService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isSlaveSelectionService(org.eclipse
	 * .ui.ISelectionService)
	 */
	public boolean isSlaveSelectionService(ISelectionService slaveService) {
		return slaveService instanceof SlaveSelectionService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isClosableInPerspective(org.eclipse
	 * .ui.IWorkbenchPartReference)
	 */
	public boolean isClosableInPerspective(IViewReference ref) {
		return ((WorkbenchPage) ref.getPage()).getActivePerspective()
				.isCloseable(ref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#isMoveableInPerspective(org.eclipse
	 * .ui.IWorkbenchPartReference)
	 */
	public boolean isMoveableInPerspective(IViewReference ref) {
		return ((WorkbenchPage) ref.getPage()).getActivePerspective()
				.isMoveable(ref);
	}
}
