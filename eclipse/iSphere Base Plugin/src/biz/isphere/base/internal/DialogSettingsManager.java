/*******************************************************************************
 * Copyright (c) 2012-2021 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.base.internal;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class DialogSettingsManager {

    private static final String SAVE_TABLE_PROPERTIES_ENABLED = "SAVE_TABLE_PROPERTIES_ENABLED";
    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String COLUMN_WIDTH = "COLUMN_WIDTH";
    private static final String COLUMN_ORDER = "COLUMN_ORDER";
    private static final String COLUMN_PREVIOUS_WIDTH = "COLUMN_PREVIOUS_WIDTH";
    private static final String COLUMN_IS_HIDDEN = "COLUMN_IS_HIDDEN";

    private IDialogSettings dialogSettings;
    private Class<?> section;

    private SimpleDateFormat dateFormatter;
    private ControlAdapter columnResizeListener;
    private Listener columnMoveListener;

    public DialogSettingsManager(IDialogSettings aDialogSettings) {
        this(aDialogSettings, null);
    }

    public DialogSettingsManager(IDialogSettings aDialogSettings, Class<?> section) {
        this.dialogSettings = aDialogSettings;
        this.section = section;
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
    }

    /**
     * Retrieves the screen value that was last displayed on the dialog.
     * 
     * @param aKey - key, that is used to retrieve the value from the store
     * @param aDefault - default value, that is returned if then key does not
     *        yet exist
     * @return the screen value that was last shown
     */
    public String loadValue(String aKey, String aDefault) {
        String tValue = getDialogSettings().get(aKey);
        if (StringHelper.isNullOrEmpty(tValue)) {
            tValue = aDefault;
        }
        return tValue;
    }

    /**
     * Stores a given string value to preserve it for the next time the dialog
     * is shown.
     * 
     * @param aKey - key, the value is assigned to
     * @param aValue - the screen value that is stored
     */
    public void storeValue(String aKey, String aValue) {
        getDialogSettings().put(aKey, aValue);
    }

    /**
     * Retrieves the screen value that was last displayed on the dialog.
     * 
     * @param aKey - key, that is used to retrieve the value from the store
     * @param aDefault - default value, that is returned if then key does not
     *        yet exist
     * @return the screen value that was last shown
     */
    public boolean loadBooleanValue(String aKey, boolean aDefault) {
        String tValue = getDialogSettings().get(aKey);
        return BooleanHelper.tryParseBoolean(tValue, aDefault);
    }

    /**
     * Stores a given boolean value to preserve it for the next time the dialog
     * is shown.
     * 
     * @param aKey - key, the value is assigned to
     * @param aValue - the screen value that is stored
     */
    public void storeValue(String aKey, boolean aValue) {
        getDialogSettings().put(aKey, aValue);
    }

    /**
     * Retrieves the value that is assigned to a given key.
     * 
     * @param aKey - key, that is used to retrieve the value from the store
     * @param aDefault - default value, that is returned if then key does not
     *        yet exist
     * @return the value that is assigned to the key
     */
    public int loadIntValue(String aKey, int aDefault) {
        return IntHelper.tryParseInt(getDialogSettings().get(aKey), aDefault);
    }

    /**
     * Retrieves the integer array that is assigned to a given key.
     * 
     * @param aKey - key, that is used to retrieve the value from the store
     * @return the integer array that is assigned to the key
     */
    public int[] loadIntValues(String aKey) {
        String arrayOfInt = getDialogSettings().get(aKey);
        if (StringHelper.isNullOrEmpty(arrayOfInt)) {
            return new int[0];
        }
        return IntHelper.toIntArray(arrayOfInt);
    }

    /**
     * Stores a given integer value to preserve it for the next time the dialog
     * is shown.
     * 
     * @param aKey - key, the value is assigned to
     * @param aValue - the screen value that is stored
     */
    public void storeValue(String aKey, int aValue) {
        getDialogSettings().put(aKey, aValue);
        IntHelper.tryParseInt(getDialogSettings().get(aKey), -1);
    }

    /**
     * Stores a given integer array to preserve it for the next time the dialog
     * is shown.
     * 
     * @param aKey - key, the value is assigned to
     * @param aValue - the screen value that is stored
     */
    public void storeValue(String aKey, int[] aValue) {
        storeValue(aKey, IntHelper.toString(aValue));
    }

    /**
     * Retrieves the value that is assigned to a given key.
     * 
     * @param aKey - key, that is used to retrieve the value from the store
     * @param aDefault - default value, that is returned if then key does not
     *        yet exist
     * @return the value that is assigned to the key
     */
    public Date loadDateValue(String aKey, Date aDefault) {

        String tValue = getDialogSettings().get(aKey);
        if (StringHelper.isNullOrEmpty(tValue)) {
            return aDefault;
        }

        try {
            Date tDate = dateFormatter.parse(tValue);
            return tDate;
        } catch (Exception e) {
            return aDefault;
        }
    }

    /**
     * Stores a given java.util.Date value to preserve it for the next time the
     * dialog is shown.
     * 
     * @param aKey - key, the value is assigned to
     * @param aValue - the screen value that is stored
     */
    public void storeValue(String aKey, Date aValue) {
        String value = dateFormatter.format(aValue);
        getDialogSettings().put(aKey, value);
    }

    /**
     * Produces a resizable table column for a given table.
     * 
     * @param table - Table, the column is added to.
     * @param style - The style of column to construct.
     * @param name - The name of the table column.
     * @param width - The initial width of the table column.
     * @return
     */
    public TableColumn createResizableTableColumn(Table table, int style, String name, int width) {

        TableColumn tableColumn = new TableColumn(table, style);
        tableColumn.setResizable(true);
        tableColumn.setData(COLUMN_NAME, name);
        tableColumn.setData(COLUMN_WIDTH, new Integer(width));
        tableColumn.setData(COLUMN_IS_HIDDEN, Boolean.FALSE);

        int effectiveWidth = loadIntValue(produceColumnWidthKey(tableColumn), width);
        setColumnWidth(tableColumn, effectiveWidth);

        tableColumn.addControlListener(getColumnResizeListener());

        return tableColumn;
    }

    /**
     * Produces a movable and resizable table column for a given table.
     * 
     * @param table - Table, the column is added to.
     * @param style - The style of column to construct.
     * @param name - The name of the table column.
     * @param width - The initial width of the table column.
     * @param index - initial index of the column in the table.
     * @return
     */
    public TableColumn createResizableTableColumn(Table table, int style, String name, int width, int index) {

        TableColumn tableColumn = createResizableTableColumn(table, style, name, width);
        tableColumn.setMoveable(true);

        tableColumn.setData(COLUMN_ORDER, new Integer(index));

        tableColumn.addListener(SWT.Move, getColumnMoveListener());

        int[] columnOrder = loadIntValues(COLUMN_ORDER);
        if (columnOrder.length == table.getColumnCount()) {
            table.setColumnOrder(columnOrder);
        }

        return tableColumn;
    }

    /**
     * Resets the order of the table columns to their default order.
     * 
     * @param table - table, whose columns are set back to their default order
     */
    // public void resetColumnOrder(Table table) {
    //
    // TableColumn[] tableColumns = table.getColumns();
    // int[] columnOrder = new int[tableColumns.length];
    //
    // for (int i = 0; i < tableColumns.length; i++) {
    // int index = getColumnIndex(tableColumns[i]);
    // if (index >= 0 && index < tableColumns.length) {
    // columnOrder[i] = index;
    // } else {
    // return;
    // }
    // }
    //
    // table.setColumnOrder(columnOrder);
    // }

    /**
     * Resets the columns sizes of a given table to their original widths. The
     * table columns must have been created with
     * {@link #createResizableTableColumn(Table, int, String, int)}. Objects
     * should use the
     * {@link biz.isphere.base.internal.actions.ResetColumnSizeAction} action
     * for resetting the column sizes.
     * 
     * @param table - table that contains the columns
     */
    public void resetColumnWidths(Table table) {

        table.setVisible(false);

        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumn(i);
            resetColumnWidth(column);
        }

        table.setVisible(true);
    }

    private void resetColumnWidth(TableColumn column) {
        if (column.getResizable()) {
            column.setWidth(getDefaultColumnWidth(column));
        }
    }

    /**
     * Resets the hidden attribute of all hidden columns to show all columns.
     * 
     * @param table - table whose hidden columns are to be shown
     */
    public void showAllTableColumns(Table table) {

        TableColumn[] columns = table.getColumns();
        for (TableColumn column : columns) {
            showTableColumn(column);
        }
    }

    /**
     * Resets the hidden attribute of the columns identified by a given list of
     * column names.
     * 
     * @param table - table whose hidden columns are to be shown
     * @param columnNames - names of the columns whose hidden attributes are
     *        reset.
     */
    public void showTableColumns(Table table, String... columnNames) {

        if (columnNames.length == 0) {
            return;
        }

        for (String columnName : columnNames) {
            TableColumn column = getColumn(table, columnName);
            showTableColumn(column);
        }
    }

    /**
     * Sets the hidden attribute of all columns to hide them all.
     * 
     * @param table - table whose columns are to be hidden
     */
    public void hideAllTableColumns(Table table) {

        TableColumn[] columns = table.getColumns();
        for (TableColumn column : columns) {
            showTableColumn(column);
        }
    }

    /**
     * Sets the hidden attribute of the columns identified by a given list of
     * column names.
     * 
     * @param table - table whose columns are hidden.
     * @param columnNames - names of the columns whose hidden attributes are
     *        set.
     */
    public void hideTableColumns(Table table, String... columnNames) {

        if (columnNames.length == 0) {
            return;
        }

        for (String columnName : columnNames) {
            TableColumn column = getColumn(table, columnName);
            hideTableColumn(column);
        }
    }

    /**
     * Sets the hidden attribute of the columns identified by a given list of
     * column names. All other columns are set to the opposite.
     * 
     * @param table - table whose columns are hidden.
     * @param shown - specifies whether the columns are shown or hidden.
     * @param columnNames - names of the columns whose hidden attributes are
     *        set.
     */
    public void setTableColumnsHidden(Table table, String[] columnNames, boolean hidden) {

        if (columnNames.length == 0) {
            return;
        }

        Set<String> names = new HashSet<String>(Arrays.asList(columnNames));

        TableColumn[] columns = table.getColumns();
        for (TableColumn column : columns) {
            boolean isHidden;
            if (names.contains(getColumnName(column))) {
                if (hidden) {
                    isHidden = true;
                } else {
                    isHidden = false;
                }
            } else {
                isHidden = !hidden;
            }
            if (isHidden) {
                hideTableColumn(column);
            } else {
                showTableColumn(column);

            }
        }
    }

    private void showTableColumn(TableColumn column) {

        if (!isHidden(column)) {
            return;
        }

        Integer width = (Integer)column.getData(COLUMN_PREVIOUS_WIDTH);
        setColumnWidth(column, width);
        column.setResizable(true);
        column.setData(COLUMN_IS_HIDDEN, Boolean.FALSE);

        column.removeControlListener(getColumnResizeListener());
    }

    private void hideTableColumn(TableColumn column) {

        if (isHidden(column)) {
            return;
        }

        column.removeControlListener(getColumnResizeListener());

        column.setData(COLUMN_PREVIOUS_WIDTH, new Integer(column.getWidth()));
        column.setWidth(0);
        column.setResizable(false);
        column.setData(COLUMN_IS_HIDDEN, Boolean.TRUE);
    }

    private boolean isHidden(TableColumn column) {
        Boolean isHidden = (Boolean)column.getData(COLUMN_IS_HIDDEN);
        return isHidden;
    }

    /**
     * Sets the column widths of the table columns provided in parameter
     * <code>widths</code>.
     * 
     * @param table - table whose column widths is changed
     * @param widths - map of column names/column widths
     */
    public void setColumnWidth(Table table, Map<String, Integer> widths) {

        for (TableColumn column : table.getColumns()) {
            String columnName = getColumnName(column);
            Integer width = widths.get(columnName);
            if (width != null) {
                column.setWidth(width);
            }
        }

    }

    private void setColumnWidth(TableColumn tableColumn, Integer width) {

        if (width != null && width >= 0) {
            tableColumn.setWidth(width);
        } else {
            resetColumnWidth(tableColumn);
            storeValue(produceColumnWidthKey(tableColumn), tableColumn.getWidth());
        }
    }

    /**
     * Returns the table column identified by the specified name.
     * 
     * @param table - table that contains the columns
     * @param columnName - name of the column that is search for
     * @return column identified by name
     */
    public TableColumn getColumn(Table table, String columnName) {

        if (table == null || columnName == null) {
            return null;
        }

        int numTableColumns = table.getColumnCount();
        for (int i = 0; i < numTableColumns; i++) {
            TableColumn column = table.getColumn(i);
            if (columnName.equals(getColumnName(column))) {
                return column;
            }
        }

        return null;
    }

    /**
     * Returns the name of a resizable table column created with
     * {@link #createResizableTableColumn(Table, int, String, int)}.
     * 
     * @param column - table column
     * @return name of the table column
     */
    public String getColumnName(TableColumn column) {

        if (column == null) {
            return null;
        }

        Object objectName = column.getData(COLUMN_NAME);
        if (objectName instanceof String) {
            return (String)objectName;
        }

        return null;
    }

    /**
     * Enables or disables saving the column properties of a given table.
     * 
     * @param table - the table for that saving the column properties is enabled
     *        or disabled.
     * @return index - saving column properties enabled status
     */
    public void setSaveTableStatusEnabled(Table table, boolean enabled) {

        if (table == null) {
            return;
        }

        table.setData(SAVE_TABLE_PROPERTIES_ENABLED, enabled);
    }

    private boolean isSaveTableStatusEnabled(Table table) {

        Object value = table.getData(SAVE_TABLE_PROPERTIES_ENABLED);
        if (value instanceof Boolean) {
            return (Boolean)value;
        }

        return true;
    }

    private ControlAdapter getColumnResizeListener() {

        if (columnResizeListener == null) {
            columnResizeListener = new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent event) {
                    TableColumn column = (TableColumn)event.getSource();
                    Table table = column.getParent();
                    if (isSaveTableStatusEnabled(table)) {
                        String key = produceColumnWidthKey(column);
                        if (key != null) {
                            storeValue(key, column.getWidth());
                        }
                    }
                }
            };
        }

        return columnResizeListener;
    }

    private Listener getColumnMoveListener() {

        if (columnMoveListener == null) {
            columnMoveListener = new Listener() {
                public void handleEvent(Event event) {
                    TableColumn column = (TableColumn)event.widget;
                    Table table = column.getParent();
                    if (isSaveTableStatusEnabled(table)) {
                        int[] columnOrder = table.getColumnOrder();
                        storeValue(COLUMN_ORDER, columnOrder);
                    }
                }
            };
        }

        return columnMoveListener;
    }

    private String produceColumnWidthKey(TableColumn column) {

        if (column == null) {
            return null;
        }

        String columnName = getColumnName(column);
        if (columnName == null) {
            return null;
        }

        return COLUMN_WIDTH + "_" + columnName; //$NON-NLS-1$
    }

    private int getDefaultColumnWidth(TableColumn column) {

        if (column == null) {
            return -1;
        }

        Object data = column.getData(COLUMN_WIDTH);
        if (data == null) {
            return -1;
        }

        int width = IntHelper.tryParseInt(data.toString(), -1);

        return width;
    }

    /**
     * Returns the dialog settings store.
     * 
     * @return dialog settings
     */
    private IDialogSettings getDialogSettings() {

        if (section == null) {
            return dialogSettings;
        }

        String sectionName = section.getName();
        IDialogSettings dialogSectionSettings = dialogSettings.getSection(sectionName);
        if (dialogSectionSettings == null) {
            dialogSectionSettings = dialogSettings.addNewSection(sectionName);
        }

        return dialogSectionSettings;
    }

}
