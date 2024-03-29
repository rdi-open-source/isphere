/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.search;

import biz.isphere.core.search.ISearchArgumentsListEditorProvider;
import biz.isphere.core.search.SearchOptionConfig;
import biz.isphere.core.search.SearchOptions;

public class SearchArgumentsListEditorProvider implements ISearchArgumentsListEditorProvider {

    public biz.isphere.core.search.SearchArgumentsListEditor getListEditor(int maxSearchArgumentLength, boolean regularExpressionsOption,
        SearchOptionConfig[] additionalSearchOptions) {
        return new SearchArgumentsListEditor(SearchOptions.ARGUMENTS_SIZE, maxSearchArgumentLength, regularExpressionsOption,
            additionalSearchOptions);
    }

}
