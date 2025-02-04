/*************************************************************************
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.wizards.NewServiceWizard;

/**
 * Pulldown action for the uno types. The wizard has to be defined as follows:
 *
 * <pre>
 *     &lt;wizard
 *           canFinishEarly="false"
 *           category="org.libreoffice.ide.eclipse.core"
 *           hasPages="true"
 *           icon="icons/newservice.gif"
 *           id="org.libreoffice.ide.eclipse.core.newservice"
 *           name="%wizards.service"
 *           project="false">
 *        &lt;class class="org.libreoffice.ide.eclipse.core.wizards.NewServiceWizard"&gt;
 *            &lt;parameter name="unotype" value="true"/&gt;
 *        &lt;/class&gt;
 *        &lt;description&gt;
 *           Creates a new 'new-styled' UNO service. A service will export one interface and define some constructors.
 *        &lt;/description&gt;
 *     &lt;/wizard&gt;
 * </pre>
 */
public class UnoTypePulldownAction extends AbstractPulldownAction {

    /**
     * UNO type wizard pulldown action.
     */
    public UnoTypePulldownAction() {
        super("unotype"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IAction action) {
        openWizard(new NewServiceWizard());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidSelection(IStructuredSelection pSelection) {

        boolean isValid = false;
        boolean projectExist = false;
        if (!pSelection.isEmpty() && pSelection.getFirstElement() instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) pSelection.getFirstElement();
            projectExist = true;
            if (adaptable.getAdapter(IResource.class) != null) {
                IResource res = adaptable.getAdapter(IResource.class);
                IProject prj = res.getProject();
                if (null != ProjectsManager.getProject(prj.getName())) {
                    isValid = true;
                }
            }
        } else {
            String msg = "You need to create/select a LibreOffice project first to use the selection";
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", msg);
        }
        if (!isValid && projectExist) {
            String msg = "The Selection only works with LibreOffice projects";
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", msg);
        }

        return isValid;
    }
}
