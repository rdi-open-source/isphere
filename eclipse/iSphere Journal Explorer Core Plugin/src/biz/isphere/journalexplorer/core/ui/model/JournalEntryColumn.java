/*******************************************************************************
 * Copyright (c) 2012-2021 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.ui.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import biz.isphere.journalexplorer.core.ui.labelproviders.JournalEntryLabelProvider;

/**
 * This class represents a column of the "Journal Entries Viewer" widget. It
 * defines the properties of the column, such as column heading, color, width.
 * 
 * @see JournalEntriesViewerForOutputFilesTab
 * @see JournalEntryLabelProvider
 */
public class JournalEntryColumn {

    private JournalEntryColumnUI columnDef;
    private boolean resizable;
    private boolean moveable;

    private int style;
    private String columnHeading;
    private String tooltipText;
    private int width;
    private Color color;

    public JournalEntryColumn(JournalEntryColumnUI columnDef, String tooltipText, int width) {
        this(columnDef, columnDef.columnName(), tooltipText, width, SWT.NONE);
    }

    public JournalEntryColumn(JournalEntryColumnUI columnDef, String tooltipText, int width, int style) {
        this(columnDef, columnDef.columnName(), tooltipText, width, style);
    }

    private JournalEntryColumn(JournalEntryColumnUI columnDef, String columnHeading, String tooltipText, int width, int style) {

        this.columnDef = columnDef;
        this.resizable = true;
        this.moveable = true;
        this.color = null;

        this.columnHeading = columnHeading;
        this.tooltipText = tooltipText;
        this.width = width;
        this.style = style;
    }

    public JournalEntryColumnUI getColumnDef() {
        return columnDef;
    }

    public String getName() {
        return this.columnDef.columnName();
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getColumnHeading() {
        return notNull(columnHeading);
    }

    public void setColumnHeading(String columnHeading) {
        this.columnHeading = columnHeading;
    }

    public String getTooltipText() {
        return notNull(tooltipText);
    }

    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isMovebale() {
        return moveable;
    }

    public void setMovebale(boolean moveable) {
        this.moveable = moveable;
    }

    private String notNull(String text) {

        if (text == null) {
            return ""; //$NON-NLS-1$
        }

        return text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return columnDef.name() + " (" + super.toString() + ")";
    }
}
