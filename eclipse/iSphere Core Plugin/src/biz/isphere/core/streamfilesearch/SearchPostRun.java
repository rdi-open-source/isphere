/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.streamfilesearch;

import java.util.Map;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import biz.isphere.core.search.SearchOptions;

public class SearchPostRun implements ISearchPostRun {

    private class ShowView extends Thread {

        private IWorkbenchWindow _workbenchWindow;
        private String _connectionName;
        private String _searchString;
        private SearchResult[] _searchResults;
        private SearchOptions _searchOptions;

        public ShowView(IWorkbenchWindow _workbenchWindow, String _connectionName, String _searchString, SearchResult[] _searchResults,
            SearchOptions _searchOptions) {

            this._workbenchWindow = _workbenchWindow;
            this._connectionName = _connectionName;
            this._searchString = _searchString;
            this._searchResults = _searchResults;
            this._searchOptions = _searchOptions;

        }

        @Override
        public void run() {
            _workbenchWindow.getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    try {
                        ViewSearchResults viewSearchResults = (ViewSearchResults)(_workbenchWindow.getActivePage()
                            .showView("biz.isphere.core.streamfilesearch.ViewSearchResults")); //$NON-NLS-1$
                        viewSearchResults.addTabItem(_connectionName, _searchString, _searchResults, _searchOptions);
                    } catch (PartInitException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private Object _connection;
    private String _connectionName;
    private String _searchString;
    private Map<String, SearchElement> _searchElements;
    private IWorkbenchWindow _workbenchWindow;

    public Object getConnection() {
        return _connection;
    }

    public void setConnection(Object _connection) {
        this._connection = _connection;
    }

    public String getConnectionName() {
        return _connectionName;
    }

    public void setConnectionName(String _connectionName) {
        this._connectionName = _connectionName;
    }

    public String getSearchString() {
        return _searchString;
    }

    public void setSearchString(String _searchString) {
        this._searchString = _searchString;
    }

    public Map<String, SearchElement> getSearchElements() {
        return _searchElements;
    }

    public void setSearchElements(Map<String, SearchElement> _searchElements) {
        this._searchElements = _searchElements;
    }

    public IWorkbenchWindow getWorkbenchWindow() {
        return _workbenchWindow;
    }

    public void setWorkbenchWindow(IWorkbenchWindow _workbenchWindow) {
        this._workbenchWindow = _workbenchWindow;
    }

    public void run(SearchResult[] _searchResults, SearchOptions _searchOptions) {

        if (_searchResults != null) {

            new ShowView(_workbenchWindow, _connectionName, _searchString, _searchResults, _searchOptions).start();

        }

    }

}
