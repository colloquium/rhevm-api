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
package com.redhat.rhevm.api.dummy.resource;

import java.util.HashMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.dummy.model.DummyStorageDomain;

public class DummyStorageDomainsResource implements StorageDomainsResource {

    private static HashMap<String, DummyStorageDomainResource> storageDomains = new HashMap<String, DummyStorageDomainResource>();

    private static void addStorageDomain(StorageDomainType domainType, String name, StorageType storageType, String host, String path) {
        Storage storage = new Storage();
        storage.setType(storageType);
        storage.setHost(host);
        storage.setPath(path);

        DummyStorageDomain domain = new DummyStorageDomain();
        domain.jaxb.setName(name);
        domain.jaxb.setType(domainType);
        domain.jaxb.setStatus(StorageDomainStatus.UNINITIALIZED);

        storageDomains.put(domain.jaxb.getId(), new DummyStorageDomainResource(domain));
    }

    static {
        addStorageDomain(StorageDomainType.DATA, "images_0", StorageType.NFS, "172.31.0.6", "/exports/RHEV/images/0");
        addStorageDomain(StorageDomainType.ISO, "isos_0", StorageType.NFS, "172.31.0.6", "/exports/RHEV/iso/0");
    }

    @Override
    public StorageDomains list(UriInfo uriInfo) {
        StorageDomains ret = new StorageDomains();

        for (DummyStorageDomainResource storageDomain : storageDomains.values()) {
            String id = storageDomain.getStorageDomain().getId();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getStorageDomains().add(storageDomain.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
        DummyStorageDomainResource newStorageDomain = new DummyStorageDomainResource(new DummyStorageDomain(storageDomain));

        String id = newStorageDomain.getStorageDomain().getId();
        storageDomains.put(id, newStorageDomain);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        storageDomain = newStorageDomain.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(storageDomain).build();
    }

    @Override
    public void remove(String id) {
        storageDomains.remove(id);
    }

    @Override
    public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
        return storageDomains.get(id);
    }
}