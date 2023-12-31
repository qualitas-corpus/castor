/*
 * Copyright 2005 Werner Guttmann
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

import java.lang.reflect.Constructor;
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
 * EHCache implementation of Castor JDO Cache.
 * 
 * For more details of EHCache, see http://ehcache.sourceforge.net 
 * 
 * @see <a href="http://ehcache.sourceforge.net">the EHCache Home Page</a>
 * @author <a href="mailto:werner DOT guttmann AT gmx DOT net">Werner Guttmann</a>
 * @version $Revision: 8102 $ $Date: 2006-04-26 00:09:10 +0200 (Mi, 26 Apr 2006) $
 * @since 1.0
 */
public final class EHCache extends AbstractBaseCache {
    /** The <a href="http://jakarta.apache.org/commons/logging/">Jakarta Commons
     *  Logging </a> instance used for all logging. */
    private static final Log LOG = LogFactory.getLog(EHCache.class);

    /** The type of the cache. */
    public static final String TYPE = "ehcache";
    
    /** The classname of the implementations factory class. */
    public static final String IMPLEMENTATION = "net.sf.ehcache.CacheManager";
    
    /** Parameter types for calling getCache() method on IMPLEMENTATION. */
    private static final Class<?>[] TYPES_GET_CACHE = new Class[] {String.class};
    
    /** Parameter types for calling getFromCache() method on cache instance. */
    private static final Class<?>[] TYPES_GET = new Class[] {Object.class};

    /** Parameter types for calling getFromCache() method on cache instance. */
    private static final Class<?>[] TYPES_REMOVE = new Class[] {Object.class};

    /** Parameter types for calling constrcutor on 'net.sf.ehcache.Element' class. */
    private static final Class<?>[] TYPES_ELEMENT_CONSTRUCTOR =
        new Class[] {Object.class, Object.class};

    /** The cache instance. */
    private Object _cache;

    /** The method to invoke on cache instead of calling get() directly. */
    private Method _getMethod;

    /** The method to invoke on cache instead of calling put() directly. */
    private Method _putMethod;

    /** The method to invoke on cache instead of calling getSize() directly. */
    private Method _getSizeMethod;

    /** The method to invoke on cache instead of calling remove() directly. */
    private Method _removeMethod;

    /** The method to invoke on cache instead of calling removeAll() directly. */
    private Method _removeAllMethod;

    /** The method to invoke on Element instead of calling getValue() directly. */
    private Method _getValueMethod;

    /** The method to test whether or not an element is expired. */
    private Method _isExpiredMethod;

    /** Class instance for 'net.sf.ehcache.Element'. */
    private Class<?> _elementClass;

    /** Constructor for 'net.sf.ehcache.Element' class. */
    private Constructor<?> _elementConstructor;

    /**
     * {@inheritDoc}
     */
    public void initialize(final Properties params) throws CacheAcquireException {
        initialize(IMPLEMENTATION, params);
    }

    /**
     * Normally called to initialize FKCache. To be able to test the method without
     * having <code>javax.util.jcache.CacheAccessFactory</code> implementation, it
     * can also be called with a test implementations classname.
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
            invokeStaticMethod(cls, "create", null, null);
            Object factory = invokeStaticMethod(cls, "getInstance", null, null);
            Boolean cacheExists =
                (Boolean) invokeMethod(factory, "cacheExists",
                        new Class[] {String.class}, new Object[] {getName()});
            if (!cacheExists.booleanValue()) {
                invokeMethod(factory, "addCache", TYPES_GET_CACHE,
                        new Object[] {getName()});
            }
            _cache = invokeMethod(factory, "getCache",
                    TYPES_GET_CACHE, new Object[] {getName()});

        } catch (Exception e) {
            String msg = "Error creating EHCache cache: " + e.getMessage();
            LOG.error(msg, e);
            throw new CacheAcquireException(msg, e);
        }

        Class<?> cls = _cache.getClass();

        try {
            _elementClass = Class.forName("net.sf.ehcache.Element");
            _getValueMethod = _elementClass.getMethod("getValue", (Class[]) null);
            _isExpiredMethod = _elementClass.getMethod("isExpired", (Class[]) null);
        } catch (Exception e) {
            String msg =
                "Failed to instantiate Class for type 'net.sf.ehcache.Element': "
                + e.getMessage();
            LOG.error(msg, e);
            throw new CacheAcquireException(msg, e);
        }

        try {
            _getSizeMethod = cls.getMethod("getSize", (Class[]) null);
            _getMethod = cls.getMethod("get", TYPES_GET);
            _putMethod = cls.getMethod("put", new Class[] {_elementClass });
            _removeMethod = cls.getMethod("remove", TYPES_REMOVE);
            _removeAllMethod = cls.getMethod("removeAll", (Class[]) null);
            _elementConstructor =
                _elementClass.getConstructor(TYPES_ELEMENT_CONSTRUCTOR);
            _removeAllMethod = cls.getMethod("removeAll", (Class[]) null);
        } catch (Exception e) {
            String msg = "Failed to find method on EHCache instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new CacheAcquireException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        Integer result = new Integer(-1);
        try {
            result = (Integer) _getSizeMethod.invoke(_cache, (Object[]) null);
        } catch (Exception e) {
            String msg = "Failed to call method on EHCache instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new IllegalStateException(e.getMessage());
        }
        return result.intValue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return (size() < 1);
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
        Object result = null;
        try {
            Object elementInCache = _getMethod.invoke(_cache, new Object[] {key});
            if (elementInCache == null) { return null; }
            Boolean isExpired = (Boolean) _isExpiredMethod.invoke(elementInCache, (Object[]) null);
            if (isExpired.equals(Boolean.FALSE)) {
                result = _getValueMethod.invoke(elementInCache, (Object[]) null);
            }
        } catch (Exception e) {
            String msg = "Failed to call method on EHCache instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new IllegalStateException(e.getMessage());
        }
        return result;
    }


    // modification operations of map interface

    /**
     * {@inheritDoc}
     */
    public Object put(final Object key, final Object value) {
        Object result = Boolean.FALSE;
        try {
            result = _elementConstructor.newInstance(new Object[] {key, value});
            _putMethod.invoke(_cache, new Object[] {result});
        } catch (Exception e) {
            String msg = "Failed to call method on EHCache instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new IllegalStateException(e.getMessage());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object remove(final Object key) {
        Object oldValue = get(key);
        try {
            _removeMethod.invoke(_cache, new Object[] {String.valueOf(key)});
        } catch (Exception e) {
            String msg = "Failed to call method on EHCache instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new IllegalStateException(e.getMessage());
        }
        return oldValue;
    }


    // bulk operations of map interface

    /**
     * {@inheritDoc}
     */
    public void putAll(final Map<? extends Object, ? extends Object> map) {
        Iterator<? extends Entry<? extends Object, ? extends Object>> iter;
        iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<? extends Object, ? extends Object> entry = iter.next();
            String key = String.valueOf(entry.getKey());
            put (key, entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        try {
            _removeAllMethod.invoke(_cache, (Object[]) null);
        } catch (Exception e) {
            String msg = "Failed to call method on EHCache instance: " + e.getMessage();
            LOG.error(msg, e);
            throw new IllegalStateException(e.getMessage());
        }
    }


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

    /**
     * Normally called to shutdown CoherenceCache. To be able to test the method
     * without having <code>com.tangosol.net.CacheFactory</code> implementation,
     * it can also be called with a test implementations classname.
     *
     * @param implementation Cache implementation classname to shutdown.
     */
    public void shutdown(final String implementation) {
        try {
            ClassLoader ldr = this.getClass().getClassLoader();
            Class<?> cls = ldr.loadClass(implementation);
            if (cls != null) {
                Object factory = invokeStaticMethod(cls, "getInstance", null, null);
                Method method = cls.getMethod("shutdown", new Class[] {cls});
                method.invoke(factory, (Object[]) null);
            }
        } catch (Exception e) {
            LOG.error("Problem shutting down Coherence cluster member", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getType() { return TYPE; }

}


