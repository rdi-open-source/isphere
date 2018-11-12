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
import org.medfoster.sqljep.*;

public class IndistinctMatching extends PostfixCommand {
	final public int getNumberOfParameters() {
		return -1;
	}

	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		int num = node.jjtGetNumChildren();
		if (num == 2) {
			Comparable param2 = runtime.stack.pop();
			Comparable param1 = runtime.stack.pop();
			runtime.stack.push(IndistinctMatching(param1, param2, 3));
		}
		else if (num == 3) {
			Comparable param3 = runtime.stack.pop();
			Comparable param2 = runtime.stack.pop();
			Comparable param1 = runtime.stack.pop();
			runtime.stack.push(IndistinctMatching(param1, param2, param3));
		} else {
			// remove all parameters from stack and push null
			removeParams(runtime.stack, num);
			throw new ParseException(PARAMS_NUMBER+" for instr");
		}
	}
	
	//------------------------------------------------------------------------------
	//������� ��������� ��������� ����� ��� ����� ��������
	//------------------------------------------------------------------------------
	//MaxMatching - ������������ ����� ��������� (���������� 3-4)
	
	// ����������� ��� ����� ��������
	// if (IndistinctMatching(4, "��������� ������", "������������ ������  - ������") > 40) then ...
	
	public static Integer IndistinctMatching(Comparable param1, Comparable param2, Comparable param3) throws ParseException {
		if (param1 == null || param2 == null || param3 == null) {
			return null;
		}
		
		String A = param1.toString();
		String B = param2.toString();
		int iMaxLen = Math.min(getInteger(param3), Math.min(A.length(), B.length()));
		int res[] = new int[2];
		for (int i = 1; i <= iMaxLen; i++) {
			matching(A, B, i, res);
			matching(B, A, i, res);
		}

		if (res[0] == 0) {
			return ZERO;
		}
		return new Integer((res[0]*100) / res[1]);
	}

	private static void matching(String A, String B, int iSubLen, int[] res) {
		int lenA = A.length();
		int lenB = B.length();
		for (int posA = 0; posA < lenA-iSubLen+1; posA++) {
			for (int posB = 0; posB < lenB-iSubLen+1; posB++) {
				if (A.regionMatches(true, posA, B, posB, iSubLen)) {
					res[0]++;
					break;
				}
			}
			res[1]++;
		}
	}
}

