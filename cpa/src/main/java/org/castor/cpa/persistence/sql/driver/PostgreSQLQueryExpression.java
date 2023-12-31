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
 * $Id: PostgreSQLQueryExpression.java 7585 2008-05-03 18:45:19Z rjoachim $
 */
package org.castor.cpa.persistence.sql.driver;

import org.exolab.castor.jdo.engine.JDBCSyntax;
import org.exolab.castor.persist.spi.PersistenceFactory;

/**
 * QueryExpression for PostgreSQL 6.5/7.
 *
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 7585 $ $Date: 2004-10-01 13:45:49 -0600 (Fri, 01 Oct 2004) $
 */
public final class PostgreSQLQueryExpression extends JDBCQueryExpression {
    public PostgreSQLQueryExpression(final PersistenceFactory factory) {
        super(factory);
    }

    public String getStatement(final boolean lock) {
        StringBuffer sql;

        sql = getStandardStatement(lock, false);

        if (_limit != null) {
            sql.append(JDBCSyntax.LIMIT).append(_limit);
            if (_offset != null) {
                sql.append(JDBCSyntax.OFFSET).append(_offset);
            }
        }

        // Use FOR UPDATE to lock selected tables.
        if (lock) {
            sql.append(" FOR UPDATE");
        }

        return sql.toString();
    }

    /** 
     * Provides an implementation of {@link QueryExpression#isLimitClauseSupported()}.
     * @return true to indicate that this feature is supported by postgreSQL. 
     * @see org.exolab.castor.persist.spi.QueryExpression#isLimitClauseSupported()
     */
    public boolean isLimitClauseSupported() {
        return true;
    }
    
    /** 
     * Provides an implementation of {@link QueryExpression#isOffsetClauseSupported()}. 
     * @return true to indicate that this feature is supported by postgreSQL. 
     * @see org.exolab.castor.persist.spi.QueryExpression#isOffsetClauseSupported()
     */
    public boolean isOffsetClauseSupported() {
        return true;
    }
}
