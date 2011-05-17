/*
 * The MIT License
 *
 * Copyright 2011 ASementsov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jsf2jpa.jsf.utils;

import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.TreeNode;

/**
 * Class implements DynamicTreeNode functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public abstract class DynamicTreeNode implements TreeNode
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    public static final String DEFAULT_TYPE = "default";

    private String                  type;
    private Object                  data;
    private List<TreeNode>          children;
    private TreeNode                parent;
    private boolean                 expanded = false;
    private boolean                 selected = false;
    private String                  displayName;

    public DynamicTreeNode(Object data, String displayName)
    {
        this(data, displayName, DEFAULT_TYPE);
    }

    /**
     * Ctor
     * @param data - data for this node
     * @param displayName - node display name
     */
    public DynamicTreeNode(Object data, String displayName, String type)
    {
        this.data = data;
        this.displayName = displayName;
        this.type = type;
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public Object getData()
    {
        return data;
    }

    @Override
    public List<TreeNode> getChildren()
    {
        if (children == null) {
            loadChildren ();
        }

        return children;
    }

    @Override
    public TreeNode getParent()
    {
        return parent;
    }

    @Override
    public void setParent(TreeNode treeNode)
    {
        parent = treeNode;
    }

    @Override
    public boolean isExpanded()
    {
        return expanded;
    }

    @Override
    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }

    @Override
    public void addChild(TreeNode treeNode)
    {
        if (children == null)
            loadChildren();

        treeNode.setParent(this);
        children.add(treeNode);
    }

    @Override
    public int getChildCount()
    {
        if (children == null)
            loadChildren();

        return children.size();
    }

    @Override
    public boolean isLeaf()
    {
        if (children == null)
            loadChildren();

        return children.isEmpty() ? true : false;
    }

    @Override
    public boolean isSelected()
    {
        return selected;
    }

    @Override
    public void setSelected(boolean value)
    {
        selected = value;
    }

    private void loadChildren()
    {
        children = onLoad(data);
        if (children == null)
            children = new ArrayList<TreeNode>();
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DynamicTreeNode other = (DynamicTreeNode) obj;
        if (this.data != other.data && (this.data == null || !this.data.equals(other.data))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 37 * hash + (this.data != null ? this.data.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return displayName;
    }

    public void refresh()
    {
        children = null;
    }

    protected abstract List<TreeNode> onLoad(Object data);
}
