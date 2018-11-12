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
import java.util.Calendar;
import java.sql.Timestamp;
import java.math.*;

import static java.util.Calendar.*;
import org.medfoster.sqljep.*;

public class Trunc extends PostfixCommand {
	private static final String PARAM_EXCEPTION = "Scale in trunc shoud be integer";
	static final String TIME_EXCEPTION = "Can't use TIME here";
	static final String DATE_EXCEPTION = "Can't use DATE here";
	static final String FORMAT_EXCEPTION = "Unknown format";

	final public int getNumberOfParameters() {
		return -1;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		int num = node.jjtGetNumChildren();
		if (num == 1) {
			Comparable param1 = runtime.stack.pop();
			runtime.stack.push(trunc(param1, runtime.calendar));
		}
		else if (num == 2) {
			Comparable param2 = runtime.stack.pop();
			Comparable param1 = runtime.stack.pop();
			runtime.stack.push(trunc(param1, param2, runtime.calendar));
		} else {
			// remove all parameters from stack and push null
			removeParams(runtime.stack, num);
			throw new ParseException(PARAMS_NUMBER+" for trunc");
		}
	}

	public static Comparable trunc(Comparable param, Calendar cal) throws ParseException {
		if (param == null) {
			return null;
		}
		if (param instanceof String) {
			param = parse((String)param);
		}
		if (param instanceof BigDecimal) {		// BigInteger is not supported
			BigDecimal b;
			try {
				b = ((BigDecimal)param).setScale(0, BigDecimal.ROUND_DOWN);
				try {
					return b.longValueExact();
				} catch (Exception e) {
				}
			} catch (Exception e) {
				throw new ParseException(e.getMessage());
			}
			return b;
		}
		if (param instanceof Double || param instanceof Float) {
			return ((Number)param).longValue();
		}
		if (param instanceof Number) {		// Long, Integer, Short, Byte 
			return param;
		}
		if (param instanceof Timestamp || param instanceof java.sql.Date) {
			Timestamp ts = (Timestamp)param;
			cal.setTimeInMillis(ts.getTime());
			int year = cal.get(YEAR);
			int month = cal.get(MONTH);
			int date = cal.get(DATE);
			cal.clear();
			cal.set(year, month, date);
			return new Timestamp(cal.getTimeInMillis());
		}
		throw new ParseException(WRONG_TYPE+" trunc("+param.getClass()+")");
	}

	public static Comparable trunc(Comparable param1, Comparable param2, Calendar cal) throws ParseException {
		if (param1 == null || param2 == null) {
			return null;
		}
		if (param1 instanceof String) {
			param1 = parse((String)param1);
		}
		if (param1 instanceof Number) {
			int scale;
			try {
				scale = getInteger(param2);
			} catch (ParseException e) {
				throw new ParseException(PARAM_EXCEPTION);
			}
			if (scale < 0) {
				return ZERO;
			}
			if (param1 instanceof BigDecimal) {		// BigInteger is not supported
				return ((BigDecimal)param1).setScale(scale, BigDecimal.ROUND_DOWN);
			}
			else if (param1 instanceof Double || param1 instanceof Float) {
				double d = ((Number)param1).doubleValue();
				long mult = 1;
				for (int i = 0; i < scale; i++) {
					mult *= 10;
				}
				long l = (long)d*mult;
				return ((double)l)/mult;
			} else {
				return param1;			// Long, Integer, Short, Byte
			}
		}
		else if (param1 instanceof java.util.Date) {
			if (param2 instanceof String) {
				String s = (String)param2;
				java.util.Date d = (java.util.Date)param1;
				cal.setTimeInMillis(d.getTime());
				if (s.equalsIgnoreCase("CC") || s.equalsIgnoreCase("SCC")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					cal.clear();
					cal.set((year/100)*100, 0, 1);
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("SYYYY") || s.equalsIgnoreCase("YYYY") || s.equalsIgnoreCase("YYY") || 
							s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("YEAR") || s.equalsIgnoreCase("SYEAR")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					cal.clear();
					cal.set(year, 0, 1);
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("IYYY") || s.equalsIgnoreCase("IY") || s.equalsIgnoreCase("I")) {
					throw new ParseException(NOT_IMPLIMENTED_EXCEPTION);
				}
				else if (s.equalsIgnoreCase("Q")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					int month = cal.get(MONTH);
					cal.clear();
					cal.set(year, (month/3)*3, 1);
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("MONTH") || s.equalsIgnoreCase("MON") || 
							s.equalsIgnoreCase("MM") || s.equalsIgnoreCase("RM")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					int month = cal.get(MONTH);
					cal.clear();
					cal.set(year, month, 1);
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("WW")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					int month = cal.get(MONTH);
					int day = cal.get(DAY_OF_MONTH);
					int dw = cal.get(DAY_OF_WEEK);
					cal.clear();
					cal.set(year, 0, 1);
					int dayOfWeek = cal.get(DAY_OF_WEEK);
					cal.set(year, month, day - (dw<dayOfWeek ? 7-(dayOfWeek-dw) : dw-dayOfWeek));
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("W")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					int month = cal.get(MONTH);
					int day = cal.get(DAY_OF_MONTH);
					int dw = cal.get(DAY_OF_WEEK);
					cal.clear();
					cal.set(year, month, 1);
					int dayOfWeek = cal.get(DAY_OF_WEEK);
					cal.set(year, month, day - (dw<dayOfWeek ? 7-(dayOfWeek-dw) : dw-dayOfWeek));
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("IW")) {
					throw new ParseException(NOT_IMPLIMENTED_EXCEPTION);
				}
				else if (s.equalsIgnoreCase("DAY") || s.equalsIgnoreCase("DY") || s.equalsIgnoreCase("D")) {
					if (d instanceof java.sql.Time) {
						throw new ParseException(TIME_EXCEPTION);
					}
					int year = cal.get(YEAR);
					int month = cal.get(MONTH);
					int day = cal.get(DAY_OF_MONTH);
					int dw = cal.get(DAY_OF_WEEK);
					cal.clear();
					cal.set(year, month, day-(dw-cal.getFirstDayOfWeek()));
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("HH") || s.equalsIgnoreCase("HH12") || s.equalsIgnoreCase("HH24")) {
					if (d instanceof java.sql.Date) {
						throw new ParseException(DATE_EXCEPTION);
					}
					cal.set(MINUTE, 0);
					cal.set(SECOND, 0);
					cal.set(MILLISECOND, 0);
					return new Timestamp(cal.getTimeInMillis());
				}
				else if (s.equalsIgnoreCase("MI")) {
					if (d instanceof java.sql.Date) {
						throw new ParseException(DATE_EXCEPTION);
					}
					cal.set(SECOND, 0);
					cal.set(MILLISECOND, 0);
					return new Timestamp(cal.getTimeInMillis());
				} else {
					throw new ParseException(FORMAT_EXCEPTION);
				}
			}
		}
		throw new ParseException(WRONG_TYPE+" trunc("+param1.getClass()+","+param2.getClass()+")");
	}
}

