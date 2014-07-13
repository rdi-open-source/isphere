/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.sourcefilesearch;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import biz.isphere.base.internal.IntHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.jface.dialogs.XDialogPage;
import biz.isphere.base.swt.widgets.NumericOnlyVerifyListener;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.ISphereHelper;
import biz.isphere.core.sourcefilesearch.SearchElement;
import biz.isphere.core.sourcefilesearch.SearchExec;
import biz.isphere.core.sourcefilesearch.SearchPostRun;
import biz.isphere.rse.ISphereRSEPlugin;
import biz.isphere.rse.Messages;

import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesMember;
import com.ibm.etools.iseries.core.dstore.common.ISeriesDataElementHelpers;
import com.ibm.etools.iseries.core.ui.widgets.IISeriesFilePromptTypes;
import com.ibm.etools.iseries.core.ui.widgets.ISeriesConnectionCombo;
import com.ibm.etools.iseries.core.ui.widgets.ISeriesMemberPrompt;
import com.ibm.etools.systems.core.ui.widgets.SystemHistoryCombo;
import com.ibm.etools.systems.dstore.core.model.DataElement;
import com.ibm.etools.systems.model.SystemConnection;

public class SourceFileSearchPage extends XDialogPage implements ISearchPage, Listener {

    private static final String START_COLUMN = "startColumn";
    private static final String END_COLUMN = "endColumn";
    private static final String SOURCE_FILE = "sourceFile";
    private static final String SOURCE_MEMBER = "sourceMember";
    private static final String LIBRARY = "library";
    private static final String SEARCH_STRING = "searchString";
    private static final String CASE_SENSITIVE = "caseSensitive";
    private static final String COLUMN_BUTTONS_SELECTION = "columnButtonsSelection";

    private ISearchPageContainer container;
    private SystemHistoryCombo searchStringCombo;
    private Button caseButton;
    private ISeriesConnectionCombo connectionCombo;
    private ISeriesMemberPrompt sourceFilePrompt;
    private Button allColumnsButton;
    private Button betweenColumnsButton;
    private Text startColumnText;
    private Text endColumnText;

    public SourceFileSearchPage() {
        super();
        return;
    }

    public void createControl(Composite aParent) {

        initializeDialogUnits(aParent);

        Composite tMainPanel = new Composite(aParent, SWT.NONE);
        tMainPanel.setLayout(new GridLayout());
        GridData tGridData = new GridData(GridData.FILL_HORIZONTAL);
        tMainPanel.setLayoutData(tGridData);

        createSearchStringGroup(tMainPanel);
        createConnectionGroup(tMainPanel);
        createSourceMemberGroup(tMainPanel);
        createColumnsGroup(tMainPanel);

        addListeners();

        loadScreenValues();

        setControl(tMainPanel);

    }

    private void createSearchStringGroup(Composite aMainPanel) {
        GridData tGridData;

        Composite tSearchStringGroup = new Composite(aMainPanel, 0);
        GridLayout tSearchStringLayout = new GridLayout(3, false);
        tSearchStringLayout.marginWidth = 0;
        tSearchStringLayout.marginHeight = 0;
        tSearchStringGroup.setLayout(tSearchStringLayout);
        tGridData = new GridData();
        tGridData.horizontalAlignment = GridData.FILL;
        tGridData.grabExcessHorizontalSpace = true;
        tGridData.widthHint = 250;
        tSearchStringGroup.setLayoutData(tGridData);

        Label tSearchStringLabel = new Label(tSearchStringGroup, SWT.LEFT);
        tSearchStringLabel.setText(Messages.Search_string);
        tSearchStringLabel.setToolTipText(Messages.String_to_be_searched);
        tGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        tSearchStringLabel.setLayoutData(tGridData);

        searchStringCombo = new SystemHistoryCombo(tSearchStringGroup, 0, "biz.isphere.core.messagefilesearch.MessageFileSearchPage.findString", 10,
            false);
        searchStringCombo.setToolTipText(Messages.Enter_or_select_search_string);
        searchStringCombo.setTextLimit(40);
        tGridData = new GridData(GridData.FILL_HORIZONTAL);
        tGridData.grabExcessHorizontalSpace = true;
        searchStringCombo.setLayoutData(tGridData);

        caseButton = new Button(tSearchStringGroup, SWT.CHECK);
        caseButton.setText(Messages.Case_sensitive);
        caseButton.setToolTipText(Messages.Specify_whether_case_should_be_considered_during_search);
        tGridData = new GridData(SWT.HORIZONTAL);
        tGridData.grabExcessHorizontalSpace = false;
        caseButton.setLayoutData(tGridData);
    }

    private void createConnectionGroup(Composite aMainPanel) {
        connectionCombo = new ISeriesConnectionCombo(aMainPanel);
        connectionCombo.getPromptLabel().setText(Messages.Connection);
    }

    private void createSourceMemberGroup(Composite aMainPanel) {
        Group tTargetGroup = createGroup(aMainPanel, Messages.Target);
        sourceFilePrompt = new ISeriesMemberPrompt(tTargetGroup, SWT.NONE, true, true, IISeriesFilePromptTypes.FILETYPE_SRC);
        sourceFilePrompt.setSystemConnection(connectionCombo.getSystemConnection());
        sourceFilePrompt.getLibraryCombo().setToolTipText(Messages.Enter_or_select_a_library_name);
        sourceFilePrompt.getObjectCombo().setToolTipText(Messages.Enter_or_select_a_simple_or_generic_message_file_name);
        sourceFilePrompt.getLibraryPromptLabel().setText(Messages.Library);
        sourceFilePrompt.setMemberPromptLabel(Messages.Source_Member);
    }

    private void createColumnsGroup(Composite aMainPanel) {
        Group tColumnsGroup = createGroup(aMainPanel, Messages.Columns);
        GridLayout tColumnsGroupLayout = new GridLayout(1, false);
        tColumnsGroupLayout.marginWidth = 0;
        tColumnsGroupLayout.marginHeight = 0;
        tColumnsGroup.setLayout(tColumnsGroupLayout);
        GridData tGridData = new GridData(GridData.FILL_VERTICAL);
        tGridData.horizontalAlignment = GridData.FILL;
        tGridData.grabExcessHorizontalSpace = true;
        tGridData.widthHint = 250;
        tColumnsGroup.setLayoutData(tGridData);

        Composite tAllColumnsPanel = new Composite(tColumnsGroup, SWT.NONE);
        GridLayout tAllColumnsLayout = new GridLayout(1, false);
        tAllColumnsPanel.setLayout(tAllColumnsLayout);
        allColumnsButton = new Button(tAllColumnsPanel, SWT.RADIO);
        allColumnsButton.setText(Messages.All_columns);
        allColumnsButton.setToolTipText(Messages.Search_all_columns);

        Composite tBetweenColumnsPanel = new Composite(tColumnsGroup, SWT.NONE);
        GridLayout tBetweenColumnsLayout = new GridLayout(4, false);
        tBetweenColumnsPanel.setLayout(tBetweenColumnsLayout);
        betweenColumnsButton = new Button(tBetweenColumnsPanel, SWT.RADIO);
        betweenColumnsButton.setText(Messages.Between);
        betweenColumnsButton.setToolTipText(Messages.Search_between_specified_columns);

        startColumnText = new Text(tBetweenColumnsPanel, SWT.BORDER);
        tGridData = new GridData();
        tGridData.widthHint = 30;
        startColumnText.setLayoutData(tGridData);
        startColumnText.setTextLimit(3);
        startColumnText.setToolTipText(Messages.Specify_start_column);

        Label tAndLabel = new Label(tBetweenColumnsPanel, SWT.LEFT);
        tAndLabel.setText(Messages.and);

        endColumnText = new Text(tBetweenColumnsPanel, SWT.BORDER);
        tGridData = new GridData();
        tGridData.widthHint = 30;
        endColumnText.setLayoutData(tGridData);
        endColumnText.setTextLimit(3);
        endColumnText.setToolTipText(Messages.Specify_end_column_max_132);
    }

    private Group createGroup(Composite aParent, String aText) {
        Group tGroup = new Group(aParent, SWT.SHADOW_ETCHED_IN);
        tGroup.setText(aText);
        GridLayout scopeLayout = new GridLayout();
        tGroup.setLayout(scopeLayout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        tGroup.setLayoutData(gd);
        return tGroup;
    }

    /**
     * Implementation of the ISearchPage" interface.
     */
    public void setContainer(ISearchPageContainer aContainer) {
        container = aContainer;
    }

    /**
     * Add listeners to verify user input.
     */
    private void addListeners() {
        searchStringCombo.getCombo().addListener(SWT.Modify, this);
        allColumnsButton.addListener(SWT.Selection, this);
        betweenColumnsButton.addListener(SWT.Selection, this);
        startColumnText.addListener(SWT.Modify, this);
        startColumnText.addVerifyListener(new NumericOnlyVerifyListener());
        endColumnText.addListener(SWT.Modify, this);
        endColumnText.addVerifyListener(new NumericOnlyVerifyListener());
    }

    /**
     * Restores the screen values of the last search search.
     */
    private void loadScreenValues() {
        searchStringCombo.setText(loadValue(SEARCH_STRING, "Enter search string here"));
        caseButton.setSelection(loadBooleanValue(CASE_SENSITIVE, false));
        sourceFilePrompt.getLibraryCombo().setText(loadValue(LIBRARY, ""));
        sourceFilePrompt.getObjectCombo().setText(loadValue(SOURCE_FILE, ""));
        sourceFilePrompt.getMemberCombo().setText(loadValue(SOURCE_MEMBER, ""));

        loadColumnButtonsSelection();
    }

    /**
     * Stores the screen values that are preserved for the next search.
     */
    private void storeScreenValues() {
        storeValue(SEARCH_STRING, getSearchString());
        storeValue(CASE_SENSITIVE, getCase());
        storeValue(LIBRARY, getSourceFileLibrary());
        storeValue(SOURCE_FILE, getSourceFile());
        storeValue(SOURCE_MEMBER, getSourceMember());

        saveColumnButtonsSelection();
    }

    /**
     * Restores the status of the "columns" buttons and text fields.
     */
    private void loadColumnButtonsSelection() {
        String tColumnButtonsSelection = loadValue(COLUMN_BUTTONS_SELECTION, "ALL");
        if ("ALL".equals(tColumnButtonsSelection)) {
            allColumnsButton.setSelection(true);
            processAllColumnsButtonSelected();
        } else {
            betweenColumnsButton.setSelection(true);
            processBetweenColumnsButtonSelected();
        }
        startColumnText.setText(loadValue(START_COLUMN, "1"));
        endColumnText.setText(loadValue(END_COLUMN, "100"));
    }

    /**
     * Saved the status of the "columns" buttons and text fields.
     */
    private void saveColumnButtonsSelection() {
        if (allColumnsButton.getSelection()) {
            storeValue(COLUMN_BUTTONS_SELECTION, "ALL");
        } else {
            storeValue(COLUMN_BUTTONS_SELECTION, "BETWEEN");
            storeValue(START_COLUMN, getNumericFieldContent(startColumnText));
            storeValue(END_COLUMN, getNumericFieldContent(endColumnText));
        }
    }

    /**
     * Returns the search string the source files are searched for.
     * 
     * @return search argument
     */
    private String getSearchString() {
        return searchStringCombo.getText();
    }

    /**
     * Checks whether the user entered a search string.
     * 
     * @return <code>true</code> if the search string is present, else
     *         <code>false</code>
     */
    private boolean isSearchStringEmpty() {
        if (getSearchString().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the simple or generic name of the libraries that are searched for
     * the source files that contain the source members.
     * 
     * @return name of the library
     */
    private String getSourceFileLibrary() {
        return sourceFilePrompt.getLibraryCombo().getText();
    }

    /**
     * Returns the simple or generic name of the source file(s) that are
     * searched for the search string.
     * 
     * @return simple or generic name of the message file
     */
    private String getSourceFile() {
        return sourceFilePrompt.getObjectCombo().getText();
    }

    /**
     * Returns the simple or generic source member name of the source file(s)
     * that are searched for the search string.
     * 
     * @return simple or generic member name of the source file
     */
    private String getSourceMember() {
        return sourceFilePrompt.getMemberCombo().getText();
    }

    /**
     * Returns the status of the "Case sensitive" check box.
     * 
     * @return status of the "Case sensitive" check box
     */
    private String getCaseAsString() {
        if (getCase()) {
            return SearchExec.CASE_MATCH;
        }
        return SearchExec.CASE_IGNORE;
    }

    /**
     * Returns the status of the "Case sensitive" check box.
     * 
     * @return status of the "Case sensitive" check box
     */
    private boolean getCase() {
        return caseButton.getSelection();
    }

    /**
     * Overridden to let {@link XDialogPage} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    protected AbstractUIPlugin getPlugin() {
        return ISphereRSEPlugin.getDefault();
    }

    /**
     * Performs the actual search search task.
     */
    public boolean performAction() {
        storeScreenValues();

        try {

            StructuredSelection tSelection = (StructuredSelection)connectionCombo.getSelection();
            SystemConnection tHost = (SystemConnection)tSelection.getFirstElement();

            ISeriesConnection tConnection = ISeriesConnection.getConnection(tHost);
            if (!ISphereHelper.checkISphereLibrary(getShell(), tConnection.getAS400ToolboxObject(getShell()))) {
                return false;
            }
            HashMap<String, SearchElement> searchElements = new HashMap<String, SearchElement>();
            Object[] tObjects = tConnection.listMembers(getShell(), getSourceFileLibrary(), getSourceFile(), getSourceMember());
            for (Object tObject : tObjects) {
                if (tObject instanceof ISeriesMember) {
                    addElement(searchElements, ((ISeriesMember)tObject).getDataElement());
                }
            }

            if (searchElements.isEmpty()) {
                MessageDialog.openInformation(getShell(), "Information", "No objects found that match the selection criteria.");
                return false;
            }

            SearchPostRun postRun = new SearchPostRun();
            postRun.setConnection(tConnection);
            postRun.setConnectionName(tConnection.getConnectionName());
            postRun.setSearchString(getSearchString());
            postRun.setSearchElements(searchElements);
            postRun.setWorkbenchWindow(PlatformUI.getWorkbench().getActiveWorkbenchWindow());

            int startColumn;
            int endColumn;
            if (allColumnsButton.getSelection()) {
                startColumn = -1;
                endColumn = -1;
            } else {
                startColumn = getNumericFieldContent(startColumnText);
                endColumn = getNumericFieldContent(endColumnText);
            }

            new SearchExec().execute(tConnection.getAS400ToolboxObject(getShell()), tConnection.getJDBCConnection(null, false), getSearchString(),
                startColumn, endColumn, getCaseAsString(), new ArrayList<SearchElement>(searchElements.values()), postRun);

        } catch (Exception e) {
            ISpherePlugin.logError(biz.isphere.core.Messages.Unexpected_Error, e);
            String message;
            if (e.getLocalizedMessage() == null) {
                message = e.getClass().getName() + " - " + getClass().getName();
            } else {
                message = e.getLocalizedMessage();
            }
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, message);
        }

        return true;
    }

    /**
     * Adds an element to the list of elements that are searched for a given
     * search string.
     * 
     * @param aSearchElements - list of elements that are searched
     * @param aSourceMember - message file that is added to the list
     */
    private void addElement(HashMap<String, SearchElement> aSearchElements, DataElement aSourceMember) {
        String tKey = ISeriesDataElementHelpers.getLibrary(aSourceMember) + "-" + ISeriesDataElementHelpers.getFile(aSourceMember) + "-"
            + ISeriesDataElementHelpers.getName(aSourceMember);
        if (!aSearchElements.containsKey(tKey)) {
            SearchElement aSearchElement = new SearchElement();
            aSearchElement.setLibrary(ISeriesDataElementHelpers.getLibrary(aSourceMember));
            aSearchElement.setFile(ISeriesDataElementHelpers.getFile(aSourceMember));
            aSearchElement.setMember(ISeriesDataElementHelpers.getName(aSourceMember));
            aSearchElement.setDescription(ISeriesDataElementHelpers.getDescription(aSourceMember));
            aSearchElements.put(tKey, aSearchElement);
        }
    }

    /**
     * Handles "modify" and "selection" events to enable/disable widgets and
     * error checking.
     */
    public void handleEvent(Event anEvent) {
        Widget widget = anEvent.widget;
        int type = anEvent.type;

        boolean result = true;

        if ((widget == searchStringCombo.getCombo()) && (type == SWT.Modify)) {
            result = !isSearchStringEmpty();

        } else if ((widget == allColumnsButton) && (type == SWT.Selection)) {
            processAllColumnsButtonSelected();
        } else if ((widget == betweenColumnsButton) && (type == SWT.Selection)) {
            processBetweenColumnsButtonSelected();
        } else if ((widget == startColumnText) && (type == SWT.Modify)) {
            result = processStartColumnTextModified();
        } else if ((widget == endColumnText) && (type == SWT.Modify)) {
            result = processEndColumnTextModified();
        }

        if (!result) {
            container.setPerformActionEnabled(false);
        } else {
            container.setPerformActionEnabled(checkAll());
        }
    }

    /**
     * Executed when the "All columns" radio button has been selected.
     */
    private void processAllColumnsButtonSelected() {
        betweenColumnsButton.setSelection(false);

        startColumnText.setEnabled(false);
        endColumnText.setEnabled(false);
    }

    /**
     * Executed when the "Between" radio button has been selected.
     */
    private void processBetweenColumnsButtonSelected() {
        allColumnsButton.setSelection(false);

        startColumnText.setEnabled(true);
        endColumnText.setEnabled(true);

        if (StringHelper.isNullOrEmpty(startColumnText.getText())) {
            startColumnText.setText("1");
        }

        if (StringHelper.isNullOrEmpty(endColumnText.getText())) {
            endColumnText.setText("132");
        }
    }

    /**
     * Executed when the value of the "Start column" text widget changes in
     * order to check the current value.
     * 
     * @return zero on success, else negative response code indicating the type
     *         of the error
     */
    private boolean processStartColumnTextModified() {
        return queryNumericFieldContent(startColumnText) == 0;
    }

    /**
     * Executed when the value of the "End column" text widget changes in order
     * to check the current value.
     * 
     * @return zero on success, else negative response code indicating the type
     *         of the error
     */
    private boolean processEndColumnTextModified() {
        return queryNumericFieldContent(endColumnText) == 0;
    }

    /**
     * Executed on every widget event to check the input values.
     * 
     * @return <code>true</code> on success, else <true>false</code>.
     */
    private boolean checkAll() {
        if (connectionCombo.getISeriesConnection() == null) {
            return false;
        }

        if (allColumnsButton.getSelection()) {
            return true;
        }

        if (betweenColumnsButton.getSelection()) {
            if (queryNumericFieldContent(startColumnText) != 0) {
                return false;
            }
            if (queryNumericFieldContent(endColumnText) != 0) {
                return false;
            }
            if (getNumericFieldContent(endColumnText) <= 132) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks a field for the following conditions:
     * <ol>
     * <li>content must not be empty (rc = -1)</li>
     * <li>content must be a numeric value (rc = -2)</li>
     * <li>numeric value must be greater zero (rc = -3)</li>
     * </ol>
     * 
     * @param aTextField - field that is checked
     * @return return 0 if the field content matches the rules, else negative
     *         value indicating the error
     */
    private int queryNumericFieldContent(Text aTextField) {
        String tText = aTextField.getText();
        if (StringHelper.isNullOrEmpty(tText)) {
            return -1; // empty
        }

        int number = 0;
        try {
            number = Integer.valueOf(tText).intValue();
            if (number <= 0) {
                return -3; // negative
            }
            return 0;
        } catch (NumberFormatException localNumberFormatException) {
            return -2; // error!
        }
    }

    /**
     * Return the content of a numeric field.
     * 
     * @param textField - numeric field widget
     * @return numeric value
     */
    private int getNumericFieldContent(Text textField) {
        return IntHelper.tryParseInt(textField.getText(), 0);
    }
}
