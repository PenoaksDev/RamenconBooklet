package io.amelia.android.data;

import android.os.Parcelable;
import android.text.TextUtils;

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
		allowableTypes.add( BoundData.class );
	}

	private Set<Entry<String, Object>> values = new HashSet<>();

	private Object collectionToValue( Collection value )
	{
		if ( value.size() == 0 )
			return new ArrayList<>();

		// Check if the entire list contains the same class type
		Object first = Lists.first( value );
		if ( Lists.isOfType( value, first.getClass() ) )
		{
			if ( first instanceof Collection )
			{
				List<Object> list = new ArrayList<>();
				for ( Object obj : value )
					list.add( collectionToValue( ( Collection ) obj ) );
				return list;
			}


			if ( isAllowedType( first.getClass() ) )
				return value;
		}

		throw new IllegalArgumentException( "Unparcelable list type: " + value.getClass().getSimpleName() + " {" + TextUtils.join( ", ", Lists.listClasses( ( Collection ) value ) ) + "}" );
	}

	public String dump()
	{
		return Objs.dumpObject( this );
	}

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

	public List<BoundData> getBoundDataList( String keyId )
	{
		List<?> list = getList( keyId, null );
		if ( list == null )
			return null;
		return Objs.castList( list, BoundData.class );
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

	public boolean isAllowedType( Class<?> aClass )
	{
		for ( Class<?> bClass : allowableTypes )
			if ( bClass.isAssignableFrom( bClass ) )
				return true;
		return false;
	}

	public Object put( String key, Object value, Class<?> aClass )
	{
		synchronized ( this )
		{
			Object oldValue = get( key );

			if ( value != null && !isAllowedType( value.getClass() ) )
				if ( value instanceof Collection )
					value = collectionToValue( ( Collection ) value );
				else
					throw new IllegalArgumentException( "Unparcelable type: " + value.getClass().getSimpleName() );

			values.add( new BoundDataEntry( key, value, aClass ) );
			return oldValue;
		}
	}

	@Override
	public Object put( String key, Object value )
	{
		return put( key, value, value == null ? null : value.getClass() );
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
			if ( aClass == Object.class && !isAllowedType( value.getClass() ) )
				throw new IllegalArgumentException( "Value must be an allowable type of: " + allowableTypes );
			else if ( !aClass.isAssignableFrom( value.getClass() ) )
				throw new IllegalArgumentException( "Value must be the same as or a superclass of class " + aClass.getSimpleName() );

			return super.setValue( value );
		}
	}
}
