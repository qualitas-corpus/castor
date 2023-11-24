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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A Node in the Parse tree which is generated by the {@link Parser} as the tree
 * representation of the OQL Query. Each node has a link back to the parent node
 * (null for the root node), and a vector of children. Each node contains the
 * {@link Token}which represents that part of the tree.
 * 
 * @author <a href="nissim@nksystems.com">Nissim Karpenstein </a>
 * @version $Revision: 8102 $ $Date: 2006-04-25 15:08:23 -0600 (Tue, 25 Apr 2006) $
 */
public final class ParseTreeNode {
    private static final class NullIterator implements Iterator<ParseTreeNode> {
        public boolean hasNext() { return false; }

        public ParseTreeNode next() { return null; }

        public void remove() { }
    }

    private static final NullIterator NULL_ITERATOR = new NullIterator();

    private ParseTreeNode _parent;

    private ArrayList<ParseTreeNode> _children;

    private Token _token;

    /**
     * Creates a new Node with supplied parent and token.
     * 
     * @param parent The parent of this node (null for root)
     * @param token The token data in this node
     */
    public ParseTreeNode(final ParseTreeNode parent, final Token token) {
        _parent = parent;
        _token = token;
    }

    /**
     * Creates a new root Node with supplied token.
     * 
     * @param token The token data in this node
     */
    public ParseTreeNode(final Token token) {
        _parent = null;
        _token = token;
    }

    public String toString() {
        return "ParseTreeNode{" + _token.getTokenValue() + "}";
    }

    public String toStringEx() {
        StringBuffer s = new StringBuffer("ParseTreeNode{");
        s.append(_token.getTokenValue());
        Iterator<ParseTreeNode> iter = children();
        while (iter.hasNext()) {
            s.append(',');
            s.append(iter.next().toStringEx());
        }
        s.append('}');
        return s.toString();
    }

    /**
     * Changes the parent of this node.
     * 
     * @param parent The new parent.
     */
    public void setParent(final ParseTreeNode parent) {
        _parent = parent;
    }

    /**
     * Adds a new node as a child of this node. Changes the nodes parent to
     * this.
     * 
     * @param child The new child
     */
    public void addChild(final ParseTreeNode child) {
        child.setParent(this);
        if (_children == null) { _children = new ArrayList<ParseTreeNode>(); }
        _children.add(child);
    }

    /**
     * Specifies whether this node is the root of a tree.
     * 
     * @return True if the node does not have a parent, otherwise false.
     */
    public boolean isRoot() {
        return (_parent == null);
    }

    /**
     * Specifies whether this node is a leaf.
     * 
     * @return True if the node does not have any children, otherwise false.
     */
    public boolean isLeaf() {
        return (_children == null) || (_children.size() == 0);
    }

    /**
     * Accessor method for the parent of this node.
     * 
     * @return The parent of this node.
     */
    public ParseTreeNode getParent() {
        return _parent;
    }

    /**
     * Accessor method for an iteration of this nodes children.
     * 
     * @return An Iterator of children.
     */
    public Iterator<ParseTreeNode> children() {
        if ((_children == null) || (_children.size() == 0)) {
            return NULL_ITERATOR;
        }
        return _children.iterator();
    }

    /**
     * Accessor method for individual children of this node.
     * 
     * @param index the index of the child to retrieve.
     * @return the index child of this node.
     */
    public ParseTreeNode getChild(final int index) {
        return _children.get(index);
    }

    /**
     * Accessor method for the number of children of this node.
     * 
     * @return the number of children of this node.
     */
    public int getChildCount() {
        if (_children == null) {
            return 0;
        }
        return _children.size();
    }

    /**
     * Accessor method for the token.
     * 
     * @return The token which is the datum of this node.
     */
    public Token getToken() {
        return _token;
    }
}
