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
package com.redhat.rhevm.api.powershell.resource;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellHostsResource
    extends AbstractPowerShellCollectionResource<Host, PowerShellHostResource>
    implements HostsResource {

    public List<Host> runAndParse(String command) {
        return PowerShellHostResource.runAndParse(getPool(), getParser(), command);
    }

    public Host runAndParseSingle(String command) {
        return PowerShellHostResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Hosts list() {
        Hosts ret = new Hosts();
        for (Host host : runAndParse(getSelectCommand("select-host", getUriInfo(), Host.class))) {
            ret.getHosts().add(PowerShellHostResource.addLinks(getUriInfo(), host));
        }
        return ret;
    }

    @Override
    public Response add(Host host) {
        validateParameters(host, "name", "address", "rootPassword");
        StringBuilder buf = new StringBuilder();

        String clusterArg = getClusterArg(buf, host);

        buf.append("add-host");

        buf.append(" -name " + PowerShellUtils.escape(host.getName()));
        buf.append(" -address " + PowerShellUtils.escape(host.getAddress()));

        buf.append(" -rootpassword " + PowerShellUtils.escape(host.getRootPassword()));

        buf.append(clusterArg);

        if (host.isSetPort()) {
            buf.append(" -port " + host.getPort());
        }

        host = PowerShellHostResource.addLinks(getUriInfo(), runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(host.getId());

        return Response.created(uriBuilder.build()).entity(host).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-host -hostid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public HostResource getHostSubResource(String id) {
        return getSubResource(id);
    }

    protected PowerShellHostResource createSubResource(String id) {
        return new PowerShellHostResource(id, getExecutor(), this, shellPools, getParser());
    }

    private String getClusterArg(StringBuilder buf, Host host) {
        String clusterArg = "";
        if (host.isSetCluster()) {
            if (host.getCluster().isSetId()) {
                clusterArg = " -hostclusterid " + PowerShellUtils.escape(host.getCluster().getId());
            } else if (host.getCluster().isSetName())  {
                buf.append("$c = select-cluster -searchtext ");
                buf.append(PowerShellUtils.escape("name=" + host.getCluster().getName()));
                buf.append(";");
                clusterArg = " -hostclusterid $c.ClusterId";
            }
        }
        return clusterArg;
    }
}
