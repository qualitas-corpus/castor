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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Convert <code>java.sql.Timestamp</code> to <code>String</code>.
 * 
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 7134 $ $Date: 2006-04-25 15:08:23 -0600 (Tue, 25 Apr 2006) $
 * @since 1.1.3
 */
public final class SqlTimestampToString extends AbstractDateTypeConvertor {
    //-----------------------------------------------------------------------------------

    /** Default pattern to convert java.sql.Timestamp to String and back. */
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /** Constant used for calculation of time nanos. */
    private static final long MILLION = 1000000;

    //-----------------------------------------------------------------------------------

    /**
     * Date format used by this date convertor. Use the {@link #getCustomDateFormat} accessor
     * to access this variable.
     */
    private SimpleDateFormat _customDateFormat;

    //-----------------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public SqlTimestampToString() {
        super(java.sql.Timestamp.class, String.class);
        
        parameterize(null);
    }

    //-----------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void parameterize(final String parameter) {
        _customDateFormat = getDefaultDateFormat();
        if ((parameter == null) || (parameter.length() ==  0)) {
            _customDateFormat.applyPattern(TIMESTAMP_PATTERN);
        } else {
            _customDateFormat.applyPattern(parameter);
        }
    }
    
    /**
     * Use this accessor to access the custom date format property.
     * 
     * @return A clone of the custom date format property.
     */
    private SimpleDateFormat getCustomDateFormat() {
        return (SimpleDateFormat) _customDateFormat.clone();
    }

    //-----------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public Object convert(final Object object) {
        java.sql.Timestamp timestamp = (java.sql.Timestamp) object;
        long date = timestamp.getTime() + timestamp.getNanos() / MILLION;
        return getCustomDateFormat().format(new Date(date));
    }

    //-----------------------------------------------------------------------------------
}
