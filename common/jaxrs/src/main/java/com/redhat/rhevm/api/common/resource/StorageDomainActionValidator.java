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
package com.redhat.rhevm.api.common.resource;

import com.redhat.rhevm.api.model.ActionValidator;
import com.redhat.rhevm.api.model.StorageDomain;

public class StorageDomainActionValidator implements ActionValidator {
    private StorageDomain storageDomain;

    public StorageDomainActionValidator(StorageDomain storageDomain) {
        this.storageDomain = storageDomain;
    }

    @Override
    public boolean validateAction(String action) {
        if (storageDomain.getStatus() == null) {
            return false;
        }
        switch (storageDomain.getStatus()) {
        case UNATTACHED:
            return action.equals("teardown");
        case ACTIVE:
        case INACTIVE:
        case LOCKED:
        case MIXED:
        case TORNDOWN:
            return false;
        default:
            assert false : storageDomain.getStatus();
            return false;
        }
    }
}
