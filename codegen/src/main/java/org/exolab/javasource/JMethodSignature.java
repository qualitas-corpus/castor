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
 */
package org.exolab.javasource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A class which holds information about the signature of a JMethod. The code in
 * this package was modeled after the Java Reflection API as much as possible
 * to reduce the learning curve.
 *
 * @author <a href="mailto:keith At kvisco DOT com">Keith Visco</a>
 * @version $Revision: 8382 $ $Date: 2004-12-03 11:57:33 -0700 (Fri, 03 Dec 2004) $
 */
public final class JMethodSignature extends JAnnotatedElementHelper {

    /** The set of modifiers for this JMethodSignature. */
    private JModifiers _modifiers = null;
    
    /** The return type of this method. */
    private JType _returnType = null;
    
    /** The name of this JMethodSignature. */
    private String _name = null;
    
    /** The list of parameters of this method in order declared. */
    private final Map<String, JParameter> _params = new LinkedHashMap<String, JParameter>();
    
    /** The JavaDoc comment for this method's signature. */
    private final JDocComment _jdc;
    
    /** The exceptions that this method throws. */
    private final Vector<JClass> _exceptions;

    /**
     * Creates a new method with the given name and "void" return type.
     *
     * @param name The method name. Must not be null.
     */
    public JMethodSignature(final String name) {
        if ((name == null) || (name.length() == 0)) {
            String err = "The method name must not be null or zero-length";
            throw new IllegalArgumentException(err);
        }

        _jdc = new JDocComment();
        _returnType = null;
        _name = name;
        _modifiers = new JModifiers();
        _exceptions = new Vector<JClass>(1);
    }

    /**
     * Creates a new method with the given name and return type.
     *
     * @param name The method name. Must not be null.
     * @param returnType The return type of the method. Must not be null.
     */
    public JMethodSignature(final String name, final JType returnType) {
        this(name);
        
        if (returnType == null) {
            String err = "The return type must not be null or zero-length";
            throw new IllegalArgumentException(err);
        }
        _returnType = returnType;
    }

    /**
     * Adds the given Exception to this JMethodSignature's throws clause.
     *
     * @param exp The JClass representing the Exception.
     */
    public void addException(final JClass exp) {
        if (exp == null) { return; }

        //-- make sure exception is not already added
        String expClassName = exp.getName();
        for (int i = 0; i < _exceptions.size(); i++) {
            JClass jClass = _exceptions.elementAt(i);
            if (expClassName.equals(jClass.getName())) { return; }
        }
        //-- add exception
        _exceptions.addElement(exp);
    }

    /**
     * Adds the given parameter to this JMethodSignature's list of parameters.
     *
     * @param parameter The parameter to add to the this JMethodSignature's list
     *        of parameters.
     */
    public void addParameter(final JParameter parameter) {
        if (parameter == null) { 
            return; 
        }

        String pName = parameter.getName();
        //-- check current params
        if (_params.get(pName) != null) {
            StringBuilder err = new StringBuilder(32);
            err.append("A parameter already exists for this method, ");
            err.append(_name);
            err.append(", with the name: ");
            err.append(pName);
            throw new IllegalArgumentException(err.toString());
        }

        _params.put(pName, parameter);

        //-- create comment
        _jdc.addDescriptor(JDocDescriptor.createParamDesc(pName, null));
    }

    /**
     * Returns the exceptions that this JMethodSignature lists in its throws
     * clause.
     *
     * @return The exceptions that this JMethodSignature lists in its throws
     *         clause.
     */
    public JClass[] getExceptions() {
        JClass[] jclasses = new JClass[_exceptions.size()];
        _exceptions.copyInto(jclasses);
        return jclasses;
    }

    /**
     * Returns the JavaDoc comment describing this JMethodSignature.
     *
     * @return The JavaDoc comment describing this JMethodSignature.
     */
    public JDocComment getJDocComment() {
        return _jdc;
    }

    /**
     * Returns the modifiers for this JMethodSignature.
     *
     * @return The modifiers for this JMethodSignature.
     */
    public JModifiers getModifiers() {
        return _modifiers;
    }

    /**
     * Returns the name of the method.
     *
     * @return The name of the method.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the JParameter at the given index.
     *
     * @param index The index of the JParameter to return.
     * @return The JParameter at the given index.
     */
    public JParameter getParameter(final int index) {
        return _params.get(index);
    }

    /**
     * Returns the set of JParameters in this JMethodSignature.
     * <br/>
     * <B>Note:</B> the array is a copy, the parameters in the array are the
     * actual references.
     *
     * @return The set of JParameters in this JMethodSignature.
     */
    public synchronized JParameter[] getParameters() {
        return _params.values().toArray(new JParameter[_params.size()]);
    }

    /**
     * Returns the JType that represents the return type for the method
     * signature.
     *
     * @return The JType that represents the return type for the method
     *         signature.
     */
    public JType getReturnType() {
        return _returnType;
    }

    /**
     * Sets the name of the method.
     *
     * @param name The name of the method.
     */
    public void setName(final String name) {
        _name = name;
    }

    /**
     * Sets the JavaDoc comment describing this JMethodSignature.
     *
     * @param comment The JavaDoc comment for this member.
     */
    public void setComment(final String comment) {
        _jdc.setComment(comment);
    }

    /**
     * Sets the JModifiers for this method signature.
     *
     * @param modifiers The JModifiers for this method signature.
     */
    public void setModifiers(final JModifiers modifiers) {
        _modifiers = modifiers.copy();
        _modifiers.setFinal(false);
    }

    /**
     * Returns an array containing the names of the classes of the parameters in
     * this JMethodSignature. For Arrays, the class name of the object type
     * stored in the Array is what is returned. Parameters that are primitive
     * types (and Arrays of primitive types) are not represented in the array of
     * names returned.
     *
     * @return An array containing the names of the classes of the parameters in
     *         this JMethodSignature.
     */
    protected String[] getParameterClassNames() {
        List<String> names = new ArrayList<String>(_params.size());

        for (JParameter parameter : _params.values()) {
            JType jType  = parameter.getType();
            while (jType.isArray()) {
                jType = ((JArrayType) jType).getComponentType();
            }
            if (!jType.isPrimitive()) {
                JClass jclass = (JClass) jType;
                names.add(jclass.getName());
            }
        }

        return names.toArray(new String[names.size()]);
    }

    /**
     * Prints the method signature. A semi-colon (end-of-statement terminator ';')
     * will <em>not</em> be printed.
     *
     * @param jsw The JSourceWriter to print to.
     */
    public void print(final JSourceWriter jsw) {
        print(jsw, true);
    }

    /**
     * Prints the method signature. A semi-colon (end-of-statement terminator ';')
     * will <em>not</em> be printed.
     *
     * @param jsw The JSourceWriter to print to.
     * @param printJavaDoc If true, print the JDocComment associated with this
     *        method signature before we print the method signature.
     */
    public void print(final JSourceWriter jsw, final boolean printJavaDoc) {
        //------------/
        //- Java Doc -/
        //------------/

        if (printJavaDoc) { _jdc.print(jsw); }

        //---------------/
        //- Annotations -/
        //---------------/

        printAnnotations(jsw);

        //-----------------/
        //- Method Source -/
        //-----------------/

        jsw.write(_modifiers.toString());
        if (_modifiers.toString().length() > 0) {
            jsw.write(' ');
        }
        if (_returnType != null) {
            jsw.write(_returnType);
        } else {
            jsw.write("void");
        }
        jsw.write(' ');
        jsw.write(_name);
        jsw.writeln('(');

        //-- print parameters
        jsw.indent();
        jsw.indent();
        int parameterCount = 0;
        for (JParameter jParameter : _params.values()) {
            if (parameterCount > 0) { 
                jsw.writeln(",");
            }
            jParameter.printAnnotations(jsw);
            String typeAndName = jParameter.toString();
            jsw.write(typeAndName);
            parameterCount++;
        }
        jsw.unindent();
        jsw.unindent();

        jsw.write(")");

        if (_exceptions.size() > 0) {
            jsw.writeln();
            jsw.write("throws ");
            for (int i = 0; i < _exceptions.size(); i++) {
                if (i > 0) { jsw.write(", "); }
                JClass jClass = _exceptions.elementAt(i);
                jsw.write(jClass.getName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        if (_returnType != null) {
            sb.append(_returnType);
        } else {
            sb.append("void");
        }
        sb.append(' ');
        sb.append(_name);
        sb.append('(');

        //-- print parameters
        int i = 0;
        for (JParameter jParam : _params.values()) {
            if (i > 0) { 
                sb.append(", "); 
            }
            sb.append(jParam.getType().getName());
        }

        sb.append(") ");

        return sb.toString();
    }

}
