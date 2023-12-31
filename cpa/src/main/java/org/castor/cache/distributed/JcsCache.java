/*
 * Copyright 2005 Tim Telcik, Werner Guttmann, Ralf Joachim
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
package org.castor.cache.distributed;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.cache.AbstractBaseCache;
import org.castor.cache.CacheAcquireException;

/**
 * JCS (Java Caching System) implementation of Castor JDO Cache.
 *
 * For more details of JCS, see http://jakarta.apache.org/jcs
 *
 * @see <a href="http://jakarta.apache.org/jcs">The JCS Home Page</a>
 * @author <a href="mailto:ttelcik AT hbf DOT com DOT au">Tim Telcik</a>
 * @author <a href="mailto:werner DOT guttmann AT gmx DOT net">Werner Guttmann</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8102 $ $Date: 2006-04-25 16:09:10 -0600 (Tue, 25 Apr 2006) $
 * @since 1.0
 */
public final class JcsCache extends AbstractBaseCache {
    //--------------------------------------------------------------------------

    /** The <a href="http://jakarta.apache.org/commons/logging/">Jakarta Commons
     *  Logging </a> instance used for all logging. */
    private static final Log LOG = LogFactory.getLog(JcsCache.class);

    /** The type of the cache. */
    public static final String TYPE = "jcs";

    /** The classname of the implementations factory class. */
    public static final String IMPLEMENTATION = "org.apache.jcs.JCS";

    /** Parameter types for calling getInstance() method on IMPLEMENTATION. */
    private static final Class<?>[] TYPES_GET_INSTANCE = new Class[] {String.class};

    /** Parameter types for calling get() method on cache instance. */
    private static final Class<?>[] TYPES_GET = new Class[] {Object.class};

    /** Parameter types for calling put() method on cache instance. */
    private static final Class<?>[] TYPES_PUT = new Class[] {Object.class, Object.class};

    /** Parameter types for calling remove() method on cache instance. */
    private static final Class<?>[] TYPES_REMOVE = TYPES_GET;

    /** The cache instance. */
    private Object _cache;

    /** The method to invoke on cache instead of calling get() directly. */
    private Method _getMethod;

    /** The method to invoke on cache instead of calling put() directly. */
    private Method _putMethod;

    /** The method to invoke on cache instead of calling remove() directly. */
    private Method _removeMethod;

    /** The method to invoke on cache instead of calling clear() directly. */
    private Method _clearMethod;

    //--------------------------------------------------------------------------
    // operations for life-cycle management of cache

    /**
     * {@inheritDoc}
     */
    public void initialize(final Properties params) throws CacheAcquireException {
        initialize(IMPLEMENTATION, params);
    }

    /**
     * Normally called to initialize JcsCache. To be able to test the method
     * without having <code>org.apache.jcs.JCS</code> implementation, it can also
     * be called with a test implementations classname.
     *
     * @param implementation Cache implementation classname to initialize.
     * @param params Parameters to initialize the cache (e.g. name, capacity).
     * @throws CacheAcquireException If cache can not be initialized.
     */
    public void initialize(final String implementation, final Properties params)
    throws CacheAcquireException {
        super.initialize(params);

        try {
            ClassLoader ldr = this.getClass().getClassLoader();
            Class<?> cls = ldr.loadClass(implementation);
            Method method = cls.getMethod("getInstance", TYPES_GET_INSTANCE);
            _cache = method.invoke(null, new Object[] {getName()});

            cls = _cache.getClass();
            _getMethod = cls.getMethod("get", TYPES_GET);
            _putMethod = cls.getMethod("put", TYPES_PUT);
            _removeMethod = cls.getMethod("remove", TYPES_REMOVE);
            _clearMethod = cls.getMethod("clear", (Class[]) null);
        } catch (Exception e) {
            String msg = "Error creating JCS cache: " + e.getMessage();
            LOG.error(msg, e);
            throw new CacheAcquireException(msg, e);
        }
    }

    //--------------------------------------------------------------------------
    // getters/setters for cache configuration

    /**
     * {@inheritDoc}
     */
    public String getType() { return TYPE; }

    //--------------------------------------------------------------------------
    // query operations of map interface

    /**
     * {@inheritDoc}
     */
    public int size() {
        throw new UnsupportedOperationException("size()");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        throw new UnsupportedOperationException("isEmpty()");
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final Object key) {
        return (get(key) != null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(final Object value) {
        throw new UnsupportedOperationException("containsValue(Object)");
    }

    /**
     * {@inheritDoc}
     */
    public Object get(final Object key) {
        return invokeCacheMethod(_getMethod, new Object[] {key});
    }

    //--------------------------------------------------------------------------
    // modification operations of map interface

    /**
     * {@inheritDoc}
     */
    public Object put(final Object key, final Object value) {
        Object oldValue = get(key);
        invokeCacheMethod(_putMethod, new Object[] {key, value});
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    public Object remove(final Object key) {
        Object oldValue = get(key);
        invokeCacheMethod(_removeMethod, new Object[] {key});
        return oldValue;
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
            Object[] params = new Object[] {entry.getKey(), entry.getValue()};
            invokeCacheMethod(_putMethod, params);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        invokeCacheMethod(_clearMethod, null);
    }

    //--------------------------------------------------------------------------
    // view operations of map interface

    /**
     * {@inheritDoc}
     */
    public Set<Object> keySet() {
        throw new UnsupportedOperationException("keySet()");
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Object> values() {
        throw new UnsupportedOperationException("values()");
    }

    /**
     * {@inheritDoc}
     */
    public Set<Entry<Object, Object>> entrySet() {
        throw new UnsupportedOperationException("entrySet()");
    }

    //--------------------------------------------------------------------------
    // helper methods

    /**
     * Invoke given method on cache with given arguments. Any possible exception will
     * be catched and IllegalStateException will be thrown instead.
     *
     * @param method The method to call on cache.
     * @param arguments The parameters.
     * @return The result of the method invocation.
     */
    private Object invokeCacheMethod(final Method method, final Object[] arguments) {
        try {
            return method.invoke(_cache, arguments);
        } catch (Exception e) {
            String msg = "Failed to call method on JCS instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
}
