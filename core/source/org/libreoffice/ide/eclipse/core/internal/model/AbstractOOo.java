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
package org.libreoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.swt.graphics.Image;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.ITableElement;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.config.IExtraOptionsProvider;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;

/**
 * Helper class to add the table element features to the OOo classes. All the {@link IOOo} interface still has to be
 * implemented by the subclasses
 */
public abstract class AbstractOOo implements IOOo, ITableElement {

    public static final String NAME = "__ooo_name"; //$NON-NLS-1$

    public static final String PATH = "__ooo_path"; //$NON-NLS-1$

    protected static final String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

    private static String sPlatform;

    private String mHome;
    private String mName;

    /**
     * Creating a new OOo or URE instance specifying its home directory.
     *
     * @param home
     *            the LibreOffice or URE home directory
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public AbstractOOo(String home) throws InvalidConfigException {
        setHome(home);
    }

    /**
     * Creating a new OOo or URE instance specifying its home directory and name.
     *
     * @param home
     *            the LibreOffice or URE installation directory
     * @param name
     *            the LibreOffice or URE instance name
     *
     * @throws InvalidConfigException
     *             if the home directory doesn't contains the required files and directories
     */
    public AbstractOOo(String home, String name) throws InvalidConfigException {
        setHome(home);
        setName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHome(String home) throws InvalidConfigException {

        Path homePath = new Path(home);
        File homeFile = homePath.toFile();

        /* Checks if the directory exists */
        if (!homeFile.isDirectory() || !homeFile.canRead()) {
            mHome = null;
            throw new InvalidConfigException(Messages.getString("AbstractOOo.NoDirectoryError") + //$NON-NLS-1$
                homeFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
        }

        mHome = home;

        /* Checks if the classes paths are directories */
        checkClassesDir();

        /* Checks if types registries are readable files */
        checkTypesRdb();

        /* Checks if services.rdb is a readable file */
        checkServicesRdb();

        /* Checks if unorc is a readable file */
        checkUnoIni();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHome() {
        return mHome;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * Set the new name only if it's neither null nor the empty string. The name will be rendered unique and therefore
     * may be changed.
     *
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        if (name != null && !name.equals("")) { //$NON-NLS-1$
            mName = OOoContainer.getUniqueName(name);
        }
    }

    /**
     * Check if the UNO configuration file is present in the OOo installation directory.
     *
     * @throws InvalidConfigException
     *             if the UNO configuration file isn't present.
     */
    private void checkUnoIni() throws InvalidConfigException {
        Path unorcPath = new Path(getUnorcPath());
        File unorcFile = unorcPath.toFile();

        if (!unorcFile.isFile() || !unorcFile.canRead()) {
            mHome = null;
            throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + //$NON-NLS-1$
                unorcFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
        }
    }

    /**
     * Check if the <code>services.rdb</code> file is present in the OOo installation directory.
     *
     * @throws InvalidConfigException
     *             if the <code>services.rdb</code> file isn't present
     */
    private void checkServicesRdb() throws InvalidConfigException {
        String[] paths = getServicesPath();

        for (String path : paths) {
            Path servicesPath = new Path(path);
            File servicesFile = servicesPath.toFile();

            if (!servicesFile.isFile() || !servicesFile.canRead()) {
                mHome = null;
                throw new InvalidConfigException(
                    Messages.getString("AbstractOOo.NoFileError") + //$NON-NLS-1$
                    servicesFile.getAbsolutePath(),
                    InvalidConfigException.INVALID_OOO_HOME);
            }
        }
    }

    /**
     * Check if the <code>types.rdb</code> file is present in the OOo installation directory.
     *
     * @throws InvalidConfigException
     *             if the <code>types.rdb</code> file isn't present
     */
    private void checkTypesRdb() throws InvalidConfigException {
        String[] paths = getTypesPath();
        for (String path : paths) {
            Path typesPath = new Path(path);
            File typesFile = typesPath.toFile();

            if (!typesFile.isFile() || !typesFile.canRead()) {
                mHome = null;
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + //$NON-NLS-1$
                    typesFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
            }
        }
    }

    /**
     * Check if the classes directory exits in the OOo installation folder.
     *
     * @throws InvalidConfigException
     *             if the classes directory can't be found
     */
    private void checkClassesDir() throws InvalidConfigException {
        String[] paths = getClassesPath();
        for (String path : paths) {
            Path javaPath = new Path(path);
            File javaDir = javaPath.toFile();

            if (!javaDir.isDirectory() || !javaDir.canRead()) {
                mHome = null;
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoDirectoryError") + //$NON-NLS-1$
                    javaDir.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
            }
        }
    }

    // -------------------------------------------- ITableElement Implementation

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(String pProperty) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel(String pProperty) {
        String result = ""; //$NON-NLS-1$
        if (pProperty.equals(NAME)) {
            result = getName();
        } else if (pProperty.equals(PATH)) {
            result = getHome();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getProperties() {
        return new String[] { NAME, PATH };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canModify(String pProperty) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String pProperty) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(String pProperty, Object pValue) {
        // Nothing to do
    }

    /**
     * Run a UNO application using an implementation of the <code>XMain</code> interface.
     *
     * @param prj
     *            the UNO project to run
     * @param main
     *            the fully qualified name of the main service to run
     * @param args
     *            the UNO program arguments
     * @param launch
     *            the Eclipse launch instance
     * @param monitor
     *            the monitor reporting the run progress
     */
    @Override
    public void runUno(IUnoidlProject prj, String main, String args, ILaunch launch, IProgressMonitor monitor) {

        String libpath = prj.getLanguage().getProjectHandler().getLibraryPath(prj);
        libpath = libpath.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        libpath = libpath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
        libpath = "file:///" + libpath; //$NON-NLS-1$

        String unoPath = getUnoPath();
        if (getPlatform().equals(Platform.OS_WIN32)) {
            /* uno is already in the PATH variable, so don't worry */
            unoPath = "uno"; //$NON-NLS-1$
        }

        String command = unoPath + " -c " + main + //$NON-NLS-1$
            " -l " + libpath + //$NON-NLS-1$
            " -- " + args; //$NON-NLS-1$

        String[] env = prj.getLanguage().getLanguageBuilder().getBuildEnv(prj);

        if (getJavaldxPath() != null) {
            Process p = prj.getSdk().runToolWithEnv(prj, getJavaldxPath(), env, monitor);
            InputStream out = p.getInputStream();
            StringWriter writer = new StringWriter();

            try {
                int c = out.read();
                while (c != -1) {
                    writer.write(c);
                    c = out.read();
                }
            } catch (IOException e) {
            }

            String libPath = writer.getBuffer().toString();
            env = SystemHelper.addEnv(env, "LD_LIBRARY_PATH", libPath.trim(), //$NON-NLS-1$
                System.getProperty("path.separator")); //$NON-NLS-1$
        }

        Process p = prj.getSdk().runToolWithEnv(prj, command, env, monitor);
        DebugPlugin.newProcess(launch, p, Messages.getString("AbstractOOo.UreProcessName") + main); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runOffice(IUnoidlProject prj, ILaunch launch, IPath userInstallation,
        IExtraOptionsProvider extraOptionsProvider, IProgressMonitor monitor) {
        try {
            String[] env = prj.getLanguage().getLanguageBuilder().getBuildEnv(prj);

            String pathSeparator = System.getProperty("path.separator");
            String[] sPaths = prj.getOOo().getBinPath();
            StringBuilder sPathValue = new StringBuilder();
            for (String sPath : sPaths) {
                sPathValue.append(sPath);
                sPathValue.append(pathSeparator);
            }

            String command = "soffice.bin --norestore";

            env = SystemHelper.addEnv(env, "PATH", sPathValue.toString(), pathSeparator);
            env = SystemHelper.addEnv(env, "SAL_ALLOW_LINKOO_SYMLINKS", "1", null);
            env = addUserProfile(userInstallation, env);
            env = extraOptionsProvider.addEnv(env);

            PluginLogger.debug("Launching LibreOffice from commandline: " + command);
            Process p = prj.getSdk().runToolWithEnv(prj, command, env, monitor);
            DebugPlugin.newProcess(launch, p, Messages.getString("AbstractOOo.LibreOfficeProcessName")); //$NON-NLS-1$
        } catch (Exception e) {
            e.printStackTrace();
            PluginLogger.error("Error running LibreOffice", e);
        }
    }

    /**
     * Adds the proper env variables for the user profile.
     *
     * @param userInstallation
     *            the path to the user profile folder.
     * @param env
     *            the original env.
     * @return the new env.
     * @throws URISyntaxException
     *             if something goes wrong.
     */
    protected String[] addUserProfile(IPath userInstallation, String[] env) throws URISyntaxException {
        if (null != userInstallation) {
            // We have to turn the path to a URI something like file:///foo/bar/.ooo-debug
            // XXX: find a better way to get the proper URI.
            URI userInstallationURI = new URI("file", "", userInstallation.toFile().toURI().getPath(), null);
            env = SystemHelper.addEnv(env, "UserInstallation", userInstallationURI.toString(), null);
        }
        return env;
    }

    /**
     * Sets the target platform for tests.
     *
     * @param pPlatform
     *            the target platform
     */
    public static void setPlatform(String pPlatform) {
        sPlatform = pPlatform;
    }

    /**
     * @return the system platform, or the test one if set.
     */
    protected String getPlatform() {
        String result = sPlatform;
        if (sPlatform == null) {
            result = Platform.getOS();
        }
        return result;
    }

    protected static String getPlatformOS() {
        return Platform.getOS();
    }

    /**
     * indicates if a code is a symbolic link or not. The code is an adaptation from apache commons
     *
     * @param file
     * @return true if the file is a symbolic link, false otherwise
     * @throws IOException
     */
    protected static boolean isSymbolicLink(File file) throws IOException {
        boolean isLink = false;
        if (file != null) {
            File fileInCanonicalParent = null;
            if (file.getParentFile() == null) {
                fileInCanonicalParent = file;
            } else {
                File canonicalParent = file.getParentFile().getCanonicalFile();
                fileInCanonicalParent = new File(canonicalParent, file.getName());
            }
            isLink = !fileInCanonicalParent.getCanonicalFile().equals(fileInCanonicalParent.getAbsoluteFile());
        }
        return isLink;
    }

    public static File getTargetLink(File link) throws IOException {
        File target = null;
        if (link != null) {
            target = new File(link.getCanonicalPath());
        }
        return target;
    }
}
