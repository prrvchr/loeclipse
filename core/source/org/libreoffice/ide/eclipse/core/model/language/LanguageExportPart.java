/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.model.language;

import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.wizards.pages.ManifestExportPage;

/**
 * Abstract class for the language specific controls part in the OXT export wizard.
 */
public abstract class LanguageExportPart {

    private ManifestExportPage mPage;

    /**
     * Create the controls in the part.
     *
     * @param parent
     *            the parent composite where to create the controls
     */
    public abstract void createControls(Composite parent);

    /**
     * Cleans the controls.
     */
    public abstract void dispose();

    /**
     * Run the export actions in a separate thread.
     *
     * <strong>Note that the controls might be disposed when this methods is called.</strong>
     *
     */
    public abstract void doFinish();

    /**
     * @param page
     *            the manifest page containing this part.
     */
    public void setPage(ManifestExportPage page) {
        mPage = page;
    }

    /**
     * @return the page containing this UI part.
     */
    protected ManifestExportPage getPage() {
        return mPage;
    }
}