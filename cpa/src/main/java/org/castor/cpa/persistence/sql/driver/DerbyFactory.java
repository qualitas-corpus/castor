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
 * $Id: DerbyFactory.java 8377 2009-08-20 20:57:24Z rjoachim $
 */
package org.castor.cpa.persistence.sql.driver;

import java.sql.Types;

import org.exolab.castor.persist.spi.QueryExpression;

/**
 * {@link org.exolab.castor.persist.spi.PersistenceFactory} for DB 2 driver.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 8377 $ $Date$
 */
public final class DerbyFactory extends GenericFactory {
    //-----------------------------------------------------------------------------------

    /** Internal name of this PersistenceFactory implementation. */
    public static final String FACTORY_NAME = "derby";

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
        return new DerbyQueryExpression(this);
    }
    
    //-----------------------------------------------------------------------------------

    @Override
    public boolean isKeyGeneratorIdentitySupported() {
        return true;
    }
    
    @Override
    public boolean isKeyGeneratorIdentityTypeSupported(final int type) {
        if (type == Types.INTEGER) { return true; }
        if (type == Types.BIGINT) { return true; }
        if (type == Types.NUMERIC) { return true; }
        return false;
    }
    
    @Override
    public String getIdentitySelectString(final String tableName, final String columnName) {
        return "SELECT IDENTITY_VAL_LOCAL() FROM " + quoteName(tableName);
    }

    //-----------------------------------------------------------------------------------
}


