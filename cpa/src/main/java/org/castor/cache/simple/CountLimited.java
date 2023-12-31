/*
 * Copyright 2005 Thomas Yip, Werner Guttmann, Ralf Joachim
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.castor.cache.AbstractBaseCache;
import org.castor.cache.CacheAcquireException;

/**
 * CountLimited is a count limted least-recently-used <tt>Map</tt>. Every object being
 * put in the Map will live until the map is full. If the map is full, the least recently
 * used object will be disposed. 
 * <p>
 * The capacity is passed to the cache at initialization by the individual cache property
 * <b>capacity</b> which defines the maximum number of objects the cache can hold. If not
 * specified a default capacity of 30 objects will be used.
 *
 * @author <a href="mailto:tyip AT leafsoft DOT com">Thomas Yip</a>
 * @author <a href="mailto:werner DOT guttmann AT gmx DOT net">Werner Guttmann</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8102 $ $Date: 2006-04-25 16:09:10 -0600 (Tue, 25 Apr 2006) $
 */
public final class CountLimited extends AbstractBaseCache {
    //--------------------------------------------------------------------------

    /** The type of the cache. */
    public static final String TYPE = "count-limited";
    
    /** Mapped initialization parameter <code>capacity</code>. */
    public static final String PARAM_CAPACITY = "capacity";

    /** Default capacity of cache. */
    public static final int DEFAULT_CAPACITY = 30;
    
    /** Status of cache entries that can be replaced at next put. */
    private static final int LRU_OLD = 0;

    /** Status of new cache entries that should not be replaced. */
    private static final int LRU_NEW = 1;
    
    /** Map keys to positions. */
    private Hashtable<Object, Integer> _mapKeyPos = null;
    
    /** Array of keys. */
    private Object[] _keys = null;
    
    /** Array of values. */
    private Object[] _values = null;
    
    /** Array with status of the entries. */
    private int[] _status = null;
    
    /** Real capacity of this cache. */
    private int _capacity = DEFAULT_CAPACITY;

    /** Current position to check if value can be replaced at put. */
    private int _cur = 0;
    
    //--------------------------------------------------------------------------
    // operations for life-cycle management of cache
    
    /**
     * {@inheritDoc}
     */
    public void initialize(final Properties params) throws CacheAcquireException {
        super.initialize(params);
        
        String param = params.getProperty(PARAM_CAPACITY);
        try {
            if (param != null) { _capacity = Integer.parseInt(param); }
            if (_capacity <= 0) { _capacity = DEFAULT_CAPACITY; }
        } catch (NumberFormatException ex) {
            _capacity = DEFAULT_CAPACITY;
        }

        _mapKeyPos = new Hashtable<Object, Integer>(_capacity);
        _keys = new Object[_capacity];
        _values = new Object[_capacity];
        _status = new int[_capacity];
    }

    //--------------------------------------------------------------------------
    // getters/setters for cache configuration

    /**
     * {@inheritDoc}
     */
    public String getType() { return TYPE; }
    
    /**
     * Get real capacity of this cache.
     * 
     * @return Real capacity of this cache.
     */
    public int getCapacity() { return _capacity; }

    //--------------------------------------------------------------------------
    // query operations of map interface

    /**
     * {@inheritDoc}
     */
    public synchronized int size() { return _mapKeyPos.size(); }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean isEmpty() { return _mapKeyPos.isEmpty(); }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean containsKey(final Object key) {
        return _mapKeyPos.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean containsValue(final Object value) {
        Iterator<Integer> iter = _mapKeyPos.values().iterator();
        while (iter.hasNext()) {
            Integer pos = iter.next();
            if (pos != null) {
                if (value == null) {
                    if (_values[pos.intValue()] == null) { return true; }  
                } else {
                    if (value.equals(_values[pos.intValue()])) { return true; }  
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Object get(final Object key) {
        Integer pos = _mapKeyPos.get(key);
        if (pos == null) { return null; }
        int intPos = pos.intValue();
        _status[intPos] = LRU_NEW;
        return _values[intPos]; 
    }
    
    //--------------------------------------------------------------------------
    // modification operations of map interface
    
    /**
     * {@inheritDoc}
     */
    public synchronized Object put(final Object key, final Object value) {
        Integer pos = _mapKeyPos.get(key);
        if (pos != null) {
            int intPos = pos.intValue();
            Object old = _values[intPos];
            _values[intPos] = value;
            _status[intPos] = LRU_NEW;
            return old;
        }
        // skip to first position with LRU_OLD status.
        while (_status[_cur] == LRU_NEW) {
            _status[_cur] = LRU_OLD;
            _cur++;
            if (_cur >= _capacity) { _cur = 0; }
        }
        
        if (_keys[_cur] != null) {
            pos = _mapKeyPos.remove(_keys[_cur]);
        } else {
            pos = new Integer(_cur);
        }
        
        _keys[_cur] = key;
        _values[_cur] = value;
        _status[_cur] = LRU_NEW;
        _mapKeyPos.put(key, pos);
        
        _cur++;
        if (_cur >= _capacity) { _cur = 0; }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Object remove(final Object key) {
        Integer pos = _mapKeyPos.remove(key);
        if (pos == null) { return null; }
        int intPos = pos.intValue();
        Object old = _values[intPos];
        _keys[intPos] = null;
        _values[intPos] = null;
        _status[intPos] = LRU_OLD;
        return old;
    }
    
    //--------------------------------------------------------------------------
    // bulk operations of map interface
    
    /**
     * {@inheritDoc}
     */
    public void putAll(final Map<? extends Object, ? extends Object> map) {
        Iterator<? extends Entry<? extends Object, ? extends Object>> iter;
        iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<? extends Object, ? extends Object> entry = iter.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void clear() {
        _mapKeyPos.clear();
        for (int intPos = 0; intPos < _capacity; intPos++) {
            _keys[intPos] = null;
            _values[intPos] = null;
            _status[intPos] = LRU_OLD;
        }
    }

    //--------------------------------------------------------------------------
    // view operations of map interface
    
    /**
     * {@inheritDoc}
     */
    public synchronized Set<Object> keySet() {
        return Collections.unmodifiableSet(_mapKeyPos.keySet());
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized Collection<Object> values() {
        Collection<Object> col = new ArrayList<Object>(_mapKeyPos.size());
        Iterator<Integer> iter = _mapKeyPos.values().iterator();
        while (iter.hasNext()) {
            Integer pos = iter.next();
            if (pos != null) {
                col.add(_values[pos.intValue()]);
            }
        }
        return Collections.unmodifiableCollection(col);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Set<Entry<Object, Object>> entrySet() {
        Map<Object, Object> map = new Hashtable<Object, Object>(_mapKeyPos.size());
        Iterator<Entry<Object, Integer>> iter = _mapKeyPos.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Object, Integer> entry = iter.next();
            Integer pos = entry.getValue();
            if (pos != null) {
                map.put(entry.getKey(), _values[pos.intValue()]);
            }
        }
        return Collections.unmodifiableSet(map.entrySet());
    }

    //--------------------------------------------------------------------------
}
