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
package org.exolab.castor.jdo.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;

import org.castor.core.util.AbstractProperties;
import org.castor.core.util.Messages;
import org.castor.cpa.CPAProperties;
import org.castor.jdo.engine.DatabaseContext;
import org.castor.jdo.engine.DatabaseRegistry;
import org.castor.persist.ProposedEntity;
import org.castor.persist.TransactionContext;
import org.exolab.castor.jdo.CacheManager;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.DatabaseNotFoundException;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.Query;
import org.exolab.castor.jdo.QueryException;
import org.exolab.castor.jdo.TransactionAbortedException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.xml.NamedNativeQuery;
import org.exolab.castor.persist.ClassMolder;
import org.exolab.castor.persist.LockEngine;
import org.exolab.castor.persist.PersistenceInfoGroup;
import org.exolab.castor.persist.TxSynchronizable;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.exolab.castor.persist.spi.Identity;
import org.exolab.castor.persist.spi.InstanceFactory;

/**
 * An implementation of the JDO database supporting explicit transaction
 * demarcation.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @author <a href="mailto:ferret AT frii dot com">Bruce Snyder</a>
 * @version $Revision: 8351 $ $Date: 2006-04-22 11:05:30 -0600 (Sat, 22 Apr 2006) $
 */
public abstract class AbstractDatabaseImpl implements Database {
    private static final String NATIVE_QUERY_PREFIX = "CALL SQL";
    private static final String NATIVE_QUERY_AS = "AS";

    /** The database engine used to access the underlying SQL database. */
    protected PersistenceInfoGroup _scope;

    /** The transaction context is this database was accessed with an
     *  {@link javax.transaction.xa.XAResource}. */
    protected TransactionContext _ctx;

    /** List of TxSynchronizeable implementations that should all be
     *  informed about changes after commit of transactions. */
    private ArrayList<TxSynchronizable> _synchronizables;

    /** The lock timeout for this database. Zero for immediate timeout, 
     *  an infinite value for no timeout. The timeout is specified in
     *  seconds. */
    protected int _lockTimeout;

    /** The default callback interceptor for transaction. */
    protected CallbackInterceptor _callback;

    /** The instance factory to that creates new instances of data object. */
    protected InstanceFactory _instanceFactory;

    /** The name of this database. */
    protected String _dbName;

    /** True if user prefer all reachable object to be stored automatically. False if user
     *  want only dependent object to be stored. */
    protected boolean _autoStore;

    /** The class loader for application classes (may be null). */
    protected ClassLoader _classLoader;

    /** CacheManager instance. */
    private CacheManager _cacheManager;

    /**
     * Creates an instance of this class.
     * 
     * @param dbName Name of the database.
     * @param lockTimeout Lock timeout to use.
     * @param callback Callback interceptors.
     * @param instanceFactory Instance factory.
     * @param classLoader Current class loader.
     * @param autoStore True if auto storing is enabled.
     * @throws DatabaseNotFoundException If there's no database configuration for the given name.
     */
    public AbstractDatabaseImpl(final String dbName, 
            final int lockTimeout, 
            final CallbackInterceptor callback,
            final InstanceFactory instanceFactory, 
            final ClassLoader classLoader, 
            final boolean autoStore)
    throws DatabaseNotFoundException {
        
        _autoStore = autoStore;
        
        // Locate a suitable ConnectionFactory and LockEngine
        // and report if not mapping registered any of the two.
        // A new ODMG engine is created each time with different
        // locking mode.
        DatabaseContext context = null;
        try {
            context = DatabaseRegistry.getDatabaseContext(dbName);
        } catch (MappingException ex) {
            throw new DatabaseNotFoundException(Messages.format("jdo.dbNoMapping", dbName));
        }
        _scope = new PersistenceInfoGroup(new LockEngine[] {context.getEngine()});
        
        _callback = callback;
        _instanceFactory = instanceFactory;
        _dbName = dbName;
        _lockTimeout = lockTimeout;
        _classLoader = classLoader;
        
        loadSynchronizables();
    }

    /**
     * Returns the {@link LockEngine} in use by this database instance.
     *  
     * @return the {@link LockEngine} in use by this database instance.
     */
    LockEngine getLockEngine() {
        return _scope.getLockEngine();
    }

    /**
     * @inheritDoc
     */
    public PersistenceInfoGroup getScope() {
        return _scope;
    }

    /**
     * Indicates whether user prefer all reachable object to be stored automatically;
     * false if user wants dependent object only to be stored.
     * 
     * @param autoStore True to indicate that 'autoStore' mode should be used.
     */
    public void setAutoStore(final boolean autoStore) {
        _autoStore = autoStore;
    }

    /**
     * Return if the current transaction is set to autoStore, it there is
     * transaction active. If there is no active transaction, return if
     * the next transaction will be set to autoStore.
     * 
     * @return True if 'auto-store' mode is in use.
     */
    public boolean isAutoStore() {
        if (_ctx != null) {
            return _ctx.isAutoStore();
        }

        return _autoStore;
    }

    /**
     * Gets the current application ClassLoader's instance.
     * 
     * @return the current ClassLoader's instance, or <code>null</code> if not provided
     */
    public ClassLoader getClassLoader() {
        return _classLoader;
    }

    /**                              
     * Return the name of the database.
     * 
     * @return Name of the database.
     */                               
    public String getDatabaseName() {                                
        return _dbName;                  
    }

    /**
     * {@inheritDoc}
     */
    public abstract void close()
        throws PersistenceException;

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() {
        return (_scope == null);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isLocked(final Class cls, final Object identity) throws PersistenceException {
        if (identity == null) {
            throw new PersistenceException("Identities can't be null!");
        }
        if (_scope == null) {
            throw new PersistenceException(Messages.message("jdo.dbClosed"));
        }
        if (isActive()) {
            return _ctx.isLocked(cls, new Identity(identity), _scope.getLockEngine());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T load(final Class<T> type, final Object identity) throws PersistenceException {
        return load(type, identity, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T load(final Class<T> type, final Object identity, final Object object)
    throws PersistenceException {
        return load(type, identity, object, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public <T> T load(final Class<T> type, final Object identity, final AccessMode mode)
    throws PersistenceException {
        return load(type, identity, null, mode);
    }

    /**
     * Loads on object instance of the specified type and its identity.
     * 
     * @param type Object type.
     * @param identity Object identity
     * @param object Object instance to be filled with loaded values (optional)
     * @param mode Access mode to use during load.
     * @return An object instance matching the specified criteria.
     * @throws PersistenceException If the object identified by the parameter can not be found.
     *         If the required access mode cannot be granted. If there's no active transaction
     *         in progress.
     */
    @SuppressWarnings("unchecked")
    private <T> T load(final Class<T> type, final Object identity, final Object object,
            final AccessMode mode) throws PersistenceException {
        if (identity == null) {
            throw new PersistenceException("Identities can't be null!");
        }
        if (_scope == null) {
            throw new PersistenceException(Messages.message("jdo.dbClosed"));
        }
        TransactionContext tx = getTransaction();
        ClassMolder molder = _scope.getClassMolder(type);
        ProposedEntity proposedObject = new ProposedEntity(molder);
        if (object != null) {
            proposedObject.setEntity(object);
            proposedObject.setProposedEntityClass(object.getClass());
        }
        return (T) tx.load(new Identity(identity), proposedObject, mode);
    }

    /**
     * {@inheritDoc}
     */
    public void create(final Object object) throws PersistenceException {
        TransactionContext tx = getTransaction();
        ClassMolder molder = _scope.getClassMolder(object.getClass());
        tx.create(molder, object, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public CacheManager getCacheManager() {
        if (_cacheManager == null) {
            _cacheManager = new CacheManager(this, _ctx, getLockEngine());
        }
        return _cacheManager;
    }

    /**
     * {@inheritDoc}
     */
    public void update(final Object object) throws PersistenceException {
        
        TransactionContext tx = getTransaction();
        ClassMolder molder = _scope.getClassMolder(object.getClass());
        tx.update(molder, object, null);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(final Object object) throws PersistenceException {
        
        TransactionContext tx = getTransaction();
        _scope.getClassMolder(object.getClass());
        tx.delete(object);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPersistent(final Object object) {
        if (_scope == null) {
            throw new IllegalStateException(Messages.message("jdo.dbClosed"));
        }
        if (isActive()) {
            return _ctx.isPersistent(object);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Identity getIdentity(final Object object) throws PersistenceException {
        if (_scope == null) {
            throw new PersistenceException(Messages.message("jdo.dbClosed"));
        }

        ClassMolder molder = _scope.getClassMolder(object.getClass());
        return molder.getActualIdentity(_classLoader, object);
    }


    /**
     * {@inheritDoc}
     */
    public void lock(final Object object) throws PersistenceException {
        if (!isActive()) {
            throw new TransactionNotInProgressException(Messages.message("jdo.txNotInProgress"));
        }
        _ctx.writeLock(object, _lockTimeout);
    }

    /**
     * {@inheritDoc}
     */
    public OQLQuery getOQLQuery() {
        return new OQLQueryImpl(this);
    }

    /**
     * {@inheritDoc}
     */
    public final OQLQuery getNamedQuery(final String name) throws PersistenceException {
        ClassMolder queryMolder = _scope.findClassMolderByQuery(name);
        if (queryMolder == null) {
            queryMolder = _scope.findClassMolderByNativeQuery(name);
            if (queryMolder != null) {
                NamedNativeQuery query = _ctx.getNamedNativeQuery(queryMolder, name);
                String result = query.getResultClass();               
                if (result == null) {
                    result = queryMolder.getName();
                }
                try {
                    return getNativeQuery(query.getQuery(), Class.forName(result));
                } catch (ClassNotFoundException ex) {
                    throw new PersistenceException("Can not find the resulting class: " + result);
                }
            }
            throw new QueryException("Cannot find a named query with the name " + name);
        }        
        String oql = _ctx.getNamedQuery(queryMolder, name);
        return getOQLQuery(oql);
    }

    /**
     * {@inheritDoc}
     */
    public final OQLQuery getNativeQuery(final String sql, final Class<?> result)
    throws PersistenceException {
        StringBuffer buff = new StringBuffer();
        buff.append(NATIVE_QUERY_PREFIX);
        buff.append(" ");
        buff.append(sql);
        buff.append(" ");
        buff.append(NATIVE_QUERY_AS);
        buff.append(" ");
        buff.append(result.getName());
        return getOQLQuery(buff.toString());
    }

    /**
     * {@inheritDoc}
     */
    public OQLQuery getOQLQuery(final String oql) throws PersistenceException {
        OQLQuery oqlImpl;

        oqlImpl = new OQLQueryImpl(this);
        oqlImpl.create(oql);
        return oqlImpl;
    }
    
    /**
     * {@inheritDoc}
     */
    public Query getQuery() {
        return new OQLQueryImpl(this);
    }

    /**
     * Returns the currently active transaction, if any.
     * 
     * @return The current active transaction.
     * @throws TransactionNotInProgressException If there's no active transaction.
     */
    protected TransactionContext getTransaction() throws TransactionNotInProgressException {
        if (_scope == null) {
            throw new TransactionNotInProgressException(Messages.message("jdo.dbClosed"));
        }
        if (isActive()) {
            return _ctx;
        }
        throw new TransactionNotInProgressException(Messages.message("jdo.dbTxNotInProgress"));
    }

    /**
     * {@inheritDoc}
     */
    public abstract void begin() throws PersistenceException;

    /**
     * {@inheritDoc}
     */
    public abstract void commit()
        throws TransactionNotInProgressException, TransactionAbortedException;

    /**
     * {@inheritDoc}
     */
    public abstract void rollback()
        throws TransactionNotInProgressException;

    /**
     * {@inheritDoc}
     */
    public boolean isActive() {
        return (_ctx != null && _ctx.isOpen());
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString() + ":" + _dbName;
    }

    /**
     * {@inheritDoc}
     */
    public abstract Connection getJdbcConnection() throws PersistenceException; 

    /**
     * Load the {@link TxSynchronizable} implementations from the 
     * properties file, if not loaded before.
     */
    protected void loadSynchronizables() {
        if (_synchronizables == null) {
            _synchronizables = new ArrayList<TxSynchronizable>();
            
            AbstractProperties properties = CPAProperties.getInstance();
            Object[] objects = properties.getObjectArray(
                    CPAProperties.TX_SYNCHRONIZABLE, properties.getApplicationClassLoader());
            if (objects != null) {
                for (int i = 0; i < objects.length; i++) {
                    TxSynchronizable sync = (TxSynchronizable) objects[i];
                    _synchronizables.add(sync);
                }
            }
            
            if (_synchronizables.size() == 0) { _synchronizables = null; }
        }
    }
    
    /**
     * Register the {@link TxSynchronizable} implementations at the
     * TransactionContect at end of begin().
     */
    protected void registerSynchronizables() {
        if (_synchronizables != null && _synchronizables.size() > 0) {
            Iterator<TxSynchronizable> iter = _synchronizables.iterator();
            while (iter.hasNext()) {
                _ctx.addTxSynchronizable(iter.next());
            }
        }
    }
    
    /**
     * Unregister the {@link TxSynchronizable} implementations at the
     * TransactionContect after commit() or rollback().
     */
    protected void unregisterSynchronizables() {
        if (_synchronizables != null  && _synchronizables.size() > 0) {
            Iterator<TxSynchronizable> iter = _synchronizables.iterator();
            while (iter.hasNext()) {
                _ctx.removeTxSynchronizable(iter.next());
            }
        }
    }
    
        
    /**
     * Gets the current Castor transaction in use.
     * 
     * @return the current Castor 
     * @throws TransactionNotInProgressException If there's no transaction in progress.
     */
    public TransactionContext getCurrentTransaction() 
    throws TransactionNotInProgressException  {
        return getTransaction();
    }
}  
                               
