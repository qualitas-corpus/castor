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
 * $Id: SybaseQueryExpression.java 8459 2009-11-09 04:27:56Z rjoachim $
 */
package org.castor.cpa.persistence.sql.driver;

import java.util.Enumeration;

import org.exolab.castor.jdo.engine.JDBCSyntax;
import org.exolab.castor.persist.spi.PersistenceFactory;

/**
 * QueryExpression for Sybase Adaptive Servers.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @version $Revision: 8459 $ $Date: 2004-10-01 07:25:46 -0600 (Fri, 01 Oct 2004) $
 */
public final class SybaseQueryExpression extends JDBCQueryExpression {
    public SybaseQueryExpression(final PersistenceFactory factory) {
        super(factory);
    }

    public String getStatement(final boolean lock) {
        StringBuffer sql;
        boolean first;

        sql = new StringBuffer();
        sql.append(JDBCSyntax.SELECT);
        if (_distinct) {
            sql.append(JDBCSyntax.DISTINCT);
        }

        sql.append(getColumnList());
        
        sql.append(JDBCSyntax.FROM);

        // Use HOLDLOCK to lock selected tables.
        Enumeration<String> enumeration = _tables.keys();
        while (enumeration.hasMoreElements()) {
            String tableAlias = enumeration.nextElement();
            String tableName = _tables.get(tableAlias);
            if (tableAlias.equals(tableName)) {
                sql.append(_factory.quoteName(tableName));
            } else {
                sql.append(_factory.quoteName(tableName) + " "
                        + _factory.quoteName(tableAlias));
            }
            if (lock) {
                sql.append(" HOLDLOCK ");
            }
            if (enumeration.hasMoreElements()) {
                sql.append(JDBCSyntax.TABLE_SEPARATOR);
            }
        }

        first = true;
        // Use asterisk notation to denote a left outer join
        // and equals to denote an inner join
        for (int i = 0; i < _joins.size(); ++i) {
            Join join;

            if (first) {
                sql.append(JDBCSyntax.WHERE);
                first = false;
            } else {
                sql.append(JDBCSyntax.AND);
            }

            join = _joins.elementAt(i);
            for (int j = 0; j < join._leftColumns.length; ++j) {
                if (j > 0) {
                    sql.append(JDBCSyntax.AND);
                }
                sql.append(_factory.quoteName(join._leftTable
                        + JDBCSyntax.TABLE_COLUMN_SEPARATOR
                        + join._leftColumns[j]));
                if (join._outer) {
                    sql.append("*=");
                } else {
                    sql.append(OP_EQUALS);
                }
                sql.append(_factory.quoteName(join._rightTable
                        + JDBCSyntax.TABLE_COLUMN_SEPARATOR
                        + join._rightColumns[j]));
            }
        }
        first = addWhereClause(sql, first);
 
        if (_order != null) {
            sql.append(JDBCSyntax.ORDER_BY).append(_order);
        }
          
        return sql.toString();
    }
}


