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

import xml.dom
import xml.dom.minidom

def getText(nodelist):
    rc = ""
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc = rc + node.data
    return rc

def parseNode(node):
    ret = {}
    for n in node.attributes.keys():
        n = node.attributes[n]
        ret[n.nodeName] = n.nodeValue
    for n in node.childNodes:
        if n.nodeType != n.ELEMENT_NODE:
            continue
        if n.nodeName == 'link':
            ret[n.attributes['rel'].nodeValue] = n.attributes['href'].nodeValue
        else:
            ret[n.nodeName] = getText(n.childNodes)
    return ret

class Base:
    def __init__(self, node):
        dict = parseNode(node)
        for key in self.KEYS:
            if key in dict:
                setattr(self, key, dict[key])

    def __str__(self):
        dict = {}
        for key in self.KEYS:
            if hasattr(self, key):
                dict[key] = getattr(self, key)
        return str(dict)

class VM(Base):
    ROOT_ELEMENT = 'vm'
    KEYS = ['id', 'name', 'self']

class Host(Base):
    ROOT_ELEMENT = 'host'
    KEYS = ['id', 'name', 'self']

def parseVM(doc):
    return VM(xml.dom.minidom.parseString(doc).getElementsByTagName(VM.ROOT_ELEMENT)[0])

def parseHost(doc):
    return Host(xml.dom.minidom.parseString(doc).getElementsByTagName(Host.ROOT_ELEMENT)[0])

def parseCollection(doc, entityType):
    collection = xml.dom.minidom.parseString(doc).getElementsByTagName('collection')[0]

    ret = []
    for n in collection.childNodes:
        if n.nodeType != n.ELEMENT_NODE or n.nodeName != entityType.ROOT_ELEMENT:
            continue
        ret.append(entityType(n))

    return ret

def parseVmCollection(doc):
    return parseCollection(doc, VM)

def parseHostCollection(doc):
    return parseCollection(doc, Host)