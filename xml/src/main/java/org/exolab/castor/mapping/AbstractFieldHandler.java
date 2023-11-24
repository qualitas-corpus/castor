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
 * $Id: AbstractFieldHandler.java 8145 2009-03-29 22:07:00Z rjoachim $
 */
 
package org.exolab.castor.mapping;

import java.util.Properties;

/**
 * An extended version of the FieldHandler interface which is
 * used for adding additional functionality while preserving
 * backward compatability.
 *
 * @author <a href="kvisco@intalio.com">Keith Visco</a>
 * @version $Revision: 8145 $ $Date: 2005-08-03 15:11:51 -0600 (Wed, 03 Aug 2005) $
 * @see FieldDescriptor
 * @see FieldHandler
 */
public abstract class AbstractFieldHandler
extends ExtendedFieldHandler
implements ConfigurableFieldHandler {
    /** The FieldDescriptor for the field that this handler is responsible for. */
    private FieldDescriptor _descriptor = null;
    
    /** Configuration that can be used by subclasses when needed. */
    protected Properties _properties;
    
    /** 
     * Creates a new default AbstractFieldHandler. This method
     * should be called by all extending classes so that any
     * important initialization code will be executed.
     */
    protected AbstractFieldHandler() {
        super();
        //-- currently nothing to do, but initialization
        //-- code may be needed in the future
    }
    
    /**
     * Returns the FieldDescriptor for the field that this 
     * handler is reponsibile for, or null if no FieldDescriptor
     * has been set. This method is useful for implementations
     * of the FieldHandler interface that wish to obtain information
     * about the field in order to make the FieldHandler more generic
     * and reusable, or simply for validation purposes.
     *
     * @return the FieldDescriptor, or null if none exists.
     */
    protected final FieldDescriptor getFieldDescriptor() {
        return _descriptor;
    }
    
    /**
     * Sets the FieldDescriptor that this FieldHander is
     * responsibile for. By setting the FieldDescriptor, it
     * allows the implementation of the FieldHandler methods 
     * to obtain information about the field itself. This allows
     * a particular implementation to become more generic and
     * reusable.
     *
     * @param fieldDesc the FieldDescriptor to set
     */
    public void setFieldDescriptor(FieldDescriptor fieldDesc) {
        _descriptor = fieldDesc;
    }

    /**
     * Returns true if the "handled" field has a value within the 
     * given object.
     * <p>
     * By default this just checks for null. Normally this method
     * is needed for checking if a value has been set via a call
     * to the setValue method, or if the primitive value has
     * been initialized by the JVM. 
     * </p>
     * <p>
     * This method should be overloaded for improved value
     * checking.
     * </p>
     *
     * @return true if the given object has a value for the handled field
     */
    public boolean hasValue(final Object object) {
        return (getValue(object) != null);
    }
    
    //---------------------------------------/
    //- Methods inherited from FieldHandler -/
    //---------------------------------------/
    
    /**
     * Returns the value of the field from the object.
     *
     * @param object The object
     * @return The value of the field
     * @throws IllegalStateException The Java object has changed and
     *  is no longer supported by this handler, or the handler is not
     *  compatiable with the Java object
     */
    public abstract Object getValue(Object object)
    throws IllegalStateException;
    

    /**
     * Creates a new instance of the object described by this field.
     *
     * @param parent The object for which the field is created
     * @return A new instance of the field's value
     * @throws IllegalStateException This field is a simple type and
     *  cannot be instantiated
     */
    public abstract Object newInstance(Object parent)
    throws IllegalStateException;
        
    /**
     * Creates a new instance of the object described by this field.
     *
     * @param parent The object for which the field is created
     * @param args the set of constructor arguments
     * @return A new instance of the field's value
     * @throws IllegalStateException This field is a simple type and
     *  cannot be instantiated
     */
    public abstract Object newInstance(Object parent, Object[] args)
    throws IllegalStateException;
        
    /**
     * Sets the value of the field to a default value.
     * <p>
     * Reference fields are set to null, primitive fields are set to
     * their default value, collection fields are emptied of all
     * elements.
     *
     * @param object The object
     * @throws IllegalStateException The Java object has changed and
     *  is no longer supported by this handler, or the handler is not
     *  compatiable with the Java object
     */
    public abstract void resetValue(Object object)
    throws IllegalStateException, IllegalArgumentException;
        
    /**
     * Sets the value of the field on the object.
     *
     * @param object The object.
     * @param value The new value.
     * @throws IllegalStateException The Java object has changed and is no longer
     *         supported by this handler, or the handler is not compatiable with the
     *         Java object.
     * @throws IllegalArgumentException The value passed is not of a supported type.
     */
    public abstract void setValue(Object object, Object value)
    throws IllegalStateException, IllegalArgumentException;
    
    /**
     * Empty implementation of the {@link ConfigurableFieldHandler} interface, for convenience
     * purpose. Subclasses that want to use any configuration should override this method.
     */
    public void setConfiguration(final Properties config) throws ValidityException { }
}
