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
package org.jsf2jpa.jsf.mb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.DataModel;
import org.jsf2jpa.ejbs.HierarhyFacade;
import org.jsf2jpa.entities.Hierarhy;
import org.jsf2jpa.jsf.utils.BaseObjectJSF;
import org.jsf2jpa.jsf.utils.DynamicTreeNode;
import org.jsf2jps.utils.NamingConstants;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;

/**
 * Class implements HierarhyBean functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@ManagedBean(name=HierarhyBean.BEAN_NAME)
@ViewScoped
public class HierarhyBean implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    public static final String BEAN_NAME = "hier";

    @EJB
    private transient HierarhyFacade        hier;
    /**
     * Map used to store any objects from JSF page
     */
    private Map<String, Object>             selectedObject = new HashMap<String, Object>();
    private int                             pageSize = 10;
    /**
     * Hierarhy top tree node
     */
    private TreeNode                        rootNode;
    private DataModel                       childNodeModel;

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public TreeNode getRootNode()
    {
        if (rootNode == null)
            createHierarhyNodes ();
        
        return rootNode;
    }

    public Map<String, Object> getSelectedObject()
    {
        return selectedObject;
    }

    public void setSelectedObject(Map<String, Object> selectedObject)
    {
        this.selectedObject = selectedObject;
    }
    
    public void onRowSelect(SelectEvent ev)
    {
    }
    
    public void onRowUnselect(UnselectEvent ev)
    {
    }

    /**
     * Class implements custom tree node used to
     * dynamically load additional nodes fom database
     */
    class MyTreeNode extends DynamicTreeNode implements Serializable
    {
        public MyTreeNode()
        {
            super(null, null);
        }
        
        public MyTreeNode(Object data, String displayName)
        {
            super(data, displayName);
        }

        public MyTreeNode(Object data, String displayName, String type)
        {
            super(data, displayName, type);
        }

        @Override
        protected List<TreeNode> onLoad(Object data)
        {
            List<TreeNode> ret = new ArrayList<TreeNode>();
            List<Hierarhy> hiers = hier.findChildren((Hierarhy) data);
            for (Hierarhy h : hiers) {
                ret.add(new MyTreeNode(h, h.getName()));
            }

            return ret;
        }
    }
    
    /**
     * Creating root tree node
     */
    private void createHierarhyNodes()
    {
        /*
         * Init data
         */
        int count = hier.count();
        if (count == 0)
            hier.initData();

        rootNode = new MyTreeNode(null, "rootNode");
    }
    
    /**
     * Function called when node selection event was occured
     * Used to refresh dependent table models
     * @param event - event data
     */
    public void onNodeSelect(NodeSelectEvent event)
    {
        childNodeModel = null;
        selectedObject.remove("childHier");
    }
    
    public DataModel getChildrenHierlazy()
    {
        MyTreeNode selected = (MyTreeNode) selectedObject.get("hier");
        if (childNodeModel == null) {
            childNodeModel = new LazyDataModel<BaseObjectJSF<Hierarhy>>(){
                @Override
                public List<BaseObjectJSF<Hierarhy>> load(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters)
                {
                    MyTreeNode selected = (MyTreeNode) selectedObject.get("hier");
                    List<BaseObjectJSF<Hierarhy>> ret = new ArrayList<BaseObjectJSF<Hierarhy>>();
                    if (selected == null) 
                        return ret;
                    
                    Map<String, Object> parentFilter = new HashMap<String, Object>();
                    parentFilter.put(NamingConstants.PARENT, selected.getData());

                    if (!filters.isEmpty())
                        parentFilter.putAll(filters);

                    setRowCount(hier.countFiltered(parentFilter));
                    if (getRowCount() == 0)
                        return Collections.emptyList();
                    
                    List<Hierarhy> nodeList = hier.findFilteredRange(first, pageSize, sortField, sortOrder, parentFilter);
                    for (Hierarhy node : nodeList) {
                        ret.add(new BaseObjectJSF<Hierarhy>(Hierarhy.class, node));
                    }

                    return ret;
                }
            };
   
            if (selected != null) {
                Map<String, Object> parentFilter = new HashMap<String, Object>();
                parentFilter.put(NamingConstants.PARENT, selected.getData());
                ((LazyDataModel) childNodeModel).setRowCount(hier.countFiltered(parentFilter));
                ((LazyDataModel) childNodeModel).setPageSize(pageSize);
            }
            else {
                ((LazyDataModel) childNodeModel).setRowCount(0);
                ((LazyDataModel) childNodeModel).setPageSize(pageSize);
            }
        }

        return childNodeModel;
    }
}
