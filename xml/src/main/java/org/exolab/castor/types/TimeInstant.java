/*
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
 * Copyright 2000 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: TimeInstant.java 6421 2006-11-22 02:18:29Z ekuns $
 * Date         Author          Changes
 * 11/02/2000   Arnaud Blandin  Changed the constructor
 * 26/10/2000   Arnaud Blandin  Created
 */
package org.exolab.castor.types;

/**
 * Describe an XML schema TimeInstant. The time type is derived from
 * recurringDuration by setting up the facet:
 * <ul>
 * <li>duration to "P0Y"</li>
 * <li>period to "P0Y"</li>
 * </ul>
 * <p>
 * Note: This datatype is not included in any recommendation. It was introduced
 * in http://www.w3.org/TR/1999/WD-xmlschema-2-19991105/ and was last in
 * http://www.w3.org/TR/2000/CR-xmlschema-2-20001024/ and was removed by
 * http://www.w3.org/TR/2001/PR-xmlschema-2-20010316/. It was not in the final
 * approved recommendation: http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/
 *
 * @author <a href="mailto:blandin@intalio.com">Arnaud Blandin</a>
 * @version $Revision: 6421 $
 * @see RecurringDuration
 * @deprecated since Castor 1.0.6 since this type is not in any recommendation.
 */
public class TimeInstant extends RecurringDuration {

    /** SerialVersionUID */
    private static final long serialVersionUID = -5261713908033956150L;

    public TimeInstant() {
        super("P0Y","P0Y");
    }

}
