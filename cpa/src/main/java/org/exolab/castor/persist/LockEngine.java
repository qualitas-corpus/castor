/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: LockEngine.java 8294 2009-07-24 08:28:23Z lukaslang $
 */

package org.exolab.castor.persist;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.cache.Cache;
import org.castor.cache.CacheAcquireException;
import org.castor.cache.CacheFactory;
import org.castor.cache.CacheFactoryRegistry;
import org.castor.core.util.AbstractProperties;
import org.castor.core.util.Messages;
import org.castor.cpa.CPAProperties;
import org.castor.cpa.util.JDOClassDescriptorResolver;
import org.castor.jdo.engine.DatabaseContext;
import org.castor.persist.AbstractTransactionContext;
import org.castor.persist.ProposedEntity;
import org.castor.persist.TransactionContext;
import org.castor.persist.cache.CacheEntry;
import org.exolab.castor.jdo.ClassNotPersistenceCapableException;
import org.exolab.castor.jdo.DuplicateIdentityException;
import org.exolab.castor.jdo.LockNotGrantedException;
import org.exolab.castor.jdo.ObjectDeletedException;
import org.exolab.castor.jdo.ObjectModifiedException;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.persist.spi.Identity;
import org.exolab.castor.persist.spi.Persistence;
import org.exolab.castor.persist.spi.PersistenceFactory;
import org.exolab.castor.xml.ClassDescriptorResolver;
import org.exolab.castor.xml.ResolverException;

/**
 * LockEngine is a gateway for all the <tt>ClassMolder</tt>s of a persistence 
 * storage. It mantains dirty checking cache state and lock, and provides a 
 * thread safe enviroment for <tt>ClassMolder</tt>. LockEngine garantees that 
 * no two conflicting operations will be let running concurrently for the same object. 
 * <p>
 * For example, it ensures that exactly one transaction may read (load) exclusively
 * on one object; transaction can not deleted an object while the other is 
 * reading it, etc...
 * <p>
 * It also provides caching for a persistence storage. Different {@link Cache} mechanisms
 * can be specified. 
 * <p>
 * User should not create more than one instance of LockEngine for each persistent 
 * storage. So that object can be properly locked and ObjectModifiedException can
 * be avoided.
 * <p>
 * However, if more than one instance of LockEngine or some other external 
 * application run concurrently, if the {@link Persistence} supports dirty checking,
 * like a fully complaint JDBC Relational Database, proper 
 * ObjectModifiedException will be thrown to ensure data consistency.
 * <p>
 * IMPLEMENTATION NOTES:
 * <p>
 * An object may be persistent in multiple caches at any given
 * time. There is no way to load an object from multiple caches,
 * but an object can be loaded in one engine and then made
 * persistent in another. The engines are totally independent and
 * no conflicts should occur.
 * <p>
 * Each class hierarchy gets its own cache, so caches can be
 * controlled on a class-by-class basis.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @author <a href="yip@intalio.com">Thomas Yip</a>
 * @author <a href="mailto:ferret AT frii dot com">Bruce Snyder</a>
 * @version $Revision: 8294 $ $Date: 2006-04-22 11:05:30 -0600 (Sat, 22 Apr 2006) $
 */
public final class LockEngine {
    /**
     * The <a href="http://jakarta.apache.org/commons/logging/">Jakarta
     * Commons Logging</a> instance used for all logging.
     */
    private static Log _log = LogFactory.getFactory().getInstance(LockEngine.class);
    
    private static CacheFactoryRegistry _cacheFactoryRegistry;

    /**
     * Mapping of type information to object types. The object's class is used
     * as the key and {@link TypeInfo} is the value. {@link TypeInfo} provides
     * sufficient information to persist the object, manipulated it in memory
     * and invoke the object's interceptor.
     */
    private final HashMap<String, TypeInfo> _typeInfo = new HashMap<String, TypeInfo>();

    /**
     * All the XA transactions running against this cache engine.
     */
    private final HashMap<Xid, TransactionContext> _xaTx = new HashMap<Xid, TransactionContext>();
    
    /**
     * The ConnectionFactory.
     */
    private DatabaseContext _databaseContext;
    
    /**
     * Used by the constructor when creating handlers to temporarily
     * hold the persistence factory for use by {@link #getClassMolder}.
     */
    private PersistenceFactory _persistenceFactory;

    /**
     * Construct a new cache engine with the specified mapping table, 
     * persistence engine and the log interceptor.
     *
     * @param databaseContext
     * @param cdResolver
     * @param persistenceFactory Factory for creating persistence engines for each
     *        object described in the map.
     * @throws MappingException Indicate that one of the mappings is invalid
     */
    public LockEngine(final DatabaseContext databaseContext,
            final ClassDescriptorResolver cdResolver,
            final PersistenceFactory persistenceFactory)
    throws MappingException {
        if (_cacheFactoryRegistry == null) {
            AbstractProperties properties = CPAProperties.getInstance();
            _cacheFactoryRegistry = new CacheFactoryRegistry(properties);
        }
        
        _databaseContext = databaseContext;
        _persistenceFactory = persistenceFactory;
        
        try {
            Vector<ClassMolder> v = resolve((JDOClassDescriptorResolver) cdResolver);
    
            Enumeration<ClassMolder> enumeration = v.elements();

            HashSet<ClassMolder> processedClasses = new HashSet<ClassMolder>();
            HashSet<ClassMolder> freshClasses = new HashSet<ClassMolder>();
            // copy things into an arraylist
            while (enumeration.hasMoreElements()) {
                freshClasses.add(enumeration.nextElement());
            }

            // iterates through all the ClassMolders in the LockEngine.
            // We first create a TypeInfo for all the base class (ie not extends
            // other class) in the first iteration.
            int counter = 0;
            do {
                counter = freshClasses.size();
                Iterator<ClassMolder> itor = freshClasses.iterator();
                while (itor.hasNext()) {
                    ClassMolder molder = itor.next();
                    ClassMolder extend = molder.getExtends();

                    if (extend == null) {
                        // create new Cache instance for the base type
                        Cache cache = null;
                        try {
                            cache = _cacheFactoryRegistry.getCache(
                                    molder.getCacheParams(),
                                    cdResolver.getMappingLoader().getClassLoader());
                        } catch (CacheAcquireException e) {
                            String msg = Messages.message("persist.cacheCreationFailed");
                            _log.error(msg, e);
                            throw new MappingException(msg, e);
                        }
                        TypeInfo info = new TypeInfo(molder,
                                new HashMap<OID, ObjectLock>(), cache); 
                        _typeInfo.put(molder.getName(), info);
                        itor.remove();
                        processedClasses.add(molder);

                    } else if (processedClasses.contains(molder.getExtends())) {
                        // use the base type to construct the new type
                        TypeInfo baseInfo = _typeInfo.get(extend.getName());
                        _typeInfo.put(molder.getName(), new TypeInfo(molder, baseInfo));
                        itor.remove();
                        processedClasses.add(molder);

                    } else {
                        // do nothing and wait for the next turn
                    }

                }
            } while ((freshClasses.size() > 0) && (counter != freshClasses.size()));

            // error if there is molder left.
            if (freshClasses.size() > 0) {
                Iterator<ClassMolder> itor = freshClasses.iterator();
                while (itor.hasNext()) {
                    ClassMolder molder = itor.next();
                    _log.error("The base class, " + (molder.getExtends().getName())
                        + ", of the extends class ," + molder.getName() 
                        + " can not be resolved! ");
                }
                throw new MappingException("Some base class can not be resolved!");
            }
        } catch (ClassNotFoundException e) {
            throw new MappingException("Declared Class not found!");
        }
    }
    
    /**
     * Resolve and construct all the <tt>ClassMolder</tt>s given a MappingLoader.
     *
     * @param cdResolver {@link ClassDescriptorResolver} instance used for resolving
     *        {@link ClassDescriptor}.
     * @return  Vector of all of the <tt>ClassMolder</tt>s from a MappingLoader
     * @throws ClassNotFoundException 
     */
    private Vector<ClassMolder> resolve(final JDOClassDescriptorResolver cdResolver)
    throws MappingException, ClassNotFoundException {
        Vector<ClassMolder> result = new Vector<ClassMolder>();

        DatingService ds = new DatingService(cdResolver.getMappingLoader().getClassLoader());

        Iterator<ClassDescriptor> iter = cdResolver.descriptorIterator();
        while (iter.hasNext()) {
            Class<?> toResolve = iter.next().getJavaClass();
            ClassDescriptor desc;
            try {
                desc = cdResolver.resolve(toResolve);
            } catch (ResolverException e) {
                throw new MappingException ("Cannot resolve type for " + toResolve.getName(), e);
            }
            
            Persistence persistence = _persistenceFactory.getPersistence(desc);
            ClassMolder molder = new ClassMolder(ds, cdResolver, this, desc, persistence);
            result.add(molder);
        }

        ds.close();
        return result;
    }
    
    public DatabaseContext getDatabaseContext() { return _databaseContext; }

    /**
     * Get classMolder which represents the given java data object class.
     * Dependent class will not be returned to avoid persistenting 
     * a dependent class without.
     * 
     * @param cls Class instance for whic a class molder should be returned. 
     * @return The class molder for the specified class.
     */
    public ClassMolder getClassMolder(final Class cls) {
        TypeInfo info = _typeInfo.get(cls.getName());
        if (info != null) {
            if (!info._molder.isDependent()) {
                return info._molder;
            }
        }
        return null;
    }
    
    public ClassMolder getClassMolder(final String classname) {
        TypeInfo info = _typeInfo.get(classname);
        if (info != null) {
            if (!info._molder.isDependent()) {
                return info._molder;
            }
        }
        return null;
    }
    
    public ClassMolder getClassMolderWithDependent(final Class cls) {
        TypeInfo info = _typeInfo.get(cls.getName());
        return (info != null) ? info._molder : null;
    }
    
    /**
     * Returns the ClassMolder instance that has a named query associated with the name given.
     * 
     * @param name Name of a named query.
     * @return ClassMolder instance associated with the named query.
     */
    public ClassMolder getClassMolderByQuery(final String name) {        
        Iterator<TypeInfo> typeIterator = _typeInfo.values().iterator();
        while (typeIterator.hasNext()) {
            TypeInfo info = typeIterator.next();
            if (info._molder.getNamedQuery(name) != null) {
                return info._molder;
            }
        }
        return null;
    }

    /**
     * Returns the ClassMolder instance that has a named native query associated with the
     * name given.
     * 
     * @param name Name of a named query.
     * @return ClassMolder instance associated with the named native query.
     */
    public ClassMolder getClassMolderByNativeQuery(final String name) {
        Iterator<TypeInfo> typeIterator = _typeInfo.values().iterator();
        while (typeIterator.hasNext()) {
            TypeInfo info = typeIterator.next();
            if (info._molder.getNamedNativeQuery(name) != null) {
                return info._molder;
            }
        }
        return null;
    }
    
    public Persistence getPersistence(final Class cls) {
        ClassMolder molder = getClassMolder(cls);
        if (molder != null) {
            return molder.getPersistence();
        }
        return null;
    }

    /**
     * Loads an object of the specified type and identity from
     * persistent storage. In exclusive mode the object is always
     * loaded and a write lock is obtained on the object, preventing
     * concurrent updates. In non-exclusive mode the object is either
     * loaded or obtained from the cache with a read lock. The object's
     * OID is always returned.
     *
     * @param tx The transaction context
     * @param oid The identity of the object to load
     * @param proposedObject The type of the object to load
     * @param suggestedAccessMode The desired access mode
     * @param timeout The timeout waiting to acquire a lock on the
     *  object (specified in seconds)
     * @throws ObjectNotFoundException The object was not found in
     *  persistent storage
     * @throws LockNotGrantedException Timeout or deadlock occured
     *  attempting to acquire lock on object
     * @throws PersistenceException An error reported by the
     *  persistence engine
     * @throws ClassNotPersistenceCapableException The class is not
     *  persistent capable
     * @throws ObjectDeletedWaitingForLockException The object has been deleted, but is waiting
     *         for a lock.
     */
    public void load(final AbstractTransactionContext tx, final OID oid,
            final ProposedEntity proposedObject, final AccessMode suggestedAccessMode,
            final int timeout, final QueryResults results, final ClassMolder molder)
    throws PersistenceException {
        TypeInfo typeinfo = _typeInfo.get(oid.getName());
        if (typeinfo == null) {
            throw new ClassNotPersistenceCapableException(Messages.format(
                    "persist.classNotPersistenceCapable", oid.getName()));
        }
        
        String typeName = typeinfo._name;
        ClassMolder typeMolder = typeinfo._molder;

        AccessMode accessMode = typeinfo._molder.getAccessMode(suggestedAccessMode);

        short action;
        if ((accessMode == AccessMode.Exclusive) || (accessMode == AccessMode.DbLocked)) {
            action = ObjectLock.ACTION_WRITE;
        } else {
            action = ObjectLock.ACTION_READ;
        }

        boolean succeed = false;
        ObjectLock lock = null;
        boolean expsucceed = false;
        ObjectLock explock = null;
        try {
            lock = typeinfo.acquire(oid, tx, action, timeout);
            
            // we have to care about on expanded entities here
            // (lock.getObject() == null) indicates a cache miss
            // (oid == lock.getOID()) found expected entity
            // everything else is an expanded entity
            if ((lock.getObject() != null) && (oid != lock.getOID())) {
                // Remove old OID from ObjectTracker
                tx.untrackObject(proposedObject.getEntity());
                
                // Adjust name and molder of typeinfo
                typeinfo._name = lock.getOID().getName();
                typeinfo._molder = getClassMolder(lock.getOID().getName());
                
                // Create instance of 'expanded object'
                Object objectInTx;
                try {
                    objectInTx = typeinfo._molder.newInstance(tx.getDatabase().getClassLoader());
                } catch (Exception e) {
                    String msg = "Cannot create instance of " + typeinfo._molder.getName();
                    _log.error(msg);
                    throw new PersistenceException(msg);
                }

                proposedObject.setActualClassMolder(null);
                proposedObject.setEntity(objectInTx);
                proposedObject.setExpanded(false);

                // Add new OID to ObjectTracker
                tx.trackObject(typeinfo._molder, lock.getOID(), proposedObject.getEntity());
            }
            
            // try to load the field values from the cache, except when being told
            // to ignore themsuggestedAccessMode
            Object[] cachedFieldValues = lock.getObject(tx);
            proposedObject.setFields(cachedFieldValues);
            
            // load the fields from the persistent storage if the cache is empty
            // or the access mode is DBLOCKloadED (thus guaranteeing that a lock at the
            // database level will be created)
            if (!proposedObject.isFieldsSet() || accessMode == AccessMode.DbLocked) {
                typeinfo._molder.load(tx, lock, proposedObject, accessMode, results);
            }

            proposedObject.setActualClassMolder(typeinfo._molder);

            if (proposedObject.isExpanded()) {
                // Current transaction holds lock for old OID
                typeinfo.release(oid, tx);

                // confirm lock before setting it to null
                lock.confirm(tx, succeed);
                lock = null;
                
                // Remove old OID from ObjectTracker
                tx.untrackObject(proposedObject.getEntity());
                
                // Create new OID
                ClassMolder expmolder = getClassMolder(proposedObject.getActualEntityClass());

                OID expoid = new OID(expmolder, oid.getIdentity());

                // Create instance of 'expanded object'
                Object objectInTx;
                try {
                    objectInTx = expmolder.newInstance(tx.getDatabase().getClassLoader());
                } catch (Exception e) {
                    String msg = "Cannot create instance of " + expmolder.getName();
                    _log.error(msg);
                    throw new PersistenceException(msg);
                }

                proposedObject.setActualClassMolder(null);
                proposedObject.setEntity(objectInTx);
                proposedObject.setExpanded(false);

                // Add new OID to ObjectTracker
                tx.trackObject(molder, expoid, proposedObject.getEntity());
                
                // reload 'expanded object' using correct ClassMolder
                TypeInfo exptypeinfo = _typeInfo.get(expoid.getName());
                if (exptypeinfo == null) {
                    throw new ClassNotPersistenceCapableException(Messages.format(
                            "persist.classNotPersistenceCapable", expoid.getName()));
                }

                AccessMode expaccessmode = exptypeinfo._molder.getAccessMode(suggestedAccessMode);

                short expaction;
                if ((expaccessmode == AccessMode.Exclusive)
                        || (expaccessmode == AccessMode.DbLocked)) {
                    expaction = ObjectLock.ACTION_WRITE;
                } else {
                    expaction = ObjectLock.ACTION_READ;
                }

                explock = exptypeinfo.acquire(expoid, tx, expaction, timeout);
                
                // set the field values to 'null'. this indicates that the field values 
                // should be loaded from the persistence storage
                proposedObject.setFields(null);

                // always load the fields from the persistent storage
                exptypeinfo._molder.load(tx, explock, proposedObject, expaccessmode, results);

                proposedObject.setActualClassMolder(exptypeinfo._molder);

                // always mold as we handle expanded objects here
                exptypeinfo._molder.mold(tx, explock, proposedObject, expaccessmode);

                if (_log.isDebugEnabled()) {
                    _log.debug(Messages.format("jdo.loading.with.id",
                            exptypeinfo._molder.getName(), expoid.getIdentity()));
                }

                expsucceed = true;
            } else {
                // mold only if object has not been expanded
                typeinfo._molder.mold(tx, lock, proposedObject, accessMode);

                if (_log.isDebugEnabled()) {
                    _log.debug(Messages.format("jdo.loading.with.id",
                            typeinfo._molder.getName(), oid.getIdentity()));
                }

                succeed = true;
            }
        } catch (ObjectDeletedWaitingForLockException except) {
            // This is equivalent to object does not exist
            throw new ObjectNotFoundException(Messages.format(
                    "persist.objectNotFound", oid.getName(), oid.getIdentity()), except);
        } catch (LockNotGrantedException e) {
            if (lock != null) { lock.release(tx); }
            if (explock != null) { explock.release(tx); }
            throw e;
        } finally {
            typeinfo._name = typeName;
            typeinfo._molder = typeMolder;

            if (lock != null) { lock.confirm(tx, succeed); }
            if (explock != null) { explock.confirm(tx, expsucceed); }
        }
    }
    
    /**
     * Mark an object and its related or dependent object to be created.
     *
     * @param tx The transaction context.
     * @param oid The identity of the object, or null.
     * @param object The newly created object.
     * @throws PersistenceException An error reported by the persistence engine. Timeout or
     *         deadlock occured attempting to acquire lock on object.
     */
    public void markCreate(final TransactionContext tx, final OID oid, final Object object)
    throws PersistenceException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        if (typeInfo == null) {
            throw new ClassNotPersistenceCapableException(Messages.format(
                    "persist.classNotPersistenceCapable", oid.getName()));
        }
        typeInfo._molder.markCreate(tx, oid, null, object);
    }

    /**
     * Creates a new object in the persistence storage. The object must not
     * be persistent and must have a unique identity within this engine.
     * If the identity is specified the object is created in
     * persistent storage immediately with the identity. If the
     * identity is not specified, the object is created only when the
     * transaction commits. The object's OID is returned. The OID is
     * guaranteed to be unique for this engine even if no identity was
     * specified.
     *
     * @param tx The transaction context
     * @param oid The identity of the object, or null
     * @param object The newly created object
     * @return The object's OID
     * @throws DuplicateIdentityException An object with this identity
     *  already exists in persistent storage
     * @throws PersistenceException An error reported by the
     *  persistence engine
     * @throws ClassNotPersistenceCapableException The class is not
     *  persistent capable
     */
    public OID create(final TransactionContext tx, final OID oid, final Object object)
    throws PersistenceException {
        OID internaloid = oid;
        boolean succeed;

        TypeInfo typeInfo = _typeInfo.get(object.getClass().getName());
        if (typeInfo == null) {
            throw new ClassNotPersistenceCapableException(Messages.format(
                    "persist.classNotPersistenceCapable", object.getClass().getName()));
        }
            
        ObjectLock lock = null;

        if (internaloid.getIdentity() != null) {

            lock = null;

            succeed = false;

            try {

                lock = typeInfo.acquire(internaloid, tx, ObjectLock.ACTION_CREATE, 0);

                if (_log.isDebugEnabled()) {
                    _log.debug(Messages.format("jdo.creating.with.id", typeInfo._molder.getName(),
                            internaloid.getIdentity()));
                }

                internaloid = lock.getOID();

                typeInfo._molder.create(tx, internaloid, lock, object);

                succeed = true;

                internaloid.setDbLock(true);

                return internaloid;
                // should catch some other exception if destory is not succeed
            } catch (LockNotGrantedException except) {
                // Someone else is using the object, definite duplicate key
                throw new DuplicateIdentityException(Messages.format(
                    "persist.duplicateIdentity", object.getClass().getName(), 
                    internaloid.getIdentity()), except);
            } catch (DuplicateIdentityException except) {
                // we got a write lock and the persistence storage already
                // recorded. Should destory the lock
                //typeInfo.delete( oid, tx );
                throw except;
            } finally {
                if (lock != null) {
                    lock.confirm(tx, succeed);
                }
            }
        }
        // identity is null

        succeed = false;

        try {
            if (_log.isDebugEnabled()) {
                _log.debug(Messages.format("jdo.creating.with.id", typeInfo._molder.getName(),
                        internaloid.getIdentity()));
            }

            lock = typeInfo.acquire(internaloid, tx, ObjectLock.ACTION_CREATE, 0);

            internaloid = lock.getOID();

            Identity newids = typeInfo._molder.create(tx, internaloid, lock, object);
            succeed = true;

            internaloid.setDbLock(true);

            OID newoid = new OID(internaloid.getMolder(), internaloid.getDepends(), newids);

            typeInfo.rename(internaloid, newoid, tx);

            return newoid;
        } catch (LockNotGrantedException e) {
//            e.printStackTrace();
            throw new PersistenceException(Messages.format(
                    "persist.nested", "Key Generator Failure. Duplicated Identity is generated!"));
        } finally {
            if (lock != null) {
                lock.confirm(tx, succeed);
            }
        }
    }

    /**
     * Called at transaction commit time to delete the object. Object
     * deletion occurs in three distinct steps:
     * <ul>
     * <li>A write lock is obtained on the object to assure it can be
     *     deleted and the object is marked for deletion in the
     *     transaction context
     * <li>As part of transaction preparation the object is deleted
     *     from persistent storage using this method
     * <li>The object is removed from the cache when the transaction
     *     completes with a call to {@link #forgetObject}
     * </ul>
     *
     * @param tx The transaction context
     * @param oid The object's identity
     * @throws PersistenceException An error reported by the
     *  persistence engine
     */
    public void delete(final TransactionContext tx, final OID oid)
            throws PersistenceException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());

        try {
            typeInfo.assure(oid, tx, true);

            if (_log.isDebugEnabled()) {
                _log.debug(Messages.format("jdo.removing", typeInfo._molder.getName(),
                        oid.getIdentity()));
            }

            typeInfo._molder.delete(tx, oid);

        } catch (LockNotGrantedException except) {
            throw new IllegalStateException(Messages.format(
                    "persist.internal", "Attempt to delete object for which no lock was acquired"));
        }
    }

    public void markDelete(final TransactionContext tx, final OID oid, final Object object,
            final int timeout) throws PersistenceException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        ObjectLock lock = typeInfo.upgrade(oid, tx, timeout);
        typeInfo._molder.markDelete(tx, oid, lock, object);
        lock.expire();
    }


    /**
     * Updates an existing object to this engine. The object must not be
     * persistent and must not have the identity of a persistent object.
     * The object's OID is returned. The OID is guaranteed to be unique
     * for this engine even if no identity was specified.
     * If the object implements TimeStampable interface, verify
     * the object's timestamp.
     *
     * @param tx The transaction context
     * @param oid The object's identity
     * @param object The object
     * @param suggestedAccessMode The desired access mode
     * @param timeout The timeout waiting to acquire a lock on the
     *  object (specified in seconds)
     * @return The object's OID
     * @throws ObjectNotFoundException The object was not found in
     *  persistent storage
     * @throws LockNotGrantedException Timeout or deadlock occured
     *  attempting to acquire lock on object
     * @throws PersistenceException An error reported by the
     *  persistence engine
     * @throws ClassNotPersistenceCapableException The class is not
     *  persistent capable
     * @throws ObjectModifiedException Dirty checking mechanism may immediately
     *  report that the object was modified in the database during the long
     *  transaction.
     * @throws ObjectDeletedWaitingForLockException
     */
    public boolean update(final TransactionContext tx, final OID oid, final Object object,
            final AccessMode suggestedAccessMode, final int timeout) throws PersistenceException {
        // If the object is new, don't try to load it from the cache
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        if (typeInfo == null) {
            throw new ClassNotPersistenceCapableException(Messages.format(
                    "persist.classNotPersistenceCapable", oid.getName()));
        }

        boolean succeed = false;
        ObjectLock lock = null;
        OID internaloid = oid;
        try {
            // exclude objects that are locked, cached, dependend or to be created
            if (!typeInfo.isLocked(internaloid)
                    && !typeInfo.isCached(internaloid)
                    && !typeInfo._molder.isDependent()
                    && (internaloid.getIdentity() != null)) {
                lock = typeInfo.acquire(internaloid, tx, ObjectLock.ACTION_UPDATE, timeout);
                internaloid = lock.getOID();
                
                // set timestamp of lock to the one of persistent object
                try {
                    typeInfo._molder.loadTimeStamp(tx, lock, suggestedAccessMode);
                } catch (PersistenceException ex) {
                    // ignore
                }
            } else {
                lock = typeInfo.acquire(internaloid, tx, ObjectLock.ACTION_UPDATE, timeout);
                internaloid = lock.getOID();
            }
            
            succeed = !typeInfo._molder.update(tx, internaloid, lock, object, suggestedAccessMode);

            return !succeed;
        } catch (ObjectModifiedException e) {
            throw e;
        } catch (ObjectDeletedWaitingForLockException except) {
            // This is equivalent to object not existing
            throw new ObjectNotFoundException(Messages.format(
                    "persist.objectNotFound", internaloid.getName(),
                    internaloid.getIdentity()), except);
        } finally {
            if (lock != null) {
                lock.confirm(tx, succeed);
            }
        }
    }

    /**
     * Called at transaction commit to store an object that has been
     * loaded during the transaction. If the object has been created
     * in this transaction but without an identity, the object will
     * be created in persistent storage. Otherwise the object will be
     * stored and dirty checking might occur in order to determine
     * whether the object is valid. The object's OID might change
     * during this process, and the new OID will be returned. If the
     * object was not stored (not modified), null is returned.
     *
     * @param tx The transaction context
     * @param oid The object's identity
     * @param object The object to store
     * @param timeout The timeout waiting to acquire a lock on the
     *  object (specified in seconds)
     * @return The object's OID if stored, null if ignored
     * @throws LockNotGrantedException Timeout or deadlock occured
     *  attempting to acquire lock on object
     * @throws ObjectDeletedException The object has been deleted from
     *  persistent storage
     * @throws ObjectModifiedException The object has been modified in
     *  persistent storage since it was loaded, the memory image is
     *  no longer valid
     * @throws DuplicateIdentityException An object with this identity
     *  already exists in persistent storage
     * @throws PersistenceException An error reported by the
     *  persistence engine
     */
    public OID preStore(final TransactionContext tx, final OID oid, final Object object,
            final int timeout) throws PersistenceException {
        OID internaloid = oid;
        ObjectLock lock = null;
        boolean    modified;

        TypeInfo typeInfo = _typeInfo.get(object.getClass().getName());

        // Acquire a read lock first. Only if the object has been modified
        // do we need a write lock.

        internaloid = new OID(typeInfo._molder, internaloid.getIdentity());

        // acquire read lock
        // getLockedField();
        // isPersistFieldChange()?
        // if no, return null
        // if yes, get flattened fields, 
        // acquire write lock
        // setLockedField( );
        try {
            lock = typeInfo.assure(internaloid, tx, false);

            internaloid = lock.getOID();

            modified = typeInfo._molder.preStore(tx, internaloid, lock, object, timeout);
        } catch (LockNotGrantedException e) {
            throw e;
        } catch (ObjectModifiedException e) {
            lock.invalidate(tx);
            throw e;
        } catch (ObjectDeletedException e) {
            lock.delete(tx);
            throw e;
        }

        if (modified) {
            return internaloid;
        }

        return null;
    }

    public void store(final TransactionContext tx, final OID oid, final Object object)
    throws PersistenceException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        ObjectLock lock = null;
        // Attempt to obtain a lock on the database. If this attempt
        // fails, release the lock and report the exception.
        try {
            lock = typeInfo.assure(oid, tx, false);

            if (_log.isDebugEnabled ()) {
                _log.debug(Messages.format("jdo.storing.with.id",
                        typeInfo._molder.getName(), oid.getIdentity()));
            }

            typeInfo._molder.store(tx, oid, lock, object);
        } catch (ObjectModifiedException e) {
            lock.invalidate(tx);
            throw e;
        } catch (DuplicateIdentityException e) {
            throw e;
        } catch (LockNotGrantedException e) {
            throw e;
        } catch (PersistenceException e) {
            lock.invalidate(tx);
            throw e;
        } 
    }



    /**
     * Acquire a write lock on the object. A write lock assures that
     * the object exists and can be stored/deleted when the
     * transaction commits. It prevents any concurrent updates to the
     * object from this point on. However, it does not guarantee that
     * the object has not been modified in the course of the
     * transaction. For that the object must be loaded with exclusive
     * access.
     *
     * @param tx The transaction context
     * @param oid The object's OID
     * @param timeout The timeout waiting to acquire a lock on the
     *  object (specified in seconds)
     * @throws LockNotGrantedException Timeout or deadlock occured
     *  attempting to acquire lock on object
     * @throws ObjectDeletedException The object has been deleted from
     *  persistent storage
     * @throws PersistenceException An error reported by the
     *  persistence engine
     */
    public void writeLock(final TransactionContext tx, final OID oid, final int timeout)
    throws PersistenceException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        // Attempt to obtain a lock on the database. If this attempt
        // fails, release the lock and report the exception.
        try {
            typeInfo.upgrade(oid, tx, timeout);
        } catch (IllegalStateException e) {
            throw e;
        } catch (ObjectDeletedWaitingForLockException e) {
            throw new IllegalStateException("Object deleted waiting for lock?????????");
        } catch (LockNotGrantedException e) {
            throw e;
        }
    }


    /**
     * Acquire a write lock on the object in memory. A soft lock prevents
     * other threads from changing the object, but does not acquire a lock
     * on the database.
     *
     * @param tx The transaction context
     * @param oid The object's OID
     * @param timeout The timeout waiting to acquire a lock on the
     *  object (specified in seconds)
     * @throws LockNotGrantedException Timeout or deadlock occured
     *  attempting to acquire lock on object
     *  persistent storage
     */
    public void softLock(final TransactionContext tx, final OID oid, final int timeout)
    throws LockNotGrantedException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        typeInfo.upgrade(oid, tx, timeout);
    }

    /**
     * Reverts an object to the cached copy given the object's OID.
     * The cached object is copied into the supplied object without
     * affecting the locks, loading relations or emitting errors.
     * This method is used during the rollback phase.
     *
     * @param tx The transaction context
     * @param oid The object's oid
     * @param object The object into which to copy
     * @throws PersistenceException An error reported by the
     *  persistence engine obtaining a dependent object
     */
    public void revertObject(final TransactionContext tx, final OID oid, final Object object)
    throws PersistenceException {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        try {
            ObjectLock lock = typeInfo.assure(oid, tx, false);
            typeInfo._molder.revertObject(tx, oid, lock, object);
        } catch (LockNotGrantedException e) {
            throw new IllegalStateException("Write Lock expected!");
        } catch (PersistenceException except) {
            throw except;
        }
    }

    /**
     * Update the cached object with changes done to its copy. The
     * supplied object is copied into the cached object using a write
     * lock. This method is generally called after a successful return
     * from {@link #store} and is assumed to have obtained a write
     * lock.
     *
     * @param tx The transaction context
     * @param oid The object's oid
     * @param object The object to copy from
     */
    public void updateCache(final TransactionContext tx, final OID oid, final Object object) {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        ObjectLock lock = typeInfo.assure(oid, tx, true);
        typeInfo._molder.updateCache(tx, oid, lock, object);
    }

    /**
     * Called at transaction commit or rollback to release all locks
     * held on the object. Must be called for all objects that were
     * queried but not created within the transaction.
     *
     * @param tx The transaction context
     * @param oid The object OID
     */
    public void releaseLock(final TransactionContext tx, final OID oid) {
        TypeInfo typeInfo = _typeInfo.get(oid.getName());
        ObjectLock lock = typeInfo.release(oid, tx);
        lock.getOID().setDbLock(false);
    }


    /**
     * Called at transaction commit or rollback to forget an object
     * and release its locks. Must be called for all objects that were
     * created when the transaction aborts, and for all objects that
     * were deleted when the transaction completes. The transaction is
     * known to have a write lock on the object.
     *
     * @param tx The transaction context
     * @param oid The object OID
     */
    public void forgetObject(final TransactionContext tx, final OID oid) {
        TypeInfo   typeInfo;

        typeInfo = _typeInfo.get(oid.getName());
        //lock = typeInfo.locks.aquire( oid, tx );
        typeInfo.assure(oid, tx, true);
        typeInfo.delete(oid, tx);
        typeInfo.release(oid, tx);
    }

    /**
     * Expire object from the cache.  If the object to be expired is currently
     * cached, then a write lock is first acquired for this object. In addition,
     * a write lock is acquired on all objects related to, or contained within,
     * this object.  The version of the objects represented by their locks is
     * then marked as "expired".  Upon the release of each write lock
     * (@see TransactionContext#expireCache), the cached version of the objects
     * will not be placed back in the cache (@see TypeInfo#release).
     * A subsequent read/query transaction will therefore load the values of
     * the object from persistent storage.
     *
     * @param tx The transaction context
     * @param oid The object OID
     * @param timeout The max time to wait while acquiring a lock on the
     *  object (specified in seconds)
     * @return True if the object was expired successfully from the cache.
     * @throws LockNotGrantedException Timeout or deadlock occured attempting to acquire lock
     *         on object
     * @throws PersistenceException An error reported by the persistence engine
     * @throws ClassNotPersistenceCapableException The class is not persistent capable
     * @throws ObjectModifiedException Dirty checking mechanism may immediately
     *  report that the object was modified in the database during the long
     *  transaction.
     * @throws ObjectDeletedException Object has been deleted from the persistence store.
     */
    public boolean expireCache(final TransactionContext tx, final OID oid, final int timeout)
    throws PersistenceException {
        
        TypeInfo   typeInfo;
        boolean    succeed;
        ObjectLock lock;
 
        typeInfo = _typeInfo.get(oid.getName());
        if (typeInfo == null) {
            throw new ClassNotPersistenceCapableException(Messages.format(
                    "persist.classNotPersistenceCapable", oid.getName()));
        }
   
        succeed = false;
        lock = null;
        try {
            if (typeInfo.isCached(oid)) {
                lock = typeInfo.acquire(oid, tx, ObjectLock.ACTION_WRITE, timeout);
                typeInfo._molder.expireCache(tx, lock);
                lock.expire();
                succeed = true;
            }
        } catch (LockNotGrantedException e) {
            throw e;
        } catch (ObjectDeletedException e) {
            throw e;
        } catch (PersistenceException e) {
            throw e;
        } finally {
            if (lock != null) {
                lock.confirm(tx, succeed);
            }
        }
 
        return succeed;
    }
 
    /**
     * Forces the cache to be expired for the object represented by
     * ClassMolder and identity.  If identity is null then expire
     * all objects of the type represented by ClassMolder.
     * @param cls Class type instance.
     */
    public void expireCache(final Class cls) {
        TypeInfo typeInfo = _typeInfo.get(cls.getName());
        if (typeInfo != null) {
            typeInfo.expireCache();
        }
    }
    /**
     * Expires all objects of all types from cache.
     */
    public void expireCache() {
        for (Iterator<TypeInfo> iter = _typeInfo.values().iterator(); iter.hasNext(); ) {
            iter.next().expireCache();
        }
    }
    /**
     * Dump cached objects of all types to output.
     */
    public void dumpCache() {
        for (Iterator<TypeInfo> iter = _typeInfo.values().iterator(); iter.hasNext(); ) {
            iter.next().dumpCache();
        }
    }

    /**
     * Close all caches (to allow for resource clean-up).
     */
    public void closeCaches() {
        for (Iterator<TypeInfo> iter = _typeInfo.values().iterator(); iter.hasNext(); ) {
            iter.next().closeCache();
        }
        
        Collection<CacheFactory> cacheFactories = _cacheFactoryRegistry.getCacheFactories();
        for (Iterator<CacheFactory> iter = cacheFactories.iterator(); iter.hasNext(); ) {
            iter.next().shutdown(); 
        }
    }

    /**
     * Dump cached objects of specific type to output.
     * @param cls A class type.
     */
    public void dumpCache(final Class cls) {
        TypeInfo typeInfo = _typeInfo.get(cls.getName());
        if (typeInfo != null) {
            typeInfo.dumpCache();
        }
    }

    /**
     * Returns an association between Xid and transactions contexts.
     * The association is shared between all transactions open against
     * this cache engine through the XAResource interface.
     * 
     * @return Association between XId and transaction contexts.
     */
    public HashMap<Xid, TransactionContext> getXATransactions() {
        return _xaTx;
    }

    /**
     * Provides information about an object of a specific type (class's full name).
     * This information includes the object's descriptor and lifecycle interceptor
     * requesting notification about activities that affect an object.
     */
    private final class TypeInfo {
        /** The molder for this class. */
        private ClassMolder _molder;

        /** The full qualified name of the Java class represented by this type info. */
        private String _name;

        /** The Map contains all the in-used ObjectLock of the class type, which
         *  keyed by the OID representing the object. All extends classes share the
         *  same map as the base class. */
        private final HashMap<OID, ObjectLock> _locks;
        
        /** The Map contains all the freed ObjectLock of the class type, which keyed
         *  by the OID representing the object. ObjectLock put into cache maybe
         *  disposed by LRU mechanisum. All extends classes share the same map as the
         *  base class. */
        private final Cache _cache;

        /**
         * Constructor for creating base class info.
         *
         * @param  molder   The classMolder of this type.
         * @param  locks    The new HashMap which will be used
         *         for holding all the in-used ObjectLock.
         * @param  cache    The new LRU which will be used to
         *         store and dispose freed ObjectLock.
         */
        private TypeInfo(final ClassMolder molder, final HashMap<OID, ObjectLock> locks,
                final Cache cache) {
            this._name = molder.getName();
            this._molder = molder;
            this._locks = locks;
            this._cache = cache;
        }

        /**
         * Constructor for creating extended class info.
         * 
         * @param  molder   The classMolder of this type.
         * @param  base     The TypeInfo of the base class of
         *         the molder's class.
         */
        private TypeInfo(final ClassMolder molder, final TypeInfo base) {
            this(molder, base._locks, base._cache);
        }

        /**
         * Life-cycle method to allow shutdown of cache instances.
         */
        public void closeCache() {
            _cache.close();
        }
        
        /**
         * Dump all objects in cache or lock to output.
         */
        public void dumpCache() {
            _log.info(_name + ".dumpCache()...");
            synchronized (_locks) {
                for (Iterator<ObjectLock> iter = _locks.values().iterator(); iter.hasNext(); ) {
                    ObjectLock entry = iter.next();
                    _log.info("In locks: " + entry);
                }

                for (Iterator<Object> iter = _cache.values().iterator(); iter.hasNext(); ) {
                    ObjectLock entry = (ObjectLock) iter.next();
                    _log.info("In cache: " + entry.getOID());
                }
            }
        }
        
        /**
         * Expire all objects of this class from the cache.
         */
        public void expireCache() {
            synchronized (_locks) {
                // Mark all objects currently participating in a
                // transaction as expired.  They will be not be added back to
                // the LRU when the transaction's complete (@see release)
                // XXX [SMH]: Reconsider removing from locks (unknown side-effects?).
                for (Iterator<ObjectLock> iter = _locks.values().iterator(); iter.hasNext(); ) {
                    ObjectLock objectLock = iter.next();
                    objectLock.expire();
                    iter.remove();
                }
                
                // Remove all objects not participating in a transaction from the cache.
                _cache.clear();
            }
        }

        /**
         * Acquire the object lock for transaction. After this method is called,
         * user must call {@link ObjectLock#confirm(TransactionContext, boolean)} 
         * exactly once.
         *
         * @param oid The OID of the lock.
         * @param tx The context of the transaction to acquire lock.
         * @param lockAction The inital action to be performed on the lock.
         * @param timeout    The time limit to acquire the lock.
         * @return The object lock for the OID within this transaction context. 
         * @throws ObjectDeletedWaitingForLockException
         * @throws LockNotGrantedException Timeout or deadlock occured attempting
         *         to acquire lock on object
         */
        private ObjectLock acquire(final OID oid, final TransactionContext tx,
                final short lockAction, final int timeout)
        throws LockNotGrantedException {
            ObjectLock entry = null;
            // sync on "locks" is, unfortunately, necessary if we employ
            // some LRU mechanism, especially if we allow NoCache, to avoid
            // duplicated LockEntry exist at the same time.
            synchronized (_locks) {
                // consult with the 'first level' cache, aka current transaction 
                entry = _locks.get(oid);
                if (entry == null) {
                    // consult with the 'second level' cache, aka physical cache
                    CacheEntry cachedEntry = (CacheEntry) _cache.remove(oid);
                    if (cachedEntry != null) {
                        // found in 'second level' cache
                        OID cacheOid = cachedEntry.getOID();
                        if (oid.getName().equals(cacheOid.getName())) {
                            // found the requested class in cache
                            entry = new ObjectLock(cachedEntry.getOID(),
                                    cachedEntry.getEntry(), cachedEntry.getTimeStamp());
                            
                            entry.setOID(oid);
                        } else if (oid.getTopClassName().equals(cacheOid.getName())) {
                            // found a base class in cache
                            entry = new ObjectLock(oid);
                        } else {
                            // found an extended class in cache
                            entry = new ObjectLock(cachedEntry.getOID(),
                                    cachedEntry.getEntry(), cachedEntry.getTimeStamp());
                        }
                    } else {
                        // not found in 'second level' cache
                        entry = new ObjectLock(oid);
                    }
                    _locks.put(entry.getOID(), entry);
                }
                entry.enter();
            }
            
            // ObjectLock.acquire() may call Object.wait(), so a thread can not
            // been synchronized with ANY shared object before acquire().
            // So, it must be called outside synchronized( locks ) block.
            boolean failed = true;
            try {
                switch (lockAction) {
                case ObjectLock.ACTION_READ:
                    entry.acquireLoadLock(tx, false, timeout);
                    break;

                case ObjectLock.ACTION_WRITE:
                    entry.acquireLoadLock(tx, true, timeout);
                    break;

                case ObjectLock.ACTION_CREATE:
                    entry.acquireCreateLock(tx);
                    break;

                case ObjectLock.ACTION_UPDATE:
                    entry.acquireUpdateLock(tx, timeout);
                    break;

                default:
                    throw new IllegalArgumentException(
                            "lockType " + lockAction + " is undefined!"); 
                }
                
                failed = false;
                return entry;
            } finally {
                synchronized (_locks) {
                    entry.leave();
                    if (failed) {
                        // The need of this block may not be too obvious.
                        // At the very moment, if it happens, current thread 
                        // failed to acquire a lock. Then, another thread just
                        // release the lock right after. The released entry
                        // then will not be moved to cache because inLocksGap 
                        // isn't zero. So, there maybe a chance of memory 
                        // leak, as the entry was in "locks", but not in 
                        // "cache" as supposed. To avoid it from happening,
                        // we ensure here that the entry which should be move 
                        // to "cache" from "locks" is actually moved.
                        if (entry.isDisposable()) {
                            _locks.remove(entry.getOID());
                            if (entry.isExpired()) {
                                _cache.expire(entry.getOID());
                                entry.expired();
                            } else {
                                _cache.put(oid, new CacheEntry(
                                        entry.getOID(), entry.getObject(), entry.getVersion()));
                            }
                        }
                    }
                }
            }
        }

        /**
         * Upgrade the lock to write lock.
         * 
         * @param  oid The OID of the lock.
         * @param  tx The transaction in action.
         * @param  timeout  Time limit.
         * @return The upgraded ObjectLock instance.
         * @throws ObjectDeletedWaitingForLockException
         * @throws LockNotGrantedException Timeout or deadlock occured attempting
         *         to acquire lock on object.
         */
        private ObjectLock upgrade(final OID oid, final TransactionContext tx, final int timeout)
        throws LockNotGrantedException {
            OID internaloid = oid;
            ObjectLock entry = null;
            synchronized (_locks) {
                entry = _locks.get(internaloid);
                if (entry == null) {
                    throw new ObjectDeletedWaitingForLockException(
                            "Lock entry not found. Deleted?");
                }
                if (!entry.hasLock(tx, false)) {
                    throw new IllegalStateException(
                            "Transaction does not hold the any lock on " + internaloid + "!");    
                }
                internaloid = entry.getOID();
                entry.enter();
            }
            
            try {
                entry.upgrade(tx, timeout);
                return entry;
            } finally {
                synchronized (_locks) {
                    entry.leave();
                }
            }
        }

        /** 
         * Reassure the lock which have been successfully acquired by the transaction.
         *
         * @param  oid      The OID of the lock.
         * @param  tx       The transaction in action.
         * @param  write    <code>true</code> if we want to upgrade or reassure a
         *                  write lock, <code>false</code> for read lock.
         * @return The reassured ObjectLock instance.
         */
        private ObjectLock assure(final OID oid, final TransactionContext tx, final boolean write) {
            synchronized (_locks) {
                ObjectLock entry = _locks.get(oid);
                if (entry == null) {
                    throw new IllegalStateException(
                            "Lock, " + oid + ", doesn't exist or no lock!");
                }
                if (!entry.hasLock(tx, write)) {
                    throw new IllegalStateException(
                            "Transaction " + tx + " does not hold the "
                            + (write ? "write" : "read") + " lock: " + entry + "!");
                }
                return entry;
            }
        }

        /**
         * Move the locked object from one OID to another OID for transaction.
         * It is to be called by after create.
         *
         * @param orgoid  Orginal OID before the object is created.
         * @param newoid  New OID after the object is created.
         * @param tx      The TransactionContext of the transaction in action.
         * @return An ObjectLock instance whose OID has been assigned to a new value.
         * @throws LockNotGrantedException Timeout or deadlock occured attempting to
         *         acquire lock on object
         */
        private ObjectLock rename(final OID orgoid, final OID newoid, final TransactionContext tx)
        throws LockNotGrantedException {
            synchronized (_locks) {
                ObjectLock entry = _locks.get(orgoid);
                ObjectLock newentry = _locks.get(newoid);

                // validate locks
                if (orgoid == newoid) {
                    throw new LockNotGrantedException("Locks are the same");
                }
                if (entry == null) {
                    throw new LockNotGrantedException("Lock doesn't exist!");
                }
                if (!entry.isExclusivelyOwned(tx)) {
                    throw new LockNotGrantedException(
                            "Lock to be renamed is not own exclusively by transaction!");
                }
                if (entry.isEntered()) {
                    throw new LockNotGrantedException(
                            "Lock to be renamed is acquired by another transaction!");
                }
                if (newentry != null) {
                    throw new LockNotGrantedException(
                            "Lock is already existed for the new oid.");
                }

                entry = _locks.remove(orgoid);
                entry.setOID(newoid);
                _locks.put(newoid, entry);

                // copy oid status
                newoid.setDbLock(orgoid.isDbLock());

                return newentry;
            }
        }

        /**
         * Delete the object lock. It's called after the object is deleted from
         * the persistence and the transaction committed.
         *
         * @param oid   The OID of the ObjectLock.
         * @param tx    The transactionContext of transaction in action.
         * @return The just-deleted ObjectLock instance.
         */
        private ObjectLock delete(final OID oid, final TransactionContext tx) {
            ObjectLock entry;
            synchronized (_locks) {
                entry = _locks.get(oid);
                if (entry == null) {
                    throw new IllegalStateException("No lock to destroy!");
                }
                entry.enter();
            }

            try {
                entry.delete(tx);
                return entry;
            } finally {
                synchronized (_locks) {
                    entry.leave();
                    if (entry.isDisposable()) {
                        _cache.put(oid, new CacheEntry(
                                entry.getOID(), entry.getObject(), entry.getVersion()));
                        _locks.remove(oid);
                    }
                }
            }
        }

        /**
         * Release the object lock. It's called after the object the transaction
         * has been committed.
         *
         * @param oid   The OID of the ObjectLock.
         * @param tx    The transactionContext of transaction in action.
         * @return The just-released ObjectLock instance.
         */
        private ObjectLock release(final OID oid, final TransactionContext tx) {
            ObjectLock entry = null;
            synchronized (_locks) {
                entry = _locks.get(oid);
                if (entry == null) {
                    throw new IllegalStateException(
                            "No lock to release! " + oid + " for transaction " + tx);
                }
                entry.enter();
            }

            try {
                entry.release(tx);
                return entry;
            } finally {
                synchronized (_locks) {
                    entry.leave();
                    if (entry.isDisposable()) {
                        _cache.put(oid, new CacheEntry(
                                entry.getOID(), entry.getObject(), entry.getVersion()));
                        if (entry.isExpired()) {
                            _cache.expire(oid);
                            entry.expired();
                        }
                        _locks.remove(oid);
                    }
                }
            }
        }

        /**
         * Indicates whether an object with the specified identifier is curretly cached.
         *  
         * @param oid     The Object identifier.
         * @return True if the object is cached. 
         */
        public boolean isCached(final OID oid) {
            return _cache.containsKey(oid);
        }
        
        /**
         * Indicates whether an object with the specified OID is currently locked.
         * 
         * @param oid Object identifier.
         * @return True if the object is locked.
         */
        public boolean isLocked(final OID oid) {
            return _locks.containsKey(oid);
        }
    }

    /**
     * Provides information about whether an object of Class cls with identity iod is
     * currently cached.
     * 
     * @param cls Class type.
     * @param oid Object identity
     * @return True if the specified object is in the cache.
     */
    public boolean isCached(final Class cls, final OID oid) {
        TypeInfo typeInfo = _typeInfo.get(cls.getName());
        return typeInfo.isCached (oid);
    }

    public boolean isLocked(final Class cls, final OID oid) {
        TypeInfo typeInfo = _typeInfo.get(cls.getName());
        return typeInfo.isLocked (oid);
    }
}
