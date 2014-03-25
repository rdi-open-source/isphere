/*******************************************************************************
 * Copyright (c) 2012-2013 Task Force IT-Consulting GmbH, Waltrop and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Task Force IT-Consulting GmbH - initial API and implementation
 *******************************************************************************/

package biz.isphere.rse.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.ISphereHelper;
import biz.isphere.core.sourcefilesearch.SearchDialog;
import biz.isphere.core.sourcefilesearch.SearchElement;
import biz.isphere.core.sourcefilesearch.SearchExec;
import biz.isphere.core.sourcefilesearch.SearchPostRun;
import biz.isphere.rse.Messages;

import com.ibm.as400.access.AS400;
import com.ibm.etools.iseries.comm.filters.ISeriesMemberFilterString;
import com.ibm.etools.iseries.comm.filters.ISeriesObjectFilterString;
import com.ibm.etools.iseries.comm.filters.ISeriesObjectTypeAttrList;
import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.dstore.common.ISeriesDataElementHelpers;
import com.ibm.etools.iseries.core.ui.actions.ISeriesSystemBaseAction;
import com.ibm.etools.iseries.core.util.ISeriesDataElementUtil;
import com.ibm.etools.systems.as400filesubsys.impl.FileSubSystemImpl;
import com.ibm.etools.systems.core.messages.SystemMessageException;
import com.ibm.etools.systems.core.ui.SystemMenuManager;
import com.ibm.etools.systems.core.ui.actions.ISystemDynamicPopupMenuExtension;
import com.ibm.etools.systems.core.ui.messages.SystemMessageDialog;
import com.ibm.etools.systems.dstore.core.model.DataElement;
import com.ibm.etools.systems.filters.SystemFilterReference;
import com.ibm.etools.systems.filters.SystemFilterStringReference;
import com.ibm.etools.systems.model.impl.SystemMessageObject;
import com.ibm.etools.systems.subsystems.SubSystem;

public class SourceFileSearchAction extends ISeriesSystemBaseAction implements ISystemDynamicPopupMenuExtension {

	private ISeriesConnection _connection;
	private boolean _multipleConnection;
	protected ArrayList _selectedElements;
	private HashMap<String, SearchElement> _searchElements;
	private ISeriesObjectFilterString _objectFilterString;
	private FileSubSystemImpl _fileSubSystemImpl;

	public SourceFileSearchAction() {
		super(Messages.getString("iSphere_Source_File_Search"), "", null);
		_selectedElements = new ArrayList();
		setContextMenuGroup("additions");
		allowOnMultipleSelection(true);
		setHelp("");
		setImageDescriptor(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_SOURCE_FILE_SEARCH));
	}

	public void populateMenu(Shell shell, SystemMenuManager menu, IStructuredSelection selection, String menuGroup) {
		setShell(shell);
		menu.add("additions", this);
	}

	public boolean supportsSelection(IStructuredSelection selection) {

		this._selectedElements.clear();

		_connection = null;
		_multipleConnection = false;
		
		ArrayList<Object> _selectedElements = new ArrayList<Object>();

		for (Iterator iterSelection = selection.iterator(); iterSelection.hasNext();) {
			
			Object _object = iterSelection.next();

			if ((_object instanceof DataElement)) {

				DataElement element = (DataElement) _object;
				
				if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isLibrary() ||
						ISeriesDataElementUtil.getDescriptorTypeObject(element).isSourceFile() ||
						ISeriesDataElementUtil.getDescriptorTypeObject(element).isMember()) {

					_selectedElements.add(element);

					checkIfMultipleConnections(ISeriesConnection
							.getConnection(ISeriesDataElementUtil
									.getConnection(element)
									.getAliasName()));
					
				}

			} 
			else if ((_object instanceof SystemFilterReference)) {
				
				SystemFilterReference element = (SystemFilterReference) _object;

				_selectedElements.add(element);
				
				checkIfMultipleConnections(ISeriesConnection
						.getConnection(((SubSystem)element
								.getFilterPoolReferenceManager()
								.getProvider())
								.getSystemConnection()
								.getAliasName()));
				
			} 
			else if ((_object instanceof SystemFilterStringReference)) {
				
				SystemFilterStringReference element = (SystemFilterStringReference) _object;

				_selectedElements.add(element);

				checkIfMultipleConnections(ISeriesConnection
						.getConnection(((SubSystem)element
								.getFilterPoolReferenceManager()
								.getProvider())
								.getSystemConnection()
								.getAliasName()));
				
			}
			
		}
		
		if (_selectedElements.isEmpty()) {
			return false;
		}
		
		this._selectedElements = _selectedElements;
		return true;
		
	}

	private void checkIfMultipleConnections(ISeriesConnection connection) {
		if (!_multipleConnection) {
			if (this._connection == null) {
				this._connection = connection;
			} 
			else if (connection != this._connection) {
				_multipleConnection = true;
			}
		}
	}

	public void run() {

		if (_multipleConnection) {
			MessageBox errorBox = new MessageBox(shell, SWT.ICON_ERROR);
			errorBox.setText(Messages.getString("E_R_R_O_R"));
			errorBox.setMessage(Messages.getString("Resources_with_different_connections_have_been_selected."));
			errorBox.open();
			return;
		}
		
		if (!_connection.isConnected()) {
			try {
				_connection.connect();
			} 
			catch (SystemMessageException e) {
				return;
			}
		}
		
		_objectFilterString = null;
		
		_searchElements = new HashMap<String, SearchElement>();
		
		boolean _continue = true;
		
		for (int idx = 0; idx < _selectedElements.size(); idx++) {
			
			Object _object = _selectedElements.get(idx);

			if ((_object instanceof DataElement)) {

				DataElement element = (DataElement) _object;

				if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isLibrary()) {
					_continue = addElementsFromLibrary(element);
				} 
				else if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isSourceFile()) {
					addElementsFromSourceFile(
							ISeriesDataElementHelpers.getLibrary(element), 
							ISeriesDataElementHelpers.getName(element));
				}
				else if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isMember()) {
					addElement(element);
				} 
				if (!_continue) {
					break;
				}

			} 
			else if ((_object instanceof SystemFilterReference)) {
				
				SystemFilterReference filterReference = (SystemFilterReference) _object;
				String[] _filterStrings = filterReference.getReferencedFilter().getFilterStrings();
				if (!addElementsFromFilterString(_filterStrings)) {
					break;
				}
				

			} 
			else if ((_object instanceof SystemFilterStringReference)) {
				
				SystemFilterStringReference filterStringReference = (SystemFilterStringReference) _object;
				String[] _filterStrings = filterStringReference.getParent().getReferencedFilter().getFilterStrings();
				if (!addElementsFromFilterString(_filterStrings)) {
					break;
				}
				
			}
			
		}

		AS400 as400 = null;
		Connection jdbcConnection = null;
		try {
			as400 = _connection.getAS400ToolboxObject(shell);
			jdbcConnection = _connection.getJDBCConnection(null, false);
		} 
		catch (SystemMessageException e) {
			e.printStackTrace();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}

		if (as400 != null && jdbcConnection != null) {

			if (ISphereHelper.checkISphereLibrary(shell, as400)) {
				
				SearchDialog dialog = new SearchDialog(shell, _searchElements);
				if (dialog.open() == Dialog.OK) {
	
					SearchPostRun postRun = new SearchPostRun();
					postRun.setConnection(_connection);
					postRun.setConnectionName(_connection.getConnectionName());
					postRun.setSearchString(dialog.getString());
					postRun.setSearchElements(_searchElements);
					postRun.setWorkbenchWindow(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					
					new SearchExec().execute(
							as400,
							jdbcConnection,
							dialog.getString(),
							dialog.getFromColumn(),
							dialog.getToColumn(),
							dialog.getCase(),
							new ArrayList<SearchElement>(_searchElements.values()),
							postRun);
					
				}
				
			}
			
		}
		
	}

	private void addElement(DataElement element) {
		
		String key = ISeriesDataElementHelpers.getLibrary(element) + "-" + ISeriesDataElementHelpers.getFile(element) + "-" + ISeriesDataElementHelpers.getName(element);

		if (!_searchElements.containsKey(key)) {

			SearchElement _searchElement = new SearchElement();
			_searchElement.setLibrary(ISeriesDataElementHelpers.getLibrary(element));
			_searchElement.setFile(ISeriesDataElementHelpers.getFile(element));
			_searchElement.setMember(ISeriesDataElementHelpers.getName(element));
			_searchElement.setDescription(ISeriesDataElementHelpers.getDescription(element));
			_searchElements.put(key, _searchElement);
			
		}
		
	}

	private void addElementsFromSourceFile(String library, String sourceFile) {
		
		/*
		String key = library + "-" + sourceFile + "-" + "*";

		if (!_searchElements.containsKey(key)) {

			SearchElement _searchElement = new SearchElement();
			_searchElement.setLibrary(library);
			_searchElement.setFile(sourceFile);
			_searchElement.setMember("*");
			_searchElements.put(key, _searchElement);
			
		}
		*/
		
		ISeriesMemberFilterString _memberFilterString = new ISeriesMemberFilterString();
		_memberFilterString.setLibrary(library);
		_memberFilterString.setFile(sourceFile);
		_memberFilterString.setMember("*");
		_memberFilterString.setMemberType("*");
		
		String[] _filterStrings = new String[1];
		_filterStrings[0] = _memberFilterString.toString();
		addElementsFromFilterString(_filterStrings);
		
	}
	
	private boolean addElementsFromLibrary(DataElement element) {

		Vector<DataElement> libElements = new Vector<DataElement>();
		Object[] children = (Object[]) null;

		if (_objectFilterString == null) {
			_objectFilterString = new ISeriesObjectFilterString();
			_objectFilterString.setObject("*");
			_objectFilterString.setObjectType("*FILE");
			String attributes = "*FILE:PF-SRC *FILE:PF38-SRC";
			_objectFilterString.setObjectTypeAttrList(new ISeriesObjectTypeAttrList(attributes));
		}

		_objectFilterString.setLibrary(element.getName());
		String _filterString = _objectFilterString.toString();

		_fileSubSystemImpl = _connection.getISeriesFileSubSystem();
		try {
			children = _fileSubSystemImpl.resolveFilterString(_filterString, null);
		} 
		catch (InterruptedException localInterruptedException) {
			return false;
		} 
		catch (Exception e) {
			SystemMessageDialog.displayExceptionMessage(shell, e);
			return false;
		}

		if ((children == null) || (children.length == 0)) {
			return true;
		}
		
		Object firstObject = children[0];
		if ((firstObject instanceof SystemMessageObject)) {
			SystemMessageDialog.displayErrorMessage(shell, ((SystemMessageObject) firstObject).getMessage());
			return true;
		}

		for (int idx2 = 0; idx2 < children.length; idx2++) {
			libElements.addElement((DataElement)children[idx2]);
		}

		for (Enumeration<DataElement> enumeration = libElements.elements(); enumeration.hasMoreElements();) {
			element = (DataElement) enumeration.nextElement();
			addElementsFromSourceFile(ISeriesDataElementHelpers.getLibrary(element), ISeriesDataElementHelpers.getName(element));
		}
		
		return true;
		
	}

	private boolean addElementsFromFilterString(String[] filterStrings) {
		
		boolean _continue = true;
		Object[] children = (Object[]) null;
		
		for (int idx = 0; idx < filterStrings.length; idx++) {
			
			String _filterString = filterStrings[idx];
			_fileSubSystemImpl = _connection.getISeriesFileSubSystem();
			
			try {
				children = _fileSubSystemImpl.resolveFilterString(_filterString, null);
			} 
			catch (InterruptedException localInterruptedException) {
				return false;
			} 
			catch (Exception e) {
				SystemMessageDialog.displayExceptionMessage(shell, e);
				return false;
			}
			
			if ((children != null) && (children.length != 0)) {
				
				Object firstObject = children[0];
				
				if ((firstObject instanceof SystemMessageObject)) {
					
					SystemMessageDialog.displayErrorMessage(shell, ((SystemMessageObject) firstObject).getMessage());
					
				} 
				else {
					
					for (int idx2 = 0; idx2 < children.length; idx2++) {
						
						DataElement element = (DataElement)children[idx2];
						
						if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isLibrary()) {
							_continue = addElementsFromLibrary(element);
						} 
						else if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isSourceFile()) {
							addElementsFromSourceFile(ISeriesDataElementHelpers.getLibrary(element), ISeriesDataElementHelpers.getName(element));
						} 
						else if (ISeriesDataElementUtil.getDescriptorTypeObject(element).isMember()) {
							addElement(element);
						} 
						
						if (!_continue)
							break;
						
					}
					
				}
				
			}
			
		}
		
		return true;
		
	}

}