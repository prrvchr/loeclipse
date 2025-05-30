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
package org.libreoffice.ide.eclipse.core.editors.syntax;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.custom.StyleRange;

/**
 * The UNO-IDL document repairer. This is used by the UNO-IDL editor. In order to fully understand the editor
 * mechanisms, please report to Eclipse plugin developer's guide.
 */
public class NonRuleBasedDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

    /**
     * The document this object works on .
     */
    protected IDocument mDocument;

    /**
     * The default text attribute if non is returned as data by the current token.
     */
    protected TextAttribute mDefaultTextAttribute;

    /**
     * Default constructor.
     *
     * @param defaultTextAttribute
     *            the attribute to assign to default text
     */
    public NonRuleBasedDamagerRepairer(TextAttribute defaultTextAttribute) {
        Assert.isNotNull(defaultTextAttribute);

        mDefaultTextAttribute = defaultTextAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocument(IDocument document) {
        mDocument = document;
    }

    /**
     * Returns the end offset of the line that contains the specified offset. If the offset is inside a line delimiter,
     * the end offset of the next line.
     *
     * @param offset
     *            the offset whose line end offset must be computed
     * @return the line end offset for the given offset
     * @exception BadLocationException
     *                if offset is invalid in the current document
     */
    protected int endOfLineOf(int offset) throws BadLocationException {
        int endOffset = mDocument.getLength();

        IRegion info = mDocument.getLineInformationOfOffset(offset);
        if (offset <= info.getOffset() + info.getLength()) {
            endOffset = info.getOffset() + info.getLength();
        }

        int line = mDocument.getLineOfOffset(offset);
        try {
            info = mDocument.getLineInformation(line + 1);
            endOffset = info.getOffset() + info.getLength();
        } catch (BadLocationException x) {
            endOffset = mDocument.getLength();
        }

        return endOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
        boolean documentPartitioningChanged) {

        IRegion damaged = partition;

        if (!documentPartitioningChanged) {
            try {

                IRegion info = mDocument.getLineInformationOfOffset(event.getOffset());
                int start = Math.max(partition.getOffset(), info.getOffset());

                int length = event.getLength();
                if (event.getText() == null) {
                    length = event.getText().length();
                }
                int end = event.getOffset() + length;

                if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
                    // optimize the case of the same line
                    end = info.getOffset() + info.getLength();
                } else {
                    end = endOfLineOf(end);
                }

                end = Math.min(partition.getOffset() + partition.getLength(), end);
                damaged = new Region(start, end - start);

            } catch (BadLocationException x) {
            }
        }

        return damaged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPresentation(TextPresentation pPresentation, ITypedRegion pRegion) {
        addRange(pPresentation, pRegion.getOffset(), pRegion.getLength(), mDefaultTextAttribute);
    }

    /**
     * Adds style information to the given text presentation.
     *
     * @param presentation
     *            the text presentation to be extended
     * @param offset
     *            the offset of the range to be styled
     * @param length
     *            the length of the range to be styled
     * @param attr
     *            the attribute describing the style of the range to be styled
     */
    protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {

        if (attr != null) {
            presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(),
                attr.getStyle()));
        }
    }
}