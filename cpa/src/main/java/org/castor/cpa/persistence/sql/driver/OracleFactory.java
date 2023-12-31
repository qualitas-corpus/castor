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
 * $Id: OracleFactory.java 8377 2009-08-20 20:57:24Z rjoachim $
 */
package org.castor.cpa.persistence.sql.driver;

import java.sql.Types;

import org.exolab.castor.persist.spi.PersistenceQuery;
import org.exolab.castor.persist.spi.QueryExpression;

/**
 * {@link org.exolab.castor.persist.spi.PersistenceFactory} for Oracle 7/8 driver.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 8377 $ $Date: 2006-04-25 15:08:23 -0600 (Tue, 25 Apr 2006) $
 */
public final class OracleFactory extends GenericFactory {
    //-----------------------------------------------------------------------------------

    public static final String FACTORY_NAME = "oracle";

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
        return new OracleQueryExpression(this);
    }

    /**
     * @inheritDoc
     */
    public String quoteName(final String name) {
        return doubleQuoteName(name).toUpperCase();
    }

    /**
     * Needed to process OQL queries of "CALL" type (using stored procedure
     * call). This feature is specific for JDO.
     * 
     * @param call Stored procedure call (without "{call")
     * @param paramTypes The types of the query parameters
     * @param javaClass The Java class of the query results
     * @param fields The field names
     * @param sqlTypes The field SQL types
     * @return null if this feature is not supported.
     */
    public PersistenceQuery getCallQuery(final String call, final Class<?>[] paramTypes,
            final Class<?> javaClass, final String[] fields, final int[] sqlTypes) {
        return new ReturnedRSCallQuery(call, paramTypes, javaClass, fields, sqlTypes);
    }

    /**
     * For INTEGER type ResultSet.getObject() returns BigDecimal:
     * dependent objects with integer identity cause type conversion error
     * (need to fix SimpleQueryExecutor).
     * 
     * @inheritDoc
     */
    public Class<?> adjustSqlType(final Class<?> sqlType) {
        if (sqlType == java.lang.Integer.class) {
            return java.math.BigDecimal.class;
        }
        return sqlType;
    }
    
    @Override
    public boolean isKeyGeneratorSequenceSupported(final boolean returning, final boolean trigger) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isKeyGeneratorSequenceTypeSupported(final int type) {
        if (type == Types.INTEGER) { return true; }
        if (type == Types.DECIMAL) { return true; }
        if (type == Types.NUMERIC) { return true; }
        if (type == Types.BIGINT) { return true; }
        if (type == Types.CHAR) { return true; }
        if (type == Types.VARCHAR) { return true; }

        return false;
    }
    
    @Override
    public String getSequenceNextValString(final String seqName) {
        return seqName + ".nextval";
    }
    
    @Override
    public String getSequenceBeforeSelectString(final String seqName, 
           final String tableName, final int increment) {
    return "SELECT " + this.quoteName(seqName + ".nextval") + " FROM DUAL";
    }
    
    //-----------------------------------------------------------------------------------
}


