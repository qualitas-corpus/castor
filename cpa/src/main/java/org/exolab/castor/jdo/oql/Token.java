/*
 * Copyright 2005 Nissim Karpenstein, Stein M. Hugubakken
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
package org.exolab.castor.jdo.oql;

/**
 * Represents tokens which are generated by the {@link Lexer}from the String
 * OQL representation. Tokens contain a token type and the string value.
 * 
 * @author <a href="nissim@nksystems.com">Nissim Karpenstein</a>
 * @version $Revision: 5951 $ $Date: 2006-01-03 17:47:48 -0700 (Tue, 03 Jan 2006) $
 */
public final class Token {
    public static final Token ARROW = new Token(TokenType.ARROW, "->");
    public static final Token COLON = new Token(TokenType.COLON, ":");
    public static final Token COMMA = new Token(TokenType.COMMA, ",");
    public static final Token CONCAT = new Token(TokenType.CONCAT, "||");
    public static final Token DIVIDE = new Token(TokenType.DIVIDE, "/");
    public static final Token DOLLAR = new Token(TokenType.DOLLAR, "$");
    public static final Token DOT = new Token(TokenType.DOT, ".");
    public static final Token END_OF_QUERY = new Token(TokenType.END_OF_QUERY, "");
    public static final Token EQUAL = new Token(TokenType.EQUAL, "=");
    public static final Token GT = new Token(TokenType.GT, ">");
    public static final Token GTE = new Token(TokenType.GTE, ">=");
    public static final Token LPAREN = new Token(TokenType.LPAREN, "(");
    public static final Token LT = new Token(TokenType.LT, "<");
    public static final Token LTE = new Token(TokenType.LTE, "<=");
    public static final Token MINUS = new Token(TokenType.MINUS, "-");
    public static final Token NOT_EQUAL = new Token(TokenType.NOT_EQUAL, "!=");
    public static final Token PLUS = new Token(TokenType.PLUS, "+");
    public static final Token RPAREN = new Token(TokenType.RPAREN, ")");
    public static final Token TIMES = new Token(TokenType.TIMES, "*");
    public static final Token KEYWORD_AS = new Token(TokenType.KEYWORD_AS, "as");

    private int _tokenType;
    private String _tokenValue;

    /**
     * Creates a new Token with the supplied type and value.
     *
     * @param tokenType The Token Type for this token
     * @param tokenValue The Token Value for this token
     */
    public Token(final int tokenType, final String tokenValue) {
        _tokenType = tokenType;
        _tokenValue = tokenValue;
    }

    public String toString() {
        return _tokenValue; 
    }

    /**
     * Token Type accessor method.
     *
     * @return The Token Type for this token
     */
    public int getTokenType() {
        return _tokenType;
    }

    /**
     * Token value accessor method.
     *
     * @return The Token Value for this token
     */
    public String getTokenValue() {
        return _tokenValue;
    }
}
