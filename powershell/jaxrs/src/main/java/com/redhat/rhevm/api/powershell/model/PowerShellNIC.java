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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.MAC;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.NicType;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellNIC {

    public static List<NIC> parse(PowerShellParser parser, String vmId, String output) {
        List<NIC> ret = new ArrayList<NIC>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            NIC nic = new NIC();

            nic.setId(entity.get("id"));
            nic.setName(entity.get("name"));

            nic.setVm(new VM());
            nic.getVm().setId(vmId);

            Network network = new Network();
            network.setName(entity.get("network"));
            nic.setNetwork(network);

            nic.setType(NicType.fromValue(entity.get("type").toUpperCase()));

            MAC mac = new MAC();
            mac.setAddress(entity.get("macaddress"));
            nic.setMac(mac);

            if (entity.get("address") != null ||
                entity.get("subnet") != null ||
                entity.get("gateway") != null) {
                IP ip = new IP();
                ip.setAddress(entity.get("address"));
                ip.setNetmask(entity.get("subnet"));
                ip.setGateway(entity.get("gateway"));
                nic.setIp(ip);
            }

            ret.add(nic);
        }

        return ret;
    }
}
