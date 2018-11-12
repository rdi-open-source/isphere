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
import org.medfoster.sqljep.*;

public final class Modulus extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 2;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable param2 = runtime.stack.pop();
		Comparable param1 = runtime.stack.pop();
		runtime.stack.push(remainder(param1, param2)); //push the result on the inStack
	}
	
	public static Comparable remainder(Comparable param1, Comparable param2) throws ParseException	{
		if (param1 == null || param2 == null) {
			return null;
		}
		if (param1 instanceof String) {
			param1 = parse((String)param1);
		}
		if (param2 instanceof String) {
			param2 = parse((String)param2);
		}
		if (param1 instanceof Number && param2 instanceof Number) {
			// BigInteger type is not supported
			if (param1 instanceof BigDecimal || param2 instanceof BigDecimal) {
				BigDecimal b1 = getBigDecimal((Number)param1);
				BigDecimal b2 = getBigDecimal((Number)param2);
				return b1.remainder(b2);
			}
			if (param1 instanceof Double || param2 instanceof Double || param1 instanceof Float || param2 instanceof Float) {
				return ((Number)param1).doubleValue() % ((Number)param2).doubleValue();
			} else {	// Long, Integer, Short, Byte 
				long l1 = ((Number)param1).longValue();
				long l2 = ((Number)param2).longValue();
				return l1 % l2;
			}
		} else {
			throw new ParseException(WRONG_TYPE+"  ("+param1.getClass()+"%"+param2.getClass()+")");
		}
	}
}
