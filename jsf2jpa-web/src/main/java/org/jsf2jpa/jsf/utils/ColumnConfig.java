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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * Class implements ColumnConfig functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class ColumnConfig implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    /**
     * Name of the column
     */
    private String              name;
    /**
     * Filter column name
     */
    private String              filter;
    /**
     * Filter match mode
     */
    private String              filterMatchMode;
    /**
     * Filter control style
     */
    private String              filterStyle;
    /**
     * Sort field name
     */
    private String              sort;
    /**
     * Value expression
     */
    private String              value;
    /**
     * Value type
     */
    private String              valueType;
    /**
     * Output text control style
     */
    private String              style;
    /**
     * Field text style
     */
    private String              textStyle;
    /**
     * Filter options
     */
    private String              filterOptions;
    /**
     * Javascript event which ought to apply the filter 
     */
    private String              filterEvent;
    /**
     * Function to compare objects when sort is performes
     */
    private String              sortFunction;
    /**
     * Colspan
     */
    private String              colspan;
    /**
     * Converter
     */
    private String              converter;
    /**
     * Value expression
     */
    private ValueExpression     _value;
    /**
     * Filter options expression
     */
    private ValueExpression     _filterOptions;
    /**
     * Sort function expression
     */
    private MethodExpression    _sortFunction;
    /**
     * Column properties accessors
     */
    private static final Map<String, PropertyAccessor> accessors = new HashMap<String, PropertyAccessor>();

    static {
        for (Field f : ColumnConfig.class.getDeclaredFields()) {
            if (f.getName().equals("accessors") || f.getName().equals("REV_NUMBER") || f.getName().startsWith("_"))
                continue;

            accessors.put(f.getName(), PropertyAccessor.findPropertyAccessors(f, ColumnConfig.class));
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public static Map<String, PropertyAccessor> getAccessors()
    {
        return accessors;
    }

    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public String getFilterMatchMode()
    {
        return filterMatchMode;
    }

    public void setFilterMatchMode(String filterMatchMode)
    {
        this.filterMatchMode = filterMatchMode;
    }

    public String getFilterStyle()
    {
        return filterStyle;
    }

    public void setFilterStyle(String filterStyle)
    {
        this.filterStyle = filterStyle;
    }

    public String getSort()
    {
        return sort;
    }

    public void setSort(String sort)
    {
        this.sort = sort;
    }

    public String getStyle()
    {
        return style;
    }

    public void setStyle(String style)
    {
        this.style = style;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getValueType()
    {
        return valueType;
    }

    public void setValueType(String valueType)
    {
        this.valueType = valueType;
    }

    public String getTextStyle()
    {
        return textStyle;
    }

    public void setTextStyle(String width)
    {
        this.textStyle = width;
    }

    public String getFilterEvent()
    {
        return filterEvent;
    }

    public void setFilterEvent(String filterEvent)
    {
        this.filterEvent = filterEvent;
    }

    public String getFilterOptions()
    {
        return filterOptions;
    }

    public void setFilterOptions(String filterOptions)
    {
        this.filterOptions = filterOptions;
    }

    public String getSortFunction()
    {
        return sortFunction;
    }

    public void setSortFunction(String sortFunction)
    {
        this.sortFunction = sortFunction;
    }

    public String getColspan()
    {
        return colspan;
    }

    public void setColspan(String colspan)
    {
        this.colspan = colspan;
    }

    public String getConverter()
    {
        return converter;
    }

    public void setConverter(String converter)
    {
        this.converter = converter;
    }
    
    /**
     * Function initialize all expressions
     */
    private void init()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory factory = fc.getApplication().getExpressionFactory();
        
        if (value != null && !value.isEmpty()) {
            _value = factory.createValueExpression(
                    fc.getELContext(), 
                    value, 
                    Object.class);
        }
        
        if (filterOptions != null && !filterOptions.isEmpty()) {
            _filterOptions = factory.createValueExpression(
                    fc.getELContext(), 
                    filterOptions, 
                    Object.class);
        }
        
        if (sortFunction != null && !sortFunction.isEmpty()) {
            _sortFunction = factory.createMethodExpression(
                    fc.getELContext(),
                    sortFunction, 
                    int.class,
                    new Class<?>[] {Object.class, Object.class});
        }
    }

    /**
     * Retrieves value expression for this column
     * @return value expression
     */
    public Object getValueExpression()
    {
        if (_value == null)
            init();

        if (_value == null)
            return null;

        return _value.getValue(FacesContext.getCurrentInstance().getELContext());
    }

    public Object getFilterOptionsExp()
    {
        if (_filterOptions == null)
            init();
        
        if (_filterOptions == null)
            return null;

        return _filterOptions.getValue(FacesContext.getCurrentInstance().getELContext());
    }

    public MethodExpression getSortFunctionExp()
    {
        if (_sortFunction == null)
            init();

        if (_sortFunction == null)
            return null;

        return _sortFunction;
    }
}
