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
package org.primefaces.component.datatable;

import java.util.Comparator;
import java.util.logging.Logger;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import org.jsf2jpa.jsf.utils.JsfUtil;
import org.primefaces.component.column.Column;

/**
 * Class implements ColumnComparator functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class ColumnComparator implements Comparator
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    /**
     * Comparing column
     */
    private Column column;
    /**
     * Flas ascending
     */
    private boolean asc;
    /**
     * Table variabble
     */
    private String var;
    /**
     * Sort column value expression
     */
    private ValueExpression sortByExpression;
    /**
     * Sort method expression
     */
    private MethodExpression sortFunction;
    /**
     * Logger
     */
    private final static Logger logger = Logger.getLogger(ColumnComparator.class.getName());

    public ColumnComparator(Column column, String var, String sort, boolean asc)
    {
        this.column = column;
        this.var = var;
        this.asc = asc;
        this.sortByExpression = JsfUtil.getExpression(sort);
        this.sortFunction = column.getSortFunction();
    }

    @Override
    public int compare(Object obj1, Object obj2)
    {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            facesContext.getExternalContext().getRequestMap().put(var, obj1);
            Object value1 = sortByExpression.getValue(facesContext.getELContext());
            facesContext.getExternalContext().getRequestMap().put(var, obj2);
            Object value2 = sortByExpression.getValue(facesContext.getELContext());

            if (value1 == null) {
                return 1;
            }
            else if (value2 == null) {
                return -1;
            }

            int result;
            if (sortFunction == null) {
                result = ((Comparable) value1).compareTo(value2);
            }
            else {
                result = (Integer) sortFunction.invoke(facesContext.getELContext(), new Object[]{value1, value2});
            }

            return asc ? result : -1 * result;
        }
        catch (Exception e) {
            logger.severe("Error in sorting");
            throw new RuntimeException(e);
        }
    }
}
