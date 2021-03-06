/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.mock.resource;

import java.util.concurrent.Executor;


import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockNetworkResource extends AbstractMockResource<Network> implements NetworkResource {

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param network     encapsulated network
     * @param executor executor used for asynchronous actions
     */
    MockNetworkResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Network network) {
        // update writable fields only
        if (network.isSetName()) {
            getModel().setName(network.getName());
        }
        if (network.isSetDescription()) {
            getModel().setDescription(network.getDescription());
        }
    }

    public Network addLinks() {
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone(OBJECT_FACTORY.createNetwork(getModel())));
    }

    @Override
    public Network get() {
        return addLinks();
    }

    @Override
    public Network update(Network network) {
        validateUpdate(network);
        updateModel(network);
        return addLinks();
    }
}
