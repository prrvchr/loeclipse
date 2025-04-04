/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2010 by Dan Corneanu
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
 * The Initial Developer of the Original Code is: Dan Corneanu.
 *
 * Copyright: 2010 by Dan Corneanu
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.launch.office;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.builders.TypesBuilder;
import org.libreoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.NullExtraOptionsProvider;
import org.libreoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * LibreOffice launcher implementation.
 */
public class OfficeLaunchDelegate extends LaunchConfigurationDelegate {

    /**
     * Export the .oxt file, deploy it in LibreOffice, run LibreOffice.
     */
    private static final int TASK_UNITS = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
        throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        try {
            monitor.beginTask(MessageFormat.format("{0}...", //$NON-NLS-1$
                new Object[] { configuration.getName() }), TASK_UNITS);
            // check for cancellation
            if (monitor.isCanceled()) {
                return;
            }

            String prjName = configuration.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, new String());
            boolean useCleanUserInstallation = configuration
                .getAttribute(IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, false);

            IUnoidlProject unoprj = ProjectsManager.getProject(prjName);

            if (null != unoprj) {
                try {
                    IPath userInstallation = null;
                    if (useCleanUserInstallation) {
                        IFolder userInstallationFolder = unoprj.getOfficeUserProfileFolder();
                        userInstallation = userInstallationFolder.getLocation();
                    }

                    // Force the build
                    IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
                    TypesBuilder.build(prj, monitor);

                    IFile properties = prj.getFile("package.properties");
                    PackagePropertiesModel propertiesModel = new PackagePropertiesModel(properties);
                    List<IResource> resources = propertiesModel.getContents();
                    File destFile = exportComponent(unoprj, resources);
                    monitor.worked(1);

                    // Deploy the component
                    deployComponent(unoprj, userInstallation, destFile);

                    monitor.worked(1);

                    // Run an LibreOffice instance
                    if (ILaunchManager.DEBUG_MODE.equals(mode)) {
                        unoprj.getLanguage().connectDebuggerToOffice(unoprj, launch, userInstallation, monitor);
                    } else {
                        unoprj.getOOo().runOffice(unoprj, launch, userInstallation, new NullExtraOptionsProvider(),
                            monitor);
                    }
                    monitor.worked(1);
                } catch (Exception e) {
                    PluginLogger.error(Messages.OfficeLaunchDelegate_LaunchError, e);
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(Display.getDefault().getActiveShell(),
                                Messages.OfficeLaunchDelegate_LaunchErrorTitle,
                                Messages.OfficeLaunchDelegate_LaunchError);
                        }
                    });
                }
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Deploys the .oxt component in a LibreOffice installation.
     *
     * @param prj
     *            target project
     * @param userInstallation
     *            user profile to use
     * @param oxtFile
     *            the .oxt file
     */
    private void deployComponent(IUnoidlProject prj, IPath userInstallation, File oxtFile) {
        IOOo mOOo = prj.getOOo();
        if (mOOo.canManagePackages()) {
            mOOo.updatePackage(oxtFile, userInstallation);
        }
    }

    /**
     * Will build and export the .oxt file.
     *
     * @param prj
     *            the target project.
     * @param resources
     *            the resources to add to the package
     *
     * @return the file containing the .oxt file.
     * @throws Exception
     *             if something goes wrong.
     */
    private File exportComponent(IUnoidlProject prj, List<IResource> resources) throws Exception {

        IFolder distFolder = prj.getDistFolder();
        File destFile = distFolder.getFile(prj.getName() + ".oxt").getLocation().toFile();

        UnoPackage pack = PackageContentSelector.createPackage(prj, destFile, resources);

        pack.close();
        return destFile;
    }

}
