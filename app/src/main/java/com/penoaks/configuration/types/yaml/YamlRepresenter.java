/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.configuration.types.yaml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import com.penoaks.configuration.ConfigurationSection;
import com.penoaks.configuration.serialization.ConfigurationSerializable;
import com.penoaks.configuration.serialization.ConfigurationSerialization;

public class YamlRepresenter extends Representer
{
	public YamlRepresenter()
	{
		this.multiRepresenters.put( ConfigurationSection.class, new RepresentConfigurationSection() );
		this.multiRepresenters.put( ConfigurationSerializable.class, new RepresentConfigurationSerializable() );
	}
	
	private class RepresentConfigurationSection extends RepresentMap
	{
		@Override
		public Node representData( Object data )
		{
			return super.representData( ( ( ConfigurationSection ) data ).getValues( false ) );
		}
	}
	
	private class RepresentConfigurationSerializable extends RepresentMap
	{
		@Override
		public Node representData( Object data )
		{
			ConfigurationSerializable serializable = ( ConfigurationSerializable ) data;
			Map<String, Object> values = new LinkedHashMap<String, Object>();
			values.put( ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias( serializable.getClass() ) );
			values.putAll( serializable.serialize() );
			
			return super.representData( values );
		}
	}
}
