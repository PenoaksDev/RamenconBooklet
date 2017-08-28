package io.amelia.android.data;

import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.amelia.android.support.Lists;
import io.amelia.android.support.Objs;

public class BoundData extends AbstractMap<String, Object>
{
	private static final List<Class<?>> allowableTypes = new ArrayList<>();

	static
	{
		allowableTypes.add( String.class );
		allowableTypes.add( Boolean.class );
		allowableTypes.add( Integer.class );
		allowableTypes.add( Long.class );
		allowableTypes.add( Double.class );
		allowableTypes.add( Parcelable.class );
	}

	public static BoundData toBoundData( Collection<Pair<String, Object>> values )
	{
		BoundData boundData = new BoundData();
		for ( Pair<String, Object> entry : values )
			boundData.put( entry.first, entry.second );
		return boundData;
	}

	private Set<Entry<String, Object>> values = new HashSet<>();

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		return values;
	}

	public Boolean getBoolean( String keyId )
	{
		return Objs.castToBoolean( get( keyId ), false );
	}

	public Boolean getBoolean( String keyId, Boolean def )
	{
		return Objs.castToBoolean( get( keyId ), def );
	}

	public BoundData getBoundData( String keyId )
	{
		return ( BoundData ) get( keyId );
	}

	public BoundData getBoundData( String keyId, BoundData def )
	{
		Object result = get( keyId );
		return result == null || !( result instanceof BoundData ) ? def : ( BoundData ) result;
	}

	public Double getDouble( String keyId )
	{
		return Objs.castToDouble( get( keyId ), null );
	}

	public Double getDouble( String keyId, Double def )
	{
		return Objs.castToDouble( get( keyId ), def );
	}

	public Integer getInt( String keyId )
	{
		return getInteger( keyId );
	}

	public Integer getInteger( String keyId )
	{
		return Objs.castToInt( get( keyId ), null );
	}

	public Integer getInteger( String keyId, Integer def )
	{
		return Objs.castToInt( get( keyId ), def );
	}

	public List getList( String keyId )
	{
		return ( List ) get( keyId );
	}

	public List getList( String keyId, List def )
	{
		Object result = get( keyId );
		return result == null || !( result instanceof List ) ? def : ( List ) result;
	}

	public Long getLong( String keyId )
	{
		return Objs.castToLong( get( keyId ), null );
	}

	public Long getLong( String keyId, Long def )
	{
		return Objs.castToLong( get( keyId ), def );
	}

	public Parcelable getParcelable( String keyId )
	{
		return ( Parcelable ) get( keyId );
	}

	public Parcelable getParcelable( String keyId, Parcelable def )
	{
		Object result = get( keyId );
		return result == null || !( result instanceof Parcelable ) ? def : ( Parcelable ) result;
	}

	public String getString( String keyId )
	{
		return Objs.castToString( get( keyId ) );
	}

	public String getString( String keyId, String def )
	{
		return Objs.castToString( get( keyId ), def );
	}

	public Set<Entry<String, String>> getStringEntrySet()
	{
		return new HashSet<Entry<String, String>>()
		{{
			for ( String key : keySet() )
			{
				String str = Objs.castToString( get( key ) );
				if ( str != null )
					add( new SimpleEntry<>( key, str ) );
			}
		}};
	}

	public List<String> getStringList( String keyId )
	{
		List<?> list = getList( keyId, null );
		if ( list == null )
			return null;
		return Objs.castList( list, String.class );
	}

	public boolean hasBoundData( String keyId )
	{
		try
		{
			return getBoundData( keyId ) != null;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	public boolean hasString( String keyId )
	{
		return getString( keyId ) != null;
	}

	public Object put( String key, Object value, Class<?> aClass )
	{
		Object oldValue = get( key );

		boolean assignable = false;

		for ( Class<?> bClass : allowableTypes )
			if ( bClass.isAssignableFrom( value.getClass() ) )
				assignable = true;

		if ( !assignable )
			if ( value instanceof Collection )
			{
				// Check if the entire list contains the same class type
				Object first = Lists.first( ( Collection ) value );
				if ( Lists.isOfType( ( Collection ) value, first.getClass() ) )
				{
					if ( Pair.class.isAssignableFrom( first.getClass() ) )
					{
						value = toBoundData( ( Collection ) value );
						assignable = true;
					}
					else
						for ( Class<?> bClass : allowableTypes )
							if ( bClass.isAssignableFrom( first.getClass() ) )
								assignable = true;
				}

				if ( !assignable )
					throw new IllegalArgumentException( "Unparcelable list type: " + value.getClass().getSimpleName() + " {" + TextUtils.join( ", ", Lists.listClasses( ( Collection ) value ) ) + "}" );
			}
			/* else if ( value instanceof Map )
			{

			} */
			else
				throw new IllegalArgumentException( "Unparcelable type: " + value.getClass().getSimpleName() );

		values.add( new BoundDataEntry( key, value, aClass ) );
		return oldValue;
	}

	@Override
	public Object put( String key, Object value )
	{
		return put( key, value, value.getClass() );
	}

	private class BoundDataEntry extends SimpleEntry<String, Object>
	{
		Class<?> aClass;

		public BoundDataEntry( String key, Object value, Class<?> aClass )
		{
			super( key, value );

			this.aClass = aClass;
		}

		public BoundDataEntry( String key, Object value )
		{
			super( key, value );

			aClass = value.getClass();
		}

		public BoundDataEntry( Entry<? extends String, ?> entry )
		{
			super( entry );

			aClass = entry.getValue().getClass();
		}

		@Override
		public Object setValue( Object value )
		{
			if ( aClass.isAssignableFrom( value.getClass() ) )
				throw new IllegalArgumentException( "Value must be the same as or a superclass of class " + aClass.getSimpleName() );

			return super.setValue( value );
		}
	}
}
