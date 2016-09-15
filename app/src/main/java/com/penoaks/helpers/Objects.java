package com.penoaks.helpers;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Objects
{
	private Objects()
	{

	}

	@SuppressWarnings("unchecked")
	public static <O> O castThis(Class<?> clz, Object o)
	{
		try
		{
			if (clz == Integer.class)
				return (O) castToIntWithException(o);
			if (clz == Long.class)
				return (O) castToLongWithException(o);
			if (clz == Double.class)
				return (O) castToDoubleWithException(o);
			if (clz == Boolean.class)
				return (O) castToBoolWithException(o);
			if (clz == String.class)
				return (O) castToStringWithException(o);
		}
		catch (Exception e1)
		{
			try
			{
				return (O) o;
			}
			catch (Exception e2)
			{
				try
				{
					return (O) castToStringWithException(o);
				}
				catch (Exception e3)
				{
					try
					{
						/*
						 * Last and final attempt to get something out of this
						 * object even if it results in the toString() method.
						 */
						return (O) ("" + o);
					}
					catch (Exception e4)
					{

					}
				}
			}
		}

		return null;
	}

	public static Boolean castToBool(Object value)
	{
		try
		{
			return castToBoolWithException(value);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static Boolean castToBoolWithException(Object value) throws ClassCastException
	{
		if (value == null)
			throw new ClassCastException("Can't cast `null` to Boolean");

		if (value.getClass() == boolean.class || value.getClass() == Boolean.class)
			return (boolean) value;

		String val = castToStringWithException(value);

		if (val == null)
			throw new ClassCastException("Uncaught Convertion to Boolean of Type: " + value.getClass().getName());

		switch (val.trim().toLowerCase())
		{
			case "yes":
				return true;
			case "no":
				return false;
			case "true":
				return true;
			case "false":
				return false;
			case "1":
				return true;
			case "0":
				return false;
			default:
				throw new ClassCastException("Uncaught Convertion to Boolean of Type: " + value.getClass().getName());
		}
	}

	public static Double castToDouble(Object value)
	{
		try
		{
			return castToDoubleWithException(value);
		}
		catch (Exception e)
		{
			return 0D;
		}
	}

	public static Double castToDoubleWithException(Object value)
	{
		if (value == null)
			throw new ClassCastException("Can't cast `null` to Double");

		if (value instanceof Long)
			return ((Long) value).doubleValue();
		if (value instanceof String)
			return Double.parseDouble((String) value);
		if (value instanceof Integer)
			return ((Integer) value).doubleValue();
		if (value instanceof Double)
			return (Double) value;
		if (value instanceof Boolean)
			return (boolean) value ? 1D : 0D;
		if (value instanceof BigDecimal)
			return ((BigDecimal) value).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();

		throw new ClassCastException("Uncaught Convertion to Integer of Type: " + value.getClass().getName());
	}

	public static Integer castToInt(Object value)
	{
		try
		{
			return castToIntWithException(value);
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	public static Integer castToIntWithException(Object value)
	{
		if (value == null)
			throw new ClassCastException("Can't cast `null` to Integer");

		if (value instanceof Long)
			if ((long) value < Integer.MIN_VALUE || (long) value > Integer.MAX_VALUE)
				return (Integer) value;
			else
				return null;
		if (value instanceof String)
			return Integer.parseInt((String) value);
		if (value instanceof Integer)
			return (Integer) value;
		if (value instanceof Double)
			return (Integer) value;
		if (value instanceof Boolean)
			return (boolean) value ? 1 : 0;
		if (value instanceof BigDecimal)
			return ((BigDecimal) value).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

		throw new ClassCastException("Uncaught Convertion to Integer of Type: " + value.getClass().getName());
	}

	public static Long castToLong(Object value)
	{
		try
		{
			return castToLongWithException(value);
		}
		catch (ClassCastException e)
		{
			e.printStackTrace();
			return 0L;
		}
	}

	public static Long castToLongWithException(Object value)
	{
		if (value == null)
			throw new ClassCastException("Can't cast `null` to Long");

		if (value instanceof Long)
			return (Long) value;
		if (value instanceof String)
			return Long.parseLong((String) value);
		if (value instanceof Integer)
			return Long.parseLong("" + value);
		if (value instanceof Double)
			return Long.parseLong("" + value);
		if (value instanceof Boolean)
			return (boolean) value ? 1L : 0L;
		if (value instanceof BigDecimal)
			return ((BigDecimal) value).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

		throw new ClassCastException("Uncaught Convertion to Long of Type: " + value.getClass().getName());
	}

	public static String castToString(Object value)
	{
		try
		{
			return castToStringWithException(value);
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public static String castToStringWithException(final Object value) throws ClassCastException
	{
		if (value == null)
			return null;
		if (value instanceof Long)
			return Long.toString((long) value);
		if (value instanceof String)
			return (String) value;
		if (value instanceof Integer)
			return Integer.toString((int) value);
		if (value instanceof Double)
			return Double.toString((double) value);
		if (value instanceof Boolean)
			return (boolean) value ? "true" : "false";
		if (value instanceof BigDecimal)
			return ((BigDecimal) value).toString();
		if (value instanceof Map)
		{
			return TextUtils.join(",", new ArrayList<String>()
			{{
				for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet())
					add(castToString(e.getKey()) + "=\"" + castToString(e.getValue()) + "\"");
			}});
		}
		if (value instanceof List)
			return TextUtils.join(",", (List) value);
		throw new ClassCastException("Uncaught Convertion to String of Type: " + value.getClass().getName());
	}

	public static Boolean isNull(Object o)
	{
		if (o == null)
			return true;
		return false;
	}

	public static void isTrue(final boolean expression)
	{
		if (expression == false)
			throw new IllegalArgumentException("The validated expression is false");
	}

	public static void isTrue(final boolean expression, final String msg)
	{
		if (expression == false)
			throw new IllegalArgumentException(msg);
	}

	public static boolean noLoopDetected(Class<?> cls, String method)
	{
		return noLoopDetected(cls.getCanonicalName(), method, 1);
	}

	public static boolean noLoopDetected(Class<?> cls, String method, int max)
	{
		return noLoopDetected(cls.getCanonicalName(), method, max);
	}

	public static boolean noLoopDetected(String cls, String method)
	{
		return noLoopDetected(cls, method, 1);
	}

	/**
	 * Detects if the specified class and method has been called in a previous stack trace event.
	 *
	 * @param cls    The class to check.
	 * @param method The method to check, null to ignore.
	 * @param max    The maximum number of recurrence until failure.
	 * @return True if no loop was detected.
	 */
	public static boolean noLoopDetected(String cls, String method, int max)
	{
		int cnt = 0;
		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
			if (ste.getClassName().equals(cls) && (method == null || ste.getMethodName().equals(method)))
			{
				cnt++;
				if (cnt >= max)
					return false;
			}
		return true;
	}

	public static <T extends CharSequence> T notEmpty(final T chars, final String message, final Object... values)
	{
		if (chars == null)
			throw new NullPointerException(String.format(message, values));
		if (chars.length() == 0)
			throw new IllegalArgumentException(String.format(message, values));
		return chars;
	}

	public static <T> T notNull(final T object)
	{
		return notNull(object, "Object is null");
	}

	public static <T> T notNull(final T object, final String message, final Object... values)
	{
		if (object == null)
			throw new NullPointerException(String.format(message, values));
		return object;
	}

	public static int safeLongToInt(long l)
	{
		if (l < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		if (l > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int) l;
	}
}
