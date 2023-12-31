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
 * Copyright 1999-2003 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: AppInfo.java 7652 2008-07-02 21:32:08Z wguttmn $
 */

package org.exolab.castor.xml.schema;

import java.util.LinkedList;
import java.util.List;


/**
 * A class which represents the AppInfo element
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision: 7652 $ $Date: 2005-12-13 14:58:48 -0700 (Tue, 13 Dec 2005) $ 
 * @see AnnotationItem
 */
public class AppInfo extends AnnotationItem {
    /** SerialVersionUID */
    private static final long serialVersionUID = -8678983924238645714L;

    private List _jdoContent = null;
    
    /**
     * Creates a new AppInfo
     */
    public AppInfo() {
        super();
        _jdoContent = new LinkedList();
    } //-- AppInfo
    
    //-------------------------------/
    //- Implementation of Structure -/
    //-------------------------------/
    
    /**
     * Returns the type of this Schema Structure
     *
     * @return the type of this Schema Structure
     */
    public short getStructureType() {
        return Structure.APPINFO;
    } //-- getStructureType

    /**
     * @return the jdoContent
     */
    public final List getJdoContent() {
        return _jdoContent;
    }

    /**
     * @param jdoContent the jdoContent to set
     */
    public final void setJdoContent(List jdoContent) {
        _jdoContent = jdoContent;
    }
    
} //-- class: AppInfo