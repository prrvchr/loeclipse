/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.editors.description;

import org.eclipse.ui.forms.editor.FormEditor;
import org.libreoffice.ide.eclipse.core.editors.utils.SourcePage;

/**
 * The source page for the description.xml file.
 */
public class DescriptionSourcePage extends SourcePage {

    /**
     * Description source editor page constructor.
     *
     * @param formEditor
     *            the editor hosting the page.
     * @param pageId
     *            the page identifier
     * @param title
     *            the page title
     */
    public DescriptionSourcePage(FormEditor formEditor, String pageId, String title) {
        super(formEditor, pageId, title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canLeaveThePage() {
        DescriptionEditor editor = (DescriptionEditor) getEditor();
        editor.loadDescFromSource();

        return super.canLeaveThePage();
    }
}
