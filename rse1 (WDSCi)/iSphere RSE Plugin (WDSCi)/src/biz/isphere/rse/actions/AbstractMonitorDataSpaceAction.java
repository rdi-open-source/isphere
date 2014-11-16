package biz.isphere.rse.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.dataspaceeditordesigner.rse.IDialogView;
import biz.isphere.core.internal.RemoteObject;
import biz.isphere.rse.Messages;
import biz.isphere.rse.dataspacemonitor.rse.DataSpaceMonitorView;

import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesObject;
import com.ibm.etools.iseries.core.descriptors.ISeriesDataElementDescriptorType;
import com.ibm.etools.iseries.core.dstore.common.ISeriesDataElementHelpers;
import com.ibm.etools.iseries.core.ui.actions.ISeriesSystemBaseAction;
import com.ibm.etools.iseries.core.util.ISeriesDataElementUtil;
import com.ibm.etools.systems.core.ui.SystemMenuManager;
import com.ibm.etools.systems.core.ui.actions.ISystemDynamicPopupMenuExtension;
import com.ibm.etools.systems.dstore.core.model.DataElement;

public abstract class AbstractMonitorDataSpaceAction extends ISeriesSystemBaseAction implements ISystemDynamicPopupMenuExtension {

    private ArrayList<DataElement> arrayListSelection;
    private String objectType;

    public AbstractMonitorDataSpaceAction(String name, String image, String objectType) {
        super(name, "", null);
        this.arrayListSelection = new ArrayList<DataElement>();
        this.objectType = objectType;

        setContextMenuGroup("additions");
        allowOnMultipleSelection(true);
        setHelp("");
        setImageDescriptor(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_USER_SPACE_MONITOR));
    }

    public void populateMenu(Shell shell, SystemMenuManager menu, IStructuredSelection selection, String menuGroup) {
        setShell(shell);
        menu.add("additions", this);
    }

    public boolean supportsSelection(IStructuredSelection selection) {

        this.arrayListSelection.clear();

        Iterator selectionIterator = selection.iterator();
        while (selectionIterator.hasNext()) {
            Object objSelection = selectionIterator.next();
            if (matchesType(objSelection, objectType)) {
                DataElement dataElement = (DataElement)objSelection;
                arrayListSelection.add(dataElement);
            }
        }

        if (arrayListSelection.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void run() {

        if (arrayListSelection != null && arrayListSelection.size() > 0) {
            Iterator selectionIterator = arrayListSelection.iterator();

            while (selectionIterator.hasNext()) {
                DataElement dataElement = (DataElement)selectionIterator.next();
                IWorkbenchWindow window = ISpherePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
                if (window != null) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        openMonitorForObject(dataElement, page);

                    }

                }
            }
        }
    }

    protected void openMonitorForObject(DataElement dataElement, IWorkbenchPage page) {
        try {

            page.showView(DataSpaceMonitorView.ID, null, IWorkbenchPage.VIEW_CREATE);
            IViewPart justActivated = page.showView(DataSpaceMonitorView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
            if (justActivated instanceof IDialogView) {

                ISeriesObject qsysRemoteObject = new ISeriesObject(dataElement);
                String connection = ISeriesConnection.getConnection(ISeriesDataElementUtil.getConnection(dataElement).getAliasName())
                    .getConnectionName();
                String name = qsysRemoteObject.getName();
                String library = qsysRemoteObject.getLibrary();
                String type = qsysRemoteObject.getType();
                String description = qsysRemoteObject.getDescription();
                RemoteObject remoteObject = new RemoteObject(connection, name, library, type, description);

                ((IDialogView)justActivated).setData(new RemoteObject[] { remoteObject });
            }

        } catch (Exception e) {
            ISpherePlugin.logError(e.getMessage(), e);
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, e.getLocalizedMessage());
        }
    }

    private boolean matchesType(Object object, String objectType) {
        if (object instanceof DataElement) {
            DataElement dataElement = (DataElement)object;
            ISeriesDataElementDescriptorType descriptorType = ISeriesDataElementDescriptorType.getDescriptorTypeObject(dataElement);
            if (descriptorType.isObject()) {
                String strType = ISeriesDataElementHelpers.getType(dataElement);
                if (objectType.equals(strType)) {
                    return true;
                }
            }
        }
        return false;
    }

}
