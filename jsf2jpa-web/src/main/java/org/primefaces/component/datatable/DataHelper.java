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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.jsf2jpa.jsf.utils.JsfUtil;
import org.primefaces.component.column.Column;
import org.primefaces.component.columngroup.ColumnGroup;
import org.primefaces.component.row.Row;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.Cell;

/**
 * Class implements DataHelper functions
 *
 * $LastChangedRevision:$
 * $LastChangedDate:$
 *
 * @author ASementsov
 */
public class DataHelper
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";

    void decodePageRequest(FacesContext context, DataTable table)
    {
        String clientId = table.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        String firstParam = params.get(clientId + "_first");
        String rowsParam = params.get(clientId + "_rows");
        String pageParam = params.get(clientId + "_page");

        table.setFirst(Integer.valueOf(firstParam));
        table.setRows(Integer.valueOf(rowsParam));
        table.setPage(Integer.valueOf(pageParam));
    }

    void decodeSortRequest(FacesContext context, DataTable table)
    {
        String clientId = table.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        String sortKey = params.get(clientId + "_sortKey");
        boolean asc = Boolean.valueOf(params.get(clientId + "_sortDir"));
        Column sortColumn = null;

        ColumnGroup group = table.getColumnGroup("header");
        if (group != null) {
            outer:
            for (UIComponent child : group.getChildren()) {
                Row headerRow = (Row) child;
                for (UIComponent headerRowChild : headerRow.getChildren()) {
                    Column column = (Column) headerRowChild;
                    if (column.getClientId(context).equals(sortKey)) {
                        sortColumn = column;
                        break outer;
                    }
                }
            }
        }
        else {
            //single header row
            for (Column column : table.getColumns()) {
                if (column.getClientId(context).equals(sortKey)) {
                    sortColumn = column;
                    break;
                }
            }
        }

        //Reset state
        table.setFirst(0);
        table.setPage(1);

        if (table.isLazy()) {
            table.setSortField(resolveField(sortColumn.getValueExpression("sortBy")));
            table.setSortOrder(asc);
        }
        else {
            List list = (List) table.getValue();
            String sortExpression = "#{" + sortColumn.getValueExpression("sortBy").getValue(FacesContext.getCurrentInstance().getELContext()) + "}";
            Collections.sort(list, new ColumnComparator(sortColumn, table.getVar(), sortExpression, asc));
        }
    }

    void decodeFilters(FacesContext context, DataTable table)
    {
        String clientId = table.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        if (table.isFilterRequest(context)) {
            //Reset state
            table.setFirst(0);
            table.setPage(1);
        }

        if (table.isLazy()) {
            Map<String, String> filters = new HashMap<String, String>();
            Map<String, Column> filterMap = table.getFilterMap();

            for (String filterName : filterMap.keySet()) {
                Column column = filterMap.get(filterName);
                String filterValue = params.get(filterName);

                if (!isValueBlank(filterValue)) {
                    String filterField = resolveField(column.getValueExpression("filterBy"));
                    filters.put(filterField, filterValue);
                }
            }

            table.setFilters(filters);

            //Metadata for callback
            if (table.isPaginator()) {
                RequestContext.getCurrentInstance().addCallbackParam("totalRecords", table.getRowCount());
            }
        }
        else {
            Map<String, Column> filterMap = table.getFilterMap();
            List filteredData = new ArrayList();

            String globalFilter = params.get(clientId + UINamingContainer.getSeparatorChar(context) + "globalFilter");
            boolean hasGlobalFilter = !isValueBlank(globalFilter);
            if (hasGlobalFilter) {
                globalFilter = globalFilter.toLowerCase();
            }

            for (int i = 0; i < table.getRowCount(); i++) {
                table.setRowIndex(i);
                boolean localMatch = true;
                boolean globalMatch = false;

                for (String filterName : filterMap.keySet()) {
                    Column column = filterMap.get(filterName);
                    String columnFilter = params.get(filterName);
                    if (columnFilter != null)
                        columnFilter = columnFilter.toLowerCase();
                    
                    /*
                     * TODO replace with getValueExpression
                     */
                    String columnValue = String.valueOf(column.getValueExpression("filterBy").getValue(context.getELContext()));
                    columnValue = String.valueOf (JsfUtil.getExpressionValue("#{" + columnValue + "}"));
                    if (columnValue != null)
                        columnValue = columnValue.toLowerCase();

                    if (hasGlobalFilter && !globalMatch) {
                        if (columnValue != null && columnValue.toLowerCase().contains(globalFilter)) {
                            globalMatch = true;
                        }
                    }

                    if (isValueBlank(columnFilter)) {
                        localMatch = true;
                    }
                    else if (columnValue == null || !column.getFilterConstraint().applies(columnValue.toLowerCase(), columnFilter)) {
                        localMatch = false;
                        break;
                    }
                }

                boolean matches = localMatch;
                if (hasGlobalFilter) {
                    matches = localMatch && globalMatch;
                }

                if (matches) {
                    filteredData.add(table.getRowData());
                }
            }

            boolean isAllFiltered = filteredData.size() == table.getRowCount();

            //Metadata for callback
            if (table.isPaginator()) {
                int totalRecords = isAllFiltered ? table.getRowCount() : filteredData.size();
                RequestContext.getCurrentInstance().addCallbackParam("totalRecords", totalRecords);
            }

            //No need to define filtered data if it is same as actual data
            if (!isAllFiltered) {
                table.setFilteredData(filteredData);
            }

            table.setRowIndex(-1);  //reset datamodel
        }
    }

    public boolean isValueBlank(String value)
    {
        if (value == null) {
            return true;
        }

        return value.trim().equals("");
    }

    void decodeSelection(FacesContext context, DataTable table)
    {
        String clientId = table.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        String selection = params.get(clientId + "_selection");

        if (table.isSingleSelectionMode()) {
            decodeSingleSelection(table, selection);
        }
        else {
            decodeMultipleSelection(table, selection);
        }

        //Instant selection and unselection
        queueInstantSelectionEvent(context, table, clientId, params);
    }

    void queueInstantSelectionEvent(FacesContext context, DataTable table, String clientId, Map<String, String> params)
    {

        if (table.isInstantSelectionRequest(context)) {
            int selectedRowIndex = Integer.parseInt(params.get(clientId + "_instantSelectedRowIndex"));
            table.setRowIndex(selectedRowIndex);
            SelectEvent selectEvent = new SelectEvent(table, table.getRowData());
            selectEvent.setPhaseId(PhaseId.INVOKE_APPLICATION);
            table.queueEvent(selectEvent);
        }
        else if (table.isInstantUnselectionRequest(context)) {
            int unselectedRowIndex = Integer.parseInt(params.get(clientId + "_instantUnselectedRowIndex"));
            table.setRowIndex(unselectedRowIndex);
            UnselectEvent unselectEvent = new UnselectEvent(table, table.getRowData());
            unselectEvent.setPhaseId(PhaseId.INVOKE_APPLICATION);
            table.queueEvent(unselectEvent);
        }
    }

    void decodeSingleSelection(DataTable table, String selection)
    {
        if (isValueBlank(selection)) {
            table.setSelection(null);
        }
        else {
            if (table.isCellSelection()) {
                table.setSelection(buildCell(table, selection));
            }
            else {
                int selectedRowIndex = Integer.parseInt(selection);

                table.setRowIndex(selectedRowIndex);
                table.setSelection(table.getRowData());
            }
        }
    }

    void decodeMultipleSelection(DataTable table, String selection)
    {
        Class<?> clazz = table.getValueExpression("selection").getType(FacesContext.getCurrentInstance().getELContext());

        if (isValueBlank(selection)) {
            Object data = Array.newInstance(clazz.getComponentType(), 0);
            table.setSelection(data);
        }
        else {
            if (table.isCellSelection()) {
                String[] cellInfos = selection.split(",");
                Cell[] cells = new Cell[cellInfos.length];

                for (int i = 0; i < cellInfos.length; i++) {
                    cells[i] = buildCell(table, cellInfos[i]);
                    table.setRowIndex(-1);  //clean
                }

                table.setSelection(cells);
            }
            else {
                String[] rowSelectValues = selection.split(",");
                Object data = Array.newInstance(clazz.getComponentType(), rowSelectValues.length);

                for (int i = 0; i < rowSelectValues.length; i++) {
                    table.setRowIndex(Integer.parseInt(rowSelectValues[i]));

                    Array.set(data, i, table.getRowData());
                }

                table.setSelection(data);
            }
        }
    }
    
    String resolveField(ValueExpression expression)
    {
        Object newValue = expression.getValue(FacesContext.getCurrentInstance().getELContext());

        if (newValue == null || !(newValue instanceof String))
            return resolveField_old (expression);
        else {
            String val = (String)newValue;
            return val.substring(val.indexOf(".") + 1); 
        }
    }

    String resolveField_old(ValueExpression expression)
    {
        String expressionString = expression.getExpressionString();
        expressionString = expressionString.substring(2, expressionString.length() - 1);      //Remove #{}

        return expressionString.substring(expressionString.indexOf(".") + 1);                //Remove var
    }

    Cell buildCell(DataTable dataTable, String value)
    {
        String[] cellInfo = value.split("#");

        //Column
        int rowIndex = Integer.parseInt(cellInfo[0]);
        UIColumn column = dataTable.getColumns().get(Integer.parseInt(cellInfo[1]));

        //RowData
        dataTable.setRowIndex(rowIndex);
        Object rowData = dataTable.getRowData();

        //Cell value
        Object cellValue = null;
        UIComponent columnChild = column.getChildren().get(0);
        if (columnChild instanceof ValueHolder) {
            cellValue = ((ValueHolder) columnChild).getValue();
        }

        return new Cell(rowData, column.getId(), cellValue);
    }
}
