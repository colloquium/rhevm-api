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

import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;

public class PowerShellDiskTest extends PowerShellModelTest {

    private static final String VM_ID = "439c0c13-3e0a-489e-a514-1b07232ace41";

    private void testDisk(PowerShellDisk d, String id, String vmId, Long size, DiskType type, DiskStatus status, DiskInterface iface, DiskFormat format, Boolean sparse, Boolean bootable, Boolean wipeAfterDelete, Boolean propagateErrors, String vmSnapshotId, String parentId, String internalDriveMapping, String lastModified) {
        assertEquals(id, d.getId());
        assertNotNull(d.getVm());
        assertEquals(vmId, d.getVm().getId());
        assertEquals(size, d.getSize());
        assertEquals(type, d.getType());
        assertEquals(status, d.getStatus());
        assertEquals(iface, d.getInterface());
        assertEquals(format, d.getFormat());
        assertEquals(sparse, d.isSparse());
        assertEquals(bootable, d.isBootable());
        assertEquals(wipeAfterDelete, d.isWipeAfterDelete());
        assertEquals(propagateErrors, d.isPropagateErrors());
        assertEquals(vmSnapshotId, d.getVmSnapshotId());
        assertEquals(parentId, d.getParentId());
        assertEquals(internalDriveMapping, d.getInternalDriveMapping());
        assertEquals(lastModified, d.getLastModified().toString());
    }

    @Test
    public void testParseDisks() throws Exception {
        String data = readFileContents("disks.xml");
        assertNotNull(data);

        List<PowerShellDisk> disks = PowerShellDisk.parse(getParser(), VM_ID, data);

        assertNotNull(disks);
        assertEquals(disks.size(), 1);

        testDisk(disks.get(0), "0b9318b4-e426-4380-9e6a-bb7f3a38a2ce", VM_ID, 1341231104L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.IDE, DiskFormat.RAW, true, true, null, null, "563055c6-b1ec-4f31-85a1-6354a916a0b5", "00000000-0000-0000-0000-000000000000", "1", "1969-12-10T17:29:35.000Z");
    }

    @Test
    public void testParseDisks22() throws Exception {
        String data = readFileContents("disks22.xml");
        assertNotNull(data);

        List<PowerShellDisk> disks = PowerShellDisk.parse(getParser(), VM_ID, data);

        assertNotNull(disks);
        assertEquals(disks.size(), 2);

        testDisk(disks.get(0), "222ea10f-7c0a-4302-8e80-2834b8fa681a", VM_ID, 1073741824L, DiskType.DATA, DiskStatus.OK, DiskInterface.IDE, DiskFormat.COW, true, null, null, null, "22a659ab-29a3-4160-9647-bb07753c612e", "00000000-0000-0000-0000-000000000000", "2", "2010-07-22T10:40:27.000Z");
        testDisk(disks.get(1), "0e833f37-3437-44f2-a04f-6f9692882431", VM_ID, 2147483648L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.VIRTIO, DiskFormat.RAW, null, true, true, null, "1d122de8-1aa2-4b07-9d42-937333ea577d", "00000000-0000-0000-0000-000000000000", "1", "2010-07-22T10:59:42.000Z");
    }
}
