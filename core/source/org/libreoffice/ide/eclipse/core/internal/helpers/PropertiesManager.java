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
package org.libreoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.model.OOo;
import org.libreoffice.ide.eclipse.core.internal.model.SDK;
import org.libreoffice.ide.eclipse.core.internal.model.URE;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * Helper class for OOo and SDK preferences handling. These aren't stored in the standard plugin preferences, but in a
 * separate file: {@link org.libreoffice.ide.eclipse.core.OOEclipsePlugin#OOO_CONFIG}
 */
public class PropertiesManager {

    /**
     * OOo SDK path preference key. Used to store the preferences
     */
    private static final String SDKPATH_PREFERENCE_KEY = "sdkpath"; //$NON-NLS-1$

    private static final String SDKNAME_PREFERENCE_KEY = "sdkname"; //$NON-NLS-1$

    /**
     * OOo path preference key. Used to store the preferences
     */
    private static final String OOOPATH_PREFERENCE_KEY = "ooopath"; //$NON-NLS-1$

    private static final String OOONAME_PREFERENCE_KEY = "oooname"; //$NON-NLS-1$

    /**
     * Loads the SDK properties.
     *
     * @return the loaded SDKs
     */
    public static ISdk[] loadSDKs() {

        ISdk[] result = null;

        try {

            // Loads the sdks config file into a properties object
            Properties sdksProperties = getProperties();

            // Analyse the properties to get the SDKs

            int i = 0;
            boolean found = false;
            Vector<SDK> sdks = new Vector<SDK>();

            do {
                String path = sdksProperties.getProperty(SDKPATH_PREFERENCE_KEY + i);
                String name = sdksProperties.getProperty(SDKNAME_PREFERENCE_KEY + i);

                found = !(null == path);
                i++;

                if (found) {
                    try {
                        SDK sdk = new SDK(path, name);
                        sdks.add(sdk);
                    } catch (InvalidConfigException e) {
                        PluginLogger.error(e.getLocalizedMessage(), e);
                        // This message is localized in SDK class
                    }
                }
            } while (found);

            // transform the vector into an array
            result = new ISdk[sdks.size()];

            for (int j = 0, length = sdks.size(); j < length; j++) {
                result[j] = sdks.get(j);
            }

            // clean the vector
            sdks.clear();

        } catch (IOException e) {
            PluginLogger.error(Messages.getString("PropertiesManager.UnreadableFileError") + //$NON-NLS-1$
                OOEclipsePlugin.OOO_CONFIG, e);
        }

        return result;
    }

    /**
     * Saves the SDK properties.
     *
     * @param pSdks
     *            the SDKs to save
     */
    public static void saveSDKs(ISdk[] pSdks) {

        FileOutputStream out = null;

        try {
            Properties sdksProperties = getProperties();

            // Load all the existing properties and remove the SDKPATH_PREFERENCE_KEY ones
            Enumeration<Object> keys = sdksProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();

                if (key.startsWith(SDKPATH_PREFERENCE_KEY) || key.startsWith(SDKNAME_PREFERENCE_KEY)) {
                    sdksProperties.remove(key);
                }
            }

            // Saving the new SDKs
            for (int i = 0; i < pSdks.length; i++) {
                ISdk sdki = pSdks[i];
                sdksProperties.put(SDKPATH_PREFERENCE_KEY + i, sdki.getHome());
                sdksProperties.put(SDKNAME_PREFERENCE_KEY + i, sdki.getName());
            }

            String sdks_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
            File file = new File(sdks_config_url + "/" + OOEclipsePlugin.OOO_CONFIG); //$NON-NLS-1$
            if (!file.exists()) {
                file.createNewFile();
            }

            out = new FileOutputStream(file);
            sdksProperties.store(out, ""); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            PluginLogger.error(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            PluginLogger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Loads the OOo properties.
     *
     * @return the loaded OOos
     */
    public static IOOo[] loadOOos() {

        IOOo[] result = null;

        try {

            // Loads the ooos config file into a properties object
            Properties ooosProperties = getProperties();

            // Analyse the properties to get the OOos

            int i = 0;
            boolean found = false;
            Vector<IOOo> ooos = new Vector<IOOo>();

            do {
                String path = ooosProperties.getProperty(OOOPATH_PREFERENCE_KEY + i);
                String name = ooosProperties.getProperty(OOONAME_PREFERENCE_KEY + i);

                found = !(null == path);
                i++;

                if (found) {
                    try {
                        OOo ooo = new OOo(path, name);
                        ooos.add(ooo);
                    } catch (InvalidConfigException e) {
                        addURE(path, name, ooos);
                    }
                }
            } while (found);

            // transform the vector into an array
            result = new IOOo[ooos.size()];

            for (int j = 0, length = ooos.size(); j < length; j++) {
                result[j] = ooos.get(j);
            }

            // Clean the vector
            ooos.clear();

        } catch (IOException e) {
            PluginLogger.error(Messages.getString("PropertiesManager.UnreadableFileError") + //$NON-NLS-1$
                OOEclipsePlugin.OOO_CONFIG, e);
        }

        return result;
    }

    /**
     * Try to add an URE instance to the list of OOo instances.
     *
     * <p>
     * If the path doesn't refer to a valid URE, nothing is changed in the list of OOo instances
     * </p>
     *
     * @param path
     *            the path to the URE installation
     * @param name
     *            the name of the URE
     * @param instances
     *            the list of OOo instances where to add the URE
     */
    private static void addURE(String path, String name, Vector<IOOo> instances) {
        try {
            URE ure = new URE(path, name);
            instances.add(ure);
        } catch (InvalidConfigException e) {
            PluginLogger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Saves the OOo properties.
     *
     * @param ooos
     *            the OOos to save
     */
    public static void saveOOos(IOOo[] ooos) {

        FileOutputStream out = null;

        try {
            Properties ooosProperties = getProperties();

            // Load all the existing properties and remove the OOOPATH_PREFERENCE_KEY ones
            Enumeration<Object> keys = ooosProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();

                if (key.startsWith(OOOPATH_PREFERENCE_KEY) || key.startsWith(OOONAME_PREFERENCE_KEY)) {
                    ooosProperties.remove(key);
                }
            }

            // Saving the new OOos
            for (int i = 0; i < ooos.length; i++) {
                IOOo oooi = ooos[i];
                ooosProperties.put(OOOPATH_PREFERENCE_KEY + i, oooi.getHome());
                ooosProperties.put(OOONAME_PREFERENCE_KEY + i, oooi.getName());
            }

            String ooos_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
            File file = new File(ooos_config_url + "/" + OOEclipsePlugin.OOO_CONFIG); //$NON-NLS-1$
            if (!file.exists()) {
                file.createNewFile();
            }

            out = new FileOutputStream(file);
            ooosProperties.store(out, ""); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            PluginLogger.error(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            PluginLogger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Loads the OOo and SDK properties from the {@link OOEclipsePlugin#OOO_CONFIG} file.
     *
     * @return the loaded properties
     * @throws IOException
     *             is thrown if any problem happened during the file reading
     */
    private static Properties getProperties() throws IOException {
        // Loads the ooos config file into a properties object
        Properties ooosProperties = new Properties();

        String ooos_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
        File file = new File(ooos_config_url + "/" + OOEclipsePlugin.OOO_CONFIG); //$NON-NLS-1$
        if (!file.exists()) {
            file.createNewFile();
        }

        FileInputStream in = new FileInputStream(file);
        try {
            ooosProperties.load(in);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }

        return ooosProperties;
    }
}
