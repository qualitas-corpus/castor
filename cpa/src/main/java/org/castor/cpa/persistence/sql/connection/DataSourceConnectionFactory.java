/*
 * Copyright 2005 Werner Guttmann, Ralf Joachim
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
package org.castor.cpa.persistence.sql.connection;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.core.util.Messages;
import org.castor.jdo.conf.DataSource;
import org.castor.jdo.conf.Param;
import org.exolab.castor.mapping.MappingException;

/**
 * @author <a href="mailto:werner DOT guttmann AT gmx DOT net">Werner Guttmann</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8111 $ $Date: 2006-04-12 15:13:08 -0600 (Wed, 12 Apr 2006) $
 * @since 0.9.9
 */
public final class DataSourceConnectionFactory implements ConnectionFactory {
    //--------------------------------------------------------------------------

    /** The <a href="http://jakarta.apache.org/commons/logging/">Jakarta
     *  Commons Logging</a> instance used for all logging. */
    private static final Log LOG = LogFactory.getLog(DataSourceConnectionFactory.class);

    //--------------------------------------------------------------------------

    /**
     * Initialize JDBC DataSource instance with the given database configuration
     * instances and the given class loader.
     * 
     * @param confDataSource DataSource configuration.
     * @param loader ClassLoader to use. 
     * @return The initialized DataSource.
     * @throws MappingException Problem related to analysing the JDO configuration.
     */
    public static javax.sql.DataSource loadDataSource(
            final DataSource confDataSource, final ClassLoader loader) 
    throws MappingException {
        String className = confDataSource.getClassName();
        ClassLoader classLoader = loader;
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        javax.sql.DataSource sqlDataSource;
        try {
            Class<?> dsClass = Class.forName(className, true, classLoader);
            sqlDataSource = (javax.sql.DataSource) dsClass.newInstance();
        } catch (Exception e) {
            String msg = Messages.format("jdo.engine.classNotInstantiable", className);
            LOG.error(msg, e);
            throw new MappingException(msg, e);
        }
        
        Param[] parameters = confDataSource.getParam();
        setParameters(sqlDataSource, parameters);
        
        return sqlDataSource;
    }
    
    /**
     * Set all the parameters of the given array at the given datasource by calling
     * one of the set methods of the datasource.
     * 
     * @param dataSource The datasource to set the parameters on.
     * @param params The parameters to set on the datasource.
     * @throws MappingException If one of the parameters could not be set.
     */
    public static void setParameters(final javax.sql.DataSource dataSource, final Param[] params)
    throws MappingException {
        Method[] methods = dataSource.getClass().getMethods();
        
        for (int j = 0; j < params.length; j++) {
            String name = buildMethodName(params[j].getName());
            String value = params[j].getValue();

            boolean success = false;
            Exception cause = null;
            
            try {
                int i = 0;
                while (!success && (i < methods.length)) {
                    Method method = methods[i];
                    Class<?>[] types = method.getParameterTypes();
                    if ((method.getName().equals(name)) && (types.length == 1)) {
                        if (types[0] == String.class) {
                            method.invoke(dataSource, new Object[] {value});
                            success = true;
                        } else if (types[0] == int.class) {
                            method.invoke(dataSource, new Object[] {new Integer(value)});
                            success = true;
                        } else if (types[0] == long.class) {
                            method.invoke(dataSource, new Object[] {new Long(value)});
                            success = true;
                        } else if (types[0] == boolean.class) {
                            method.invoke(dataSource, new Object[] {new Boolean(value)});
                            success = true;
                        }
                    }
                    i++;
                }
            } catch (Exception e) {
                cause = e;
            }
            
            if (!success || (cause != null)) {
                String msg = Messages.format("jdo.engine.datasourceParaFail",
                                             params[j].getName(), value);
                LOG.error(msg, cause);
                throw new MappingException(msg, cause);
            }
        }
    }
    
    /**
     * Build the name of the method to set the parameter value of the given name. The
     * name of the method is build by preceding given parameter name with 'set' followed
     * by all letters of the name. In addition the first letter and all letters
     * following a '-' sign are converted to upper case. 
     * 
     * @param name The name of the parameter.
     * @return The name of the method to set the value of this parameter.
     */
    public static String buildMethodName(final String name) {
        StringBuffer sb = new StringBuffer("set");
        boolean first = true;
        for (int i = 0; i < name.length(); i++) {
            char chr = name.charAt(i);
            if (first && Character.isLowerCase(chr)) {
                sb.append(Character.toUpperCase(chr));
                first = false;
            } else if (Character.isLetter(chr)) {
                sb.append(chr);
                first = false;
            } else if (chr == '-') {
                first = true;
            }
        }
        return sb.toString();
    }

    //--------------------------------------------------------------------------
    
    /** DataSource configuration. */
    private final DataSource _confDataSource;

    /** Wrap JDBC connections by proxies? */
    private final boolean _useProxies;
    
    /** ClassLoader to use. */
    private final ClassLoader _loader;
    
    /** The data source when using a JDBC dataSource. */
    private javax.sql.DataSource _sqlDataSource = null;

    //--------------------------------------------------------------------------

    /**
     * Constructs a new DataSourceConnectionFactory with given name, engine, mapping
     * and datasource. Factory will be ready to use without calling initialize first.
     * 
     * @param datasource The preconfigured datasource to use for creating connections.
     * @param useProxies Wrap JDBC connections by proxies?
     */
    public DataSourceConnectionFactory(final javax.sql.DataSource datasource,
            final boolean useProxies) {
        _confDataSource = null;
        _useProxies = useProxies;
        _loader = null;
        _sqlDataSource = datasource;
    }

    /**
     * Constructs a new DataSourceConnectionFactory with given database and mapping.
     * Initialize needs to be called before using the factory to create connections.
     * 
     * @param dataSource DataSouce configuration. 
     * @param useProxies Wrap JDBC connections by proxies?
     * @param loader ClassLoader to use. 
     */
    public DataSourceConnectionFactory(final DataSource dataSource, final boolean useProxies,
            final ClassLoader loader) {
        _confDataSource = dataSource;
        _useProxies = useProxies;
        _loader = loader;
    }

    /**
     * {@inheritDoc}
     */
    public void initializeFactory() throws MappingException {
        if (_sqlDataSource == null) {
            _sqlDataSource = loadDataSource(_confDataSource, _loader);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Using DataSource: " + _confDataSource.getClassName());
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public Connection createConnection () throws SQLException {
        Connection connection = _sqlDataSource.getConnection();
        if (!_useProxies) { return connection; }
        return ConnectionProxyFactory.newConnectionProxy(connection, getClass().getName());
    }

    //--------------------------------------------------------------------------
}
