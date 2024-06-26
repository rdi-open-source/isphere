/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.tn5250j.core.designerview;

import biz.isphere.tn5250j.core.tn5250jview.TN5250JView;

public abstract class CoreDesignerView extends TN5250JView {

    @Override
    public boolean isMultiSession() {
        return false;
    }

}
