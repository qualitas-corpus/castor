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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: MySQLFactory.java 8377 2009-08-20 20:57:24Z rjoachim $
 * Date        Author         Changes
 * Nov 3 2000  Leonardo Bueno Created
 *                            

 */
package org.castor.cpa.persistence.sql.driver;

import java.sql.Types;

import org.exolab.castor.persist.spi.QueryExpression;

/**
 * {@link org.exolab.castor.persist.spi.PersistenceFactory} for MySQL JDBC driver.
 *
 * @author <a href="leonardo@itera.com.br">Leonardo Souza Mario Bueno</a>
 * @version $Revision: 8377 $ $Date: 2006-02-21 16:05:42 -0700 (Tue, 21 Feb 2006) $
 */
public final class MySQLFactory extends GenericFactory {
    //-----------------------------------------------------------------------------------

    /** Internal name for this {@link org.exolab.castor.persist.spi.PersistenceFactory} instance. */
    public static final String FACTORY_NAME = "mysql";

    /**
     * @inheritDoc
     */
    public String getFactoryName() {
        return FACTORY_NAME;
    }

    /**
     * @inheritDoc
     */
    public QueryExpression getQueryExpression() {
        return new MySQLQueryExpression(this);
    }

    /**
     * For CLOB type ResultSet.setClob() is not supported yet by mm.MySql JDBC driver.
     * BLOB support is buggy in MM.MySQL 2.0.3: it handles NULL values in incorrect way.
     */
    public Class<?> adjustSqlType(final Class<?> sqlType) {
        if (sqlType == java.sql.Clob.class) {
            return java.lang.String.class;
        } else if (sqlType == java.io.InputStream.class) {
            return byte[].class;
        } else {
            return sqlType;
        }
    }
    
    //-----------------------------------------------------------------------------------

    @Override
    public boolean isKeyGeneratorIdentitySupported() {
        return true;
    }
    
    @Override
    public boolean isKeyGeneratorIdentityTypeSupported(final int type) {
        if (type == Types.INTEGER) { return true; }
        if (type == Types.NUMERIC) { return true; }
        if (type == Types.DECIMAL) { return true; }
        if (type == Types.BIGINT) { return true; }
        return false;
    }
    
    @Override
    public String getIdentitySelectString(final String tableName, final String columnName) {
        return "SELECT LAST_INSERT_ID()";
    }

    //-----------------------------------------------------------------------------------
}


