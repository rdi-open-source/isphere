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

import java.math.BigDecimal;
import java.util.Calendar;
import java.text.DateFormatSymbols;
import org.medfoster.sqljep.function.OracleNumberFormat;
import org.medfoster.sqljep.function.OracleTimestampFormat;
import org.medfoster.sqljep.*;

public class ToChar extends PostfixCommand {
	static final String PARAM_EXCEPTION = "Format should be string";
	static final String TYPE_EXCEPTION = "Unsupported type";
	
	final public int getNumberOfParameters() {
		return -1;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		int num = node.jjtGetNumChildren();
		if (num == 1) {
			Comparable param1 = runtime.stack.pop();
			runtime.stack.push(to_char(param1));
		}
		else if (num == 2) {
			Comparable param2 = runtime.stack.pop();
			Comparable param1 = runtime.stack.pop();
			runtime.stack.push(to_char(param1, param2, runtime.calendar, runtime.dateSymbols));
		} else {
			// remove all parameters from stack and push null
			removeParams(runtime.stack, num);
			throw new ParseException("Wrong number of parameters for instr");
		}
	}

	public static String to_char(Comparable param1) {
		String res;
		if (param1 == null) {
			res = null;
		}
		else if (param1 instanceof BigDecimal) {
			res = ((BigDecimal)param1).toPlainString();
		} else {
			res = param1.toString();
		}
		return res;
	}
	
	public static String to_char(Comparable param1, Comparable param2, Calendar cal, DateFormatSymbols symb) throws ParseException {
		if (param1 == null || param2 == null) {
			return null;
		}
		if (param1 instanceof String && ((String)param1).length() == 0) {
			return null;
		}
		if (!(param2 instanceof String)) {
			throw new ParseException(PARAM_EXCEPTION);
		}
		if (param1 instanceof String) {
			return (String)param1;
		}
		else if (param1 instanceof Number) {
			try {
				OracleNumberFormat format = new OracleNumberFormat((String)param2);
				return format.format((Number)param1);
			} catch (java.text.ParseException e) {
				throw new ParseException(e.getMessage());
			}
		}
		else if (param1 instanceof java.util.Date) {
			try {
				OracleTimestampFormat format = new OracleTimestampFormat((String)param2, cal, symb);
				return format.format((java.sql.Timestamp)param1);
			} catch (java.text.ParseException e) {
				throw new ParseException(e.getMessage());
			}
		} else {
			throw new ParseException(TYPE_EXCEPTION);
		}
	}
}

