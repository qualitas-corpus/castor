/*
 * Copyright 2007 Ralf Joachim
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
package org.castor.cpa.persistence.convertor;

import java.util.Date;

/**
 * Convert <code>Date</code> to <code>java.sql.Timestamp</code>.
 * 
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 7134 $ $Date: 2006-04-25 15:08:23 -0600 (Tue, 25 Apr 2006) $
 * @since 1.1.3
 */
public final class DateToSqlTimestamp extends AbstractSimpleTypeConvertor {
    //-----------------------------------------------------------------------------------

    /** Constant used for calculation of time nanos. */
    private static final long THOUSAND = 1000;

    /** Constant used for calculation of time nanos. */
    private static final long MILLION = 1000000;

    //-----------------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public DateToSqlTimestamp() {
        super(Date.class, java.sql.Timestamp.class);
    }

    //-----------------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public Object convert(final Object object) {
        long time = ((Date) object).getTime();
        
        java.sql.Timestamp timestamp = new java.sql.Timestamp(time);
        timestamp.setNanos((int) ((time % THOUSAND) * MILLION));
        //timestamp.setNanos(0);  // this can workaround the bug in SAP DB
        return timestamp;
    }

    //-----------------------------------------------------------------------------------
}
