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

import java.text.MessageFormat;
import org.junit.Assert;
import org.junit.Ignore;
import com.redhat.rhevm.api.powershell.util.PowerShellTestUtils;

@Ignore
public class BasePowerShellResourceTest extends Assert {

    protected String formatXmlReturn(String type, String[] names, String[] descriptions, String[] args) {
        String tmpl = PowerShellTestUtils.readClassPathFile(type + ".tmpl");
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\"?>");
        buffer.append("<Objects>");
        for (int i = 0; i < names.length; i++) {
            buffer.append(MessageFormat.format(tmpl, buildArgs(Integer.toString(names[i].hashCode()),
                                                               names[i],
                                                               descriptions[i],
                                                               args)));
        }
        buffer.append("</Objects>");
        return buffer.toString();
    }

    static Object[] buildArgs(String id, String name, String description, Object[] args) {
        Object[] newArgs = new Object[args.length + 3];
        newArgs[0] = id;
        newArgs[1] = name;
        newArgs[2] = description;
        System.arraycopy(args, 0, newArgs, 3, args.length);
        return newArgs;
    }
}