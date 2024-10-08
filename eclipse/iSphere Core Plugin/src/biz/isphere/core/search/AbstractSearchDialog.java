/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.search;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.jface.dialogs.XDialog;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.annotations.CMOne;
import biz.isphere.core.swt.widgets.WidgetFactory;

public abstract class AbstractSearchDialog<M> extends XDialog implements Listener {

    private boolean _editor;
    private SearchArgumentsListEditor _listEditor;
    private Text textString;
    private Button buttonCaseNo;
    private Button buttonCaseYes;
    private Text textFromColumn;
    private Text textToColumn;
    private SearchOptions _searchOptions;
    private String _string;
    private String _case;
    private int _fromColumn;
    private int _toColumn;
    private Label labelFromColumn;
    private Label labelToColumn;
    private Button okButton;
    private int maxColumns;
    private int maxSearchArgumentLength;
    private boolean regularExpressionsOption;
    private SearchOptionConfig[] searchOptionConfig;
    private List listArea;
    private Label labelNumElem;
    private boolean isClosing;
    private boolean hasListBox = true;

    // iSphere settings
    private static final String TO_COLUMN = "toColumn";
    private static final String FROM_COLUMN = "fromColumn";

    @CMOne(info = "CMOne settings")
    private static final String TEXT_STRING = "textString";
    @CMOne(info = "CMOne settings")
    private static final String IGNORE_CASE = "ignoreCase";

    public AbstractSearchDialog(Shell parentShell, int maxColumns, boolean searchArgumentsListEditor, int maxSearchArgumentLength,
        boolean regularExpressionsOption) {
        this(parentShell, maxColumns, searchArgumentsListEditor, maxSearchArgumentLength, regularExpressionsOption, null);
    }

    public AbstractSearchDialog(Shell parentShell, int maxColumns, boolean searchArgumentsListEditor, int maxSearchArgumentLength,
        boolean regularExpressionsOption, SearchOptionConfig[] searchOptionConfig) {
        super(parentShell);

        this.maxColumns = maxColumns;
        this.maxSearchArgumentLength = maxSearchArgumentLength;
        _fromColumn = 1;
        _toColumn = maxColumns;
        if (searchArgumentsListEditor) {
            _editor = ISpherePlugin.isSearchArgumentsListEditor();
        } else {
            _editor = false;
        }
        this.regularExpressionsOption = regularExpressionsOption;
        this.searchOptionConfig = searchOptionConfig;

        this.isClosing = false;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite dialogArea = (Composite)super.createDialogArea(parent);
        dialogArea.setLayout(new GridLayout());
        dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite upperArea = new Composite(dialogArea, SWT.NONE);
        upperArea.setLayout(new GridLayout(1, false));
        upperArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (_editor) {
            createSearchStringEditorGroup(upperArea);
        } else {
            createSearchStringEditorForCMOne(upperArea);
            createCaseOptionsForCMOne(upperArea);
        }

        Composite lowerArea = new Composite(dialogArea, SWT.NONE);
        lowerArea.setLayout(new GridLayout(1, false));
        lowerArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        createColumnsGroup(lowerArea);
        createOptionsGroup(lowerArea);

        if (hasListBox) {
            createListArea(lowerArea);
        }

        loadScreenValues();

        dialogArea.layout();

        return parent;
    }

    private void createSearchStringEditorGroup(Composite container) {

        _listEditor = ISpherePlugin.getSearchArgumentsListEditorProvider().getListEditor(maxSearchArgumentLength, regularExpressionsOption,
            searchOptionConfig);
        _listEditor.createControl(container);
        _listEditor.setListener(this);
    }

    private void createColumnsGroup(Composite container) {

        Group groupAttributes = new Group(container, SWT.NONE);
        groupAttributes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        groupAttributes.setText(Messages.Columns);
        groupAttributes.setLayout(new GridLayout(2, false));

        labelFromColumn = new Label(groupAttributes, SWT.NONE);
        labelFromColumn.setText(Messages.From_column_colon);

        textFromColumn = WidgetFactory.createText(groupAttributes);
        textFromColumn.setTextLimit(10);
        textFromColumn.setLayoutData(new GridData(50, SWT.DEFAULT));
        textFromColumn.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                _fromColumn = 0;
                if (textFromColumn.getText().toUpperCase().trim().equals("*START")) {
                    _fromColumn = 1;
                } else {
                    try {
                        _fromColumn = Integer.parseInt(textFromColumn.getText().trim());
                    } catch (NumberFormatException e1) {
                    }
                }
                setOKButtonEnablement();
            }
        });

        labelToColumn = new Label(groupAttributes, SWT.NONE);
        labelToColumn.setText(Messages.To_column_colon);

        textToColumn = WidgetFactory.createText(groupAttributes);
        textToColumn.setTextLimit(10);
        textToColumn.setLayoutData(new GridData(50, SWT.DEFAULT));
        textToColumn.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                _toColumn = 0;
                if (textToColumn.getText().toUpperCase().trim().equals("*END")) {
                    _toColumn = maxColumns;
                } else {
                    try {
                        _toColumn = Integer.parseInt(textToColumn.getText().trim());
                        if (_toColumn > maxColumns) {
                            _toColumn = maxColumns;
                        }
                    } catch (NumberFormatException e1) {
                    }
                }
                setOKButtonEnablement();
            }
        });
    }

    private void createListArea(Composite lowerArea) {

        Group groupArea = new Group(lowerArea, SWT.NONE);
        groupArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        groupArea.setText(Messages.Area);
        groupArea.setLayout(new GridLayout());

        listArea = new List(groupArea, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData listAreaLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listAreaLayoutData.minimumHeight = 150;
        listArea.setLayoutData(listAreaLayoutData);
        labelNumElem = new Label(groupArea, SWT.NONE);

        refreshListArea();
    }

    @CMOne(info = "Create dialog for CMOne")
    private void createSearchStringEditorForCMOne(Composite parent) {

        String searchString = getSearchArgument();
        if (searchString.equals("")) {
            searchString = Messages.Enter_search_string_here;
        }

        Label labelString = new Label(parent, SWT.NONE);
        labelString.setText(Messages.String_colon);

        textString = WidgetFactory.createText(parent);
        textString.setText(searchString);
        textString.setTextLimit(SearchOptions.MAX_STRING_SIZE);
        textString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textString.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                setOKButtonEnablement();
            }
        });
    }

    @CMOne(info = "Create dialog for CMOne")
    private void createCaseOptionsForCMOne(Composite parent) {

        Group groupCaseSensitive = new Group(parent, SWT.NONE);
        GridLayout editableLayout = new GridLayout();
        editableLayout.numColumns = 2;
        groupCaseSensitive.setLayout(editableLayout);
        groupCaseSensitive.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupCaseSensitive.setText(Messages.Case_sensitive);

        buttonCaseNo = WidgetFactory.createRadioButton(groupCaseSensitive);
        buttonCaseNo.setText(Messages.No);
        buttonCaseNo.setSelection(true);

        buttonCaseYes = WidgetFactory.createRadioButton(groupCaseSensitive);
        buttonCaseYes.setText(Messages.Yes);
        buttonCaseYes.setSelection(false);
    }

    public void setListBoxEnabled(boolean enabled) {
        hasListBox = enabled;
    }

    public void setOKButtonEnablement() {
        if (okButton == null) {
            return;
        }

        if (!_editor) {
            if (StringHelper.isNullOrEmpty(textString.getText())) {
                okButton.setEnabled(false);
                return;
            }
        } else {
            if (!isSearchStringValid()) {
                okButton.setEnabled(false);
                return;
            }
        }

        if (_fromColumn == 0 || _toColumn == 0 || _fromColumn > _toColumn || _toColumn > maxColumns) {
            okButton.setEnabled(false);
            return;
        }

        if (!checkElements()) {
            okButton.setEnabled(false);
            return;
        }

        okButton.setEnabled(true);
    }

    protected void setSearchOptionsEnablement(Event anEvent) {

        if (!(anEvent.data instanceof SearchOptionConfig)) {
            return;
        }

        SearchOptionConfig config = (SearchOptionConfig)anEvent.data;

        labelFromColumn.setEnabled(config.isColumnRangeEnabled());
        textFromColumn.setEnabled(config.isColumnRangeEnabled());
        labelToColumn.setEnabled(config.isColumnRangeEnabled());
        textToColumn.setEnabled(config.isColumnRangeEnabled());
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        setOKButtonEnablement();
        return control;
    }

    @Override
    protected void okPressed() {

        storeScreenValues();

        updateSearchOptions();

        isClosing = true;

        super.okPressed();
    }

    private void updateSearchOptions() {

        if (isClosing) {
            return;
        }

        if (_editor) {

            _searchOptions = new SearchOptions(_listEditor.getMatchOption(), true);
            java.util.List<SearchArgument> searchArguments = _listEditor.getSearchArguments(_fromColumn, _toColumn);
            for (SearchArgument searchArgument : searchArguments) {
                if (!StringHelper.isNullOrEmpty(searchArgument.getString())) {
                    _searchOptions.addSearchArgument(searchArgument);
                }
            }
            setElementsSearchOptions(_searchOptions);

        } else {

            setSearchArgument(textString.getText());

            if (buttonCaseNo.getSelection()) {
                _case = SearchOptions.CASE_IGNORE;
            } else {
                _case = SearchOptions.CASE_MATCH;
            }

            _searchOptions = new SearchOptions(MatchOption.ALL, true);
            _searchOptions.addSearchArgument(new SearchArgument(getSearchArgument(), getFromColumn(), getToColumn(), getCase()));
            setElementsSearchOptions(_searchOptions);

        }

        _string = _searchOptions.getCombinedSearchString();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID, Messages.OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.Cancel, false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(getTitle());
    }

    @CMOne(info = "Don`t change this method due to CMOne compatibility reasons")
    public SearchOptions getSearchOptions() {
        updateSearchOptions();
        return _searchOptions;
    }

    @CMOne(info = "Don`t change this method due to CMOne compatibility reasons")
    public String getString() {
        return _string;
    }

    public String getCombinedSearchString() {
        return _string;
    }

    private String getCase() {
        return _case;
    }

    public int getFromColumn() {
        return _fromColumn;
    }

    public int getToColumn() {
        return _toColumn;
    }

    /**
     * Overridden to make this dialog resizable.
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Overridden to provide a default size to {@link XDialog}.
     */
    @Override
    protected Point getDefaultSize() {
        return super.getDefaultSize();
    }

    /**
     * Overridden to let {@link XDialog} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return super.getDialogBoundsSettings(ISpherePlugin.getDefault().getDialogSettings());
    }

    public void handleEvent(Event anEvent) {
        setOKButtonEnablement();
        setSearchOptionsEnablement(anEvent);
    }

    private boolean isSearchStringValid() {
        java.util.List<SearchArgument> tSearchArguments = _listEditor.getSearchArguments(0, 0);
        for (SearchArgument tSearchArgument : tSearchArguments) {
            if (StringHelper.isNullOrEmpty(tSearchArgument.getString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Restores the screen values of the last search search.
     */
    private void loadScreenValues() {

        if (_editor) {
            _listEditor.loadScreenValues(getDialogBoundsSettings());
        } else {
            textString.setText(loadValue(TEXT_STRING, Messages.Enter_search_string_here));
            buttonCaseNo.setSelection(loadBooleanValue(IGNORE_CASE, false));
            buttonCaseYes.setSelection(!buttonCaseNo.getSelection());
        }

        loadColumnButtonsSelection();

        loadElementValues();
    }

    private void loadColumnButtonsSelection() {
        textFromColumn.setText(loadValue(FROM_COLUMN, "*START"));
        textToColumn.setText(loadValue(TO_COLUMN, "*END"));
    }

    /**
     * Stores the screen values that are preserved for the next search.
     */
    private void storeScreenValues() {
        if (_editor) {
            _listEditor.storeScreenValues(getDialogBoundsSettings());
        } else {
            storeValue(TEXT_STRING, textString.getText());
            storeValue(IGNORE_CASE, buttonCaseNo.getSelection());
            buttonCaseYes.setSelection(!buttonCaseNo.getSelection());
        }

        saveColumnButtonsSelection();

        saveElementValues();
    }

    private void saveColumnButtonsSelection() {
        storeValue(FROM_COLUMN, textFromColumn.getText());
        storeValue(TO_COLUMN, textToColumn.getText());
    }

    protected void refreshListArea() {

        if (listArea == null) {
            return;
        }

        listArea.setItems(getItems());
        labelNumElem.setText(Messages.Items_colon + " " + listArea.getItemCount()); //$NON-NLS-1$
    }

    protected abstract String getTitle();

    protected abstract String[] getItems();

    public abstract ArrayList<M> getSelectedElements();

    protected abstract String getSearchArgument();

    protected abstract void setSearchArgument(String argument);

    protected void createOptionsGroup(Composite container) {
    };

    protected void loadElementValues() {
    };

    protected void saveElementValues() {
    };

    protected boolean checkElements() {
        return true;
    }

    protected void setElementsSearchOptions(SearchOptions _searchOptions) {
    };

}
