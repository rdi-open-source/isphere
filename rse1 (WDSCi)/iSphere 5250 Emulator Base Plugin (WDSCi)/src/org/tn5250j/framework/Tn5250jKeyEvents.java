/**Copyright (C) 2004 Seagull Software
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*@author bvansomeren (bvansomeren@seagull.nl)
*/
package org.tn5250j.framework;

//import org.tn5250j.Screen5250;
import org.tn5250j.framework.tn5250.Screen5250;

public class Tn5250jKeyEvents extends Tn5250jEvent {
	private String keystrokes;

	public Tn5250jKeyEvents(Screen5250 screen, String strokes) {
		super(screen);
		this.keystrokes = strokes;
	}

	public String getKeystrokes() {
		return this.keystrokes;
	}

}
