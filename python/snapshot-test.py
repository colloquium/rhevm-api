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
import time
from testutils import *

opts = parseOptions()

(vm_name, ) = (None, )
if len(opts['oargs']) >= 1:
   (vm_name, ) = opts['oargs'][0:1]

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   if vm_name is None:
       continue

   vm = t.find(links['vms'], vm_name)

   for snap in t.get(vm.link['snapshots'].href):
       t.get(snap.href)

   snap = fmt.Snapshot()
   snap = t.create(vm.link['snapshots'].href, snap)

   t.syncAction(snap.actions, 'restore')

   t.delete(snap.href)
