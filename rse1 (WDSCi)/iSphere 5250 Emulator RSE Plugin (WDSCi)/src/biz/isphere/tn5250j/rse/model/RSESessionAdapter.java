// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this software; see the file COPYING.  If not, write to
// the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
// Boston, MA 02111-1307 USA

package biz.isphere.tn5250j.rse.model;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import biz.isphere.tn5250j.rse.TN5250JRSEPlugin;
import biz.isphere.tn5250j.rse.actions.ChangeSessionAction;
import biz.isphere.tn5250j.rse.actions.DeleteSessionAction;
import biz.isphere.tn5250j.rse.actions.DisplaySessionAction;
import biz.isphere.tn5250j.rse.sessionseditor.SessionsEditor;
import biz.isphere.tn5250j.rse.sessionspart.SessionsInfo;
import biz.isphere.tn5250j.rse.sessionsview.SessionsView;
import biz.isphere.tn5250j.rse.subsystems.TN5250JSubSystem;

import com.ibm.etools.systems.core.ui.SystemMenuManager;
import com.ibm.etools.systems.core.ui.view.AbstractSystemViewAdapter;
import com.ibm.etools.systems.core.ui.view.ISystemRemoteElementAdapter;

import biz.isphere.tn5250j.core.Messages;
import biz.isphere.tn5250j.core.tn5250jeditor.TN5250JEditorInput;
import biz.isphere.tn5250j.core.tn5250jpart.DisplaySession;

public class RSESessionAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter {

	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup) {
		
		IAction changeSessionAction = new ChangeSessionAction(shell);
		menu.add(menuGroup, changeSessionAction);
		
		IAction deleteSessionAction = new DeleteSessionAction(shell);
		menu.add(menuGroup, deleteSessionAction);
		
		IAction displaySessionAction = new DisplaySessionAction(shell);
		menu.add(menuGroup, displaySessionAction);
		
	}

	public ImageDescriptor getImageDescriptor(Object element) {
		return TN5250JRSEPlugin.getImageDescriptor(TN5250JRSEPlugin.IMAGE_TN5250J);
	}

	public String getText(Object element) {
		return ((RSESession)element).getName();
	}

	public String getAbsoluteName(Object element) {
		return "Session_" + ((RSESession)element).getName();
	}

	public String getType(Object element) {
		return "Session";
	}

	public Object getParent(Object element) {
		return null; 
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getChildren(Object element) {
		return null;
	}

	protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
		return null;
	}

	public Object internalGetPropertyValue(Object key) {
		return null;
	}

	public boolean canRename(Object element) {
		return false;
	}

	public boolean showRename(Object element) {
		return false;
	}

	public boolean canDelete(Object element) {
		return false;
	}

	public boolean showDelete(Object element) {
		return false;
	}

	public boolean showRefresh(Object element) {
		return false;
	}

	public String getAbsoluteParentName(Object element) {
		return "root";
	}

	public String getSubSystemFactoryId(Object element) {
		return "biz.isphere.tn5250j.rse.subsystems.factory";
	}

	public String getRemoteTypeCategory(Object element) {
		return "TN5250J"; 
	}

	public String getRemoteType(Object element) {
		return "Session"; 
	}

	public String getRemoteSubType(Object element) {
		return null; 
	}

	public boolean refreshRemoteObject(Object oldElement, Object newElement) {
		RSESession oldRSESession = (RSESession)oldElement;
		RSESession newRSESession = (RSESession)newElement;
		newRSESession.setName(oldRSESession.getName());
		return false;
	}

	public Object getRemoteParent(Shell shell, Object element) throws Exception {
		return null; 
	}

	public String[] getRemoteParentNamesInUse(Shell shell, Object element) throws Exception {
		TN5250JSubSystem ourSS = (TN5250JSubSystem)getSubSystem(element);

		RSESession[] rseSessions = ourSS.getRSESessions();
		String[] allNames = new String[rseSessions.length];
		for (int idx = 0; idx < rseSessions.length; idx++)
			allNames[idx] = rseSessions[idx].getName();
		return allNames; 	
	}

	public boolean supportsUserDefinedActions(Object element) {
		return false;
	}

	public boolean handleDoubleClick(Object element) {
		
		RSESession rseSession = (RSESession)element;
		 
		String area = rseSession.getSession().getArea();

		try {
			
			if (area.equals("*VIEW")) {
 
				if (rseSession.getName().equals("_DESIGNER")) {

					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("biz.isphere.tn5250j.rse.designerview.DesignerView");
				
				}
				else {
				
					SessionsView sessionsView = (SessionsView)(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("biz.isphere.tn5250j.rse.sessionsview.SessionsView"));
					
					SessionsInfo sessionsInfo = new SessionsInfo(sessionsView);
					sessionsInfo.setRSEProfil(rseSession.getRSEProfil());
					sessionsInfo.setRSEConnection(rseSession.getRSEConnection());
					sessionsInfo.setSession(rseSession.getName());
					
					DisplaySession.run(TN5250JRSEPlugin.getRSESessionDirectory(rseSession.getRSEProfil() + "-" + rseSession.getRSEConnection()), rseSession.getRSEProfil() + "-" + rseSession.getRSEConnection(), rseSession.getName(), sessionsInfo);
					
				}
				
			}
			else if (area.equals("*EDITOR")) {
				
				if (rseSession.getName().equals("_DESIGNER")) {
 
					TN5250JEditorInput editorInput = 
						new TN5250JEditorInput(
								"biz.isphere.tn5250j.rse.designereditor.DesignerEditor", 
    							Messages.getString("iSphere_5250_Designer"), 
								"TN5250J", 
								TN5250JRSEPlugin.getDefault().getImageRegistry().get(TN5250JRSEPlugin.IMAGE_TN5250J));
					
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, "biz.isphere.tn5250j.rse.designereditor.DesignerEditor");
				
				}
				else {

					TN5250JEditorInput editorInput = 
						new TN5250JEditorInput(
								"biz.isphere.tn5250j.rse.sessionseditor.SessionsEditor", 
    							Messages.getString("iSphere_5250_Sessions"), 
								"TN5250J", 
								TN5250JRSEPlugin.getDefault().getImageRegistry().get(TN5250JRSEPlugin.IMAGE_TN5250J));
					
					SessionsEditor sessionsEditor = (SessionsEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, "biz.isphere.tn5250j.rse.sessionseditor.SessionsEditor");

					SessionsInfo sessionsInfo = new SessionsInfo(sessionsEditor);
					sessionsInfo.setRSEProfil(rseSession.getRSEProfil());
					sessionsInfo.setRSEConnection(rseSession.getRSEConnection());
					sessionsInfo.setSession(rseSession.getName());
					
					DisplaySession.run(TN5250JRSEPlugin.getRSESessionDirectory(rseSession.getRSEProfil() + "-" + rseSession.getRSEConnection()), rseSession.getRSEProfil() + "-" + rseSession.getRSEConnection(), rseSession.getName(), sessionsInfo);
					
				}
				
			}
			
		} 
		catch (PartInitException e1) {
		}
		
		return true;
		
	}
	
}
