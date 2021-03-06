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

import org.junit.Test;

import java.util.List;

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostStatus;
import com.redhat.rhevm.api.model.PowerManagement;

public class PowerShellHostTest extends PowerShellModelTest {

    private void testPowerManagement(PowerManagement p, String type, Boolean enabled, String address, String username, String[] optionNames, String[] optionValues) {
        assertEquals(type, p.getType());
        assertEquals(enabled, p.isEnabled());
        assertEquals(address, p.getAddress());
        assertEquals(username, p.getUsername());
        assertFalse(p.isSetPassword());
        assertEquals(optionNames.length, p.getOptions().getOptions().size());
        for (int i = 0; i < optionNames.length; i++) {
            assertEquals(optionNames[i], p.getOptions().getOptions().get(i).getName());
            assertEquals(optionValues[i], p.getOptions().getOptions().get(i).getValue());
            assertFalse(p.getOptions().getOptions().get(i).isSetType());
        }
    }

    private void testHost(Host h, String id, String name, HostStatus status, String clusterId, int port) {
        assertEquals(h.getId(), id);
        assertEquals(h.getName(), name);
        assertEquals(h.getStatus(), status);
        assertTrue(h.isSetCluster());
        assertEquals(clusterId, h.getCluster().getId());
        assertEquals(Integer.valueOf(port), h.getPort());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("host.xml");
        assertNotNull(data);

        List<Host> hosts = PowerShellHost.parse(getParser(), data);

        assertEquals(hosts.size(), 1);

        testHost(hosts.get(0), "5f38363b-7457-4884-831e-78c27cebb31d", "zig", HostStatus.UP, "99408929-82cf-4dc7-a532-9d998063fa95", 54321);

        assertNotNull(hosts.get(0).getPowerManagement());
        testPowerManagement(hosts.get(0).getPowerManagement(), "ilo", true, "192.168.1.107", "foo", new String[] { "secure", "port" }, new String[] { "true", "12345" });
    }
}
