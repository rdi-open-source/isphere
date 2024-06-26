/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.objectsynchronization.rse;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.UIJob;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;

import biz.isphere.base.internal.DialogSettingsManager;
import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.internal.UIHelper;
import biz.isphere.base.swt.events.TableAutoSizeControlListener;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.compareeditor.SourceMemberCompareEditorConfiguration;
import biz.isphere.core.externalapi.ISynchronizeMembersEditorConfiguration;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.ibmi.contributions.extension.point.BasicQualifiedConnectionName;
import biz.isphere.core.internal.IEditor;
import biz.isphere.core.internal.ISeries;
import biz.isphere.core.internal.ISphereHelper;
import biz.isphere.core.internal.Member;
import biz.isphere.core.internal.MessageDialogAsync;
import biz.isphere.core.internal.RemoteObject;
import biz.isphere.core.internal.Size;
import biz.isphere.core.objectsynchronization.CompareOptions;
import biz.isphere.core.objectsynchronization.MemberDescription;
import biz.isphere.core.objectsynchronization.SYNCMBR_retrieveMemberAttributes;
import biz.isphere.core.objectsynchronization.SYNCMBR_retrieveMemberAttributes.MemberAttributes;
import biz.isphere.core.objectsynchronization.SynchronizationResult;
import biz.isphere.core.objectsynchronization.SynchronizeMembersEditorInput;
import biz.isphere.core.objectsynchronization.SynchronizeMembersJob;
import biz.isphere.core.objectsynchronization.TableContentProvider;
import biz.isphere.core.objectsynchronization.TableFilter;
import biz.isphere.core.objectsynchronization.TableFilterData;
import biz.isphere.core.objectsynchronization.TableSorter;
import biz.isphere.core.objectsynchronization.TableStatistics;
import biz.isphere.core.objectsynchronization.jobs.CompareMembersJob;
import biz.isphere.core.objectsynchronization.jobs.CompareMembersSharedJobValues;
import biz.isphere.core.objectsynchronization.jobs.ICancelableJob;
import biz.isphere.core.objectsynchronization.jobs.ICompareMembersPostrun;
import biz.isphere.core.objectsynchronization.jobs.ISynchronizeMembersPostRun;
import biz.isphere.core.objectsynchronization.jobs.SyncMbrMode;
import biz.isphere.core.preferences.Preferences;
import biz.isphere.core.sourcemembercopy.CopyMemberItem;
import biz.isphere.core.sourcemembercopy.ICopyItemMessageListener;
import biz.isphere.core.sourcemembercopy.IValidateItemMessageListener;
import biz.isphere.core.sourcemembercopy.MemberCopyError;
import biz.isphere.core.sourcemembercopy.SynchronizeMembersAction;
import biz.isphere.core.sourcemembercopy.ValidateMembersJob;
import biz.isphere.core.sourcemembercopy.rse.CopyMembersJob;
import biz.isphere.core.sourcemembercopy.rse.ExistingMemberAction;
import biz.isphere.core.sourcemembercopy.rse.MissingFileAction;
import biz.isphere.core.swt.widgets.HistoryCombo;
import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.core.swt.widgets.dialogs.ConfirmationMessageDialog;

public abstract class AbstractSynchronizeMembersEditor extends EditorPart
    implements ICompareMembersPostrun, ISynchronizeMembersPostRun, IValidateItemMessageListener, ICopyItemMessageListener {

    public static final String ID = "biz.isphere.core.objectsynchronization.rse.SynchronizeMembersEditor"; //$NON-NLS-1$

    private static final String CHKBOX_IGNORE_DATE = "CHKBOX_IGNORE_DATE"; //$NON-NLS-1$
    private static final String CHKBOX_RTN_CHG_ONLY = "CHKBOX_RTN_CHG_ONLY"; //$NON-NLS-1$
    private static final String BUTTON_COPY_LEFT = "BUTTON_COPY_LEFT"; //$NON-NLS-1$
    private static final String BUTTON_COPY_RIGHT = "BUTTON_COPY_RIGHT"; //$NON-NLS-1$
    private static final String BUTTON_NO_COPY = "BUTTON_NO_COPY"; //$NON-NLS-1$
    private static final String BUTTON_EQUAL = "BUTTON_EQUAL"; //$NON-NLS-1$
    private static final String BUTTON_SINGLES = "BUTTON_SINGLES"; //$NON-NLS-1$
    private static final String BUTTON_DUPLICATES = "BUTTON_DUPLICATES"; //$NON-NLS-1$
    private static final String BUTTON_COMPARE_AFTER_SYNC = "BUTTON_COMPARE_AFTER_SYNC"; //$NON-NLS-1$

    private static final String MEMBER_FILTER_HISTORY_KEY = "memberFilterHistory"; //$NON-NLS-1$
    private static final String REGEX_MARKER = "<";

    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private boolean isLeftObjectValid;
    private boolean isRightObjectValid;

    private TableViewer tableViewer;
    private TableFilter tableFilter;
    private TableFilterData filterData;
    private AbstractTableLabelProvider labelProvider;

    private Button btnCompare;
    private Button btnSynchronize;
    private Button btnCancel;
    private Button chkCompareAfterSync;
    private Button chkDisplayErrorsOnly;

    private DialogSettingsManager dialogSettingsManager;

    private Label lblLeftObject;
    private Button btnSelectLeftObject;
    private HistoryCombo cboMemberFilter;
    private Label lblRightObject;
    private Button btnSelectRightObject;

    private Button btnCopyRight;
    private Button btnEqual;
    private Button btnNoCopy;
    private Button btnCopyLeft;
    private Button btnDuplicates;
    private Button btnSingles;

    private Button chkIgnoreDate;

    private Group existingMembersActionGroup;
    private Button chkBoxError;
    private Button chkBoxReplace;
    // private Link lnkPreferences;

    private Shell shell;

    private Composite headerArea;
    private Composite optionsArea;

    private StatusLine statusLine;
    private String statusMessage;
    private int numFilteredItems;

    private CompareMembersSharedJobValues sharedValues;

    private boolean isComparing;
    private boolean isSynchronizing;
    private ICancelableJob jobToCancel;

    private SynchronizationResult synchronizationResult;

    private EditorCloseListener editorCloseListener;

    private Set<Image> disposableImages;

    public AbstractSynchronizeMembersEditor() {

        isLeftObjectValid = false;
        isRightObjectValid = false;

        dialogSettingsManager = new DialogSettingsManager(ISpherePlugin.getDefault().getDialogSettings(), getClass());
        shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        disposableImages = new HashSet<Image>();
    }

    @Override
    public void createPartControl(Composite parent) {

        parent.setLayout(new GridLayout(1, false));

        createHeaderArea(parent);
        createOptionsArea(parent);
        createCompareArea(parent);
        createrFooterArea(parent);

        loadScreenValues();

        refreshAndCheckObjectNames();
        refreshTableFilter();

        if (getEditorInput().areSameObjects()) {
            MessageDialogAsync.displayNonBlockingError(getShell(), Messages.Warning_The_left_and_right_site_display_the_same_object);
        }

        getSite().setSelectionProvider(tableViewer);

        registerEditorListener(tableViewer.getTable());
    }

    private void registerEditorListener(Table table) {

        editorCloseListener = new EditorCloseListener(this);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPartService partService = window.getPartService();
        partService.addPartListener(editorCloseListener);

        debug("Editor close liestener added.");
    }

    private void unregisterEditorListener(EditorCloseListener editorCloseListener) {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPartService partService = window.getPartService();
        partService.removePartListener(editorCloseListener);

        debug("Editor close listener removed.");
    }

    private void createHeaderArea(Composite parent) {

        headerArea = new Composite(parent, SWT.NONE);
        headerArea.setLayout(createGridLayoutNoBorder(3, false));
        headerArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite leftHeaderArea = new Composite(headerArea, SWT.NONE);
        GridData leftHeaderAreaLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        leftHeaderAreaLayoutData.minimumWidth = 120;
        leftHeaderArea.setLayoutData(leftHeaderAreaLayoutData);
        leftHeaderArea.setLayout(createGridLayoutNoBorder(2, false));

        lblLeftObject = new Label(leftHeaderArea, SWT.BORDER);
        GridData lblLeftObjectLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        lblLeftObjectLayoutData.minimumWidth = 120;
        lblLeftObject.setLayoutData(lblLeftObjectLayoutData);

        btnSelectLeftObject = WidgetFactory.createPushButton(leftHeaderArea);
        btnSelectLeftObject.setToolTipText(Messages.Tooltip_Select_object);
        btnSelectLeftObject.setImage(getImage(ISpherePlugin.IMAGE_OPEN));
        btnSelectLeftObject.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                String connectionName = null;
                String libraryName = null;
                String fileName = null;
                String objectType = null;
                RemoteObject defaultValues = getEditorInput().getLeftObject();
                if (defaultValues == null) {
                    defaultValues = getEditorInput().getRightObject();
                }
                if (defaultValues != null) {
                    connectionName = defaultValues.getConnectionName();
                    libraryName = defaultValues.getLibrary();
                    fileName = defaultValues.getName();
                    objectType = defaultValues.getObjectType();
                } else {
                    objectType = ISeries.LIB;
                }
                RemoteObject sourceFile = performSelectRemoteObject(connectionName, libraryName, fileName, objectType);
                if (sourceFile != null) {
                    isLeftObjectValid = false;
                    getEditorInput().setLeftObject(sourceFile);
                    refreshAndCheckObjectNames(); // sets: isLeftObjectValid
                } else {
                    setButtonEnablementAndDisplayCompareStatus();
                }
            }
        });

        Composite middleHeaderArea = new Composite(headerArea, SWT.NONE);
        middleHeaderArea.setLayout(createGridLayoutNoBorder(1, false));
        GridData middleHeaderAreaLayoutData = new GridData();
        middleHeaderAreaLayoutData.minimumWidth = 120;
        middleHeaderArea.setLayoutData(middleHeaderAreaLayoutData);

        cboMemberFilter = WidgetFactory.createHistoryCombo(middleHeaderArea);
        GridData cboMemberFilterLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        cboMemberFilterLayoutData.widthHint = 150;
        cboMemberFilter.setLayoutData(cboMemberFilterLayoutData);
        cboMemberFilter.setToolTipText(Messages.Tooltip_Member_name_and_source_type_filter);

        Composite rightHeaderArea = new Composite(headerArea, SWT.NONE);
        rightHeaderArea.setLayout(createGridLayoutNoBorder(2, false));
        GridData rightHeaderAreaLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        rightHeaderAreaLayoutData.minimumWidth = 120;
        rightHeaderArea.setLayoutData(rightHeaderAreaLayoutData);

        lblRightObject = new Label(rightHeaderArea, SWT.BORDER);
        GridData lblRightObjectLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        lblRightObjectLayoutData.minimumWidth = 120;
        lblRightObject.setLayoutData(lblRightObjectLayoutData);

        btnSelectRightObject = WidgetFactory.createPushButton(rightHeaderArea);
        btnSelectRightObject.setToolTipText(Messages.Tooltip_Select_object);
        btnSelectRightObject.setImage(getImage(ISpherePlugin.IMAGE_OPEN));
        btnSelectRightObject.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                String connectionName = null;
                String libraryName = null;
                String fileName = null;
                String objectType = null;
                RemoteObject defaultValues = getEditorInput().getRightObject();
                if (defaultValues == null) {
                    defaultValues = getEditorInput().getLeftObject();
                }
                if (defaultValues != null) {
                    connectionName = defaultValues.getConnectionName();
                    libraryName = defaultValues.getLibrary();
                    fileName = defaultValues.getName();
                    objectType = defaultValues.getObjectType();
                } else {
                    objectType = ISeries.LIB;
                }
                RemoteObject sourceFile = performSelectRemoteObject(connectionName, libraryName, fileName, objectType);
                if (sourceFile != null) {
                    isRightObjectValid = false;
                    getEditorInput().setRightObject(sourceFile);
                    refreshAndCheckObjectNames(); // sets: isRightObjectValid
                } else {
                    setButtonEnablementAndDisplayCompareStatus();
                }
            }
        });
    }

    private void createOptionsArea(Composite parent) {

        optionsArea = new Composite(parent, SWT.NONE);
        optionsArea.setLayout(createGridLayoutNoBorder(5, false));
        optionsArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createCompareControlsArea(optionsArea);
        createFilterOptionsArea(optionsArea);
        new Composite(optionsArea, SWT.NONE).setLayoutData(new GridData(GridData.FILL_BOTH));
        createExistingMembersActionArea(optionsArea);
        createSynchronizeControlsArea(optionsArea);
    }

    private void createCompareControlsArea(Composite parent) {

        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(createGridLayoutNoBorder(1, false));
        area.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, true));

        btnCompare = WidgetFactory.createPushButton(area);
        btnCompare.setLayoutData(createButtonLayoutData(1));
        btnCompare.setText(Messages.Compare);
        btnCompare.setToolTipText(Messages.Tooltip_start_compare_source_members);
        btnCompare.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {
                storeScreenValues();
                performCompareMembers();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        chkIgnoreDate = WidgetFactory.createCheckbox(area, Messages.Label_Ignore_date);
        chkIgnoreDate.setToolTipText(Messages.Tooltip_Ignore_date);
    }

    private void createFilterOptionsArea(Composite parent) {

        int numColumns = 5;

        Group filterOptionsGroup = new Group(parent, SWT.NONE);
        filterOptionsGroup.setLayout(createGridLayoutNoBorder(numColumns, false));
        filterOptionsGroup.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, true));
        filterOptionsGroup.setText(Messages.Display);

        filterData = new TableFilterData();

        btnCopyRight = WidgetFactory.createToggleButton(filterOptionsGroup, SWT.FLAT);
        btnCopyRight.setImage(getImage(ISpherePlugin.IMAGE_COPY_RIGHT));
        btnCopyRight.setToolTipText(Messages.Tooltip_display_copy_from_left_to_right);
        btnCopyRight.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        btnEqual = WidgetFactory.createToggleButton(filterOptionsGroup, SWT.FLAT);
        btnEqual.setImage(getImage(ISpherePlugin.IMAGE_COPY_EQUAL));
        btnEqual.setToolTipText(Messages.Tooltip_display_equal_items);
        btnEqual.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        btnNoCopy = WidgetFactory.createToggleButton(filterOptionsGroup, SWT.FLAT);
        btnNoCopy.setImage(getImage(ISpherePlugin.IMAGE_COPY_NOT_EQUAL));
        btnNoCopy.setToolTipText(Messages.Tooltip_display_unequal_items);
        btnNoCopy.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        btnCopyLeft = WidgetFactory.createToggleButton(filterOptionsGroup, SWT.FLAT);
        btnCopyLeft.setImage(getImage(ISpherePlugin.IMAGE_COPY_LEFT));
        btnCopyLeft.setToolTipText(Messages.Tooltip_display_copy_from_right_to_left);
        btnCopyLeft.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Composite displayOccurences = new Composite(filterOptionsGroup, SWT.NONE);
        displayOccurences.setLayout(new GridLayout());
        displayOccurences.setLayoutData(new GridData());

        btnDuplicates = WidgetFactory.createToggleButton(displayOccurences);
        btnDuplicates.setLayoutData(createButtonLayoutData());
        btnDuplicates.setText(Messages.Duplicates);
        btnDuplicates.setToolTipText(Messages.Tooltip_display_duplicates);
        btnDuplicates.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        btnSingles = WidgetFactory.createToggleButton(displayOccurences);
        btnSingles.setLayoutData(createButtonLayoutData());
        btnSingles.setText(Messages.Singles);
        btnSingles.setToolTipText(Messages.Tooltip_display_singles);
        btnSingles.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    private void createExistingMembersActionArea(Composite parent) {

        if (isSynchronizationEnabled()) {

            existingMembersActionGroup = new Group(parent, SWT.NONE);
            existingMembersActionGroup.setLayout(new GridLayout(1, false));
            existingMembersActionGroup.setLayoutData(new GridData(GridData.END, GridData.FILL, false, true));
            existingMembersActionGroup.setText(Messages.Label_Existing_members_action_colon);

            chkBoxError = WidgetFactory.createRadioButton(existingMembersActionGroup, Messages.Label_Error);
            chkBoxError.setLayoutData(new GridData(GridData.BEGINNING));

            chkBoxReplace = WidgetFactory.createRadioButton(existingMembersActionGroup, Messages.Label_Replace_existing_members);
            chkBoxError.setLayoutData(new GridData(GridData.BEGINNING));
        }
    }

    private void createSynchronizeControlsArea(Composite parent) {

        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(createGridLayoutNoBorder(1, false));
        area.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, true));

        if (isSynchronizationEnabled()) {

            btnSynchronize = WidgetFactory.createPushButton(area);
            btnSynchronize.setLayoutData(createButtonLayoutData(1, 1, SWT.RIGHT));
            btnSynchronize.setText(Messages.Synchronize);
            btnSynchronize.setToolTipText(Messages.Tooltip_start_synchronize_source_members);
            btnSynchronize.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent event) {
                    performSynchronizeMembers();
                }

                public void widgetDefaultSelected(SelectionEvent event) {
                }
            });
        }

        btnCancel = WidgetFactory.createPushButton(area);
        btnCancel.setLayoutData(createButtonLayoutData(1, 1, SWT.RIGHT));
        btnCancel.setText(Messages.Cancel);
        btnCancel.setToolTipText(Messages.Tooltip_cancel_operation);
        btnCancel.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {
                performCancelOperation();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        if (isSynchronizationEnabled()) {

            chkCompareAfterSync = WidgetFactory.createCheckbox(area);
            chkCompareAfterSync.setText(Messages.Compare_after_synchronization);
            chkCompareAfterSync.setToolTipText(Messages.Tooltip_Compare_after_member_synchronization);
            chkCompareAfterSync.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent paramSelectionEvent) {
                    storeScreenValues();
                }

                public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
                }
            });
        }

        chkDisplayErrorsOnly = WidgetFactory.createCheckbox(area, Messages.Errors_only);
        chkDisplayErrorsOnly.setLayoutData(createButtonLayoutData(1, 1));
        chkDisplayErrorsOnly.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                refreshTableFilter();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    private void createCompareArea(Composite parent) {

        Composite compareArea = new Composite(parent, SWT.NONE);
        compareArea.setLayout(new GridLayout(1, false));
        compareArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        tableViewer = new TableViewer(compareArea, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        /* First column is always RIGHT aligned, see bug 151342 */
        TableColumn tblClmnDummy = new TableColumn(tableViewer.getTable(), SWT.NONE);
        tblClmnDummy.setResizable(true);
        tblClmnDummy.setWidth(Size.getSize(0));

        final TableColumn tblClmnLeftLibrary = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnLeftLibrary.setText(Messages.Library);
        tblClmnLeftLibrary.setResizable(true);
        tblClmnLeftLibrary.setWidth(Size.getSize(80));

        final TableColumn tblClmnLeftFile = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnLeftFile.setText(Messages.File);
        tblClmnLeftFile.setResizable(true);
        tblClmnLeftFile.setWidth(Size.getSize(80));

        final TableColumn tblClmnLeftMember = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnLeftMember.setText(Messages.Member);
        tblClmnLeftMember.setResizable(true);
        tblClmnLeftMember.setWidth(Size.getSize(80));

        final TableColumn tblClmnLeftSourceType = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnLeftSourceType.setText(Messages.Source_Type);
        tblClmnLeftSourceType.setResizable(true);
        tblClmnLeftSourceType.setWidth(Size.getSize(60));

        final TableColumn tblClmnLeftLastChanges = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnLeftLastChanges.setText(Messages.Last_changed);
        tblClmnLeftLastChanges.setResizable(true);
        tblClmnLeftLastChanges.setWidth(Size.getSize(60));

        final TableColumn tblClmnLeftDescription = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnLeftDescription.setText(Messages.Description);
        tblClmnLeftDescription.setWidth(Size.getSize(200));

        final TableColumn tblClmnCompareResult = new TableColumn(tableViewer.getTable(), SWT.CENTER);
        tblClmnCompareResult.setResizable(true);
        tblClmnCompareResult.setWidth(Size.getSize(25));

        final TableColumn tblClmnRightLibrary = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnRightLibrary.setText(Messages.Library);
        tblClmnRightLibrary.setResizable(tblClmnLeftLibrary.getResizable());
        tblClmnRightLibrary.setWidth(tblClmnLeftLibrary.getWidth());

        final TableColumn tblClmnRightFile = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnRightFile.setText(Messages.File);
        tblClmnRightFile.setResizable(tblClmnLeftFile.getResizable());
        tblClmnRightFile.setWidth(tblClmnLeftFile.getWidth());

        final TableColumn tblClmnRightMember = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnRightMember.setText(Messages.Member);
        tblClmnRightMember.setResizable(tblClmnLeftMember.getResizable());
        tblClmnRightMember.setWidth(tblClmnLeftMember.getWidth());

        final TableColumn tblClmnRightSourceType = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnRightSourceType.setText(Messages.Source_Type);
        tblClmnRightSourceType.setResizable(tblClmnLeftSourceType.getResizable());
        tblClmnRightSourceType.setWidth(tblClmnLeftSourceType.getWidth());

        final TableColumn tblClmnRightLastChanges = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnRightLastChanges.setText(Messages.Last_changed);
        tblClmnRightLastChanges.setResizable(tblClmnLeftLastChanges.getResizable());
        tblClmnRightLastChanges.setWidth(tblClmnLeftLastChanges.getWidth());

        final TableColumn tblClmnRightDescription = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        tblClmnRightDescription.setText(Messages.Description);
        tblClmnRightDescription.setResizable(tblClmnLeftDescription.getResizable());
        tblClmnRightDescription.setWidth(tblClmnLeftDescription.getWidth());

        tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof StructuredSelection) {
                    StructuredSelection structuredSelection = (StructuredSelection)selection;
                    for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
                        Object item = (Object)iterator.next();
                        if (item instanceof MemberCompareItem) {
                            MemberCompareItem compareItem = (MemberCompareItem)item;
                            performOpenCompareMembersDialog(compareItem);
                        }
                    }
                }
            }
        });

        TableStatistics tableStatistics = new TableStatistics();
        tableFilter = new TableFilter(tableStatistics);

        tableViewer.setContentProvider(new TableContentProvider(tableStatistics));
        tableViewer.addFilter(tableFilter);
        labelProvider = getTableLabelProvider(tableViewer, 7);
        tableViewer.setLabelProvider(labelProvider);
        Menu menuTableViewerContextMenu = new Menu(tableViewer.getTable());
        menuTableViewerContextMenu.addMenuListener(new TableContextMenu(menuTableViewerContextMenu, getEditorInput().getConfiguration()));
        tableViewer.getTable().setMenu(menuTableViewerContextMenu);

        TableAutoSizeControlListener tableAutoSizeAdapter = new TableAutoSizeControlListener(tableViewer.getTable());
        tableAutoSizeAdapter.addResizableColumn(tblClmnLeftDescription, 1);
        tableAutoSizeAdapter.addResizableColumn(tblClmnRightDescription, 1);
        tableViewer.getTable().addControlListener(tableAutoSizeAdapter);

        tableViewer.setSorter(new TableSorter(SyncMbrMode.LEFT_SYSTEM));

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                String errorMessage = null;
                Object source = event.getSource();
                if (source instanceof TableViewer) {
                    TableViewer tableViewer = (TableViewer)source;
                    int countSelected = tableViewer.getTable().getSelectionCount();
                    if (countSelected == 1) {
                        TableItem tableItem = tableViewer.getTable().getItem(tableViewer.getTable().getSelectionIndex());
                        MemberCompareItem item = (MemberCompareItem)tableItem.getData();
                        errorMessage = item.getErrorMessage();
                    }
                }
                displayCompareStatus(errorMessage);
            }
        });
    }

    private void createrFooterArea(Composite parent) {

        Composite footerArea = new Composite(parent, SWT.NONE);
        footerArea.setLayout(new GridLayout(1, false));
        footerArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private GridLayout createGridLayoutNoBorder(int numColumns, boolean makeColumnsEqualWidth) {

        GridLayout layout = new GridLayout(numColumns, makeColumnsEqualWidth);
        layout.marginHeight = 0;
        layout.marginWidth = 0;

        return layout;
    }

    private GridData createButtonLayoutData() {
        return createButtonLayoutData(1);
    }

    private GridData createButtonLayoutData(int verticalSpan) {
        return createButtonLayoutData(verticalSpan, 1);
    }

    private GridData createButtonLayoutData(int verticalSpan, int horizontalSpan) {
        return createButtonLayoutData(verticalSpan, horizontalSpan, SWT.LEFT);
    }

    private GridData createButtonLayoutData(int verticalSpan, int horizontalSpan, int horizontalAlignment) {

        GridData gridData = new GridData(horizontalAlignment, SWT.TOP, false, false, 1, 1);
        gridData.widthHint = 120;
        gridData.verticalSpan = verticalSpan;
        gridData.horizontalSpan = horizontalSpan;

        return gridData;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        setSite(site);
        setInput(input);
        setPartName(input.getName());
        setTitleImage(((SynchronizeMembersEditorInput)input).getTitleImage());
    }

    @Override
    public void setFocus() {
    }

    protected Shell getShell() {
        return shell;
    }

    public SynchronizeMembersEditorInput getEditorInput() {

        IEditorInput input = super.getEditorInput();
        if (input instanceof SynchronizeMembersEditorInput) {
            return (SynchronizeMembersEditorInput)input;
        }

        return null;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        return;
    }

    @Override
    public void doSaveAs() {
        return;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isSaveOnCloseNeeded() {
        return true;
    }

    public static void openEditor(RemoteObject leftRemoteObject, RemoteObject rightRemoteObject, ISynchronizeMembersEditorConfiguration configuration)
        throws PartInitException {

        if (leftRemoteObject != null) {
            String leftConnectionName = leftRemoteObject.getConnectionName();
            if (!ISphereHelper.checkISphereLibrary(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), leftConnectionName)) {
                return;
            }
        }

        if (rightRemoteObject != null) {
            String rightConnectionName = rightRemoteObject.getConnectionName();
            if (!ISphereHelper.checkISphereLibrary(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), rightConnectionName)) {
                return;
            }
        }

        SynchronizeMembersEditorInput editorInput = new SynchronizeMembersEditorInput(leftRemoteObject, rightRemoteObject, configuration);
        UIHelper.getActivePage().openEditor(editorInput, AbstractSynchronizeMembersEditor.ID);
    }

    private void refreshAndCheckObjectNames() {

        SynchronizeMembersEditorInput editorInput = getEditorInput();
        if (editorInput != null) {
            lblLeftObject.setText(getEditorInput().getLeftObjectName());
            lblRightObject.setText(getEditorInput().getRightObjectName());
        } else {
            lblLeftObject.setText(Messages.EMPTY);
            lblRightObject.setText(Messages.EMPTY);
        }

        headerArea.layout(true);

        setButtonEnablementAndDisplayCompareStatus();
    }

    private void refreshTableFilter() {

        if (tableViewer != null) {

            // long startTime = System.currentTimeMillis();

            Object input = null;

            try {

                tableViewer.getControl().setRedraw(false);
                input = tableViewer.getInput();
                tableViewer.setInput(null);

                if (tableFilter != null) {
                    tableViewer.removeFilter(tableFilter);
                    clearTableStatistics();
                }

                if (filterData != null) {

                    filterData.setCopyLeft(btnCopyLeft.getSelection());
                    filterData.setCopyRight(btnCopyRight.getSelection());
                    filterData.setEqual(btnEqual.getSelection());
                    filterData.setNoCopy(btnNoCopy.getSelection());
                    filterData.setSingles(btnSingles.getSelection());
                    filterData.setDuplicates(btnDuplicates.getSelection());

                    if (isSynchronizationEnabled()) {
                        filterData.setErrorsOnly(chkDisplayErrorsOnly.getSelection());
                    } else {
                        filterData.setErrorsOnly(false);
                    }

                    if (tableFilter == null) {
                        tableFilter = new TableFilter(getTableStatistics());
                    }

                    clearTableStatistics();
                    tableFilter.setFilterData(filterData);
                    tableViewer.addFilter(tableFilter);
                }

                storeScreenValues();

            } finally {
                if (input != null) {
                    tableViewer.setInput(input);
                }
                tableViewer.getControl().setRedraw(true);

                setButtonEnablementAndDisplayCompareStatus();

                // System.out.println("Time in msecs: " +
                // (System.currentTimeMillis() - startTime));
            }
        }
    }

    private TableContentProvider getTableContentProvider() {

        return (TableContentProvider)tableViewer.getContentProvider();
    }

    private TableStatistics getTableStatistics() {

        return getTableContentProvider().getTableStatistics();
    }

    private void clearTableStatistics() {

        getTableStatistics().clearStatistics();
    }

    private boolean isSynchronizationEnabled() {
        ISynchronizeMembersEditorConfiguration config = getEditorInput().getConfiguration();
        return config.isLeftEditorEnabled() || config.isRightEditorEnabled();
    }

    private synchronized void setButtonEnablementAndDisplayCompareStatus() {

        boolean isCompareEnabled = true;
        boolean isSynchronizeEnabled = true;

        if (getEditorInput().getLeftObject() != null && !isLeftObjectValid) {
            String connectionName = getEditorInput().getLeftObject().getConnectionName();
            if (!ISphereHelper.checkISphereLibrary(getShell(), connectionName)) {
                isCompareEnabled = false;
                isSynchronizeEnabled = false;
                isLeftObjectValid = false;
            } else {
                isLeftObjectValid = true;
            }
        }

        if (getEditorInput().getRightObject() != null && !isRightObjectValid) {
            String connectionName = getEditorInput().getRightObject().getConnectionName();
            if (!ISphereHelper.checkISphereLibrary(getShell(), connectionName)) {
                isCompareEnabled = false;
                isSynchronizeEnabled = false;
                isRightObjectValid = false;
            } else {
                isRightObjectValid = true;
            }
        }

        if (getEditorInput().getLeftObject() == null || getEditorInput().getRightObject() == null) {
            isCompareEnabled = false;
        }

        if (tableViewer.getTable().getItems().length <= 0) {
            isSynchronizeEnabled = false;
        }

        if (isWorking()) {
            setChildrenEnabled(headerArea, false);
            setChildrenEnabled(optionsArea, false);
            isCompareEnabled = false;
        } else {
            setChildrenEnabled(headerArea, true);
            setChildrenEnabled(optionsArea, true);
        }

        if (jobToCancel == null) {
            btnCancel.setEnabled(false);
        } else {
            btnCancel.setEnabled(true);
        }

        btnCompare.setEnabled(isCompareEnabled);

        ISynchronizeMembersEditorConfiguration config = getEditorInput().getConfiguration();

        if (existingMembersActionGroup != null) {
            if (isWorking()) {
                chkBoxError.setEnabled(false);
                chkBoxReplace.setEnabled(false);
            } else {
                if (isSynchronizationEnabled()) {
                    chkBoxError.setEnabled(isSynchronizeEnabled);
                    chkBoxReplace.setEnabled(isSynchronizeEnabled);
                } else {
                    chkBoxError.setEnabled(false);
                    chkBoxReplace.setEnabled(false);
                }
            }
        }

        if (btnSynchronize != null && chkCompareAfterSync != null) {
            if (isWorking()) {
                btnSynchronize.setEnabled(false);
                chkCompareAfterSync.setEnabled(false);
            } else {
                if (isSynchronizationEnabled()) {
                    btnSynchronize.setEnabled(isSynchronizeEnabled);
                    chkCompareAfterSync.setEnabled(isSynchronizeEnabled);
                } else {
                    btnSynchronize.setEnabled(false);
                    chkCompareAfterSync.setEnabled(false);
                }
            }
        }

        if (config.isLeftSelectObjectEnabled()) {
            btnSelectLeftObject.setEnabled(true);
        } else {
            btnSelectLeftObject.setEnabled(false);
        }

        if (config.isRightSelectObjectEnabled()) {
            btnSelectRightObject.setEnabled(true);
        } else {
            btnSelectRightObject.setEnabled(false);
        }

        displayCompareStatus();
    }

    private void setChildrenEnabled(Composite parent, boolean enabled) {
        for (Control control : parent.getChildren()) {
            if (control instanceof Button) {
                control.setEnabled(enabled);
            } else if (control instanceof Composite) {
                setChildrenEnabled((Composite)control, enabled);
            }
        }
    }

    private synchronized boolean isWorking() {
        return isComparing || isSynchronizing;
    }

    private synchronized void setIsComparing(boolean isComparing) {
        this.isComparing = isComparing;
    }

    private synchronized void setIsSynchronizing(boolean isSynchronizing) {
        this.isSynchronizing = isSynchronizing;
    }

    private void displayCompareStatus() {
        displayCompareStatus(null);
    }

    private void displayCompareStatus(String errorMessage) {

        if (isWorking()) {
            statusMessage = Messages.Working;
        } else {
            TableStatistics tableStatistics = getTableStatistics();

            if (StringHelper.isNullOrEmpty(errorMessage)) {
                statusMessage = tableStatistics.toString();
            } else {
                statusMessage = errorMessage;
            }

            numFilteredItems = tableStatistics.getFilteredElements();
        }

        updateStatusLine();
    }

    public void loadHistory() {
        cboMemberFilter.load(dialogSettingsManager, MEMBER_FILTER_HISTORY_KEY);
    }

    public void updateHistory() {
        cboMemberFilter.updateHistory(cboMemberFilter.getText());
    }

    public void storeHistory() {
        cboMemberFilter.store();
    }

    /**
     * Restores the screen values of the last search search.
     */
    private void loadScreenValues() {

        chkIgnoreDate.setSelection(dialogSettingsManager.loadBooleanValue(CHKBOX_IGNORE_DATE, false));

        btnCopyLeft.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_COPY_LEFT, true));
        btnCopyRight.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_COPY_RIGHT, true));
        btnEqual.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_EQUAL, true));
        btnNoCopy.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_NO_COPY, true));
        btnSingles.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_SINGLES, true));
        btnDuplicates.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_DUPLICATES, true));

        if (isSynchronizationEnabled()) {
            chkCompareAfterSync.setSelection(dialogSettingsManager.loadBooleanValue(BUTTON_COMPARE_AFTER_SYNC, true));
            setDisplayErrorsOnly(false);
        }

        if (chkBoxError != null) {
            chkBoxError.setSelection(true);
        }

        loadHistory();

        if (StringHelper.isNullOrEmpty(cboMemberFilter.getText())) {
            cboMemberFilter.setText("*.*");
            storeHistory();
        }
    }

    /**
     * Stores the screen values that are preserved for the next search.
     */
    private void storeScreenValues() {

        dialogSettingsManager.storeValue(CHKBOX_IGNORE_DATE, chkIgnoreDate.getSelection());

        dialogSettingsManager.storeValue(BUTTON_COPY_LEFT, btnCopyLeft.getSelection());
        dialogSettingsManager.storeValue(BUTTON_COPY_RIGHT, btnCopyRight.getSelection());
        dialogSettingsManager.storeValue(BUTTON_EQUAL, btnEqual.getSelection());
        dialogSettingsManager.storeValue(BUTTON_NO_COPY, btnNoCopy.getSelection());
        dialogSettingsManager.storeValue(BUTTON_SINGLES, btnSingles.getSelection());
        dialogSettingsManager.storeValue(BUTTON_DUPLICATES, btnDuplicates.getSelection());
        if (isSynchronizationEnabled()) {
            dialogSettingsManager.storeValue(BUTTON_COMPARE_AFTER_SYNC, chkCompareAfterSync.getSelection());
        }

        updateHistory();
        storeHistory();
    }

    private MemberCompareItem[] getSelectedItems() {

        List<MemberCompareItem> selectedItems = new ArrayList<MemberCompareItem>();

        if (tableViewer.getSelection() instanceof StructuredSelection) {
            StructuredSelection selection = (StructuredSelection)tableViewer.getSelection();
            for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
                Object selectedItem = (Object)iterator.next();
                if (selectedItem instanceof MemberCompareItem) {
                    MemberCompareItem compareItem = (MemberCompareItem)selectedItem;
                    selectedItems.add(compareItem);
                }
            }
        }

        return selectedItems.toArray(new MemberCompareItem[selectedItems.size()]);
    }

    private void changeCompareStatus(int newStatus) {

        MemberCompareItem[] selectedItems = getSelectedItems();

        for (MemberCompareItem compareItem : selectedItems) {
            compareItem.setCompareStatus(newStatus, this.sharedValues.getCompareOptions());
            tableViewer.update(compareItem, null);
        }
        tableViewer.getTable().redraw();
        setButtonEnablementAndDisplayCompareStatus();
    }

    private void performEditMember(MemberDescription memberDescription, MemberCompareItem parent, Rectangle editorBounds) {

        String connectionName = memberDescription.getConnectionName();
        String libraryName = memberDescription.getLibraryName();
        String fileName = memberDescription.getFileName();
        String memberName = memberDescription.getMemberName();

        IEditor editor = ISpherePlugin.getEditor();
        if (editor != null) {
            editor.openEditor(connectionName, libraryName, fileName, memberName, IEditor.EDIT);
            IEditorPart editorPart = editor.findEditorPart(connectionName, libraryName, fileName, memberName);
            if (editorPart != null) {
                editorCloseListener.addMember(editorPart, new WatchedMember(memberDescription, parent));
                if (Preferences.getInstance().isSyncMembersEditorDetached()) {
                    UIHelper.detachEditor(editorPart, editorBounds);
                }
            }
        }
    }

    private void performDisplayMember(MemberDescription memberDescription, Rectangle editorBounds) {

        String connectionName = memberDescription.getConnectionName();
        String libraryName = memberDescription.getLibraryName();
        String fileName = memberDescription.getFileName();
        String memberName = memberDescription.getMemberName();

        IEditor editor = ISpherePlugin.getEditor();
        if (editor != null) {
            editor.openEditor(connectionName, libraryName, fileName, memberName, IEditor.EDIT);
            IEditorPart editorPart = editor.findEditorPart(connectionName, libraryName, fileName, memberName);
            if (editorPart != null) {
                editorCloseListener.addMember(editorPart, new WatchedMember(memberDescription, null));
                if (Preferences.getInstance().isSyncMembersEditorDetached()) {
                    UIHelper.detachEditor(editorPart, editorBounds);
                }
            }
        }
    }

    private Rectangle getEditorBounds(Shell shell, int side) {

        Rectangle drawingArea;

        boolean centerOnScreen = Preferences.getInstance().isSyncMembersCenterOnScreen();
        boolean sideBySide = Preferences.getInstance().isSyncMembersSideBySide();

        Rectangle monitorArea = shell.getMonitor().getClientArea();

        if (centerOnScreen || shell.getMaximized()) {
            drawingArea = shell.getMonitor().getClientArea();
        } else {
            drawingArea = shell.getBounds();
        }

        Rectangle editorBounds = getEditorBoundsInternal(side, monitorArea, drawingArea, sideBySide);

        // Check left and right edges
        int diffX = 0;
        if (side == LEFT) {
            Rectangle leftEditorBounds = editorBounds;
            Rectangle rightEditorBounds = getEditorBoundsInternal(RIGHT, monitorArea, drawingArea, sideBySide);
            if (leftEditorBounds.x < monitorArea.x) {
                diffX = leftEditorBounds.x - monitorArea.x;
            } else if (rightEditorBounds.x + leftEditorBounds.width > monitorArea.width + monitorArea.x) {
                diffX = (rightEditorBounds.x + leftEditorBounds.width) - (monitorArea.width + monitorArea.x);
            }
        } else {
            Rectangle leftEditorBounds = getEditorBoundsInternal(LEFT, monitorArea, drawingArea, sideBySide);
            Rectangle rightEditorBounds = editorBounds;
            if (rightEditorBounds.x + rightEditorBounds.width > monitorArea.width + monitorArea.x) {
                diffX = (rightEditorBounds.x + rightEditorBounds.width) - (monitorArea.width + monitorArea.x);
            } else if (leftEditorBounds.x < monitorArea.x) {
                diffX = leftEditorBounds.x - monitorArea.x;
            }
        }

        editorBounds.x = editorBounds.x - diffX;

        return editorBounds;
    }

    private Rectangle getEditorBoundsInternal(int side, Rectangle monitorArea, Rectangle drawingArea, boolean sideBySide) {

        int editorHeight;
        int editorWidth;
        int editorPosX;
        int editorPosY;

        // Set preferred editor dimensions
        final int maxHeight = (int)Math.abs(drawingArea.height * .8);
        final int maxWidth = (int)Math.abs(drawingArea.width * .8);

        editorHeight = 900;
        if (editorHeight > maxHeight) {
            debug("Max. height exceeded: " + editorHeight);
            editorHeight = maxHeight;
        }
        editorWidth = 800;
        if (editorWidth > maxWidth) {
            debug("Max. width exceeded: " + editorWidth);
            editorWidth = maxWidth;
        }

        int minHeight = 450;
        if (editorHeight < minHeight) {
            debug("Min. height succeeded: " + editorHeight);
            editorHeight = minHeight;
        }
        int minWidth = 600;
        if (editorWidth < minWidth) {
            debug("Min. width succeeded: " + editorWidth);
            editorWidth = minWidth;
        }

        // Ensure editor fits on screen
        final int maxMonitorHeight = (int)Math.abs(monitorArea.height * .9);
        final int maxMonitorWidth = (int)Math.abs(monitorArea.width * .9);

        if (sideBySide) {
            if (editorWidth * 2 > maxMonitorWidth) {
                editorWidth = (int)Math.abs(maxMonitorWidth / 2);
            }
        } else {
            if (editorWidth > maxMonitorWidth) {
                editorWidth = maxMonitorWidth;
            }
        }

        if (editorHeight > maxMonitorHeight) {
            editorHeight = (int)Math.abs(maxMonitorHeight * .9);
        }

        // Position editor on screen
        if (sideBySide) {
            if (side == LEFT) {
                editorPosX = Math.abs(drawingArea.width / 2) - Math.abs(editorWidth / 1);
            } else {
                editorPosX = Math.abs(drawingArea.width / 2);
            }
        } else {
            editorPosX = Math.abs(drawingArea.width / 2) - Math.abs(editorWidth / 2);
        }

        editorPosY = Math.abs(drawingArea.height / 2) - Math.abs(editorHeight / 2);

        // Add monitor offset
        editorPosX = editorPosX + drawingArea.x;
        editorPosY = editorPosY + drawingArea.y;

        return new Rectangle(editorPosX, editorPosY, editorWidth, editorHeight);
    }

    private boolean performDeleteMember(MemberDescription memberDescription) {

        try {

            AS400 system = IBMiHostContributionsHandler.getSystem(memberDescription.getConnectionName());
            String libraryName = memberDescription.getLibraryName();
            String fileName = memberDescription.getFileName();
            String memberName = memberDescription.getMemberName();

            if (!ISphereHelper.checkMember(system, libraryName, fileName, memberName)) {
                return true;
            }

            String command = String.format("RMVM FILE(%s/%s) MBR(%s)", libraryName, fileName, memberName); //$NON-NLS-1$

            List<AS400Message> rtnMessages = new LinkedList<AS400Message>();
            String errorMessageId = ISphereHelper.executeCommand(system, command, rtnMessages);
            if (!StringHelper.isNullOrEmpty(errorMessageId)) {
                ISphereHelper.displayCommandExecutionError(getShell(), command, rtnMessages);
                return false;
            }

            return true;

        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(e));
        }

        return false;
    }

    private void performCompareMembers() {

        final SynchronizeMembersEditorInput editorInput = getEditorInput();

        if (editorInput.getLeftObject() == null) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Left_source_file_or_library_is_missing);
            return;
        }

        if (editorInput.getRightObject() == null) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Right_source_file_or_library_is_missing);
            return;
        }

        if (!editorInput.getLeftObject().getObjectType().equals(editorInput.getRightObject().getObjectType())) {
            MessageDialog.openError(shell, Messages.E_R_R_O_R, Messages.Invalid_selection_Objects_must_be_of_the_same_type);
            return;
        }

        if (editorInput.getLeftObjectName().equals(editorInput.getRightObjectName())) {
            MessageDialog dialog = new MessageDialog(getShell(), Messages.Warning, null, Messages.Warning_Both_sides_show_the_same_source_members,
                MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
            if (dialog.open() == 1) {
                return;
            }
        }

        tableViewer.setInput(getEditorInput().clearAll());

        if (isSynchronizationEnabled()) {
            setDisplayErrorsOnly(false);
        }

        setIsComparing(true);
        setButtonEnablementAndDisplayCompareStatus();

        sharedValues = createSharedValues();

        jobToCancel = new CompareMembersJob(getEditorInput(), sharedValues, this);
        setButtonEnablementAndDisplayCompareStatus();
        jobToCancel.schedule();
    }

    public void compareMembersPostRun(boolean isCanceled, MemberDescription[] leftMemberDescriptions, MemberDescription[] rightMemberDescriptions) {

        if (isCanceled) {
            getEditorInput().setLeftMemberDescriptions(new MemberDescription[0]);
            getEditorInput().setRightMemberDescriptions(new MemberDescription[0]);
        } else {
            getEditorInput().setLeftMemberDescriptions(leftMemberDescriptions);
            getEditorInput().setRightMemberDescriptions(rightMemberDescriptions);
        }

        jobToCancel = null;

        EndLoadMembersUIJob job = new EndLoadMembersUIJob(isCanceled);
        job.schedule();
    }

    private CompareMembersSharedJobValues createSharedValues() {

        sharedValues = new CompareMembersSharedJobValues(new CompareOptions());
        updateCompareOptions();

        return sharedValues;
    }

    protected void updateCompareOptions() {

        if (sharedValues == null) {
            return;
        }

        CompareOptions compareOptions = sharedValues.getCompareOptions();

        boolean ignoreDate = chkIgnoreDate.getSelection();
        String memberFilter = cboMemberFilter.getText();

        boolean isRegEx;
        if (memberFilter.startsWith(REGEX_MARKER)) {
            isRegEx = true;
            memberFilter = memberFilter.substring(1);
        } else {
            isRegEx = false;
        }

        compareOptions.setIgnoreDate(ignoreDate);
        compareOptions.setMemberFilter(memberFilter);
        compareOptions.setIsRegEx(isRegEx);

        labelProvider.setCompareOptions(sharedValues.getCompareOptions());
        tableFilter.setCompareOptions(sharedValues.getCompareOptions());
    }

    private void performSynchronizeMembers() {

        synchronizationResult = new SynchronizationResult();

        RemoteObject leftObject = getEditorInput().getLeftObject();
        RemoteObject rightObject = getEditorInput().getRightObject();

        SynchronizeMembersJob synchronizeMembersJob = new SynchronizeMembersJob(leftObject, rightObject, this);
        synchronizeMembersJob.setValidateItemErrorListener(this);
        synchronizeMembersJob.setCopyItemErrorListener(this);
        synchronizeMembersJob.setMissingFileAction(MissingFileAction.ASK_USER);

        if (chkBoxReplace.getSelection()) {
            synchronizeMembersJob.setExistingMemberAction(ExistingMemberAction.REPLACE);
        } else {
            synchronizeMembersJob.setExistingMemberAction(ExistingMemberAction.ERROR);
        }

        CompareOptions compareOptions = sharedValues.getCompareOptions();
        for (int i = 0; i < tableViewer.getTable().getItemCount(); i++) {
            MemberCompareItem compareItem = (MemberCompareItem)tableViewer.getTable().getItem(i).getData();
            if (compareItem.getOriginalCompareStatus(compareOptions) == MemberCompareItem.LEFT_MISSING) {
                synchronizeMembersJob.addCopyRightToLeftMember(compareItem);
            } else if (compareItem.getOriginalCompareStatus(compareOptions) == MemberCompareItem.RIGHT_MISSING) {
                synchronizeMembersJob.addCopyLeftToRightMember(compareItem);
            }
        }

        if (synchronizeMembersJob.getNumCopyLeftToRight() == 0 && synchronizeMembersJob.getNumCopyRightToLeft() == 0) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.No_items_selected_for_processing);
            return;
        }

        String leftToRight = Messages.bind(Messages.Copy_A_source_members_from_left_to_right, synchronizeMembersJob.getNumCopyLeftToRight());
        String rightToLeft = Messages.bind(Messages.Copy_A_source_members_from_right_to_left, synchronizeMembersJob.getNumCopyRightToLeft());

        if (MessageDialog.openConfirm(getShell(), Messages.Confirmation,
            Messages.Do_you_want_to_start_synchronizing_members + "\n\n" + leftToRight + "\n" + rightToLeft)) { // //$NON-NLS-1$ //$NON-NLS-2$
            setIsSynchronizing(true);
            setButtonEnablementAndDisplayCompareStatus();
            jobToCancel = synchronizeMembersJob;
            setButtonEnablementAndDisplayCompareStatus();
            synchronizeMembersJob.schedule();
        }
    }

    /**
     * Member error callback of {@link ValidateMembersJob}.
     * <p>
     * {@inheritDoc}
     */
    public SynchronizeMembersAction reportValidateMemberMessage(MemberCopyError errorId, CopyMemberItem item, String errorMessage) {

        debug("ValidateMembersJob -> Validation error: " + item.getFromMember() + " - " + errorMessage);

        final MemberCompareItem compareItem = (MemberCompareItem)item.getData();

        compareItem.resetErrorStatus();

        if (MemberCopyError.ERROR_NONE == errorId) {
            // Nothing to do here.
            // Let the CopyMembersJob decide what to do.
        } else {
            compareItem.setErrorStatus(errorMessage, sharedValues.getCompareOptions());
            synchronizationResult.addErrorMessage(errorMessage);
        }

        synchronizationResult.addDirtyMember(compareItem);

        return errorId.getDefaultAction();
    }

    /**
     * Member error callback of {@link CopyMembersJob}.
     * <p>
     * {@inheritDoc}
     */
    public SynchronizeMembersAction reportCopyMemberMessage(MemberCopyError errorId, CopyMemberItem item, String errorMessage) {

        // File or library error.
        if (item == null) {
            return errorId.getDefaultAction();
        }

        if (MemberCopyError.ERROR_NONE == errorId) {
            debug("CopyMembersJob -> Copy error: " + item.getFromFile() + "." + item.getFromMember() + " - " + errorMessage);
        } else {
            debug("CopyMembersJob -> Copied: " + item.getFromFile() + "." + item.getFromMember());
        }

        final MemberCompareItem compareItem = (MemberCompareItem)item.getData();

        compareItem.resetErrorStatus();

        // Update copy status...
        if (MemberCopyError.ERROR_NONE == errorId) {
            MemberCompareItem memberCompareItem = (MemberCompareItem)item.getData();
            if (compareItem.getCompareStatus(sharedValues.getCompareOptions()) == MemberCompareItem.LEFT_MISSING) {
                MemberDescription rightMemberDescription = memberCompareItem.getRightMemberDescription();
                String connectionName = getEditorInput().getLeftObject().getConnectionName();
                String libraryName = item.getToLibrary();
                String fileName = item.getToFile();
                String memberName = item.getToMember();
                String srcType = item.getToSrcType();
                Timestamp lastChanged = rightMemberDescription.getLastChangedDate();
                Long checksum = rightMemberDescription.getChecksum();
                String text = rightMemberDescription.getText();
                compareItem.setLeftMemberDescription(connectionName, libraryName, fileName, memberName, srcType, lastChanged, checksum, text);
            } else if (compareItem.getCompareStatus(sharedValues.getCompareOptions()) == MemberCompareItem.RIGHT_MISSING) {
                MemberDescription leftMemberDescription = memberCompareItem.getLeftMemberDescription();
                String connectionName = getEditorInput().getRightObject().getConnectionName();
                String libraryName = item.getToLibrary();
                String fileName = item.getToFile();
                String memberName = item.getToMember();
                String srcType = item.getToSrcType();
                Timestamp lastChanged = leftMemberDescription.getLastChangedDate();
                Long checksum = leftMemberDescription.getChecksum();
                String text = leftMemberDescription.getText();
                compareItem.setRightMemberDescription(connectionName, libraryName, fileName, memberName, srcType, lastChanged, checksum, text);
            }
            compareItem.clearCompareStatus();
        } else {
            compareItem.setErrorStatus(errorMessage, sharedValues.getCompareOptions());
            synchronizationResult.addErrorMessage(errorMessage);
        }

        synchronizationResult.addDirtyMember(compareItem);

        return errorId.getDefaultAction();
    }

    /**
     * PostRun of {@link SynchronizeMembersJob}.
     * <p>
     * {@inheritDoc}
     */
    public void synchronizeMembersPostRun(final String status, final int countCopied, final int countErrors, final String message) {

        synchronizationResult.setStatus(status);
        synchronizationResult.setCountCopied(countCopied);
        synchronizationResult.setCountErrors(countErrors);
        synchronizationResult.setJobFinishedMessage(message);

        UIJob uiJob = new EndSynchronisationUIJob(synchronizationResult);
        uiJob.schedule();

        debug("\nSynchronizeMembersEditor.synchronizeMembersPostRun:");
        debug("status:         " + status);
        debug("copied #:       " + countCopied);
        debug("errors #:       " + countErrors);
        debug("message:        " + message);
    }

    protected void performOpenCompareMembersDialog(MemberCompareItem compareItem) {

        if (compareItem.isSingle()) {
            return;
        }

        try {

            MemberDescription leftMemberDescription = compareItem.getLeftMemberDescription();
            MemberDescription rightMemberDescription = compareItem.getRightMemberDescription();

            Member leftMember = createRemoteObject(leftMemberDescription);
            Member rightMember = createRemoteObject(rightMemberDescription);

            SourceMemberCompareEditorConfiguration cc = new SourceMemberCompareEditorConfiguration();
            cc.setIgnoreCase(false);
            cc.setIgnoreChangesLeft(false);
            cc.setIgnoreChangesRight(false);
            cc.setConsiderDate(false);
            cc.setThreeWay(false);
            cc.setLeftEditable(false);
            cc.setRightEditable(false);
            cc.setLeftLabel(createLabel(leftMember));
            cc.setRightLabel(createLabel(rightMember));
            cc.setOpenInEditor(false);
            cc.setShowDialog(false);

            List<Member> members = new LinkedList<Member>();
            members.add(leftMember);
            members.add(rightMember);

            IBMiHostContributionsHandler.compareSourceMembers(members, cc);

        } catch (Throwable e) {
            ISpherePlugin.logError("*** Clould not open source member compare editor ***", e); //$NON-NLS-1$
        }
    }

    private String createLabel(Member member) {

        StringBuilder buffer = new StringBuilder();

        buffer.append(member.getLibrary());
        buffer.append("/");
        buffer.append(member.getSourceFile());
        buffer.append("(");
        buffer.append(member.getMember());
        buffer.append(")");

        return buffer.toString();
    }

    private Member createRemoteObject(MemberDescription memberDescription) throws Exception {

        String connectionName = memberDescription.getConnectionName();
        String fileName = memberDescription.getFileName();
        String libraryName = memberDescription.getLibraryName();
        String memberName = memberDescription.getMemberName();

        Member remoteObject = IBMiHostContributionsHandler.getMember(connectionName, libraryName, fileName, memberName);

        return remoteObject;
    }

    private void performCancelOperation() {

        if (jobToCancel != null) {
            jobToCancel.cancelOperation();
        }
    }

    private void setDisplayErrorsOnly(boolean enabled) {
        chkDisplayErrorsOnly.setSelection(enabled);
        refreshTableFilter();
    }

    @Override
    public void dispose() {

        if (jobToCancel != null) {
            jobToCancel.cancelOperation();
        }

        disposeImages();

        super.dispose();
    }

    private void disposeImages() {

        for (Image image : disposableImages) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
    }

    private Image getImage(String name) {

        Image image = ISpherePlugin.getDefault().getImage(name);
        disposableImages.add(image);

        return image;
    }

    protected abstract RemoteObject performSelectRemoteObject(String connectionName, String libraryName, String objectName, String objectType);

    protected abstract AbstractTableLabelProvider getTableLabelProvider(TableViewer tableViewer, int columnIndex);

    private class WatchedMember {

        private MemberDescription memberDescription;
        private MemberCompareItem parent;

        public WatchedMember(MemberDescription memberDescription, MemberCompareItem parent) {
            this.memberDescription = memberDescription;
            this.parent = parent;
        }

        public MemberDescription getMemberDescription() {
            return memberDescription;
        }

        public MemberCompareItem getParent() {
            return parent;
        }
    }

    private class EditorCloseListener implements IPartListener2 {

        private AbstractSynchronizeMembersEditor owner;
        Map<IEditorPart, WatchedMember> members;

        public EditorCloseListener(AbstractSynchronizeMembersEditor editor) {
            this.owner = editor;
            this.members = new HashMap<IEditorPart, WatchedMember>();
        }

        public void addMember(IEditorPart editorPart, WatchedMember watchedMember) {
            members.put(editorPart, watchedMember);
            debug("Member added: " + watchedMember.getMemberDescription().getQualifiedMemberName());
        }

        public void removeMember(IEditorPart editorPart) {
            WatchedMember watchedMember = members.remove(editorPart);
            debug("Member removed: " + watchedMember.getMemberDescription().getQualifiedMemberName());
        }

        public void partClosed(IWorkbenchPartReference partRef) {

            IWorkbenchPart closedPart = partRef.getPart(false);
            if (closedPart == owner) {
                unregisterEditorListener(this);
                for (Object entry : members.keySet().toArray()) {
                    IEditorPart editorPart = (IEditorPart)entry;
                    closeEditor(editorPart);
                }
            } else {
                if (closedPart instanceof IEditorPart) {
                    IEditorPart closedEditorPart = (IEditorPart)closedPart;
                    WatchedMember watchedMember = members.get(closedEditorPart);
                    updateAndRemoveWatchedMember(closedEditorPart, watchedMember);
                }
            }
        }

        private void closeEditor(IEditorPart editorPart) {
            editorPart.getEditorSite().getPage().closeEditor(editorPart, true);
        }

        private void updateAndRemoveWatchedMember(IEditorPart closedEditorPart, WatchedMember watchedMember) {
            if (watchedMember != null) {
                MemberDescription memberDescription = watchedMember.getMemberDescription();
                MemberCompareItem memberCompareItem = watchedMember.getParent();
                if (memberCompareItem != null) {
                    performUpdateMemberDescription(memberDescription, memberCompareItem);
                }
                removeMember(closedEditorPart);
            }
        }

        private void performUpdateMemberDescription(MemberDescription memberDescription, MemberCompareItem parent) {

            String connectionName = memberDescription.getConnectionName();
            AS400 system = IBMiHostContributionsHandler.getSystem(connectionName);
            String iSphereLibrary = ISpherePlugin.getISphereLibrary(connectionName);

            if (ISphereHelper.checkISphereLibrary(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), connectionName)) {

                String currentLibrary = null;
                try {
                    currentLibrary = ISphereHelper.getCurrentLibrary(system);
                } catch (Exception e) {
                    ISpherePlugin.logError("*** Could not retrieve current library ***", e); //$NON-NLS-1$
                }

                if (currentLibrary != null) {

                    try {

                        boolean ok = false;
                        try {
                            ok = ISphereHelper.setCurrentLibrary(system, iSphereLibrary);
                        } catch (Exception e) {
                            ISpherePlugin.logError("Could not set current library to: " + iSphereLibrary, e); //$NON-NLS-1$
                        }

                        if (ok) {
                            String library = memberDescription.getLibraryName();
                            String file = memberDescription.getFileName();
                            String member = memberDescription.getMemberName();
                            MemberAttributes memberAttributes = new SYNCMBR_retrieveMemberAttributes().run(system, library, file, member);
                            if (memberAttributes != null) {
                                memberDescription.setLastChangedDate(memberAttributes.getLastChanged());
                                memberDescription.setChecksum(memberAttributes.getCheckSum());
                                parent.clearCompareStatus();
                                owner.tableViewer.refresh(parent);
                                debug("Member updated: " + memberDescription.getQualifiedMemberName());
                            }
                        }

                    } finally {
                        try {
                            ISphereHelper.setCurrentLibrary(system, currentLibrary);
                        } catch (Exception e) {
                            ISpherePlugin.logError("Could not restore current library to: " + currentLibrary, e); //$NON-NLS-1$
                        }
                    }

                }
            }
        }

        public void partActivated(IWorkbenchPartReference arg0) {
        }

        public void partBroughtToTop(IWorkbenchPartReference arg0) {
        }

        public void partDeactivated(IWorkbenchPartReference arg0) {
        }

        public void partHidden(IWorkbenchPartReference arg0) {
        }

        public void partInputChanged(IWorkbenchPartReference arg0) {
        }

        public void partOpened(IWorkbenchPartReference arg0) {
        }

        public void partVisible(IWorkbenchPartReference arg0) {
        }
    }

    private class EndLoadMembersUIJob extends UIJob {

        private boolean isCanceled;

        public EndLoadMembersUIJob(boolean isCanceled) {
            super(Messages.EMPTY);
            this.isCanceled = isCanceled;
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor arg0) {

            if (tableViewer.getTable().isDisposed()) {
                return Status.OK_STATUS;
            }

            tableViewer.setInput(getEditorInput());
            setIsComparing(false);
            setButtonEnablementAndDisplayCompareStatus();

            if (isCanceled) {
                MessageDialogAsync.displayNonBlockingInformation(getShell(), Messages.Operation_has_been_canceled_by_the_user);
            }

            return Status.OK_STATUS;
        }
    }

    private class EndSynchronisationUIJob extends UIJob {

        private SynchronizationResult result;

        public EndSynchronisationUIJob(SynchronizationResult result) {
            super(Messages.EMPTY);

            this.result = result;
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {

            debug("-- End synchronisation job: --");

            debug("Copied #: " + result.getCountCopied());
            debug("Errors #: " + result.getCountErrors());

            debug("Errors:");
            String[] errorMessages = synchronizationResult.getErrorMesssages();
            for (String message : errorMessages) {
                debug("   " + message);
            }

            if (!ISynchronizeMembersPostRun.OK.equals(result.getStatus())) {
                MessageDialogAsync.displayNonBlockingError(getShell(), result.getJobFinishedMessage());
            } else {
                MessageDialogAsync.displayNonBlockingInformation(getShell(), result.getJobFinishedMessage());
            }

            if (tableViewer.getTable().isDisposed()) {
                return Status.OK_STATUS;
            }

            jobToCancel = null;
            setIsSynchronizing(false);
            setButtonEnablementAndDisplayCompareStatus();

            if (isSynchronizationEnabled()) {

                if (result.getCountErrors() == 0 && chkCompareAfterSync.getSelection()) {
                    performCompareMembers();
                } else {
                    if (synchronizationResult.hasDirtyMembers()) {
                        try {
                            tableViewer.getTable().setRedraw(false);
                            for (MemberCompareItem memberCompareItem : synchronizationResult.getDirtyMembers()) {
                                tableViewer.refresh(memberCompareItem);
                            }
                        } finally {
                            tableViewer.getTable().setRedraw(true);
                        }
                    }
                }
            }

            synchronizationResult = null;

            return Status.OK_STATUS;
        }
    }

    /**
     * Class that implements the context menu for the table rows.
     */
    private class TableContextMenu extends MenuAdapter {

        private Menu parent;
        private ISynchronizeMembersEditorConfiguration configuration;

        private MenuItem menuItemRemoveSelection;
        private MenuItem menuItemSelectForCopyingToTheRight;
        private MenuItem menuItemSelectForCopyingToTheLeft;
        private MenuItem menuItemEditLeft;
        private MenuItem menuItemEditRight;
        private MenuItem menuItemCompareLeftAndRight;
        private MenuItem menuItemSeparator;
        private MenuItem menuItemDeleteLeft;
        private MenuItem menuItemDeleteRight;

        public TableContextMenu(Menu parent, ISynchronizeMembersEditorConfiguration configuration) {
            this.parent = parent;
            this.configuration = configuration;
        }

        @Override
        public void menuShown(MenuEvent event) {
            destroyMenuItems();
            createMenuItems();
        }

        private void destroyMenuItems() {
            if (!((menuItemRemoveSelection == null) || (menuItemRemoveSelection.isDisposed()))) {
                menuItemRemoveSelection.dispose();
            }
            if (!((menuItemSelectForCopyingToTheRight == null) || (menuItemSelectForCopyingToTheRight.isDisposed()))) {
                menuItemSelectForCopyingToTheRight.dispose();
            }
            if (!((menuItemSelectForCopyingToTheLeft == null) || (menuItemSelectForCopyingToTheLeft.isDisposed()))) {
                menuItemSelectForCopyingToTheLeft.dispose();
            }
            if (!((menuItemEditLeft == null) || (menuItemEditLeft.isDisposed()))) {
                menuItemEditLeft.dispose();
            }
            if (!((menuItemEditRight == null) || (menuItemEditRight.isDisposed()))) {
                menuItemEditRight.dispose();
            }
            if (!((menuItemCompareLeftAndRight == null) || (menuItemCompareLeftAndRight.isDisposed()))) {
                menuItemCompareLeftAndRight.dispose();
            }
            if (!((menuItemSeparator == null) || (menuItemSeparator.isDisposed()))) {
                menuItemSeparator.dispose();
            }
            if (!((menuItemDeleteLeft == null) || (menuItemDeleteLeft.isDisposed()))) {
                menuItemDeleteLeft.dispose();
            }
            if (!((menuItemDeleteRight == null) || (menuItemDeleteRight.isDisposed()))) {
                menuItemDeleteRight.dispose();
            }
        }

        private void createMenuItems() {

            if (tableViewer.getTable().getItems().length <= 0) {
                return;
            }

            boolean isLeftEditorEnabled = configuration.isLeftEditorEnabled();
            boolean isRightEditorEnabled = configuration.isRightEditorEnabled();

            createMenuItemRemoveSelection();

            if (isLeftEditorEnabled) createMenuItemSelectForCopyingToTheLeft(getTheSelectedItem());
            if (isRightEditorEnabled) createMenuItemSelectForCopyingToTheRight(getTheSelectedItem());

            createMenuItemEditLeft(getTheSelectedItem(), isLeftEditorEnabled);
            createMenuItemEditRight(getTheSelectedItem(), isRightEditorEnabled);

            createMenuItemCompareLeftAndRight(getTheSelectedItem());

            if (isLeftEditorEnabled || isRightEditorEnabled) createMenuItemSeparator();

            if (isLeftEditorEnabled) createMenuItemDeleteLeft(getTheSelectedItem());
            if (isRightEditorEnabled) createMenuItemDeleteRight(getTheSelectedItem());
        }

        private void createMenuItemSeparator() {
            menuItemSeparator = new MenuItem(parent, SWT.SEPARATOR);
        }

        private void createMenuItemRemoveSelection() {
            menuItemRemoveSelection = new MenuItem(parent, SWT.NONE);
            menuItemRemoveSelection.setText(Messages.Remove_selection);
            menuItemRemoveSelection.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    changeCompareStatus(MemberCompareItem.NO_ACTION);
                }
            });
        }

        private void createMenuItemSelectForCopyingToTheLeft(MemberCompareItem compareItem) {
            menuItemSelectForCopyingToTheLeft = new MenuItem(parent, SWT.NONE);
            menuItemSelectForCopyingToTheLeft.setText(Messages.Select_for_copying_right_to_left);
            menuItemSelectForCopyingToTheLeft.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    changeCompareStatus(MemberCompareItem.LEFT_MISSING);
                }
            });

            if (compareItem != null && compareItem.getRightMemberDescription() == null) {
                menuItemSelectForCopyingToTheLeft.setEnabled(false);
            }
        }

        private void createMenuItemSelectForCopyingToTheRight(MemberCompareItem compareItem) {
            menuItemSelectForCopyingToTheRight = new MenuItem(parent, SWT.NONE);
            menuItemSelectForCopyingToTheRight.setText(Messages.Select_for_copying_left_to_right);
            menuItemSelectForCopyingToTheRight.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    changeCompareStatus(MemberCompareItem.RIGHT_MISSING);
                }
            });

            if (compareItem != null && compareItem.getLeftMemberDescription() == null) {
                menuItemSelectForCopyingToTheRight.setEnabled(false);
            }
        }

        private void createMenuItemEditLeft(MemberCompareItem compareItem, final boolean isEditable) {

            String label;
            if (isEditable) {
                label = Messages.Edit_left;
            } else {
                label = Messages.Display_left;
            }

            menuItemEditLeft = new MenuItem(parent, SWT.NONE);
            menuItemEditLeft.setText(label);
            menuItemEditLeft.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    performEditMember(LEFT, isEditable);
                }
            });

            if (compareItem != null && compareItem.getLeftMemberDescription() == null) {
                menuItemEditLeft.setEnabled(false);
            }
        }

        private void createMenuItemEditRight(MemberCompareItem compareItem, final boolean isEditable) {

            String label;
            if (isEditable) {
                label = Messages.Edit_right;
            } else {
                label = Messages.Display_right;
            }

            menuItemEditRight = new MenuItem(parent, SWT.NONE);
            menuItemEditRight.setText(label);
            menuItemEditRight.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    performEditMember(RIGHT, isEditable);
                }
            });

            if (compareItem != null && compareItem.getRightMemberDescription() == null) {
                menuItemEditRight.setEnabled(false);
            }
        }

        private void createMenuItemCompareLeftAndRight(MemberCompareItem compareItem) {
            menuItemCompareLeftAndRight = new MenuItem(parent, SWT.NONE);
            menuItemCompareLeftAndRight.setText(Messages.Compare_left_AND_right);
            menuItemCompareLeftAndRight.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    performOpenCompareMembersDialog(getTheSelectedItem());
                }
            });

            if (compareItem == null || compareItem.getLeftMemberDescription() == null || compareItem.getRightMemberDescription() == null) {
                menuItemCompareLeftAndRight.setEnabled(false);
            }
        }

        private void createMenuItemDeleteLeft(MemberCompareItem compareItem) {
            menuItemDeleteLeft = new MenuItem(parent, SWT.NONE);
            menuItemDeleteLeft.setText(Messages.Delete_left);
            menuItemDeleteLeft.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    performDeleteMember(LEFT);
                }
            });

            if (compareItem != null && compareItem.getLeftMemberDescription() == null) {
                menuItemDeleteLeft.setEnabled(false);
            }
        }

        private void createMenuItemDeleteRight(MemberCompareItem compareItem) {
            menuItemDeleteRight = new MenuItem(parent, SWT.NONE);
            menuItemDeleteRight.setText(Messages.Delete_right);
            menuItemDeleteRight.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    performDeleteMember(RIGHT);
                }
            });

            if (compareItem != null && compareItem.getRightMemberDescription() == null) {
                menuItemDeleteRight.setEnabled(false);
            }
        }

        private MemberCompareItem getTheSelectedItem() {

            MemberCompareItem[] selectedItems = getSelectedItems();
            if (selectedItems.length == 1) {
                return selectedItems[0];
            }

            return null;
        }

        private void performEditMember(int side, boolean isEditable) {

            MemberCompareItem[] selectedItems = getSelectedItems();
            for (MemberCompareItem selectedItem : selectedItems) {

                Rectangle editorBounds = getEditorBounds(getShell(), side);

                if (side == LEFT) {
                    if (isEditable) {
                        MemberDescription leftMemberDescription = selectedItem.getLeftMemberDescription();
                        AbstractSynchronizeMembersEditor.this.performEditMember(leftMemberDescription, selectedItem, editorBounds);
                    } else {
                        AbstractSynchronizeMembersEditor.this.performDisplayMember(selectedItem.getLeftMemberDescription(), editorBounds);
                    }
                } else if (side == RIGHT) {
                    if (isEditable) {
                        MemberDescription rightMemberDescription = selectedItem.getRightMemberDescription();
                        AbstractSynchronizeMembersEditor.this.performEditMember(rightMemberDescription, selectedItem, editorBounds);
                    } else {
                        AbstractSynchronizeMembersEditor.this.performDisplayMember(selectedItem.getRightMemberDescription(), editorBounds);
                    }
                }
            }
        }

        private void performDeleteMember(int side) {

            boolean isYesToAll = false;

            MemberCompareItem[] selectedItems = getSelectedItems();
            for (MemberCompareItem selectedItem : selectedItems) {

                MemberDescription memberDescription;
                String qualifiedConnectionName;
                String qualifiedMemberName;

                StringBuilder confirmationMessage = new StringBuilder();

                if (side == LEFT) {
                    memberDescription = selectedItem.getLeftMemberDescription();
                    if (memberDescription == null) {
                        continue;
                    }
                    qualifiedConnectionName = new BasicQualifiedConnectionName(memberDescription.getConnectionName()).getUIConnectionName();
                    qualifiedMemberName = memberDescription.getQualifiedMemberName();
                    confirmationMessage.append("<-- "); //$NON-NLS-1$
                    confirmationMessage.append(Messages.Delete_left_member_colon);
                } else if (side == RIGHT) {
                    memberDescription = selectedItem.getRightMemberDescription();
                    if (memberDescription == null) {
                        continue;
                    }
                    qualifiedConnectionName = new BasicQualifiedConnectionName(memberDescription.getConnectionName()).getUIConnectionName();
                    qualifiedMemberName = memberDescription.getQualifiedMemberName();
                    confirmationMessage.append(Messages.Delete_right_member_colon);
                    confirmationMessage.append(" -->"); //$NON-NLS-1$
                } else {
                    throw new IllegalArgumentException("Unexpected value in 'side': " + side);
                }

                confirmationMessage.append("\n"); //$NON-NLS-1$
                confirmationMessage.append("\n"); //$NON-NLS-1$
                confirmationMessage.append(qualifiedConnectionName);
                confirmationMessage.append("\n"); //$NON-NLS-1$
                confirmationMessage.append(qualifiedMemberName);

                int rc;
                if (isYesToAll) {
                    rc = IDialogConstants.YES_TO_ALL_ID;
                } else {
                    ConfirmationMessageDialog dialog = new ConfirmationMessageDialog(getShell(), confirmationMessage.toString());
                    rc = dialog.open();
                }

                if (rc == IDialogConstants.YES_TO_ALL_ID) {
                    rc = IDialogConstants.YES_ID;
                    isYesToAll = true;
                }

                if (rc == IDialogConstants.YES_ID) {

                    if (AbstractSynchronizeMembersEditor.this.performDeleteMember(memberDescription)) {
                        if (side == LEFT) {
                            selectedItem.setLeftMemberDescription(null);
                        } else {
                            selectedItem.setRightMemberDescription(null);
                        }
                        selectedItem.clearCompareStatus();
                        tableViewer.refresh(selectedItem);
                    }
                } else {
                    break;
                }
            }
        }
    }

    public void setStatusLine(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    public void updateActionsStatusAndStatusLine() {
        updateStatusLine();
    }

    private void updateStatusLine() {

        if (statusLine == null) {
            return;
        }

        statusLine.setShowNumItems(true);
        statusLine.setShowMessage(true);

        if (statusLine != null) {
            statusLine.setMessage(statusMessage);
            statusLine.setNumItems(numFilteredItems);
        }
    }

    private void debug(String message) {
        // System.out.println(message);
    }
}
