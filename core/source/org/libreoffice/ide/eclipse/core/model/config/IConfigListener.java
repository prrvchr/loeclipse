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
package org.libreoffice.ide.eclipse.core.model.config;

/**
 * Interface describing a OOo or SDK configuration listener.
 *
 */
public interface IConfigListener {

    /**
     * Method fired when a config element has been added to the container.
     *
     * @param element
     *            added sdk or ooo
     */
    public void ConfigAdded(Object element);

    /**
     * Method fired when a config element has been removed from the container.
     *
     * @param element
     *            removed sdk or ooo. <code>null</code> if the container has been cleared
     */
    public void ConfigRemoved(Object element);

    /**
     * Method fired when a config element has been updated in the container.
     *
     * @param element
     *            new value of the sdk or ooo
     */
    public void ConfigUpdated(Object element);
}
