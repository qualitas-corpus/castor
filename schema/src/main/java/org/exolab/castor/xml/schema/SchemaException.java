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
 * Copyright 1999-2002 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: SchemaException.java 5951 2006-05-30 22:18:48Z bsnyder $
 */

package org.exolab.castor.xml.schema;


/**
 * An XML Schema Exception class
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision: 5951 $ $Date: 2006-04-14 04:14:43 -0600 (Fri, 14 Apr 2006) $
**/
public class SchemaException extends org.exolab.castor.xml.XMLException {
    /** SerialVersionUID */
    private static final long serialVersionUID = 7814714272702298809L;

    /**
     * Creates a new SchemaException with no message
     * or nested Exception.
    **/
    public SchemaException() {
        super();
    } //-- SchemaException()
    
    /**
     * Creates a new SchemaException with the given message.
     *
     * @param message the message for this Exception
    **/
    public SchemaException(String message) {
        super(message);
    } //-- SchemaException(String)

    
    /**
     * Creates a new SchemaException with the given nested
     * exception.
     *
     * @param exception the nested exception
    **/
    public SchemaException(Exception exception) {
        super(exception);
    } //-- SchemaException(Exception)

    /**
     * Creates a new SchemaException with the given message
     * and nested exception.
     *
     * @param message the detail message for this exception
     * @param exception the nested exception
    **/
    public SchemaException(String message, Exception exception) {
        super(message, exception);
    } //-- SchemaException(String, Exception)
    
} //-- SchemaException