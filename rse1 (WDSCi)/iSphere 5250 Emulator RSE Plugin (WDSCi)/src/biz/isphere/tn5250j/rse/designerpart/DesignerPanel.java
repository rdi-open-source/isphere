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

package biz.isphere.tn5250j.rse.designerpart;

import org.eclipse.swt.widgets.Shell;
import org.tn5250j.Session5250;

import com.ibm.etools.iseries.core.api.ISeriesConnection;

import biz.isphere.tn5250j.core.designerpart.CoreDesignerPanel;
import biz.isphere.tn5250j.core.session.Session;
import biz.isphere.tn5250j.core.tn5250jpart.TN5250JGUI;
import biz.isphere.tn5250j.core.tn5250jpart.TN5250JInfo;

public class DesignerPanel extends CoreDesignerPanel {

	private static final long serialVersionUID = 1L;
	
	public DesignerPanel(TN5250JInfo tn5250jInfo, Session session, Shell shell) {
		super(tn5250jInfo, session, shell);
	}
	
	public TN5250JGUI getTN5250JGUI(TN5250JInfo tn5250jInfo, Session5250 session5250) {
		return new DesignerGUI(tn5250jInfo, session5250);
	}

	public String getHost() {
		DesignerInfo designerInfo = (DesignerInfo)getTN5250JInfo();
		ISeriesConnection iSeriesConnection = ISeriesConnection.getConnection(designerInfo.getRSEProfil(), designerInfo.getRSEConnection());
		if (iSeriesConnection != null) {
			return iSeriesConnection.getHostName();
		}
		return "";
	}

}
