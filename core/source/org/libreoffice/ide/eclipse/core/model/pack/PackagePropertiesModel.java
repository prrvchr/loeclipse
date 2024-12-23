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
package org.libreoffice.ide.eclipse.core.model.pack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.Messages;
import org.libreoffice.ide.eclipse.core.model.utils.IModelChangedListener;

/**
 *
 */
public class PackagePropertiesModel {

    private static final String CONTENTS = "contents"; //$NON-NLS-1$
    private static final String BASICLIBS = "basicLibs"; //$NON-NLS-1$
    private static final String DIALOGLIBS = "dialogLibs"; //$NON-NLS-1$
    private static final String DESCRIPTION = "description"; //$NON-NLS-1$
    private static final String SEPARATOR = ", "; //$NON-NLS-1$

    private IFile mPropertiesFile;
    private Properties mProperties = new Properties();
    private List<IResource> mFiles = null;
    private Map<IResource, Boolean> mFolders = null;

    private boolean mIsDirty = false;
    private boolean mIsQuiet = false;
    private Vector<IModelChangedListener> mListeners = new Vector<IModelChangedListener>();

    /**
     * Create a new package.properties model for a given file. If the file can be read, the existing properties will be
     * imported.
     *
     * @param pFile
     *            the package.properties file represented by the object.
     * @throws IllegalArgumentException
     *             if the file is <code>null</code>
     */
    public PackagePropertiesModel(IFile pFile) throws IllegalArgumentException {

        FileInputStream is = null;

        try {
            is = new FileInputStream(pFile.getLocation().toFile());
            mPropertiesFile = pFile;
        } catch (FileNotFoundException e) {
            mPropertiesFile = null;
            throw new IllegalArgumentException(Messages.getString("PackagePropertiesModel.NullFileException")); //$NON-NLS-1$
        }

        try {
            mProperties.load(is);
        } catch (IOException e) {
            PluginLogger.warning(Messages.getString("PackagePropertiesModel.FileReadException") + pFile.getLocation()); //$NON-NLS-1$
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            mFiles = deserializeContent();
            mFolders = getFolderCheckState();
        }
    }

    /**
     * Set whether the changes should be notified to the listeners or not.
     *
     * @param pQuiet
     *            <code>true</code> if the changes should be notified, <code>false</code> otherwise.
     */
    public void setQuiet(boolean pQuiet) {
        mIsQuiet = pQuiet;
    }

    /**
     * Add a listener notified of the model changes.
     *
     * @param pListener
     *            the listener to add.
     */
    public void addChangeListener(IModelChangedListener pListener) {
        mListeners.add(pListener);
    }

    /**
     * Removes a class listening the model changes.
     *
     * @param pListener
     *            the listener to remove
     */
    public void removeChangedListener(IModelChangedListener pListener) {
        if (mListeners.contains(pListener)) {
            mListeners.remove(pListener);
        }
    }

    /**
     * Notify that the package properties model has been saved.
     */
    public void firePackageSaved() {
        if (!mIsQuiet) {
            mIsDirty = false;
            for (IModelChangedListener listener : mListeners) {
                listener.modelSaved();
            }
        }
    }

    /**
     * Notify that the package properties model has changed.
     */
    public void firePackageChanged() {
        if (!mIsQuiet) {
            mIsDirty = true;
            for (IModelChangedListener listener : mListeners) {
                listener.modelChanged();
            }
        }
    }

    /**
     * @return <code>true</code> if the properties model has changed but isn't saved, <code>false</code> otherwise.
     */
    public boolean isDirty() {
        return mIsDirty;
    }

    /**
     * Writes the Package properties to the file.
     *
     * @return the content of the package properties under the form of a string
     *         as it would have been written to the file.
     *
     * @throws Exception
     *             if the data can't be written
     */
    public String write() throws Exception {
        String content = writeToString();
        FileOutputStream os = new FileOutputStream(mPropertiesFile.getLocation().toFile());
        try {
            mProperties.store(os, Messages.getString("PackagePropertiesModel.Comment")); //$NON-NLS-1$
            firePackageSaved();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                // Nothing to log
            }
        }
        return content;
    }

    /**
     * Clears all the content of the package properties and replace it by a string as if it would have been the
     * properties file content.
     *
     * @param pContent
     *            the string describing the data
     */
    public void reloadFromString(String pContent) {
        String initContent = writeToString();
        if (!pContent.equals(initContent)) {
            mProperties.clear();
            try {
                mProperties.load(new StringReader(pContent));
            } catch (IOException e) {
                // Nothing to log
                return;
            } 
            mFiles.clear(); 
            mFiles.addAll(deserializeContent());
            mFolders.clear();
            mFolders.putAll(getFolderCheckState());

            firePackageChanged();
        }
    }

    /**
     * @return the content of the package properties under the form of a string
     *         as it would have been written to the file.
     */
    public String writeToString() {
        String fileContent = ""; //$NON-NLS-1$
        mProperties.setProperty(CONTENTS, serializeContent());
        Set<Entry<Object, Object>> entries = mProperties.entrySet();
        Iterator<Entry<Object, Object>> iter = entries.iterator();
        while (iter.hasNext()) {
            Entry<Object, Object> entry = iter.next();
            fileContent += (String) entry.getKey() + "=" + (String) entry.getValue() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return fileContent;
    }

    /**
     * Adds a Basic library folder to the package.
     *
     * @param pLibFolder
     *            the library folder to add
     * @throws IllegalArgumentException
     *             is thrown if the argument is <code>null</code>
     */
    public void addBasicLibrary(IFolder pLibFolder) throws IllegalArgumentException {

        String libs = mProperties.getProperty(BASICLIBS);
        if (libs == null) {
            libs = ""; //$NON-NLS-1$
        }

        try {
            if (!libs.equals("")) { //$NON-NLS-1$
                libs += SEPARATOR; //$NON-NLS-1$
            }
            libs += pLibFolder.getProjectRelativePath().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        mProperties.setProperty(BASICLIBS, libs);
        firePackageChanged();
    }

    /**
     * Adds a basic dialog library folder to the package.
     *
     * @param pLibFolder
     *            the library folder to add
     * @throws IllegalArgumentException
     *             is thrown if the argument is <code>null</code>
     */
    public void addDialogLibrary(IFolder pLibFolder) throws IllegalArgumentException {
        String libs = mProperties.getProperty(DIALOGLIBS);
        if (libs == null) {
            libs = ""; //$NON-NLS-1$
        }

        try {
            if (!libs.equals("")) { //$NON-NLS-1$
                libs += SEPARATOR; //$NON-NLS-1$
            }
            libs += pLibFolder.getProjectRelativePath().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        mProperties.setProperty(DIALOGLIBS, libs);
        firePackageChanged();
    }

    /**
     * @return the list of the dialog libraries addedd to the package properties
     */
    public List<IFolder> getDialogLibraries() {

        ArrayList<IFolder> result = new ArrayList<IFolder>();

        try {
            String libs = mProperties.getProperty(DIALOGLIBS);
            IProject prj = mPropertiesFile.getProject();

            if (libs != null && !libs.equals("")) { //$NON-NLS-1$
                String[] fileNames = libs.split(SEPARATOR); //$NON-NLS-1$
                for (String fileName : fileNames) {
                    result.add(prj.getFolder(fileName));
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do nor return
        }
        return result;
    }

    /**
     * @return the list of the basic libraries addedd to the package properties
     */
    public List<IFolder> getBasicLibraries() {
        ArrayList<IFolder> result = new ArrayList<IFolder>();

        try {
            String libs = mProperties.getProperty(BASICLIBS);
            IProject prj = mPropertiesFile.getProject();

            if (libs != null && !libs.equals("")) { //$NON-NLS-1$
                String[] fileNames = libs.split(SEPARATOR); //$NON-NLS-1$
                for (String fileName : fileNames) {
                    result.add(prj.getFolder(fileName));
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do nor return
        }
        return result;
    }

    /**
     * Removes all the basic libraries from the package properties.
     */
    public void clearBasicLibraries() {
        mProperties.setProperty(BASICLIBS, ""); //$NON-NLS-1$
        firePackageChanged();
    }

    /**
     * Removes all the dialog libraries from the package properties.
     */
    public void clearDialogLibraries() {
        mProperties.setProperty(DIALOGLIBS, ""); //$NON-NLS-1$
        firePackageChanged();
    }

    /**
     * add resource entry if not already in
     */
    public void addResource(IResource pRes) {
        // If it's a folder we need to add all children resources
        try {
            if (pRes.getType() == IResource.FOLDER) {
                addFolderResource(pRes);
            }
            else if (!mFiles.contains(pRes)) {
                addFileResource(pRes);
            }
            firePackageChanged();
        } catch (CoreException e) {
            // Log ?
         }
    }

    /**
     * remove resource entry if already in
     */
    public void removeResource(IResource pRes) {
        // If it's a folder we need to remove all children resources to
        try {
            if (pRes.getType() == IResource.FOLDER) {
                removeFolderResource(pRes);
            } else if (mFiles.contains(pRes)) {
                removeFileResource(pRes);
            }
            firePackageChanged();
        } catch (CoreException e) {
            // Log ?
        }
    }

    /**
     * @return if resource is checked
     */
    public boolean isChecked(IResource pRes) {
        boolean checked = false;
        if (pRes.getType() == IResource.FILE) {
            checked = mFiles.contains(pRes);
        }
        else if (pRes.getType() == IResource.FOLDER) {
            checked = mFolders.containsKey(pRes);
        }
        return checked;
    }

    /**
     * @return if resource is grayed
     */
    public boolean isGrayed(IResource pRes) {
        boolean grayed = false;
        if (pRes.getType() == IResource.FOLDER) {
            grayed = mFolders.getOrDefault(pRes, false);
        }
        return grayed;
    }

    /**
     * @return the list of the the files and directories added to the package properties that are not dialog or basic
     *         libraries or package descriptions
     */
    public List<IResource> getContents() {
        return mFiles;
    }

    /**
     * Removes all the file and directories from the package properties that has been added using
     * {@link #addResource(IResource)}.
     */
    public void clearContents() {
        mProperties.setProperty(CONTENTS, ""); //$NON-NLS-1$
        mFiles.clear();
        mFolders.clear();
        firePackageChanged();
    }

    /**
     * Adds a localized package description file. The description file has to exist and the locale can't be
     * <code>null</code>.
     *
     * @param pDescription
     *            the description file
     * @param pLocale
     *            the file locale.
     *
     * @throws IllegalArgumentException
     *             is thrown if the file is <code>null</code> or doesn't exists or if the locale is <code>null</code>.
     */
    public void addDescriptionFile(IFile pDescription, Locale pLocale) throws IllegalArgumentException {

        if (pLocale == null) {
            throw new IllegalArgumentException(Messages.getString("PackagePropertiesModel.NoLocaleException")); //$NON-NLS-1$
        }

        if (pDescription == null || !pDescription.exists()) {
            throw new IllegalArgumentException(Messages.getString("PackagePropertiesModel.NoDescriptionFileException")); //$NON-NLS-1$
        }

        String countryName = ""; //$NON-NLS-1$
        if (pLocale.getCountry() != "") { //$NON-NLS-1$
            countryName = "_" + pLocale.getCountry(); //$NON-NLS-1$
        }

        String propertyName = DESCRIPTION + "-" + pLocale.getLanguage() + countryName; //$NON-NLS-1$
        mProperties.setProperty(propertyName, pDescription.getProjectRelativePath().toString());
        firePackageChanged();
    }

    /**
     * @return a map of the description files accessed by their locale. There is no support of a default locale.
     */
    public Map<Locale, IFile> getDescriptionFiles() {
        Map<Locale, IFile> descriptions = new HashMap<>();
        IProject prj = mPropertiesFile.getProject();

        Iterator<Object> keys = mProperties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String regex = DESCRIPTION + "-([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?"; //$NON-NLS-1$
            Matcher matcher = Pattern.compile(regex).matcher(key);
            if (matcher.matches()) {
                String language = matcher.group(1);
                String country = matcher.group(2);

                Locale locale = new Locale(language);
                if (country != null) {
                    locale = new Locale(language, country);
                }

                IFile file = prj.getFile(mProperties.getProperty(key));

                if (file != null) {
                    descriptions.put(locale, file);
                }
            }
        }
        return descriptions;
    }

    /**
     * Removes all the description files from the package properties.
     */
    public void clearDescriptions() {
        int nbRemoved = 0;

        Iterator<Object> keys = ((Properties) mProperties.clone()).keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String regex = DESCRIPTION + "-([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?"; //$NON-NLS-1$
            Matcher matcher = Pattern.compile(regex).matcher(key);
            if (matcher.matches()) {
                mProperties.remove(key);
                nbRemoved++;
            }
        }

        if (nbRemoved > 0) {
            firePackageChanged();
        }
    }

    /**
     * Add all files that are members of a folder resource recursively
     */
    private void addFolderResource(IResource pFolder) throws CoreException {
        mFolders.put(pFolder, false);
        IResource[] members = ((IContainer) pFolder).members();
        for (IResource res :members) {
            if (res.getType() == IResource.FOLDER) {
                addFolderResource(res);
            }
            else if (!mFiles.contains(res)) {
                mFiles.add(res);
            }
        }
    }

    /**
     * Remove all files that are members of a folder resource recursively
     */
    private void removeFolderResource(IResource pFolder) throws CoreException {
        if (mFolders.containsKey(pFolder)) {
            mFolders.remove(pFolder);
        }
        IResource[] members = ((IContainer) pFolder).members();
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                removeFolderResource(res);
            }
            else if (mFiles.contains(res)) {
                mFiles.remove(res);
            }
        }
    }

    /**
     * Add a files and updated folders
     */
    private void addFileResource(IResource pFile) throws CoreException {
        mFiles.add(pFile);
        IProject prj = mPropertiesFile.getProject();
        IContainer parent = pFile.getParent();
        while (parent != null && parent != prj) {
            parent = getParentCheckState(parent);
        }
    }

    /**
     * Remove a files and updated folders
     */
    private void removeFileResource(IResource pFile) throws CoreException {
        if (mFiles.contains(pFile)) {
            mFiles.remove(pFile);
        }
        IProject prj = mPropertiesFile.getProject();
        IContainer parent = pFile.getParent();
        while (parent != null && parent != prj) {
            if (mFolders.containsKey(parent)) {
                if (parent.members().length == 1) {
                    mFolders.remove(parent);
                } else {
                    mFolders.put(parent, true);
                }
            }
            parent = parent.getParent();
        }
    }

    /**
     * Serialize all files resource to the package properties.
     */
    private String serializeContent() {
        List<String> result = new ArrayList<>();
        for (IResource res : mFiles) {
            if (res.getType() == IResource.FILE && res.exists()) {
                result.add(res.getProjectRelativePath().toString());
            }
        }
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return String.join(SEPARATOR, result);
    }

    /**
     * De-serialize all files resource from the package properties.
     */
    private List<IResource> deserializeContent() {
        List<IResource> resources = new ArrayList<>();
        try {
            String libs = mProperties.getProperty(CONTENTS);
            IProject prj = mPropertiesFile.getProject();

            if (libs != null && !libs.equals("")) { //$NON-NLS-1$
                for (String path : libs.split(SEPARATOR)) {
                    if (prj.getFile(path).exists()) {
                        resources.add(prj.getFile(path));
                    }
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do nor return
        }
        return resources;
    }

    /**
     * Get project check state from files resource.
     */
    private Map<IResource, Boolean> getFolderCheckState() {
        Map<IResource, Boolean> resources = new HashMap<>();
        try {
            IResource [] members = mPropertiesFile.getProject().members();
            for (IResource res : members) {
                if (res.getType() == IResource.FOLDER) {
                    getSubFolderCheckState(resources, res);
                }
            }
        } catch (NullPointerException | CoreException e) {
            // Nothing to do nor return
        }
        return resources;
    }

    /**
     * Get folder check state from files resource.
     */
    private void getSubFolderCheckState(Map<IResource, Boolean> pFolders, IResource pParent) throws CoreException {
        IResource[] members = ((IContainer) pParent).members();
        boolean checked = true;
        boolean grayed = false;
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                getSubFolderCheckState(pFolders, res);
            } else if (mFiles.contains(res)) {
                grayed = true;
            } else {
                checked = false;
            }
        }
        if (members.length == 0) {
            pFolders.put(pParent, false);
        }
        else if (checked || grayed) {
            pFolders.put(pParent, grayed && !checked);
        }
    }

    /**
     * Get parent check state from files resource.
     */
    private IContainer getParentCheckState(IResource pParent) throws CoreException {
        IResource[] members = ((IContainer) pParent).members();
        boolean checked = true;
        boolean grayed = false;
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                if (mFolders.containsKey(res)) {
                    grayed = mFolders.get(res);
                } else {
                    checked = false;
                }
            } else if (mFiles.contains(res)) {
                grayed = true;
            } else {
                checked = false;
            }
        }
        if (members.length == 0) {
            mFolders.put(pParent, false);
        }
        else if (checked || grayed) {
            mFolders.put(pParent, grayed && !checked);
        }
        return pParent.getParent();
    }

}
