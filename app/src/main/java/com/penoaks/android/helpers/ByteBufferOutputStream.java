/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.android.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Wraps a ByteBuffer in an OutputStream for writing
 */
public class ByteBufferOutputStream extends OutputStream
{
	ByteBuffer buf;
	
	public ByteBufferOutputStream( ByteBuffer buf )
	{
		this.buf = buf;
	}
	
	@Override
	public void write( int b ) throws IOException
	{
		buf.put( ( byte ) b );
	}
	
	@Override
	public void write( byte[] bytes, int off, int len ) throws IOException
	{
		buf.put( bytes, off, len );
	}
}
