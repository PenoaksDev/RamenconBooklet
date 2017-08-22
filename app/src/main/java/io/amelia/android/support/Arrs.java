package io.amelia.android.support;

import java.lang.reflect.Array;

class Arrs
{
	public static byte[] concat( byte[] arr1, byte[] arr2 )
	{
		int len1 = arr1.length;
		int len2 = arr2.length;

		byte[] c = new byte[len1 + len2];
		System.arraycopy( arr1, 0, c, 0, len1 );
		System.arraycopy( arr2, 0, c, len1, len2 );

		return c;
	}

	public static <T> T[] concat( T[] arr1, T[] arr2 )
	{
		int len1 = arr1.length;
		int len2 = arr2.length;

		@SuppressWarnings( "unchecked" )
		T[] c = ( T[] ) Array.newInstance( arr1.getClass().getComponentType(), len1 + len2 );
		System.arraycopy( arr1, 0, c, 0, len1 );
		System.arraycopy( arr2, 0, c, len1, len2 );

		return c;
	}

	private Arrs()
	{

	}
}
