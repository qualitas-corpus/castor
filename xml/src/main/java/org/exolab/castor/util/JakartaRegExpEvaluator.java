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
 * Copyright 2000 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: JakartaRegExpEvaluator.java 7951 2008-10-09 20:52:17Z wguttmn $
 */

package org.exolab.castor.util;

import org.apache.regexp.*;
import org.castor.core.util.Messages;

/**
 * An implementation of {@link RegExpEvaluator} that uses the Jakarta Regular
 * Expression library. For more information about the Jakarta RE library please
 * visit: <a href="http://jakarta.apache.org/regexp/">
 * http://jakarta.apache.org/regexp/</a>
 * 
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision: 7951 $ $Date: 2004-02-14 02:16:35 -0700 (Sat, 14 Feb
 *          2004) $
 */
public class JakartaRegExpEvaluator implements RegExpEvaluator {

    /**
     * Reguar expression denoting a beginning of line.
     */
    private static final String BOL = "^(";

    /**
     * Regular expression denoting end of line.
     */
    private static final String EOL = ")$";

    /**
     * The Regular expression
     */
    private RE _regexp;

    /**
     * {@inheritDoc}
     * 
     * @see org.exolab.castor.util.RegExpEvaluator#setExpression(java.lang.String)
     */
    public void setExpression(final String rexpr) {

        if (rexpr != null) {
            try {
                // -- patch and compile expression
                _regexp = new RE(BOL + rexpr + EOL);
            } catch (RESyntaxException ex) {
                String message = Messages.format("regexp.eval.error", rexpr);
                IllegalArgumentException iae = new IllegalArgumentException(
                        message);
                iae.initCause(ex);
                throw iae;
            }
        } else {
            _regexp = null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.exolab.castor.util.RegExpEvaluator#matches(java.lang.String)
     */
    public boolean matches(final String value) {
        if (_regexp != null) {
            return _regexp.match(value);
        }
        return true;
    }

}
