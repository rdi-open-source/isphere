/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.sourcefilesearch;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import biz.isphere.base.internal.ClipboardHelper;
import biz.isphere.base.internal.DialogSettingsManager;
import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.base.internal.IResizableTableColumnsViewer;
import biz.isphere.base.internal.UIHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.DateTimeHelper;
import biz.isphere.core.internal.FilterDialog;
import biz.isphere.core.internal.IEditor;
import biz.isphere.core.internal.ISourceFileSearchMemberFilterCreator;
import biz.isphere.core.internal.Member;
import biz.isphere.core.preferences.Preferences;
import biz.isphere.core.resourcemanagement.filter.RSEFilter;
import biz.isphere.core.search.SearchOptions;
import biz.isphere.core.sourcemembercopy.rse.CopyMemberDialog;
import biz.isphere.core.sourcemembercopy.rse.CopyMemberService;

public class SearchResultViewer implements IResizableTableColumnsViewer {

    private String qualifiedConnectionName;
    private String searchString;
    private SearchResult[] _searchResults;
    private SearchOptions _searchOptions;
    private TableViewer tableViewerMembers;
    private Table tableMembers;
    private Object[] selectedItemsMembers;
    private Shell shell;
    private TableViewer tableViewerStatements;
    private Table tableStatements;
    private String[] statements;
    private boolean isEditMode;

    private DialogSettingsManager dialogSettingsManager;

    private class LabelProviderTableViewerMembers extends LabelProvider implements ITableLabelProvider {

        private static final String UNKNOWN = "*UNKNOWN"; //$NON-NLS-1$

        public String getColumnText(Object element, int columnIndex) {
            SearchResult searchResult = (SearchResult)element;
            if (columnIndex == 0) {
                return searchResult.getLibrary();
            } else if (columnIndex == 1) {
                return searchResult.getFile();
            } else if (columnIndex == 2) {
                return searchResult.getMember();
            } else if (columnIndex == 3) {
                return searchResult.getSrcType();
            } else if (columnIndex == 4) {
                return searchResult.getDescription();
            } else if (columnIndex == 5) {
                Timestamp lastChangedDate = searchResult.getLastChangedDate();
                return DateTimeHelper.getTimestampFormatted(lastChangedDate);
            } else if (columnIndex == 6) {
                return Integer.toString(searchResult.getStatementsCount());
            }
            return UNKNOWN;
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class ContentProviderTableViewerMembers implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return _searchResults;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class SorterTableViewerMembers extends ViewerSorter {

        private TableColumn initialColumn;
        private int initialDirection;
        private TableViewer tableViewer;

        public SorterTableViewerMembers(TableViewer tableViewer, TableColumn column, int direction) {

            this.initialColumn = column;
            this.initialDirection = direction;

            this.tableViewer = tableViewer;
            this.tableViewer.getTable().setSortColumn(column);
            this.tableViewer.getTable().setSortDirection(direction);
        }

        public void setOrder(TableColumn column) {

            int direction = changeSortDirection(column);
            if (direction == SWT.NONE) {
                direction = initialDirection;
                column = null;
            }

            this.tableViewer.getTable().setSortDirection(direction);
            this.tableViewer.getTable().setSortColumn(column);
        }

        private int changeSortDirection(TableColumn column) {

            if (column == tableViewer.getTable().getSortColumn()) {
                if (tableViewer.getTable().getSortDirection() == SWT.NONE) {
                    return SWT.UP;
                } else if (tableViewer.getTable().getSortDirection() == SWT.UP) {
                    return SWT.DOWN;
                } else {
                    return SWT.NONE;
                }
            } else {
                return SWT.UP;
            }
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {

            int result;

            TableColumn column = tableViewer.getTable().getSortColumn();
            if (column == null) {
                column = initialColumn;
            }

            if (Messages.Library.equals(column.getText())) {
                result = sortByLibrary(viewer, e1, e2);
            } else if (Messages.File.equals(column.getText())) {
                result = sortByFile(viewer, e1, e2);
            } else if (Messages.Member.equals(column.getText())) {
                result = sortByMember(viewer, e1, e2);
            } else if (Messages.Member_type_short.equals(column.getText())) {
                result = sortBySrcType(viewer, e1, e2);
            } else if (Messages.Description.equals(column.getText())) {
                result = sortByDescription(viewer, e1, e2);
            } else if (Messages.Last_changed.equals(column.getText())) {
                result = sortByLastChangedDate(viewer, e1, e2);
            } else if (Messages.StatementsCount.equals(column.getText())) {
                result = sortByStatementsCount(viewer, e1, e2);
            } else {
                result = sortByLibrary(viewer, e1, e2);
            }

            if (tableViewer.getTable().getSortDirection() == SWT.DOWN) {
                result = result * -1;
            }

            return result;
        }

        private int sortByLibrary(Viewer viewer, Object e1, Object e2) {

            int result;

            result = ((SearchResult)e1).getLibrary().compareTo(((SearchResult)e2).getLibrary());
            if (result == 0) {
                result = ((SearchResult)e1).getFile().compareTo(((SearchResult)e2).getFile());
                if (result == 0) {
                    result = ((SearchResult)e1).getMember().compareTo(((SearchResult)e2).getMember());
                }
            }

            return result;
        }

        private int sortByFile(Viewer viewer, Object e1, Object e2) {

            int result;

            result = ((SearchResult)e1).getFile().compareTo(((SearchResult)e2).getFile());
            if (result == 0) {
                result = ((SearchResult)e1).getLibrary().compareTo(((SearchResult)e2).getLibrary());
                if (result == 0) {
                    result = ((SearchResult)e1).getMember().compareTo(((SearchResult)e2).getMember());
                }
            }

            return result;
        }

        private int sortByMember(Viewer viewer, Object e1, Object e2) {

            int result = ((SearchResult)e1).getMember().compareTo(((SearchResult)e2).getMember());
            if (result == 0) {
                result = ((SearchResult)e1).getLibrary().compareTo(((SearchResult)e2).getLibrary());
                if (result == 0) {
                    result = ((SearchResult)e1).getFile().compareTo(((SearchResult)e2).getFile());
                }
            }

            return result;
        }

        private int sortBySrcType(Viewer viewer, Object e1, Object e2) {

            int result;

            result = ((SearchResult)e1).getSrcType().compareTo(((SearchResult)e2).getSrcType());
            if (result == 0) {
                result = sortByMember(viewer, e1, e2);
            }

            return result;
        }

        private int sortByDescription(Viewer viewer, Object e1, Object e2) {

            int result = ((SearchResult)e1).getDescription().compareTo(((SearchResult)e2).getDescription());
            if (result == 0) {
                result = sortByMember(viewer, e1, e2);
            }

            return result;
        }

        private int sortByLastChangedDate(Viewer viewer, Object e1, Object e2) {

            int result = ((SearchResult)e1).getLastChangedDate().compareTo(((SearchResult)e2).getLastChangedDate());
            if (result == 0) {
                result = sortByMember(viewer, e1, e2);
            }

            return result;
        }

        private int sortByStatementsCount(Viewer viewer, Object e1, Object e2) {

            Integer count1 = ((SearchResult)e1).getStatementsCount();
            Integer count2 = ((SearchResult)e2).getStatementsCount();

            int result = count1.compareTo(count2);
            if (result == 0) {
                result = sortByMember(viewer, e1, e2);
            }

            return result;
        }
    }

    private class LabelProviderStatements extends LabelProvider implements ITableLabelProvider {

        private static final String UNKNOWN = "*UNKNOWN"; //$NON-NLS-1$

        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return (String)element;
            }
            return UNKNOWN;
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class ContentProviderStatements implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return statements;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class TableViewerMembersDoubleClickListener implements IDoubleClickListener {

        public void doubleClick(DoubleClickEvent event) {

            if (tableViewerMembers.getSelection() instanceof IStructuredSelection) {

                IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerMembers.getSelection();
                SearchResult _searchResult = (SearchResult)structuredSelection.getFirstElement();
                IEditor editor = ISpherePlugin.getEditor();

                if (editor != null) {
                    editor.openEditor(qualifiedConnectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(), 0,
                        getEditMode());
                }
            }
        }
    }

    private class TableMembersMenuAdapter extends MenuAdapter {

        private Menu menuTableMembers;
        private MenuItem menuItemOpenEditor;
        private MenuItem menuItemOpenViewer;
        private MenuItem menuItemSelectAll;
        private MenuItem menuItemDeselectAll;
        private MenuItem menuItemInvertSelection;
        private MenuItem menuCopySelected;
        private MenuItem menuItemRemove;
        private MenuItem menuItemSeparator1;
        private MenuItem menuCopySelectedMembers;
        private MenuItem menuSourceFileSearch;
        private MenuItem menuCompareSelectedMembers;
        private MenuItem menuItemSeparator2;
        private MenuItem menuCreateFilterFromSelectedMembers;
        private MenuItem menuExportSelectedMembersToExcel;

        public TableMembersMenuAdapter(Menu menuTableMembers) {
            this.menuTableMembers = menuTableMembers;
        }

        @Override
        public void menuShown(MenuEvent event) {
            retrieveSelectedTableItems();
            destroyMenuItems();
            createMenuItems();
        }

        public void destroyMenuItems() {
            dispose(menuItemOpenEditor);
            dispose(menuItemOpenViewer);
            dispose(menuItemSelectAll);
            dispose(menuItemDeselectAll);
            dispose(menuItemInvertSelection);
            dispose(menuCopySelected);
            dispose(menuItemRemove);
            dispose(menuItemSeparator1);
            dispose(menuCopySelectedMembers);
            dispose(menuSourceFileSearch);
            dispose(menuCompareSelectedMembers);
            dispose(menuItemSeparator2);
            dispose(menuCreateFilterFromSelectedMembers);
            dispose(menuExportSelectedMembersToExcel);
        }

        private void dispose(MenuItem menuItem) {
            if (!((menuItem == null) || (menuItem.isDisposed()))) {
                menuItem.dispose();
            }
        }

        public void createMenuItems() {

            if (hasSelectedItems()) {
                menuItemOpenEditor = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemOpenEditor.setText(Messages.Open_for_edit);
                menuItemOpenEditor.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_EDITOR));
                menuItemOpenEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenEditor(0);
                    }
                });

                menuItemOpenViewer = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemOpenViewer.setText(Messages.Open_for_browse);
                menuItemOpenViewer.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_VIEWER));
                menuItemOpenViewer.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenViewer(0);
                    }
                });
            }

            if (hasItems()) {
                menuItemSelectAll = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemSelectAll.setText(Messages.Select_all);
                menuItemSelectAll.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_SELECT_ALL));
                menuItemSelectAll.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemSelectAll();
                    }
                });

                menuItemDeselectAll = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemDeselectAll.setText(Messages.Deselect_all);
                menuItemDeselectAll.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_DESELECT_ALL));
                menuItemDeselectAll.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemDeselectAll();
                    }
                });
            }

            if (hasSelectedItems()) {
                menuItemInvertSelection = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemInvertSelection.setText(Messages.Invert_selection);
                menuItemInvertSelection.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_INVERT_SELECTION));
                menuItemInvertSelection.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemInvertSelectedItems();
                    }
                });

                menuCopySelected = new MenuItem(menuTableMembers, SWT.NONE);
                menuCopySelected.setText(Messages.Copy_selected);
                menuCopySelected.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_COPY_TO_CLIPBOARD));
                menuCopySelected.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemCopySelectedItems();
                    }
                });

                menuItemRemove = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemRemove.setText(Messages.Remove);
                menuItemRemove.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_REMOVE));
                menuItemRemove.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemRemoveSelectedItems();
                    }
                });

                if (IBMiHostContributionsHandler.hasContribution()) {

                    menuItemSeparator1 = new MenuItem(menuTableMembers, SWT.SEPARATOR);

                    menuCopySelectedMembers = new MenuItem(menuTableMembers, SWT.NONE);
                    menuCopySelectedMembers.setText(Messages.Menu_Copy_members);
                    menuCopySelectedMembers.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_COPY_MEMBERS_TO));
                    menuCopySelectedMembers.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            executeMenuItemCopySelectedMembers();
                        }
                    });

                    menuSourceFileSearch = new MenuItem(menuTableMembers, SWT.NONE);
                    menuSourceFileSearch.setText(Messages.iSphere_Source_File_Search);
                    menuSourceFileSearch.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_SOURCE_FILE_SEARCH));
                    menuSourceFileSearch.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            executeMenuItemSourceFileSearch();
                        }
                    });

                    menuCompareSelectedMembers = new MenuItem(menuTableMembers, SWT.NONE);
                    menuCompareSelectedMembers.setText(Messages.Menu_Compare_source_members);
                    menuCompareSelectedMembers.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_COMPARE));
                    menuCompareSelectedMembers.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            executeMenuItemCompareSelectedMembers();
                        }
                    });
                }

                menuItemSeparator2 = new MenuItem(menuTableMembers, SWT.SEPARATOR);

                menuCreateFilterFromSelectedMembers = new MenuItem(menuTableMembers, SWT.NONE);
                menuCreateFilterFromSelectedMembers.setText(Messages.Export_to_Member_Filter);
                menuCreateFilterFromSelectedMembers.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_MEMBER_FILTER));
                menuCreateFilterFromSelectedMembers.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemCreateFilterFromSelectedMembers();
                    }
                });

                menuExportSelectedMembersToExcel = new MenuItem(menuTableMembers, SWT.NONE);
                menuExportSelectedMembersToExcel.setText(Messages.Export_to_Excel);
                menuExportSelectedMembersToExcel.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_EXCEL));
                menuExportSelectedMembersToExcel.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemExportSelectedMembersToExcel();
                    }
                });
            }

        }
    }

    private class TableViewerStatementsDoubleClickListener implements IDoubleClickListener {

        public void doubleClick(DoubleClickEvent event) {

            if (selectedItemsMembers != null && selectedItemsMembers.length == 1) {

                SearchResult _searchResult = (SearchResult)selectedItemsMembers[0];
                if (tableViewerStatements.getSelection() instanceof IStructuredSelection) {

                    IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerStatements.getSelection();
                    String statement = (String)structuredSelection.getFirstElement();
                    int statementLine = 0;
                    int startAt = statement.indexOf('(');
                    int endAt = statement.indexOf(')');

                    if (startAt != -1 && endAt != -1 && startAt < endAt) {
                        String _statementLine = statement.substring(startAt + 1, endAt);
                        try {
                            statementLine = Integer.parseInt(_statementLine);
                        } catch (NumberFormatException e1) {
                        }
                    }

                    IEditor editor = ISpherePlugin.getEditor();
                    if (editor != null) {
                        editor.openEditor(qualifiedConnectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(),
                            statementLine, getEditMode());
                    }
                }
            }
        }
    }

    private class TableStatementsMenuAdapter extends MenuAdapter {

        private Menu menuTableStatements;
        private MenuItem menuItemOpenEditor;
        private MenuItem menuItemOpenViewer;

        public TableStatementsMenuAdapter(Menu menuTableStatements) {
            this.menuTableStatements = menuTableStatements;
        }

        @Override
        public void menuShown(MenuEvent event) {
            retrieveSelectedTableItems();
            destroyMenuItems();
            createMenuItems();
        }

        public void destroyMenuItems() {
            if (!((menuItemOpenEditor == null) || (menuItemOpenEditor.isDisposed()))) {
                menuItemOpenEditor.dispose();
            }
            if (!((menuItemOpenViewer == null) || (menuItemOpenViewer.isDisposed()))) {
                menuItemOpenViewer.dispose();
            }
        }

        public void createMenuItems() {

            if (hasSelectedItems()) {
                menuItemOpenEditor = new MenuItem(menuTableStatements, SWT.NONE);
                menuItemOpenEditor.setText(Messages.Open_for_edit);
                menuItemOpenEditor.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_EDITOR));
                menuItemOpenEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenEditor(getStatementLine());
                    }
                });

                menuItemOpenViewer = new MenuItem(menuTableStatements, SWT.NONE);
                menuItemOpenViewer.setText(Messages.Open_for_browse);
                menuItemOpenViewer.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_VIEWER));
                menuItemOpenViewer.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenViewer(getStatementLine());
                    }
                });
            }
        }
    }

    public SearchResultViewer(String qualifiedConnectionName, String searchString, SearchResult[] _searchResults, SearchOptions _searchOptions) {
        this.qualifiedConnectionName = qualifiedConnectionName;
        this.searchString = searchString;
        this._searchResults = _searchResults;
        this._searchOptions = _searchOptions;
    }

    /**
     * @wbp.parser.entryPoint
     */
    public void createContents(Composite parent) {

        shell = parent.getShell();

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        final SashForm sashFormSearchResult = new SashForm(container, SWT.BORDER);
        sashFormSearchResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tableViewerMembers = new TableViewer(sashFormSearchResult, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        tableViewerMembers.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                retrieveSelectedTableItems();
                setStatements();
            }
        });
        tableViewerMembers.addDoubleClickListener(new TableViewerMembersDoubleClickListener());

        tableViewerMembers.setLabelProvider(new LabelProviderTableViewerMembers());
        tableViewerMembers.setContentProvider(new ContentProviderTableViewerMembers());

        tableMembers = tableViewerMembers.getTable();
        tableMembers.setLinesVisible(true);
        tableMembers.setHeaderVisible(true);
        tableMembers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final TableColumn tableColumnLibrary = createTableColumn(tableMembers, "library", 100, Messages.Library, 0);
        final TableColumn tableColumnFile = createTableColumn(tableMembers, "file", 100, Messages.File, 1);
        final TableColumn tableColumnMember = createTableColumn(tableMembers, "member", 100, Messages.Member, 2);
        final TableColumn tableColumnSrcType = createTableColumn(tableMembers, "type", 100, Messages.Member_type_short, 3);
        final TableColumn tableColumnDescription = createTableColumn(tableMembers, "description", 300, Messages.Description, 4);
        final TableColumn tableColumnLastChangedDate = createTableColumn(tableMembers, "lastChanged", 120, Messages.Last_changed, 5);
        final TableColumn tableColumnStatementsCount = createTableColumn(tableMembers, "statementCount", 80, Messages.StatementsCount, 6);

        final Menu menuTableMembers = new Menu(tableMembers);
        menuTableMembers.addMenuListener(new TableMembersMenuAdapter(menuTableMembers));
        tableMembers.setMenu(menuTableMembers);

        final SorterTableViewerMembers sorterTableViewerMembers = new SorterTableViewerMembers(tableViewerMembers, tableColumnLibrary, SWT.UP);
        tableViewerMembers.setSorter(sorterTableViewerMembers);
        sorterTableViewerMembers.setOrder(null);

        Listener sortListener = new Listener() {
            public void handleEvent(Event e) {
                TableColumn column = (TableColumn)e.widget;
                sorterTableViewerMembers.setOrder(column);
                tableViewerMembers.refresh();
            }
        };

        tableColumnLibrary.addListener(SWT.Selection, sortListener);
        tableColumnFile.addListener(SWT.Selection, sortListener);
        tableColumnMember.addListener(SWT.Selection, sortListener);
        tableColumnSrcType.addListener(SWT.Selection, sortListener);
        tableColumnDescription.addListener(SWT.Selection, sortListener);
        tableColumnLastChangedDate.addListener(SWT.Selection, sortListener);
        tableColumnStatementsCount.addListener(SWT.Selection, sortListener);

        tableViewerMembers.setInput(new Object());

        tableViewerStatements = new TableViewer(sashFormSearchResult, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewerStatements.addDoubleClickListener(new TableViewerStatementsDoubleClickListener());
        tableViewerStatements.setLabelProvider(new LabelProviderStatements());
        tableViewerStatements.setContentProvider(new ContentProviderStatements());

        tableStatements = tableViewerStatements.getTable();
        tableStatements.setHeaderVisible(true);
        tableStatements.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createTableColumn(tableStatements, "statement", 800, Messages.Statement, 1);

        final Menu menuTableStatement = new Menu(tableStatements);
        menuTableStatement.addMenuListener(new TableStatementsMenuAdapter(menuTableStatement));
        tableStatements.setMenu(menuTableStatement);

        setStatements();
        tableViewerStatements.setInput(new Object());

        sashFormSearchResult.setWeights(new int[] { 1, 1 });

    }

    private TableColumn createTableColumn(Table table, String columnName, int width, String label, int index) {

        TableColumn column = getDialogSettingsManager().createResizableTableColumn(table, SWT.LEFT, columnName, width, index);
        column.setText(label);

        return column;
    }

    // private void saveColumnSizes() {
    //
    // if (columnSizeUpdateJob != null) {
    // columnSizeUpdateJob.cancel();
    // columnSizeUpdateJob = null;
    // }
    //
    // columnSizeUpdateJob = new ColumnSizeUpdateJob(columnMemberSize,
    // columnSrcTypeSize, columnLastChangedDateSize, columnStatementsCountSize);
    // columnSizeUpdateJob.schedule(500);
    // }

    private int getStatementLine() {

        int statementLine = 0;

        if (selectedItemsMembers != null && selectedItemsMembers.length == 1) {

            if (tableViewerStatements.getSelection() instanceof IStructuredSelection) {

                IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerStatements.getSelection();

                String statement = (String)structuredSelection.getFirstElement();
                int startAt = statement.indexOf('(');
                int endAt = statement.indexOf(')');
                if (startAt != -1 && endAt != -1 && startAt < endAt) {
                    String _statementLine = statement.substring(startAt + 1, endAt);
                    try {
                        statementLine = Integer.parseInt(_statementLine);
                    } catch (NumberFormatException e1) {
                    }
                }
            }
        }

        return statementLine;
    }

    private String getEditMode() {

        if (isEditMode) {
            return IEditor.EDIT;
        } else {
            return IEditor.DISPLAY;
        }
    }

    private void retrieveSelectedTableItems() {
        if (tableViewerMembers.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerMembers.getSelection();
            selectedItemsMembers = structuredSelection.toArray();
        } else {
            selectedItemsMembers = new Object[0];
        }
    }

    private void executeMenuItemSelectAll() {

        Object[] objects = new Object[tableMembers.getItemCount()];
        for (int idx = 0; idx < tableMembers.getItemCount(); idx++) {
            objects[idx] = tableViewerMembers.getElementAt(idx);
        }
        tableViewerMembers.setSelection(new StructuredSelection(objects), true);
        selectedItemsMembers = objects;
        tableMembers.setFocus();

        setStatements();

    }

    private void executeMenuItemDeselectAll() {

        tableViewerMembers.setSelection(new StructuredSelection(), true);
        selectedItemsMembers = new Object[0];

        setStatements();

    }

    private void executeMenuItemOpenEditor(int statement) {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {

            for (int idx = 0; idx < selectedItemsMembers.length; idx++) {

                SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
                editor.openEditor(qualifiedConnectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(), statement,
                    IEditor.EDIT);
            }
        }
    }

    private void executeMenuItemOpenViewer(int statement) {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {

            for (int idx = 0; idx < selectedItemsMembers.length; idx++) {

                SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
                editor.openEditor(qualifiedConnectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(), statement,
                    IEditor.DISPLAY);
            }
        }
    }

    private void executeMenuItemInvertSelectedItems() {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {
            ContentProviderTableViewerMembers contentProvider = (ContentProviderTableViewerMembers)tableViewerMembers.getContentProvider();
            List<Object> allItems = new ArrayList<Object>(Arrays.asList(contentProvider.getElements(null)));
            allItems.removeAll(Arrays.asList(selectedItemsMembers));
            executeMenuItemDeselectAll();
            tableViewerMembers.setSelection(new StructuredSelection(allItems), true);
        }
    }

    private void executeMenuItemCopySelectedItems() {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {
            if (selectedItemsMembers.length > 0) {
                StringBuilder list = new StringBuilder();
                list.append(Messages.Library);
                list.append("\t");
                list.append(Messages.File);
                list.append("\t");
                list.append(Messages.Member);
                list.append("\t");
                list.append(Messages.Type);
                list.append("\t");
                list.append(Messages.Description);
                list.append("\t");
                list.append(Messages.Last_changed);
                list.append("\t");
                list.append(Messages.StatementsCount);
                list.append("\n");
                for (Object item : selectedItemsMembers) {
                    SearchResult searchResult = (SearchResult)item;
                    list.append(searchResult.getLibrary());
                    list.append("\t");
                    list.append(searchResult.getFile());
                    list.append("\t");
                    list.append(searchResult.getMember());
                    list.append("\t");
                    list.append(searchResult.getSrcType());
                    list.append("\t");
                    list.append(searchResult.getDescription());
                    list.append("\t");
                    list.append(getLastChangedDate(searchResult));
                    list.append("\t");
                    list.append(searchResult.getStatementsCount());
                    list.append("\n");
                }
                ClipboardHelper.setText(list.toString());
            }
        }
    }

    private String getLastChangedDate(SearchResult searchResult) {

        String date = Preferences.getInstance().getDateFormatter().format(searchResult.getLastChangedDate());
        String time = Preferences.getInstance().getTimeFormatter().format(searchResult.getLastChangedDate());

        return date + " " + time;
    }

    private void executeMenuItemCopySelectedMembers() {

        CopyMemberService jobDescription = new CopyMemberService(getShell(), qualifiedConnectionName);

        for (int idx = 0; idx < selectedItemsMembers.length; idx++) {
            SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
            jobDescription.addItem(_searchResult.getFile(), _searchResult.getLibrary(), _searchResult.getMember(), _searchResult.getSrcType());
        }

        CopyMemberDialog dialog = new CopyMemberDialog(getShell());
        dialog.setContent(jobDescription);
        dialog.open();
    }

    private void executeMenuItemCompareSelectedMembers() {

        try {

            List<Member> members = new LinkedList<Member>();

            for (int idx = 0; idx < selectedItemsMembers.length; idx++) {
                SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
                Member member = IBMiHostContributionsHandler.getMember(qualifiedConnectionName, _searchResult.getLibrary(), _searchResult.getFile(),
                    _searchResult.getMember());
                members.add(member);
            }

            IBMiHostContributionsHandler.compareSourceMembers(qualifiedConnectionName, members, isEditMode);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(e));
        }
    }

    private void executeMenuItemSourceFileSearch() {

        Map<String, SearchElement> _searchElements = new LinkedHashMap<String, SearchElement>();

        for (int i = 0; i < selectedItemsMembers.length; i++) {
            SearchResult searchResult = (SearchResult)selectedItemsMembers[i];
            SearchElement _searchElement = new SearchElement(searchResult);
            _searchElements.put(_searchElement.getKey(), _searchElement);
        }

        SearchDialog dialog = new SearchDialog(getShell(), _searchElements, true);
        if (dialog.open() == Dialog.OK) {

            SearchOptions searchOptions = dialog.getSearchOptions();
            ArrayList<SearchElement> selectedElements = dialog.getSelectedElements();

            SearchPostRun postRun = new SearchPostRun();
            postRun.setConnection(null);
            postRun.setConnectionName(qualifiedConnectionName);
            postRun.setSearchString(searchOptions.getCombinedSearchString());
            postRun.setSearchElements(_searchElements);
            postRun.setWorkbenchWindow(UIHelper.getActivePage().getWorkbenchWindow());

            Connection jdbcConnection = IBMiHostContributionsHandler.getJdbcConnection(qualifiedConnectionName);
            SearchExec searchExec = new SearchExec();
            searchExec.execute(qualifiedConnectionName, jdbcConnection, searchOptions, selectedElements, postRun);

        }

    }

    private void executeMenuItemCreateFilterFromSelectedMembers() {

        ISourceFileSearchMemberFilterCreator creator = ISpherePlugin.getSourceFileSearchMemberFilterCreator();

        if (creator != null) {

            SearchResult[] _selectedMembers = new SearchResult[selectedItemsMembers.length];
            for (int i = 0; i < _selectedMembers.length; i++) {
                _selectedMembers[i] = (SearchResult)selectedItemsMembers[i];
            }

            FilterDialog dialog = new FilterDialog(shell, RSEFilter.TYPE_MEMBER);
            dialog.setFilterPools(creator.getFilterPools(getConnectionName()));
            if (dialog.open() == Dialog.OK) {
                if (!creator.createMemberFilter(getConnectionName(), dialog.getFilterPool(), dialog.getFilter(), dialog.getFilterUpdateType(),
                    _selectedMembers)) {
                }
            }
        }
    }

    private void executeMenuItemExportSelectedMembersToExcel() {

        SearchResult[] _selectedMembers = new SearchResult[selectedItemsMembers.length];
        for (int i = 0; i < _selectedMembers.length; i++) {
            _selectedMembers[i] = (SearchResult)selectedItemsMembers[i];
        }

        MembersToExcelExporter exporter = new MembersToExcelExporter(shell, getSearchOptions(), _selectedMembers);
        if (_selectedMembers.length != getSearchResults().length) {
            exporter.setPartialExport(true);
        }
        exporter.export();
    }

    private void executeMenuItemRemoveSelectedItems() {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {
            List<SearchResult> searchResult = new ArrayList<SearchResult>(Arrays.asList(_searchResults));
            searchResult.removeAll(Arrays.asList(selectedItemsMembers));
            _searchResults = searchResult.toArray(new SearchResult[searchResult.size()]);
            tableViewerMembers.remove(selectedItemsMembers);
        }
    }

    private void setStatements() {
        if (selectedItemsMembers == null || selectedItemsMembers.length == 0) {
            statements = new String[1];
            statements[0] = Messages.No_selection;
        } else if (selectedItemsMembers.length == 1) {
            SearchResult _searchResult = (SearchResult)selectedItemsMembers[0];
            SearchResultStatement[] _statements = _searchResult.getStatements();
            statements = new String[_statements.length];
            for (int idx = 0; idx < _statements.length; idx++) {
                statements[idx] = "(" + Integer.toString(_statements[idx].getStatement()) + ") " + _statements[idx].getLine(); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            statements = new String[1];
            statements[0] = Messages.Multiple_selection;
        }
        tableViewerStatements.refresh();

    }

    public String getConnectionName() {
        return qualifiedConnectionName;
    }

    public String getSearchString() {
        return searchString;
    }

    public SearchResult[] getSearchResults() {
        return _searchResults;
    }

    public SearchOptions getSearchOptions() {
        return _searchOptions;
    }

    public boolean hasItems() {

        if (tableViewerMembers != null && tableViewerMembers.getContentProvider() != null) {
            ContentProviderTableViewerMembers contentProvider = (ContentProviderTableViewerMembers)tableViewerMembers.getContentProvider();
            if (contentProvider.getElements(null).length > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSelectedItems() {

        if (selectedItemsMembers != null && selectedItemsMembers.length > 0) {
            return true;
        }
        return false;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        tableViewerMembers.addSelectionChangedListener(listener);
    }

    public void removeSelectedItems() {
        executeMenuItemRemoveSelectedItems();
    }

    public void invertSelectedItems() {
        executeMenuItemInvertSelectedItems();
    }

    public void setEditEnabled(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public boolean isEditEnabled() {
        return this.isEditMode;
    }

    public void resetColumnWidths() {
        getDialogSettingsManager().resetColumnWidths(tableMembers);
        getDialogSettingsManager().resetColumnWidths(tableStatements);
    }

    private Shell getShell() {
        return shell;
    }

    private DialogSettingsManager getDialogSettingsManager() {

        if (dialogSettingsManager == null) {
            dialogSettingsManager = new DialogSettingsManager(ISpherePlugin.getDefault().getDialogSettings(), getClass());
        }
        return dialogSettingsManager;
    }
}
