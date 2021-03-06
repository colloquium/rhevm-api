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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellStorageDomainResource extends AbstractPowerShellActionableResource<StorageDomain> implements StorageDomainResource {

    private PowerShellStorageDomainsResource parent;
    private StorageDomain tornDown;

    public PowerShellStorageDomainResource(String id,
                                           PowerShellStorageDomainsResource parent,
                                           PowerShellPoolMap shellPools,
                                           PowerShellParser parser) {
        super(id, parent.getExecutor(), parent, shellPools, parser);
        this.parent = parent;
    }

    public StorageDomain getTornDown() {
        return tornDown;
    }

    public static List<StorageDomain> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        List<PowerShellStorageDomain> storageDomains =
            PowerShellStorageDomain.parse(parser, PowerShellCmd.runCommand(pool, command));
        List<StorageDomain> ret = new ArrayList<StorageDomain>();
        for (PowerShellStorageDomain storageDomain : storageDomains) {
            ret.add(storageDomain);
        }
        return ret;
    }

    /**
     * Run a powershell command and parse the output as a single storage
     * domain.
     *
     * @param command the powershell command to execute
     * @param whether the 'sharedStatus' property is needed
     * @return a single storage domain, or null
     */
    public static StorageDomain runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<StorageDomain> storageDomains = runAndParse(pool, parser, command);

        return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
    }

    public StorageDomain runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    public static StorageDomain addLinks(UriInfo uriInfo, StorageDomain storageDomain) {
        storageDomain = JAXBHelper.clone("storage_domain", StorageDomain.class, storageDomain);

        storageDomain = LinkHelper.addLinks(uriInfo, storageDomain);

        ActionsBuilder actionsBuilder = new ActionsBuilder(LinkHelper.getUriBuilder(uriInfo, storageDomain),
                                                           StorageDomainResource.class);
        storageDomain.setActions(actionsBuilder.build());

        return storageDomain;
    }

    @Override
    public StorageDomain get() {
        StorageDomain storageDomain;
        if (tornDown != null) {
            storageDomain = tornDown;
        } else {
            storageDomain = runAndParseSingle("get-storagedomain " + PowerShellUtils.escape(getId()));
        }
        return addLinks(getUriInfo(), storageDomain);
    }

    @Override
    public StorageDomain update(StorageDomain storageDomain) {
        validateUpdate(storageDomain);

        StringBuilder buf = new StringBuilder();
        if (tornDown != null) {
            // update writable fields only
            if (storageDomain.isSetName()) {
                tornDown.setName(storageDomain.getName());
            }
            storageDomain = tornDown;
        } else {
            buf.append("$d = get-storagedomain " + PowerShellUtils.escape(getId()) + ";");

            if (storageDomain.isSetName()) {
                buf.append("$d.name = " + PowerShellUtils.escape(storageDomain.getName()) + ";");
            }

            buf.append("update-storagedomain -storagedomainobject $d");

            storageDomain = runAndParseSingle(buf.toString());
        }
        return addLinks(getUriInfo(), storageDomain);
    }

    @Override
    public Response teardown(Action action) {
        validateParameters(action, "host.id|name");
        return doAction(getUriInfo(), new StorageDomainTeardowner(action));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return null;
    }

    private class StorageDomainTeardowner extends AbstractPowerShellActionTask {

        public StorageDomainTeardowner(Action action) {
            super(action, "remove-storagedomain -force");
        }

        public void execute() {
            String id = PowerShellStorageDomainResource.this.getId();

            StorageDomain storageDomain = runAndParseSingle("get-storagedomain " + PowerShellUtils.escape(id));

            StringBuilder buf = new StringBuilder();

            String hostArg = null;
            if (action.getHost().isSetId()) {
                hostArg = PowerShellUtils.escape(action.getHost().getId());
            } else {
                buf.append("$h = select-host -searchtext ");
                buf.append(PowerShellUtils.escape("name=" +  action.getHost().getName()));
                buf.append(";");
                hostArg = "$h.hostid";
            }

            buf.append(command);

            buf.append(" -storagedomainid " + PowerShellUtils.escape(id));

            buf.append(" -hostid " + hostArg);

            PowerShellCmd.runCommand(getPool(), buf.toString());

            storageDomain.setStatus(StorageDomainStatus.TORNDOWN);
            PowerShellStorageDomainResource.this.tornDown = storageDomain;
            PowerShellStorageDomainResource.this.parent.addToTornDownDomains(id, PowerShellStorageDomainResource.this);
        }
    }
}
