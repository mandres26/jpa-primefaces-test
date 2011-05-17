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

import java.text.MessageFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.NumberConverter;
import javax.faces.model.SelectItem;
import org.jsf2jpa.entities.AbstractAttribute;
import org.jsf2jps.utils.NamingConstants;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.context.RequestContext;

/**
 * Class implements JsfUtil functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class JsfUtil
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    
    public static final String BUNDLE_VALUE = "#{bundle.%s}";
    public static final String ACTION_SUCCESS = "actionSuccess";
    public static final String CHOOSE_PARENT = "chooseParent";
    public static final String VALUE_NAME = "value";

    public static String getBundleString (String key)
    {
        try {
            return ResourceBundle.getBundle("ResourceBundle").getString(key);
        }
        catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne)
    {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }

    public static void addErrorMessage(Exception ex, String defaultMsg)
    {
        String msg = ex.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(msg);
        }
        else {
            addErrorMessage(defaultMsg);
        }
    }

    public static void addErrorMessages(List<String> messages)
    {
        for (String message : messages) {
            addErrorMessage(message);
        }
    }

    public static void addErrorMessage(String msg)
    {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg)
    {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static String getRequestParameter(String key)
    {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
    }

    public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component)
    {
        String theId = JsfUtil.getRequestParameter(requestParameterName);
        return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
    }

    public static Object getExpressionValue (String expression)
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext ctx = fc.getELContext();
        ExpressionFactory factory = fc.getApplication().getExpressionFactory();
        ValueExpression ve = factory.createValueExpression(
                ctx, 
                expression, 
                Object.class);
        if (ve != null)
            return ve.getValue(ctx);
        else
            return null;
    }

    public static ValueExpression getExpression (String expression)
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext ctx = fc.getELContext();
        ExpressionFactory factory = fc.getApplication().getExpressionFactory();
        return factory.createValueExpression(
                ctx, 
                expression, 
                Object.class);
    }

    public static MethodExpression getMethodExpression(String expression, Class<?> type, Class<?> ... types)
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext ctx = fc.getELContext();
        ExpressionFactory factory = fc.getApplication().getExpressionFactory();
        return factory.createMethodExpression(
                ctx, 
                expression, 
                type, types);
    }
    
    public static ValueExpression getExpression (String expression, Class<?> type)
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext ctx = fc.getELContext();
        ExpressionFactory factory = fc.getApplication().getExpressionFactory();
        return factory.createValueExpression(
                ctx, 
                expression, 
                type);
    }

    public static void addTableColumns (String tableName, DataTable table)
    {
        UserConfig cfg = (UserConfig)getExpressionValue ("#{userConfig}");

        TableConfig config = cfg.getTable(tableName);
        Application app = FacesContext.getCurrentInstance().getApplication();

        for (ColumnConfig col : config.getColumns()) {
            Column cl = (Column) app.createComponent(Column.COMPONENT_TYPE);

            if (col.getFilter() != null && !col.getFilter().isEmpty())
                cl.setValueExpression("filterBy", JsfUtil.getExpression(col.getFilter()));

            if (col.getSort() != null && !col.getSort().isEmpty())
                cl.setValueExpression("sortBy", JsfUtil.getExpression(col.getSort()));

            if (col.getFilterEvent() != null && !col.getFilterEvent().isEmpty())
                cl.setValueExpression("filterEvent", JsfUtil.getExpression(col.getFilterEvent()));

            if (col.getFilterOptions() != null && !col.getFilterOptions().isEmpty())
                cl.setValueExpression("filterOptions", JsfUtil.getExpression(col.getFilterOptions()));

            if (col.getSortFunction() != null && !col.getSortFunction().isEmpty())
                cl.setSortFunction(col.getSortFunctionExp());

            if (col.getColspan() != null && !col.getColspan().isEmpty())
                cl.setValueExpression("colspan", JsfUtil.getExpression(col.getColspan()));

            if (col.getFilterStyle() != null && !col.getFilterStyle().isEmpty())
                cl.setValueExpression("filterStyle", JsfUtil.getExpression(col.getFilterStyle()));

            if (col.getStyle() != null && !col.getStyle().isEmpty())
                cl.setValueExpression("style", JsfUtil.getExpression(col.getStyle()));

            if (col.getName() != null && !col.getName().isEmpty())
                cl.setValueExpression("headerText", JsfUtil.getExpression(col.getName()));

            /*
             * Output text
             */
            HtmlOutputText text = (HtmlOutputText)FacesContext.getCurrentInstance().getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
            text.setValueExpression("value", JsfUtil.getExpression (col.getValue()));
            text.setValueExpression("style", JsfUtil.getExpression (col.getTextStyle()));

            if (col.getConverter() != null && !col.getConverter().isEmpty()) {
                try {
                    text.setConverter((Converter)Class.forName(col.getConverter()).newInstance());
                }
                catch (Exception ex) {
                    Logger.getLogger(JsfUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            cl.getChildren().add(text);
            table.getChildren().add(cl);
//            table.getColumns().add(cl);
        }
    }
    
    /**
     * Function create attribute panel. There is bar code component in the header facet. 
     * Other attributes will be layed in the grid panel
     * @param attributes - attribues list
     * @param columns - number of grid columns
     * @return attributes
     */
    public static HtmlPanelGrid createAttributePanel (String varName, List<? extends AbstractAttribute> attributes, String beanName, int columns)
    {
        Application app = FacesContext.getCurrentInstance().getApplication();

        String labelVar = BUNDLE_VALUE;
        String var = "#{" + varName + "." + NamingConstants.ATTRIBUTES + "['%s'].%s}";

        columns  = columns == 0 ? 2 : columns * 2;
        
        HtmlPanelGrid panel = (HtmlPanelGrid) app.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        panel.setColumns(columns);

        /*
         * First find bar code attribute it should be added before
         */
        for (AbstractAttribute attr : attributes) {
            String valExp = null;
            UIComponent cmp = null;
            switch (attr.getDataType()) {
                case STRING:
                    valExp = String.format(var, 
                                            attr.getName(), 
                                            NamingConstants.STRING_VALUE);

                    cmp = app.createComponent(InputText.COMPONENT_TYPE);
                    ((InputText)cmp).setValueExpression(VALUE_NAME, getExpression(valExp));
                    break;

                case NUMBER:
                    valExp = String.format(var, attr.getName(), 
                                            NamingConstants.NUMBER_VALUE);

                    cmp = app.createComponent(InputText.COMPONENT_TYPE);
                    ((InputText)cmp).setValueExpression(VALUE_NAME, getExpression(valExp));
                    ((InputText)cmp).setConverter(new NumberConverter());
                    break;

                case DATE:
                    valExp = String.format(var, attr.getName(), 
                                            NamingConstants.DATE_VALUE);

                    cmp = app.createComponent(Calendar.COMPONENT_TYPE);
                    ((Calendar)cmp).setValueExpression(VALUE_NAME, getExpression(valExp));
                    break;
            }

            HtmlOutputLabel label = (HtmlOutputLabel) app.createComponent(HtmlOutputLabel.COMPONENT_TYPE);
            label.setValueExpression(VALUE_NAME, getExpression(String.format(labelVar, attr.getName())));

            panel.getChildren().add(label);
            panel.getChildren().add(cmp);
        }

        return panel;
    }

    /**
     * Function sets success flag callback parameter. This flag could be used
     * from javascript function on application client side through the <b>args</b> 
     * function argument.
     * @param successFlag - flag
     */
    public static void setSuccessFlag (boolean successFlag)
    {
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam(ACTION_SUCCESS, successFlag);
    }

    /**
     * Fucnion sets callback parameter to allow use this value from javascript 
     * on application client side through the <b>args</b> function argument.
     * @param paramName - parameter name
     * @param value - parameter value
     */
    public static void setCallbackParameter (String paramName, Object value)
    {
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam(paramName, value);
    }
}
