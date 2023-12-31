package org.exolab.castor.jdo.oql;

/**
 * Exception thrown to indicate that a feature is not supported by a particular database.
 *
 * @author <a href="mailto:werner DOT guttmann AT gmx DOT net">Werner Guttmann</a>
 * @version $Revision: 7093 $ $Date: 2005-12-13 14:58:48 -0700 (Tue, 13 Dec 2005) $
 */
public class SyntaxNotSupportedException extends OQLSyntaxException {
    /** SerialVersionUID. */
    private static final long serialVersionUID = 4631661265633584506L;

    /**
     * @param message A description of the error encountered.
     */
    public SyntaxNotSupportedException(final String message) {
        super(message);
    }

    /**
     * @param message A description of the error encountered.
     * @param exception The root cause of this exception.
     */
    public SyntaxNotSupportedException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
