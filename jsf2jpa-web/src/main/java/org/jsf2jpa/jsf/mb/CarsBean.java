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
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import org.jsf2jpa.ejbs.CarsFacade;
import org.jsf2jpa.entities.Car;
import org.jsf2jpa.jsf.utils.BaseObjectJSF;
import org.jsf2jpa.jsf.utils.JsfUtil;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;

/**
 * Class implements cars managed bean
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@ManagedBean(name=CarsBean.BEAN_NAME)
@ViewScoped
public class CarsBean implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    public static final String BEAN_NAME = "cars";
    
    private static final String PAGINATOR_TEMPLATE = "#{FirstPageLink} {PreviousPageLink} {CurrentPageReport} {NextPageLink} {LastPageLink}";
    private static final String TABLE_VAR = "row";
    private static final String SELECTED_OBJECT = "#{%s.selectedObject['%s']}";
    private static final String PAGE_ZIZE = "#{%s.pageSize}";
    private static final String ON_ROW_SELECT = "#{%s.onRowSelect}";
    private static final String ON_ROW_UNSELECT = "#{%s.onRowUnselect}";
    
    private static final String LAZY_TABLE_MODEL = "carsLazyModel";
    private static final String TABLE_MODEL = "carsModel";
    private static final String CAR_SEL_OBJECT = "car";
    
    /**
     * Map used to store any objects from JSF page
     */
    private Map<String, Object>     selectedObject = new HashMap<String, Object>();
    /**
     * Cars EJB object
     */
    @EJB
    private transient CarsFacade        cars;

    private Panel                       basePanel;
    private Panel                       attrPanel;
    private Panel                       carsLazyPanel;
    private Panel                       carsPanel;

    private int                         pageSize = 10;
    private DataModel                   carsLazyModel;
    private List<BaseObjectJSF<Car>>    carsModel;

    public Panel getAttrPanel()
    {
        return attrPanel;
    }

    public void setAttrPanel(Panel attrPanel)
    {
        this.attrPanel = attrPanel;
    }

    public Panel getBasePanel()
    {
        return basePanel;
    }

    public void setBasePanel(Panel basePanel)
    {
        this.basePanel = basePanel;
    }

    public Map<String, Object> getSelectedObject()
    {
        return selectedObject;
    }

    public void setSelectedObject(Map<String, Object> selectedObject)
    {
        this.selectedObject = selectedObject;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public Panel getCarsPanel()
    {
        return carsPanel;
    }

    public void setCarsPanel(Panel carsPanel)
    {
        this.carsPanel = carsPanel;
    }

    public Panel getCarsLazyPanel()
    {
        return carsLazyPanel;
    }

    public void setCarsLazyPanel(Panel carsLazyPanel)
    {
        this.carsLazyPanel = carsLazyPanel;
    }
    
    public void onRowSelect(SelectEvent ev)
    {
    }
    
    public void onRowUnselect(UnselectEvent ev)
    {
    }
    
    private void showError (Exception ex)
    {
        String error = ex.getMessage();
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause.getMessage() != null)
                error = cause.getMessage();

            cause = cause.getCause();
        }

        String err_msg = error.substring(0, error.length()-1);
        JsfUtil.addErrorMessage(err_msg);
    }

    private void addLazyTablePanel (Application app)
    {
        DataTable table = (DataTable) app.createComponent(DataTable.COMPONENT_TYPE);
        String tableId = table.getClientId();

        /*
         * Set variable name for generated table it constant
         */
        table.setVar(TABLE_VAR);
        table.setLazy(true);
        table.setPaginator(true);
        table.setPaginatorAlwaysVisible(false);
        table.setStyle("width: 100%");

        table.setSelectionMode("single");
        table.setValueExpression("selection", 
                JsfUtil.getExpression(String.format (SELECTED_OBJECT, BEAN_NAME, CAR_SEL_OBJECT)));

        table.setValueExpression("paginatorTemplate", JsfUtil.getExpression(PAGINATOR_TEMPLATE));

        table.setValueExpression("rows", JsfUtil.getExpression(String.format(PAGE_ZIZE, BEAN_NAME)));
        table.setValueExpression(JsfUtil.VALUE_NAME, JsfUtil.getExpression("#{" + BEAN_NAME + "." + LAZY_TABLE_MODEL + "}"));

        /*
         * Row select and unselect listeners is needed for properly work of selection events.
         * By default these are empty functions
         */
        table.setRowSelectListener(
                JsfUtil.getMethodExpression (
                    String.format (ON_ROW_SELECT, BEAN_NAME),
                    null,
                    SelectEvent.class));
        /*
         * Unselect listener
         */
        table.setRowUnselectListener(JsfUtil.getMethodExpression (
                    String.format (ON_ROW_UNSELECT, BEAN_NAME),
                    null,
                    UnselectEvent.class));

        JsfUtil.addTableColumns("cars", table);
        
        /*
         * Add table to cars panel
         */
        carsLazyPanel.getChildren().add(table);
    }
    
    private void addTablePanel (Application app)
    {
        DataTable table = (DataTable) app.createComponent(DataTable.COMPONENT_TYPE);
        String tableId = table.getClientId();

        /*
         * Set variable name for generated table it constant
         */
        table.setVar(TABLE_VAR);
        table.setLazy(false);
        table.setPaginator(true);
        table.setPaginatorAlwaysVisible(true);
        table.setStyle("width: 100%");

        table.setSelectionMode("single");
        table.setValueExpression("selection", 
                JsfUtil.getExpression(String.format (SELECTED_OBJECT, BEAN_NAME, CAR_SEL_OBJECT)));

        table.setValueExpression("paginatorTemplate", JsfUtil.getExpression(PAGINATOR_TEMPLATE));

        table.setValueExpression("rows", JsfUtil.getExpression(String.format(PAGE_ZIZE, BEAN_NAME)));
        table.setValueExpression(JsfUtil.VALUE_NAME, JsfUtil.getExpression("#{" + BEAN_NAME + "." + TABLE_MODEL + "}"));

        /*
         * Row select and unselect listeners is needed for properly work of selection events.
         * By default these are empty functions
         */
        table.setRowSelectListener(
                JsfUtil.getMethodExpression (
                    String.format (ON_ROW_SELECT, BEAN_NAME),
                    null,
                    SelectEvent.class));
        /*
         * Unselect listener
         */
        table.setRowUnselectListener(JsfUtil.getMethodExpression (
                    String.format (ON_ROW_UNSELECT, BEAN_NAME),
                    null,
                    UnselectEvent.class));

        JsfUtil.addTableColumns("cars", table);
        
        /*
         * Add table to cars panel
         */
        carsPanel.getChildren().add(table);
    }
    
    /**
     * Function create panel
     */
    public void buildCarsPanel()
    {
        int count = cars.count();
        if (count == 0) {
            try {
                cars.initData();
            }
            catch (Exception e) {
                showError(e);
            }
        }
        
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();
        
        /*
         * Create cars table
         */
        addLazyTablePanel (app);
        addTablePanel(app);
    }
    
    public DataModel getCarsLazyModel ()
    {
        if (carsLazyModel == null) {
            carsLazyModel = new LazyDataModel<BaseObjectJSF<Car>>() {
                @Override
                public List<BaseObjectJSF<Car>> load(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters)
                {
                    List<BaseObjectJSF<Car>> ret = new ArrayList<BaseObjectJSF<Car>>();
                    Map<String, Object> parentFilter = new HashMap<String, Object>();
                    if (!filters.isEmpty())
                        parentFilter.putAll(filters);

                    setRowCount(cars.countFiltered(parentFilter));
                    if (getRowCount() == 0)
                        return Collections.emptyList();
                    
                    List<Car> carList = cars.findFilteredRange(first, pageSize, sortField, sortOrder, parentFilter);
                    for (Car car : carList) {
                        ret.add(new BaseObjectJSF<Car>(Car.class, car));
                    }

                    return ret;
                }
            };

            ((LazyDataModel) carsLazyModel).setRowCount(cars.count());
            ((LazyDataModel) carsLazyModel).setPageSize(pageSize);
        }
        
        return carsLazyModel;
    }

    public List<BaseObjectJSF<Car>> getCarsModel()
    {
        if (carsModel == null) {
            carsModel = new ArrayList<BaseObjectJSF<Car>>();
            List<Car> carList = cars.findAll();
            for (Car car : carList) {
                carsModel.add(new BaseObjectJSF<Car>(Car.class, car));
            }
        }
        
        return carsModel;
    }
}
