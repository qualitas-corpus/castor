/*
 * Copyright 2005 Ralf Joachim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.castor.cache.simple;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.castor.cache.Cache;

/**
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8143 $ $Date: 2006-04-29 04:11:14 -0600 (Sat, 29 Apr 2006) $
 * @since 1.0
 */
public final class TestUnlimited extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("Unlimited Tests");

        suite.addTest(new TestUnlimited("testBasics"));

        suite.addTest(new TestUnlimited("testContainsKey"));
        suite.addTest(new TestUnlimited("testContainsValue"));
        suite.addTest(new TestUnlimited("testClear"));

        suite.addTest(new TestUnlimited("testSize"));
        suite.addTest(new TestUnlimited("testIsEmpty"));
        suite.addTest(new TestUnlimited("testGet"));
        suite.addTest(new TestUnlimited("testPut"));
        suite.addTest(new TestUnlimited("testRemove"));
        suite.addTest(new TestUnlimited("testPutAll"));
        
        suite.addTest(new TestUnlimited("testKeySet"));
        suite.addTest(new TestUnlimited("testValues"));
        suite.addTest(new TestUnlimited("testEntrySet"));

        return suite;
    }

    public TestUnlimited(final String name) { super(name); }
    
    public void testBasics() {
        assertEquals("unlimited", Unlimited.TYPE);

        Cache cache = new Unlimited();
        assertTrue(cache instanceof Unlimited);

        assertEquals("unlimited", cache.getType());

        assertFalse(cache.containsKey("first key"));
        assertFalse(cache.containsKey("second key"));

        assertNull(cache.put("first key", "first value"));

        assertTrue(cache.containsKey("first key"));
        assertFalse(cache.containsKey("second key"));

        assertNull(cache.put("second key", "second value"));

        assertTrue(cache.containsKey("first key"));
        assertTrue(cache.containsKey("second key"));
    }

    private Cache initialize() {
        Cache cache = new Unlimited();

        assertNull(cache.put("first key", "first value"));
        assertNull(cache.put("second key", "second value"));
        assertNull(cache.put("third key", "third value"));
        
        return cache;
    }
    
    public void testContainsKey() {
        Cache cache = initialize();

        assertTrue(cache.containsKey("first key"));
        assertTrue(cache.containsKey("second key"));
        assertTrue(cache.containsKey("third key"));
        assertFalse(cache.containsKey("fourth key"));
        assertFalse(cache.containsKey("fifth key"));
    }

    public void testContainsValue() {
        Cache cache = initialize();

        assertTrue(cache.containsValue("first value"));
        assertTrue(cache.containsValue("second value"));
        assertTrue(cache.containsValue("third value"));
        assertFalse(cache.containsValue("fourth value"));
        assertFalse(cache.containsValue("fifth value"));
    }

    public void testClear() {
        Cache cache = initialize();

        cache.clear();

        assertFalse(cache.containsKey("first key"));
        assertFalse(cache.containsKey("second key"));
        assertFalse(cache.containsKey("third key"));
        assertFalse(cache.containsKey("fourth key"));
        assertFalse(cache.containsKey("fifth key"));
    }

    public void testSize() {
        Cache cache = initialize();

        assertEquals(3, cache.size());
        cache.clear();
        assertEquals(0, cache.size());
    }

    public void testIsEmpty() {
        Cache cache = initialize();

        assertFalse(cache.isEmpty());
        cache.clear();
        assertTrue(cache.isEmpty());
    }

    public void testGet() {
        Cache cache = initialize();

        assertEquals("first value", cache.get("first key"));
        assertEquals("second value", cache.get("second key"));
        assertEquals("third value", cache.get("third key"));
        assertNull(cache.get("fourth key"));
        assertNull(cache.get("fifth key"));
    }

    public void testPut() {
        Cache cache = initialize();

        assertEquals("third value", cache.put("third key", "alternate third value"));
        assertNull(cache.put("fourth key", "forth value"));

        assertTrue(cache.containsKey("first key"));
        assertTrue(cache.containsKey("second key"));
        assertTrue(cache.containsKey("third key"));
        assertTrue(cache.containsKey("fourth key"));
        assertFalse(cache.containsKey("fifth key"));
    }

    public void testRemove() {
        Cache cache = initialize();

        assertEquals("third value", cache.remove("third key"));

        assertTrue(cache.containsKey("first key"));
        assertTrue(cache.containsKey("second key"));
        assertFalse(cache.containsKey("third key"));
        assertFalse(cache.containsKey("fourth key"));
        assertFalse(cache.containsKey("fifth key"));
    }

    public void testPutAll() {
        Cache cache = initialize();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("fourth key", "forth value");
        map.put("fifth key", "fifth value");
        
        cache.putAll(map);
        
        assertTrue(cache.containsKey("first key"));
        assertTrue(cache.containsKey("second key"));
        assertTrue(cache.containsKey("third key"));
        assertTrue(cache.containsKey("fourth key"));
        assertTrue(cache.containsKey("fifth key"));
    }

    public void testKeySet() {
        Cache cache = initialize();

        Set<Object> set = cache.keySet();
        
        assertEquals(3, set.size());
        assertTrue(set.contains("first key"));
        assertTrue(set.contains("second key"));
        assertTrue(set.contains("third key"));
    }

    public void testValues() {
        Cache cache = initialize();

        Collection<Object> col = cache.values();
        
        assertEquals(3, col.size());
        assertTrue(col.contains("first value"));
        assertTrue(col.contains("second value"));
        assertTrue(col.contains("third value"));
    }

    public void testEntrySet() {
        Cache cache = initialize();

        Set<Map.Entry<Object, Object>> set = cache.entrySet();
        
        assertEquals(3, set.size());
        
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        for (Iterator<Map.Entry<Object, Object>> iter = set.iterator(); iter.hasNext();) {
            Map.Entry<Object, Object> entry = iter.next();
            map.put(entry.getKey(), entry.getValue());
        }

        assertTrue(map.containsKey("first key"));
        assertEquals("first value", map.get("first key"));

        assertTrue(map.containsKey("second key"));
        assertEquals("second value", map.get("second key"));

        assertTrue(map.containsKey("third key"));
        assertEquals("third value", map.get("third key"));
    }
}
