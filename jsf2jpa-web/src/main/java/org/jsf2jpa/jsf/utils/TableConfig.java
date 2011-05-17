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
import java.util.ArrayList;
import java.util.List;

/**
 * Class implements TableConfig functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class TableConfig implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    private String                  tableName;
    private String                  var;
    private List<ColumnConfig>      columns = new ArrayList<ColumnConfig>();

    public List<ColumnConfig> getColumns()
    {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns)
    {
        this.columns = columns;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getVar()
    {
        return var;
    }

    public void setVar(String var)
    {
        this.var = var;
    }
}
