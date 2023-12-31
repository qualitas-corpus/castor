/*
 * Copyright 2006 Assaf Arkin, Thomas Yip, Bruce Snyder, Werner Guttmann, Ralf Joachim
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
 *
 * $Id: SQLEngine.java 8459 2009-11-09 04:27:56Z rjoachim $
 */
package org.exolab.castor.jdo.engine;

import java.sql.Connection;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.core.util.Messages;
import org.castor.cpa.persistence.sql.engine.SQLStatementInsert;
import org.castor.cpa.persistence.sql.engine.SQLStatementDelete;
import org.castor.cpa.persistence.sql.engine.SQLStatementUpdate;
import org.castor.persist.ProposedEntity;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryException;
import org.exolab.castor.jdo.engine.nature.ClassDescriptorJDONature;
import org.exolab.castor.jdo.engine.nature.FieldDescriptorJDONature;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.TypeConvertor;
import org.exolab.castor.mapping.loader.ClassDescriptorImpl;
import org.exolab.castor.mapping.loader.FieldHandlerImpl;
import org.exolab.castor.persist.SQLRelationLoader;
import org.exolab.castor.persist.spi.Identity;
import org.exolab.castor.persist.spi.Persistence;
import org.exolab.castor.persist.spi.PersistenceFactory;
import org.exolab.castor.persist.spi.PersistenceQuery;
import org.exolab.castor.persist.spi.QueryExpression;

/**
 * The SQL engine performs persistence of one object type against one
 * SQL database. It can only persist simple objects and extended
 * relationships. An SQL engine is created for each object type
 * represented by a database. When persisting, it requires a physical
 * connection that maps to the SQL database and the transaction
 * running on that database
 *
 * @author <a href="mailto:arkin AT intalio DOT com">Assaf Arkin</a>
 * @author <a href="mailto:yip AT intalio DOT com">Thomas Yip</a>
 * @author <a href="mailto:ferret AT frii DOT com">Bruce Snyder</a>
 * @author <a href="mailto:werner DOT guttmann AT gmx DOT net">Werner Guttmann</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8459 $ $Date: 2006-04-26 16:24:34 -0600 (Wed, 26 Apr 2006) $
 */
public final class SQLEngine implements Persistence {
    
    /** The <a href="http://jakarta.apache.org/commons/logging/">Jakarta
     *  Commons Logging</a> instance used for all logging. */
    private static final Log LOG = LogFactory.getLog(SQLEngine.class);
    
    private static final String JDO_FIELD_NATURE = FieldDescriptorJDONature.class.getName();

    private final SQLFieldInfo[]        _fields;

    private final SQLColumnInfo[]       _ids;

    private SQLEngine                   _extends;

    private final PersistenceFactory    _factory;

    private final ClassDescriptor    _clsDesc;

    private final SQLStatementQuery _queryStatement;

    private final SQLStatementLoad _loadStatement;

    private final SQLStatementInsert _createStatement;

    private final SQLStatementDelete _removeStatement;

    private final SQLStatementUpdate _storeStatement;

    public SQLEngine(final ClassDescriptor clsDesc, final PersistenceFactory factory)
    throws MappingException {

        _clsDesc = clsDesc;
        _factory = factory;

        // construct field and id info
        Vector<SQLColumnInfo> idsInfo = new Vector<SQLColumnInfo>();
        Vector<SQLFieldInfo> fieldsInfo = new Vector<SQLFieldInfo>();

        /*
         * Implementation Note:
         * Extends and Depends has some special mutual exclusive
         * properties, which implementator should aware of.
         *
         * A Depended class may depends on another depended class
         * A class should either extends or depends on other class
         * A class should not depend on extending class.
         *  because, it is the same as depends on the base class
         * A class may be depended by zero or more classes
         * A class may be extended by zero or more classes
         * A class may extends only zero or one class
         * A class may depends only zero or one class
         * A class may depend on extended class
         * A class may extend a dependent class.
         * A class may extend a depended class.
         * No loop or circle should exist
         */
        // then, we put depended class ids in the back
        ClassDescriptor base = clsDesc;

        // walk until the base class which this class extends
        base = clsDesc;
        Stack<ClassDescriptor> stack = new Stack<ClassDescriptor>();
        stack.push(base);
        while (base.getExtends() != null) {
            // if (base.getDepends() != null) {
            //     throw new MappingException(
            //             "Class should not both depends on and extended other classes");
            // }
            base = base.getExtends();
            stack.push(base);
            // do we need to add loop detection?
        }

        // now base is either the base of extended class, or
        // clsDesc
        // we always put the original id info in front
        // [oleg] except for SQL name, it may differ.
        FieldDescriptor[] baseIdDescriptors = ((ClassDescriptorImpl) base).getIdentities();
        FieldDescriptor[] idDescriptors = ((ClassDescriptorImpl) clsDesc).getIdentities();

        for (int i = 0; i < baseIdDescriptors.length; i++) {
            if (baseIdDescriptors[i].hasNature(FieldDescriptorJDONature.class.getName())) {
                String name = baseIdDescriptors[i].getFieldName();
                String[] sqlName = 
                    new FieldDescriptorJDONature(baseIdDescriptors[i]).getSQLName();
                int[] sqlType =  new FieldDescriptorJDONature(baseIdDescriptors[i]).getSQLType();
                FieldHandlerImpl fh = (FieldHandlerImpl) baseIdDescriptors[i].getHandler();

                // The extending class may have other SQL names for identity fields
                for (int j = 0; j < idDescriptors.length; j++) {
                    if (name.equals(idDescriptors[j].getFieldName())
                            && (idDescriptors[j].hasNature(JDO_FIELD_NATURE))) {
                        sqlName = new FieldDescriptorJDONature(idDescriptors[j]).getSQLName();
                        break;
                    }
                }
                idsInfo.add(new SQLColumnInfo(sqlName[0], sqlType[0], fh.getConvertTo(),
                        fh.getConvertFrom()));
            } else {
                throw new MappingException("Except JDOFieldDescriptor");
            }
        }

        // then do the fields
        while (!stack.empty()) {
            base = stack.pop();
            FieldDescriptor[] fieldDescriptors = base.getFields();
            for (int i = 0; i < fieldDescriptors.length; i++) {
                // fieldDescriptors[i] is persistent in db if it is not transient
                // and it is a JDOFieldDescriptor or has a ClassDescriptor
                if (!fieldDescriptors[i].isTransient()) {
                    if ((fieldDescriptors[i].hasNature(FieldDescriptorJDONature.class.getName()))
                            || (fieldDescriptors[i].getClassDescriptor() != null))  {
                        
                        fieldsInfo.add(new SQLFieldInfo(clsDesc, fieldDescriptors[i],
                                new ClassDescriptorJDONature(base).getTableName(), !stack.empty()));
                    }
                }
            }
        }

        _ids = new SQLColumnInfo[idsInfo.size()];
        idsInfo.copyInto(_ids);

        _fields = new SQLFieldInfo[fieldsInfo.size()];
        fieldsInfo.copyInto(_fields);
        
        _queryStatement = new SQLStatementQuery(this, factory);
        _loadStatement = new SQLStatementLoad(this, factory);
        _createStatement = new SQLStatementInsert(this, factory);
        _removeStatement = new SQLStatementDelete(this, factory);
        _storeStatement = new SQLStatementUpdate(this, factory);
    }
    
    public SQLRelationLoader createSQLRelationLoader(final String manyTable,
            final String[] idSQL, final int[] idType,
            final TypeConvertor[] idTo, final TypeConvertor[] idFrom,
            final String[] relatedIdSQL, final int[] relatedIdType,
            final TypeConvertor[] ridTo, final TypeConvertor[] ridFrom) {
        return new SQLRelationLoader(manyTable, idSQL, idType, idTo, idFrom,
                relatedIdSQL, relatedIdType, ridTo, ridFrom, _factory);
    }

    public SQLColumnInfo[] getColumnInfoForIdentities() {
        return _ids;
    }
    
    public SQLFieldInfo[] getInfo() {
        return _fields;
    }

    /**
     * Mutator method for setting extends SQLEngine.
     * 
     * @param engine
     */
    public void setExtends(final SQLEngine engine) {
        _extends = engine;
    }
    
    public SQLEngine getExtends() { return _extends; }

    /**
     * Used by {@link org.exolab.castor.jdo.OQLQuery} to retrieve the class descriptor.
     * @return the JDO class descriptor.
     */
    public ClassDescriptor getDescriptor() {
        return _clsDesc;
    }

    public PersistenceQuery createQuery(final QueryExpression query, final Class[] types,
                                        final AccessMode accessMode)
    throws QueryException {
        AccessMode mode = (accessMode != null)
                        ? accessMode
                        : new ClassDescriptorJDONature(_clsDesc).getAccessMode();
        String sql = query.getStatement(mode == AccessMode.DbLocked);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.format("jdo.createSql", sql));
        }
        
        return new SQLQuery(this, _factory, sql, types, false);
    }


    public PersistenceQuery createCall(final String spCall, final Class[] types) {
        FieldDescriptor[] fields;
        String[] jdoFields0;
        String[] jdoFields;
        String sql;
        int[] sqlTypes0;
        int[] sqlTypes;
        int count;

        // changes for the SQL Direct interface begins here
        if (spCall.startsWith("SQL")) {
            sql = spCall.substring(4);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.format("jdo.directSQL", sql));
            }
            
            return new SQLQuery(this, _factory, sql, types, true);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.format("jdo.spCall", spCall));
        }

        fields = _clsDesc.getFields();
        jdoFields0 = new String[fields.length + 1];
        sqlTypes0 = new int[fields.length + 1];
        // the first field is the identity

        count = 1;
        jdoFields0[0] = _clsDesc.getIdentity().getFieldName();
        sqlTypes0[0] =  new FieldDescriptorJDONature(_clsDesc.getIdentity()).getSQLType()[0];
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].hasNature(FieldDescriptorJDONature.class.getName())) {
                jdoFields0[count] = new FieldDescriptorJDONature(fields[i]).getSQLName()[0];
                sqlTypes0[count] =  new FieldDescriptorJDONature(fields[i]).getSQLType()[0];
                ++count;
            }
        }
        jdoFields = new String[count];
        sqlTypes = new int[count];
        System.arraycopy(jdoFields0, 0, jdoFields, 0, count);
        System.arraycopy(sqlTypes0, 0, sqlTypes, 0, count);

        return _factory.getCallQuery(spCall, types,
                _clsDesc.getJavaClass(), jdoFields, sqlTypes);
    }

    public QueryExpression getQueryExpression() {
        return _factory.getQueryExpression();
    }

    public QueryExpression getFinder() {
        return _queryStatement.getQueryExpression();
    }

    protected Object idToJava(final int index, final Object object) {
        if ((object == null) || (_ids[index].getConvertTo() == null)) {
            return object;
        }
        return _ids[index].getConvertTo().convert(object);
    }

    protected Object toJava(final int field, final int column, final Object object) {
        SQLColumnInfo col = _fields[field].getColumnInfo()[column];
        if ((object == null) || (col.getConvertTo() == null)) {
            return object;
        }
        return col.getConvertTo().convert(object);
    }

    public Identity create(final Database database, final Object conn,
                         final ProposedEntity entity, final Identity identity)
    throws PersistenceException {
        return (Identity) _createStatement.executeStatement(
                database, (Connection) conn, identity, entity);
    }

    public Object store(final Object conn, final Identity identity,
                        final ProposedEntity newentity,
                        final ProposedEntity oldentity)
    throws PersistenceException {
        // check size of identity columns
        if (identity.size() != _ids.length) {
            throw new PersistenceException("Size of identity field mismatched!");
        }

        return _storeStatement.executeStatement((Connection) conn, identity, newentity, oldentity);
    }

    public void delete(final Object conn, final Identity identity)
    throws PersistenceException {
        // check size of identity columns
        if (identity.size() != _ids.length) {
            throw new PersistenceException("Size of identity field mismatched!");
        }

        _removeStatement.executeStatement((Connection) conn, identity);
    }

    /**
     * Loads the object from persistence storage. This method will load
     * the object fields from persistence storage based on the object's
     * identity. This method may return a stamp which can be used at a
     * later point to determine whether the copy of the object in
     * persistence storage is newer than the cached copy (see {@link
     * #store}). If <tt>lock</tt> is true the object must be
     * locked in persistence storage to prevent concurrent updates.
     *
     * @param conn An open connection
     * @param entity An Object[] to load field values into
     * @param identity Identity of the object to load.
     * @param accessMode The access mode (null equals shared)
     * @throws PersistenceException A persistence error occured
     */
    public void load(final Object conn, final ProposedEntity entity,
                       final Identity identity, final AccessMode accessMode)
    throws PersistenceException {
        if (identity.size() != _ids.length) {
        	throw new PersistenceException("Size of identity field mismatched!");
        }
        
        _loadStatement.executeStatement((Connection) conn, identity, entity, accessMode);
    }
    
    public String toString() { return _clsDesc.toString(); }
}
