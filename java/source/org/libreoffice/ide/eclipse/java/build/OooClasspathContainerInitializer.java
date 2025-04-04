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
package org.libreoffice.ide.eclipse.java.build;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;

/**
 * Initializes a classpath container for OOo instances.
 */
public class OooClasspathContainerInitializer extends
    ClasspathContainerInitializer {

    static final int HINT_SEGMENT = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(IPath containerPath, IJavaProject project)
        throws CoreException {

        String hint = containerPath.segment(HINT_SEGMENT);
        IOOo ooo = OOoContainer.getOOo(hint);

        if (ooo != null) {
            OOoClasspathContainer container = new OOoClasspathContainer(ooo, containerPath);

            IJavaProject[] projects = new IJavaProject[] { project };
            IClasspathContainer[] containers = new IClasspathContainer[] { container };

            JavaCore.setClasspathContainer(containerPath, projects, containers, null);
        }
    }

    /**
     * Always allow container modification: it could be necessary to add additional OOo
     * jars or set the sources path.
     *
     * @param containerPath the path of the container
     * @param project the project for which to change the container
     *
     * @return always <code>true</code>
     */
    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
        return true;
    }
}
