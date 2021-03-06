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

import java.util.List;

import javax.ws.rs.WebApplicationException;

import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.TagParent;

import org.junit.Test;

public class PowerShellTagsResourceTest
    extends AbstractPowerShellCollectionResourceTest<Tag, PowerShellTagResource, PowerShellTagsResource> {

    private static final String PARENT_NAME = "parent";
    private static final String PARENT_ID = asId(PARENT_NAME);

    private static final String ADD_WITH_PARENT_PROLOG = "$parent = get-tag \"" + PARENT_ID + "\"; ";
    private static final String ADD_WITH_PARENT_EPILOG = " -parenttagobject $parent";

    private static final String GET_TAGS_COMMAND = "$tags = @(); $tags += get-tag -1; $tags += get-tags; $tags";

    public static final String[] extraArgs = new String[]{};

    public PowerShellTagsResourceTest() {
        super(new PowerShellTagResource("0", null, null, null, null), "tags", "tag", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(GET_TAGS_COMMAND, getSelectReturn(), NAMES));
        List<Tag> tags = resource.list().getTags();
        verifyCollection(tags, NAMES, DESCRIPTIONS);
    }

    @Test
    public void testAdd() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(asArray(getAddCommand()),
                                                      asArray(getAddReturn()),
                                                      true,
                                                      null,
                                                      NEW_NAME));
        verifyResponse(resource.add(getModel(NEW_NAME, NEW_DESCRIPTION)),
                       NEW_NAME,
                       NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithParent() throws Exception {
        Tag tag = getModel(NEW_NAME, NEW_DESCRIPTION);
        tag.setParent(new TagParent());
        tag.getParent().setTag(new Tag());
        tag.getParent().getTag().setId(PARENT_ID);

        resource.setUriInfo(setUpResourceExpectations(asArray(ADD_WITH_PARENT_PROLOG +
                                                              getAddCommand() +
                                                              ADD_WITH_PARENT_EPILOG),
                                                      asArray(getAddReturn()),
                                                      true,
                                                      null,
                                                      NEW_NAME));
        verifyResponse(resource.add(tag), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(new Tag());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Tag", "add", "name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        verifyResource(
            (PowerShellTagResource)resource.getTagSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellTagsResource getResource() {
        return new PowerShellTagsResource();
    }

    protected void populateModel(Tag tag) {
    }
}
