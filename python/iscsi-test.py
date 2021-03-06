#!/usr/bin/env python

# Copyright (C) 2010 Red Hat, Inc.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

import http
import xmlfmt
import yamlfmt
import jsonfmt
import sys
import getopt
import time
from testutils import *

opts = parseOptions()

(host, portal, target, lun) = (None, None, None, None)
if len(opts['oargs']) >= 4:
   (host, portal, target, lun) = opts['oargs'][0:4]

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
    t = TestUtils(opts, fmt)

    print "=== ", fmt.MEDIA_TYPE, " ==="

    if host is None:
        continue

    h = fmt.Host()
    h.name = host

    dom = fmt.StorageDomain()
    dom.name = randomName("iscsi")
    dom.type = 'DATA'
    dom.storage = fmt.Storage()
    dom.storage.type = 'ISCSI'
    dom.storage.logical_unit = fmt.LogicalUnit()
    dom.storage.logical_unit.address = portal
    dom.storage.logical_unit.target = target
    dom.storage.logical_unit.id = lun
    dom.host = h
    dom = t.create(links['storagedomains'], dom)

    t.syncAction(dom.actions, "teardown", host=h)

    t.delete(dom.href)
