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
import javax.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;

/**
 * Represents a users sub-collection, for example scoped by VM.
 */
@Produces(MediaType.APPLICATION_XML)
public interface AttachedUsersResource {

    @GET
    @Formatted
    public Users list();

    @POST
    @Formatted
    @Consumes(MediaType.APPLICATION_XML)
    public Response add(User User);

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id);

    /**
     * Sub-resource locator method, returns individual UserResource on which the
     * remainder of the URI is dispatched.
     *
     * @param id  the User ID
     * @return    matching subresource if found
     */
    @Path("{id}")
    public UserResource getUserSubResource(@PathParam("id") String id);
}
