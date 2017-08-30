/*
 * Copyright (C) 2015 Gson Type Adapter Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amelia.android.data;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoundDataTypeAdapterFactory implements TypeAdapterFactory
{
	@SuppressWarnings( "unchecked" )
	@Override
	public <T> TypeAdapter<T> create( final Gson gson, TypeToken<T> type )
	{
		if ( !BoundData.class.isAssignableFrom( type.getRawType() ) )
			return null;

		return ( TypeAdapter<T> ) new TypeAdapter<BoundData>()
		{
			@Override
			public BoundData read( JsonReader in ) throws IOException
			{
				switch ( in.peek() )
				{
					case NULL:
						in.nextNull();
						return null;
					case BEGIN_OBJECT:
						return readObject( in );
					default:
						throw new IOException( "expecting object: " + in.getPath() );
				}
			}

			@SuppressWarnings( "rawtypes" )
			private List readArray( JsonReader in ) throws IOException
			{
				List list = new ArrayList();
				in.beginArray();
				while ( in.peek() != JsonToken.END_ARRAY )
				{
					Object element = readValue( in );
					list.add( element );
				}
				in.endArray();
				return list;
			}

			private Object readNumber( JsonReader in ) throws IOException
			{
				double doubleValue = in.nextDouble();
				if ( doubleValue - Math.ceil( doubleValue ) == 0 )
				{
					long longValue = ( long ) doubleValue;
					if ( longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE )
					{
						return ( int ) longValue;
					}
					return longValue;
				}
				return doubleValue;
			}

			private BoundData readObject( JsonReader in ) throws IOException
			{
				BoundData object = new BoundData();
				in.beginObject();
				while ( in.peek() != JsonToken.END_OBJECT )
				{
					switch ( in.peek() )
					{
						case NAME:
							String name = in.nextName();
							Object value = readValue( in );
							object.put( name, value );
							break;
						case END_OBJECT:
							break;
						default:
							throw new IOException( "expecting object: " + in.getPath() );
					}
				}
				in.endObject();
				return object;
			}

			private Object readValue( JsonReader in ) throws IOException
			{
				switch ( in.peek() )
				{
					case BEGIN_ARRAY:
						return readArray( in );
					case BEGIN_OBJECT:
						return readObject( in );
					case BOOLEAN:
						return in.nextBoolean();
					case NULL:
						in.nextNull();
						return null;
					case NUMBER:
						return readNumber( in );
					case STRING:
						return in.nextString();
					default:
						throw new IOException( "expecting value: " + in.getPath() );
				}
			}

			@Override
			public void write( JsonWriter out, BoundData bundle ) throws IOException
			{
				if ( bundle == null )
				{
					out.nullValue();
					return;
				}
				out.beginObject();
				for ( String key : bundle.keySet() )
				{
					out.name( key );
					Object value = bundle.get( key );
					if ( value == null )
						out.nullValue();
					else
						gson.toJson( value, value.getClass(), out );
				}
				out.endObject();
			}
		};
	}
}
