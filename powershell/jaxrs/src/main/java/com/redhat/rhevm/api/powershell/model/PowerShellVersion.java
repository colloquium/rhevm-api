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

import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellVersion {

    public static List<Version> parse(PowerShellParser parser, String output) {
        List<Version> ret = new ArrayList<Version>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            Version version = new Version();
            version.setMajor(entity.get("major", Integer.class));
            version.setMinor(entity.get("minor", Integer.class));
            ret.add(version);
        }

        return ret;
    }
}
