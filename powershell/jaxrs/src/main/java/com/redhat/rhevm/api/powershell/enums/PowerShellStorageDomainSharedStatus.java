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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

import com.redhat.rhevm.api.model.StorageDomainStatus;

public enum PowerShellStorageDomainSharedStatus {
    Unattached(0), Active(1), InActive(2), Mixed(3), Locked(4);

    private static HashMap<Integer, PowerShellStorageDomainSharedStatus> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellStorageDomainSharedStatus>();
        for (PowerShellStorageDomainSharedStatus value : values()) {
            mapping.put(value.value, value);
        }
    }

    private int value;

    private PowerShellStorageDomainSharedStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public StorageDomainStatus map() {
        switch (this) {
        case Unattached:
            return StorageDomainStatus.UNATTACHED;
        case Active:
            return StorageDomainStatus.ACTIVE;
        case InActive:
            return StorageDomainStatus.INACTIVE;
        case Mixed:
            return StorageDomainStatus.MIXED;
        case Locked:
            return StorageDomainStatus.LOCKED;
        default:
            assert false : this;
            return null;
        }
    }

    public static PowerShellStorageDomainSharedStatus forValue(int value) {
        return mapping.get(value);
    }
}
