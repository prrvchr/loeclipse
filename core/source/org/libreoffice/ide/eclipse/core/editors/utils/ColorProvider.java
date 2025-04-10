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
package org.libreoffice.ide.eclipse.core.editors.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;

/**
 * This class provides colors of the editor to all the other objects.
 */
public class ColorProvider {
    protected Map<String, Color> mColorTable = new HashMap<>();
    private IPreferenceStore mStore;

    private final IPropertyChangeListener mPropertyListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            mColorTable.clear();
        }
    };

    /**
     * Default constructor getting the preferences.
     */
    public ColorProvider() {
        OOEclipsePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(mPropertyListener);
        mStore = OOEclipsePlugin.getDefault().getPreferenceStore();
    }

    /**
     * Disposing the color provider.
     */
    public void dispose() {
        Iterator<Color> e = mColorTable.values().iterator();
        while (e.hasNext()) {
            e.next().dispose();
        }
        OOEclipsePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(mPropertyListener);
    }

    /**
     * Returns the color corresponding to the given name.
     *
     * @param colorString
     *            name of the color to get
     * @return the color from the preferences or the eclipse default color
     */
    public Color getColor(String colorString) {

        Color color = mColorTable.get(colorString);
        if (color == null) {
            color = new Color(Display.getCurrent(), PreferenceConverter.getColor(mStore, colorString));
            mColorTable.put(colorString, color);
        }
        return color;
    }
}
