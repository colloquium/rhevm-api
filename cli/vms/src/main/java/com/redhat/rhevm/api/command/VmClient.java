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
package com.redhat.rhevm.api.command;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.ws.rs.core.Response;

import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;

/**
 * VM client make RESTful invocations via a direct API (to avoid leakage
 * of the URI structure, as would occur via a proxy-based API).
 */
public class VmClient {

    protected String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected VMs getVMs() throws Exception {
        WebClient get = WebClient.create(getTopLink("vms"));
        Response vms = get.path("/").accept("application/xml").get();
        InputStream is = (InputStream)vms.getEntity();
        JAXBContext context = JAXBContext.newInstance(VMs.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (VMs)unmarshaller.unmarshal(is);
    }

    protected void actionVM(String action, Link link) throws Exception {
        WebClient post = WebClient.create(link.getHref());
        Response r = post.path("/").post(null);
        if (r.getStatus() == 204) {
            System.out.println(action + " succeeded");
        } else {
            System.err.println(action + " failed");
        }
    }

    protected String getTopLink(String rel) throws Exception {
        BindingFactoryManager manager =
            BusFactory.getDefaultBus().getExtension(BindingFactoryManager.class);
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, 
                                       new JAXRSBindingFactory());

        WebClient head = WebClient.create(getBaseUrl());
        Response links = head.path("/").head();
        return links.getStatus() == 200 ? getLink(links, rel) : null;
    }

    private String getLink(Response r, String rel) {
        String link = null;
        List<Object> links = r.getMetadata().get("Link");
        for (Object o : links) {
            String l = (String)o;
            if (l.endsWith("rel=" + rel)) {
                return l.substring(l.indexOf("<") + 1, l.indexOf(">"));
            }
        }
        return link;
    }
}
