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
package com.redhat.rhevm.api.common.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.easymock.IExpectationSetters;

import static org.easymock.classextension.EasyMock.expect;


public class ReapableMapTest extends Assert {

    private static final String[] NUMBERS = { "one", "two", "three", "four", "five" };

    private static final NumberMapper MAPPER = new NumberMapper();

    private IMocksControl control;
    private ReapedMap<String, Integer> map;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
    }

    @Test
    public void testReapingWithoutGC() throws Exception {
        map = new ReapedMap<String, Integer>(1000);
        populate(1, 2, 3);
        assertSizes(3, 0);
        assertExpected(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        map.reapable("one");
        assertSizes(1, 2);
        assertExpected(1, 2, 3);
        assertEquals(new Integer(3), map.remove("three"));
        assertSizes(1, 1);
        assertExpected(1, 2);
        assertNull(map.get("three"));
        Thread.sleep(1100);
        assertExpected(1);
        assertNull(map.get("one"));
        assertExpected(2);
        assertSizes(1, 0);
        populate(4, 5);
        map.clear();
    }

    @Test
    public void testReapingOnGetWithGC() throws Exception {
        setUpGCExpectations(5);

        populate(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        assertExpected(3);
        assertSizes(1, 1);
        assertNull(map.get("three"));

        control.verify();
    }

    @Test
    public void testReapingOnGetWithAccessBasedAging() throws Exception {
        setUpAccessBaseAgingExpectations();

        populate(1, 2, 3);
        assertSizes(3, 0);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        for (int i = 0 ; i < 6 ; i++) {
            Thread.sleep(200);
            assertExpected(i == 0 ? 1 : 3);
        }
        assertSizes(1, 1);
        assertNull(map.get("one"));
        assertExpected(3);
        Thread.sleep(500);
        assertExpected(3);
        assertSizes(1, 1);

        Thread.sleep(1200);
        assertExpected(2);
        assertSizes(1, 0);
        assertNull(map.get("three"));

        control.verify();
    }

    @Test
    public void testReapingOnPutWithGC() throws Exception {
        setUpGCExpectations(5);

        populate(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        populate(4);
        assertSizes(2, 1);
        assertNull(map.get("three"));

        control.verify();
    }

    @Test
    public void testReapingOnRemoveWithGC() throws Exception {
        setUpGCExpectations(5);

        populate(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        map.remove("two");
        assertSizes(0, 1);
        assertNull(map.get("three"));

        control.verify();
    }

    @SuppressWarnings("unchecked")
    private void setUpGCExpectations(int gcAfter) {
        ReferenceQueue<Integer> queue = (ReferenceQueue<Integer>)control.createMock(ReferenceQueue.class);
        map = new ReapedMap<String, Integer>(10000, false, queue);
        for (int i = 0 ; i < gcAfter ; i++) {
            expect(queue.poll()).andReturn(null);
        }
        // the gcAfter^th queue poll simulates a GC event and triggers deletion
        // on the reapable map
        ReapedMap.IdAwareReference<String, Integer> ref = control.createMock(ReapedMap.IdAwareReference.class);
        // awkward syntax required to work around compilation error
        // on Reference<capture#nnn ? extends Integer> mismatch
        IExpectationSetters<? extends Reference<? extends Integer>> qSetter = expect(queue.poll());
        ((IExpectationSetters<Reference<? extends Integer>>)qSetter).andReturn(ref);
        IExpectationSetters<? extends String> refSetter = expect(ref.getKey());
        ((IExpectationSetters<String>)refSetter).andReturn("three");

        control.replay();
    }

    @SuppressWarnings("unchecked")
    private void setUpAccessBaseAgingExpectations() {
        ReferenceQueue<Integer> queue = (ReferenceQueue<Integer>)control.createMock(ReferenceQueue.class);
        map = new ReapedMap<String, Integer>(1000, true, queue);
        expect(queue.poll()).andReturn(null).anyTimes();

        control.replay();
    }

    private void populate(Integer ... values) {
        for (Integer v: values) {
            map.put(MAPPER.getKey(v), v);
        }
    }

    private void assertSizes(int i, int j) {
        assertEquals("unexpected primary map size", i, map.size());
        assertEquals("unexpected secondary map size", j, map.reapableSize());
    }

    private void assertExpected(Integer ... values) {
        for (Integer v: values) {
            assertEquals(v, map.get(MAPPER.getKey(v)));
        }
    }

    private static class NumberMapper {
        public String getKey(Integer i) {
            return i <= NUMBERS.length ? NUMBERS[i-1] : null;
        }
    };
}