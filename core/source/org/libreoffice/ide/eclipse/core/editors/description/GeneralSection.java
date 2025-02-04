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

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.editors.utils.LocalizedSection;
import org.libreoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;


public class GeneralSection extends LocalizedSection<DescriptionModel> {

    private static final int LAYOUT_COLS = 3;

    private IProject mProject;

    private Text mNameTxt;
    private Text mIdTxt;
    private Text mVersionTxt;
    private Text mDescriptionTxt;
    private Button mDescriptionBtn;
    private Text mIconText;
    private Button mIconButton;
    private Text mIconHCText;
    private Button mIconHCButton;

    /**
     * @param parent
     *            the parent composite where to add the section
     * @param page
     *            the parent page
     * @param project
     *            the project containing the description.xml file
     */
    public GeneralSection(Composite parent, DescriptionFormPage page, IProject project) {
        super(parent, page, ExpandableComposite.TITLE_BAR);
        getSection().setText(Messages.getString("GeneralSection.Title")); //$NON-NLS-1$

        mProject = project;
        setModel(page.getModel());
    }

    /**
     * Loads the values from the model into the controls.
     */
    @Override
    public void loadData() {
        getModel().setSuspendEvent(true);
        if (!getModel().getDisplayNames().isEmpty()) {
            mNameTxt.setText(getModel().getDisplayNames().get(mCurrentLocale));
        }
        mIdTxt.setText(getModel().getId());
        mVersionTxt.setText(getModel().getVersion());
        if (!getModel().getDescriptions().isEmpty()) {
            mDescriptionTxt.setText(getModel().getDescriptions().get(mCurrentLocale));
        }
        mIconText.setText(getModel().getDefaultIcon());
        mIconHCText.setText(getModel().getHCIcon());
        getModel().setSuspendEvent(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {

        pParent.setLayout(new GridLayout(LAYOUT_COLS, false));

        Label descrLbl = pToolkit.createLabel(pParent, Messages.getString("GeneralSection.Description"), //$NON-NLS-1$
            SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS;
        descrLbl.setLayoutData(gd);

        createNameControls(pToolkit, pParent);
        createIdentifierControls(pToolkit, pParent);
        createVersionControls(pToolkit, pParent);
        createDescriptionControls(pToolkit, pParent);
        createIconControls(pToolkit, pParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLocale(Locale locale) {
        if (!getModel().getDisplayNames().containsKey(locale)) {
            getModel().addDisplayName(locale, new String());
        }
        if (!getModel().getDescriptions().containsKey(locale)) {
            getModel().addDescription(locale, new String());
        }
        mNameTxt.setEnabled(true);
        mDescriptionTxt.setEnabled(true);
        mDescriptionBtn.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteLocale(Locale locale) {
        getModel().removeDisplayName(locale);
        getModel().removeDescription(locale);
        if (getModel().getDisplayNames().isEmpty()) {
            mNameTxt.setEnabled(false);
        }

        if (getModel().getDescriptions().isEmpty()) {
            mDescriptionTxt.setEnabled(false);
            mDescriptionBtn.setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale locale) {

        if (mCurrentLocale != null) {
            getModel().addDisplayName(mCurrentLocale, mNameTxt.getText());
            getModel().addDescription(mCurrentLocale, mDescriptionTxt.getText());
        }
        super.selectLocale(locale);
        String name = getModel().getDisplayNames().get(locale);
        mNameTxt.setText(name);
        mDescriptionTxt.setText(getModel().getDescriptions().get(locale));
    }

    private void createNameControls(FormToolkit pToolkit, Composite pParent) {
        pToolkit.createLabel(pParent, Messages.getString("GeneralSection.Name")); //$NON-NLS-1$
        mNameTxt = pToolkit.createText(pParent, new String());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS - 1;
        mNameTxt.setLayoutData(gd);
        mNameTxt.setEnabled(false);
        mNameTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getModel().addDisplayName(mCurrentLocale, mNameTxt.getText());
            }
        });
    }

    private void createIdentifierControls(FormToolkit pToolkit, Composite pParent) {
        pToolkit.createLabel(pParent, Messages.getString("GeneralSection.Identifier")); //$NON-NLS-1$
        mIdTxt = pToolkit.createText(pParent, new String());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS - 1;
        mIdTxt.setLayoutData(gd);
        mIdTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getModel().setId(mIdTxt.getText());
            }
        });
    }

    private void createVersionControls(FormToolkit pToolkit, Composite pParent) {
        pToolkit.createLabel(pParent, Messages.getString("GeneralSection.Version")); //$NON-NLS-1$
        mVersionTxt = pToolkit.createText(pParent, new String());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS - 1;
        mVersionTxt.setLayoutData(gd);
        mVersionTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getModel().setVersion(mVersionTxt.getText());
            }
        });
    }

    private void createDescriptionControls(FormToolkit pToolkit, Composite pParent) {
        pToolkit.createLabel(pParent, Messages.getString("GeneralSection.DescriptionFile")); //$NON-NLS-1$
        mDescriptionTxt = pToolkit.createText(pParent, new String());
        mDescriptionTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDescriptionTxt.setEnabled(false);
        mDescriptionTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                getModel().addDescription(mCurrentLocale, mDescriptionTxt.getText());
            }
        });

        mDescriptionBtn = pToolkit.createButton(pParent, "...", SWT.PUSH); //$NON-NLS-1$
        mDescriptionBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mDescriptionBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Open the folder selection dialog
                ProjectSelectionDialog dlg = new ProjectSelectionDialog(mProject,
                    Messages.getString("GeneralSection.FileChooserTooltip")); //$NON-NLS-1$

                if (dlg.open() == Window.OK) {
                    IResource res = dlg.getSelected();
                    if (res != null && res.getType() == IResource.FILE) {
                        IFile file = (IFile) res;
                        String path = file.getProjectRelativePath().toString();
                        mDescriptionTxt.setText(path);
                    }
                }
            }
        });
    }

    private void createIconControls(FormToolkit pToolkit, Composite pParent) {
        pToolkit.createLabel(pParent, Messages.getString("GeneralSection.Icon")); //$NON-NLS-1$
        mIconText = pToolkit.createText(pParent, new String());
        mIconText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mIconText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getModel().setDefaultIcon(mIconText.getText());
            }
        });
        mIconButton = pToolkit.createButton(pParent, "...", SWT.PUSH);
        mIconButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mIconButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
             // Open the folder selection dialog
                ProjectSelectionDialog dlg = new ProjectSelectionDialog(mProject,
                    Messages.getString("GeneralSection.FileChooserTooltip")); //$NON-NLS-1$

                if (dlg.open() == Window.OK) {
                    IResource res = dlg.getSelected();
                    if (res != null && res.getType() == IResource.FILE) {
                        IFile file = (IFile) res;
                        String path = file.getProjectRelativePath().toString();
                        mIconText.setText(path);
                    }
                }
            }
        });

        pToolkit.createLabel(pParent, Messages.getString("GeneralSection.HCIcon")); //$NON-NLS-1$
        mIconHCText = pToolkit.createText(pParent, new String());
        mIconHCText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mIconHCText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getModel().setHCIcon(mIconHCText.getText());
            }
        });
        mIconHCButton = pToolkit.createButton(pParent, "...", SWT.PUSH);
        mIconHCButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mIconHCButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
             // Open the folder selection dialog
                ProjectSelectionDialog dlg = new ProjectSelectionDialog(mProject,
                    Messages.getString("GeneralSection.FileChooserTooltip")); //$NON-NLS-1$

                if (dlg.open() == Window.OK) {
                    IResource res = dlg.getSelected();
                    if (res != null && res.getType() == IResource.FILE) {
                        IFile file = (IFile) res;
                        String path = file.getProjectRelativePath().toString();
                        mIconHCText.setText(path);
                    }
                }
            }
        });
    }

}
