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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.editors.utils.AbstractSection;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * Section showing the update-informations part of the description.xml file.
 *
 */
public class MirrorsSection extends AbstractSection<DescriptionModel> {

    private static final int COLUMN_WIDTH = 200;

    private DescriptionFormPage mPage;

    private TableViewer mTable;
    private Text mUrlTxt;
    private Button mAddBtn;
    private MenuItem mDeleteAction;

    /**
     * @param parent
     *            the parent composite where to add the section
     * @param page
     *            the parent page
     */
    public MirrorsSection(Composite parent, DescriptionFormPage page) {
        super(parent, page, ExpandableComposite.TITLE_BAR);
        mPage = page;

        createContent();
        setModel(page.getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData() {
        getModel().setSuspendEvent(true);
        mTable.setInput(getModel().getUpdateInfos());
        getModel().setSuspendEvent(false);
    }

    /**
     * Creates the sections controls.
     */
    private void createContent() {
        Section section = getSection();
        section.setText("Update mirrors"); //$NON-NLS-1$

        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        FormToolkit toolkit = mPage.getManagedForm().getToolkit();
        Composite clientArea = toolkit.createComposite(section);
        clientArea.setLayout(new GridLayout(2, false));

        Label descrLbl = toolkit.createLabel(clientArea, Messages.getString("MirrorsSection.Description"), //$NON-NLS-1$
            SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData(gd);

        // Create the list control
        createTable(clientArea);

        // Create the add controls
        String msg = Messages.getString("MirrorsSection.MirrorTextTitle"); //$NON-NLS-1$
        Label addLbl = toolkit.createLabel(clientArea, msg);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        addLbl.setLayoutData(gd);

        mUrlTxt = toolkit.createText(clientArea, new String());
        mUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mUrlTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                mAddBtn.setEnabled(!(0 == mUrlTxt.getText().trim().length()));
            }
        });

        mAddBtn = toolkit.createButton(clientArea, Messages.getString("MirrorsSection.Add"), SWT.PUSH); //$NON-NLS-1$
        mAddBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mAddBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String text = mUrlTxt.getText();
                getModel().addUpdateInfo(text);
                mTable.add(text);
                mUrlTxt.setText(new String());
                markDirty();
            }
        });

        toolkit.paintBordersFor(clientArea);
        section.setClient(clientArea);
    }

    /**
     * Create the URLs table control.
     *
     * @param pParent
     *            the parent composite where to create the table.
     */
    private void createTable(Composite pParent) {
        Table table = new Table(pParent, SWT.SINGLE | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        table.setLayoutData(gd);
        mTable = new TableViewer(table);
        mTable.setContentProvider(new ArrayContentProvider());
        mTable.setLabelProvider(new UrlLabelProvider());
        mTable.setColumnProperties(new String[] { "url" }); //$NON-NLS-1$
        mTable.setCellEditors(new CellEditor[] { new TextCellEditor(table) });
        mTable.setCellModifier(new UrlCellModifier());

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setMoveable(false);
        column.setWidth(COLUMN_WIDTH);

        // Create the table context menu
        Menu menu = new Menu(table);
        mDeleteAction = new MenuItem(menu, SWT.PUSH);
        mDeleteAction.setText(Messages.getString("MirrorsSection.Remove")); //$NON-NLS-1$
        mDeleteAction.setImage(OOEclipsePlugin.getImage(ImagesConstants.DELETE));
        mDeleteAction.setEnabled(false);
        mDeleteAction.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = (IStructuredSelection) mTable.getSelection();
                Object selected = sel.getFirstElement();
                mTable.remove(selected);
                getModel().removeUpdateInfo(selected.toString());
                markDirty();
            }
        });

        mTable.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                mDeleteAction.setEnabled(!e.getSelection().isEmpty());
            }
        });

        table.setMenu(menu);
    }

    /**
     * Label provider for the urls table.
     */
    private class UrlLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            return element.toString();
        }
    }

    /**
     * Class allowing changes from the Urls table viewer on the model.
     */
    private class UrlCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(Object element, String property) {
            return element;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void modify(Object element, String property, Object value) {
            if (element instanceof TableItem) {
                Object o = ((TableItem) element).getData();
                String oldValue = o.toString();

                int pos = getModel().getUpdateInfos().indexOf(oldValue);
                getModel().replaceUpdateInfo(pos, value.toString());
                mTable.replace(value, pos);
                mTable.refresh(o);
                markDirty();
            }
        }
    }
}
