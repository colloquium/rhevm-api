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
package com.redhat.rhevm.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/* FIXME: doesn't seem to do anything ? Also, we could do without
 *        the explicit dependency on RESTeasy
 */
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;

@Path("/vms")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
@Formatted
public interface VmsResource {
    /* REVISIT: Singleton lifecycle probably requires that UriInfo
     * must be modelled as a method parameter, as there would be
     * concurrency issues around injection into a data member
     */

    @GET
    public VMs list(@Context UriInfo uriInfo);

    /* FIXME: need to move this to e.g. a top-level /search
     * @GET
     * public VMs search(String criteria);
     */

    /**
     * Creates a new VM and adds it to the database. The VM is created
     * based on the properties of @vm.
     * <p>
     * The VM#name, VM#templateId and VM#clusterId properties are required.
     *
     * @param vm  the VM definition from which to create the new VM
     * @return    the new newly created VM
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    public Response add(@Context UriInfo uriInfo, VM vm);

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id);

    /**
     * Sub-resource locator method, returns individual VmResource on which the
     * remainder of the URI is dispatched.
     *
     * @param id  the VM ID
     * @return    matching subresource if found
     */
    @Path("{id}")
    public VmResource getVmSubResource(@Context UriInfo uriInfo, @PathParam("id") String id);
}