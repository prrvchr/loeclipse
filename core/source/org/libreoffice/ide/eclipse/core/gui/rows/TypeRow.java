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
package org.libreoffice.ide.eclipse.core.gui.rows;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.libreoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;

/**
 * Row for the selection of a UNO type.
 *
 * <p>
 * The row allows to type the text in a text field or selecting the type using the UNO type browser. The text field
 * support a simple auto-completion.
 * </p>
 */
public class TypeRow extends TextRow {

    private InternalUnoType mSelectedType;
    private int mType = 0;

    private boolean mIncludeSequences = false;
    private boolean mIncludeSimpleTypes = false;
    private boolean mIncludeVoid = true;

    /**
     * Creates a row for the selection of a UNO type.
     *
     * <p>
     * The types mask is an integer from 0 to 2048-1. The type mask can be obtained by bit-OR of the types constants
     * defined in {@link InternalUnoType} class.
     * </p>
     *
     * @param parent
     *            the parent composite where to create the row
     * @param property
     *            the property name of the row
     * @param label
     *            the label of the row
     * @param type
     *            the types mask of the row.
     */
    public TypeRow(Composite parent, String property, String label, int type) {
        super(parent, property, label);

        if (type >= 0 && type <= InternalUnoType.ALL_TYPES) {
            mType = type;
        }
    }

    /**
     * Set whether the row should support include auto-completion for sequences. Sequences aren't included in the
     * auto-completion by default.
     *
     * @param include
     *            <code>true</code> if the row can auto-complete sequences
     */
    public void includeSequences(boolean include) {
        mIncludeSequences = include;
    }

    /**
     * Set whether the row should support include auto-completion for simple UNO types. If the simple types are not
     * included in the auto-completion, the void type isn't included too. Simple types aren't included in the
     * auto-completion by default.
     *
     * @param include
     *            <code>true</code> if the row can auto-complete sequences
     * @see #includeVoid(boolean) to include/exclude the void type
     */
    public void includeSimpleTypes(boolean include) {
        mIncludeSimpleTypes = include;
    }

    /**
     * Set whether the row should support include auto-completion for the void type. The void type is included in the
     * auto-completion by default as long as the simple types are included.
     *
     * @param include
     *            <code>true</code> if the row can auto-complete sequences
     * @see #includeSimpleTypes(boolean) for more precisions on the inclusion of the void type dependence on the other
     *      simple types inclusion.
     */
    public void includeVoid(boolean include) {
        mIncludeVoid = include;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContent(Composite parent, Control label, Control field, String browseText,
        boolean browseLink) {

        super.createContent(parent, label, field, Messages.getString("TypeRow.Browse"), true); //$NON-NLS-1$

        // Add a completion listener on the Text field
        ((Text) mField).addKeyListener(new KeyAdapter() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void keyPressed(KeyEvent event) {

                // react on Ctrl+space
                if (event.character == ' ' && (event.stateMask & SWT.CTRL) != 0) {
                    // if the word sequence is started, complete it
                    Text text = (Text) mField;

                    int pos = text.getCaretPosition();
                    String value = text.getText(0, pos);

                    int i = getStartOfWord(pos, text.getText());

                    if ("sequence".startsWith(value.substring(i, pos)) && mIncludeSequences) { //$NON-NLS-1$
                        String toadd = "sequence".substring(pos - i) + "<>"; //$NON-NLS-1$ //$NON-NLS-2$
                        text.insert(toadd);
                        setValue(text.getText().trim());
                        text.setSelection(pos + toadd.length() - 1);
                        event.doit = false;
                    } else {
                        // check the simple types
                        if (mIncludeSimpleTypes) {
                            String[] simpleTypes = new String[] { "unsigned ", //$NON-NLS-1$
                                "string", //$NON-NLS-1$
                                "short", //$NON-NLS-1$
                                "long", //$NON-NLS-1$
                                "hyper", //$NON-NLS-1$
                                "double", //$NON-NLS-1$
                                "float", //$NON-NLS-1$
                                "any", //$NON-NLS-1$
                                "void", //$NON-NLS-1$
                                "char", //$NON-NLS-1$
                                "type", //$NON-NLS-1$
                                "boolean" //$NON-NLS-1$
                            };

                            for (String type : simpleTypes) {
                                if ((!type.equals("void") || mIncludeVoid) && //$NON-NLS-1$
                                    type.startsWith(value.substring(i, pos))) {
                                    String toadd = type.substring(pos - i);
                                    text.insert(toadd);
                                    setValue(text.getText().trim());
                                    text.setSelection(pos + toadd.length());
                                    event.doit = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        final Shell shell = parent.getShell();

        addBrowseSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                super.widgetSelected(event);

                // Remove the module type from the current types
                int allowedTypes = mType & Integer.MAX_VALUE - IUnoFactoryConstants.MODULE;

                UnoTypeBrowser browser = new UnoTypeBrowser(shell, allowedTypes);
                browser.setSelectedType(mSelectedType);

                if (Window.OK == browser.open()) {
                    mSelectedType = browser.getSelectedType();
                    if (null != mSelectedType) {
                        Text text = (Text) mField;
                        int pos = text.getCaretPosition();
                        text.insert(mSelectedType.getFullName().replaceAll("\\.", "::")); //$NON-NLS-1$ //$NON-NLS-2$
                        text.setFocus();
                        text.setSelection(pos + mSelectedType.getFullName().length());
                        setValue(text.getText().trim());
                    }
                }
            }
        });
    }

    /**
     * Get the position of the start of the selected word.
     *
     * @param pPos
     *            the caret position
     * @param pText
     *            the text to analyze
     *
     * @return the position of the first character of the word in the text.
     */
    private int getStartOfWord(int pPos, String pText) {
        int i = pPos - 1;
        // For unsigned types
        while (pText.charAt(i) >= 'a' && pText.charAt(i) <= 'z' && i > 0) {
            i--;
        }

        if (i != 0) {
            i++;
        }
        return i;
    }
}
