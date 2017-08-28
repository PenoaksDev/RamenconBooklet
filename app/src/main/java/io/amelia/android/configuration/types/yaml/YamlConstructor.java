/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.android.configuration.types.yaml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import io.amelia.android.configuration.serialization.ConfigurationSerialization;

public class YamlConstructor extends SafeConstructor
{

	public YamlConstructor()
	{
		this.yamlConstructors.put( Tag.MAP, new ConstructCustomObject() );
	}

	private class ConstructCustomObject extends ConstructYamlMap
	{
		@Override
		public Object construct( Node node )
		{
			if ( node.isTwoStepsConstruction() )
				throw new YAMLException( "Unexpected referential mapping structure. Node: " + node );

			Map<?, ?> raw = ( Map<?, ?> ) super.construct( node );

			if ( raw.containsKey( ConfigurationSerialization.SERIALIZED_TYPE_KEY ) )
			{
				Map<String, Object> typed = new LinkedHashMap<String, Object>( raw.size() );
				for ( Map.Entry<?, ?> entry : raw.entrySet() )
				{
					typed.put( entry.getKey().toString(), entry.getValue() );
				}

				try
				{
					return ConfigurationSerialization.deserializeObject( typed );
				}
				catch ( IllegalArgumentException ex )
				{
					throw new YAMLException( "Could not deserialize object", ex );
				}
			}

			return raw;
		}

		@Override
		public void construct2ndStep( Node node, Object object )
		{
			throw new YAMLException( "Unexpected referential mapping structure. Node: " + node );
		}
	}
}