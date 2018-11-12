/*****************************************************************************
      SQLJEP - Java SQL Expression Parser 0.2
      November 1 2006
         (c) Copyright 2006, Alexey Gaidukov
      SQLJEP Author: Alexey Gaidukov

      SQLJEP is based on JEP 2.24 (http://www.singularsys.com/jep/)
           (c) Copyright 2002, Nathan Funk
 
      See LICENSE.txt for license information.
*****************************************************************************/

package org.medfoster.sqljep.function;

import java.lang.Math;
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.medfoster.sqljep.*;

public class AddDate extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 2;
	}
	
	/**
	 * Calculates the result of applying the "+" operator to the arguments from
	 * the stack and pushes it back on the stack.
	 */
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable param2 = runtime.stack.pop();
		Comparable param1 = runtime.stack.pop();
		runtime.stack.push(addDate(param1, param2)); //push the result on the inStack
	}

	public static java.util.Date addDate(Comparable param1, Comparable param2) throws ParseException {
		if (param1 == null || param2 == null) {
			return null;
		}
		try {
			int days = (int)getDouble(param2);
			if (param1 instanceof Timestamp) {
				Timestamp d = (Timestamp)param1;
				return new Timestamp(d.getTime()+days*86400000);
			}
			else if (param1 instanceof java.sql.Date) {
				java.sql.Date d = (java.sql.Date)param1;
				return new java.sql.Date(d.getTime()+days*86400000);
			} else {
				throw new ParseException();
			}
		} catch (ParseException e) {
			throw new ParseException(WRONG_TYPE+"  addDate("+param1.getClass()+","+param2.getClass()+")");
		}
	}
}

