/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.android.support;

import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import io.amelia.booklet.App;

public class LibIO
{
	public static final String PATH_SEPERATOR = File.separator;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final int EOF = -1;

	public static File buildFile( boolean absolute, String... args )
	{
		return new File( ( absolute ? PATH_SEPERATOR : "" ) + buildPath( args ) );
	}

	public static File buildFile( File file, String... args )
	{
		return new File( file, buildPath( args ) );
	}

	public static File buildFile( String... args )
	{
		return buildFile( false, args );
	}

	public static String buildPath( String... path )
	{
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for ( String node : path )
		{
			if ( node.isEmpty() )
				continue;

			if ( !first )
				builder.append( PATH_SEPERATOR );

			builder.append( node );

			first = false;
		}

		return builder.toString();
	}

	public static String bytesToStringUTFNIO( byte[] bytes )
	{
		if ( bytes == null )
			return null;

		CharBuffer cBuffer = ByteBuffer.wrap( bytes ).asCharBuffer();
		return cBuffer.toString();
	}

	public static boolean checkMd5( File file, String expectedMd5 )
	{
		if ( expectedMd5 == null || file == null || !file.exists() )
			return false;

		String md5 = md5( file );
		return md5 != null && md5.equals( expectedMd5 );
	}

	public static void closeQuietly( Closeable closeable )
	{
		try
		{
			if ( closeable != null )
				closeable.close();
		}
		catch ( IOException ioe )
		{
			// ignore
		}
	}

	public static int copy( InputStream input, OutputStream output ) throws IOException
	{
		long count = copyLarge( input, output );
		if ( count > Integer.MAX_VALUE )
			return -1;
		return ( int ) count;
	}

	/**
	 * This method copies one file to another location
	 *
	 * @param inFile  the source filename
	 * @param outFile the target filename
	 * @return true on success
	 */
	@SuppressWarnings( "resource" )
	public static boolean copy( File inFile, File outFile )
	{
		if ( !inFile.exists() )
			return false;

		FileChannel in = null;
		FileChannel out = null;

		try
		{
			in = new FileInputStream( inFile ).getChannel();
			out = new FileOutputStream( outFile ).getChannel();

			long pos = 0;
			long size = in.size();

			while ( pos < size )
				pos += in.transferTo( pos, 10 * 1024 * 1024, out );
		}
		catch ( IOException ioe )
		{
			return false;
		}
		finally
		{
			try
			{
				if ( in != null )
					in.close();
				if ( out != null )
					out.close();
			}
			catch ( IOException ioe )
			{
				return false;
			}
		}

		return true;
	}

	public static long copyLarge( InputStream input, OutputStream output ) throws IOException
	{
		return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE] );
	}

	public static long copyLarge( InputStream input, OutputStream output, byte[] buffer ) throws IOException
	{
		long count = 0;
		int n = 0;
		while ( EOF != ( n = input.read( buffer ) ) )
		{
			output.write( buffer, 0, n );
			count += n;
		}
		return count;
	}

	public static String fileExtension( File file )
	{
		return fileExtension( file.getName() );
	}

	public static String fileExtension( String fileName )
	{
		return Strs.regexCapture( fileName, ".*\\.(.*)$" );
	}

	public static String getFileName( String path )
	{
		path = path.replace( "\\/", "/" );
		if ( path.contains( File.pathSeparator ) )
			return path.substring( path.lastIndexOf( File.pathSeparator ) + 1 );
		if ( path.contains( "/" ) )
			return path.substring( path.lastIndexOf( "/" ) + 1 );
		if ( path.contains( "\\" ) )
			return path.substring( path.lastIndexOf( "\\" ) + 1 );
		return path;
	}

	public static String getFileNameWithoutExtension( String path )
	{
		path = getFileName( path );
		if ( path.contains( "." ) )
			path = path.substring( 0, path.lastIndexOf( "." ) );
		return path;
	}

	/**
	 * List directory contents for a resource folder. Not recursive.
	 * This is basically a brute-force implementation.
	 * Works for regular files and also JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path  Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 * @author Greg Briggs
	 */
	public static String[] getResourceListing( Class<?> clazz, String path ) throws URISyntaxException, IOException
	{
		URL dirURL = clazz.getClassLoader().getResource( path );

		if ( dirURL == null )
		{
			/*
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace( ".", "/" ) + ".class";
			dirURL = clazz.getClassLoader().getResource( me );
		}

		if ( dirURL.getProtocol().equals( "file" ) )
			/* A file path: easy enough */
			return new File( dirURL.toURI() ).list();

		if ( dirURL.getProtocol().equals( "jar" ) )
		{
			/* A JAR path */
			String jarPath = dirURL.getPath().substring( 5, dirURL.getPath().indexOf( "!" ) ); // strip out only the JAR file
			JarFile jar = new JarFile( URLDecoder.decode( jarPath, "UTF-8" ) );
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
			Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
			while ( entries.hasMoreElements() )
			{
				String name = entries.nextElement().getName();
				if ( name.startsWith( path ) )
				{ // filter according to the path
					String entry = name.substring( path.length() );
					int checkSubdir = entry.indexOf( "/" );
					if ( checkSubdir >= 0 )
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring( 0, checkSubdir );
					result.add( entry );
				}
			}
			jar.close();
			return result.toArray( new String[result.size()] );
		}

		if ( dirURL.getProtocol().equals( "zip" ) )
		{
			/* A ZIP path */
			String zipPath = dirURL.getPath().substring( 5, dirURL.getPath().indexOf( "!" ) ); // strip out only the JAR file
			ZipFile zip = new ZipFile( URLDecoder.decode( zipPath, "UTF-8" ) );
			Enumeration<? extends ZipEntry> entries = zip.entries(); // gives ALL entries in jar
			Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
			while ( entries.hasMoreElements() )
			{
				String name = entries.nextElement().getName();
				if ( name.startsWith( path ) )
				{ // filter according to the path
					String entry = name.substring( path.length() );
					int checkSubdir = entry.indexOf( "/" );
					if ( checkSubdir >= 0 )
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring( 0, checkSubdir );
					result.add( entry );
				}
			}
			zip.close();
			return result.toArray( new String[result.size()] );
		}

		throw new UnsupportedOperationException( "Cannot list files for URL " + dirURL );
	}

	public static String getStringFromFile( String filePath ) throws IOException
	{
		return getStringFromFile( new File( filePath ) );
	}

	public static String getStringFromFile( File file ) throws IOException
	{
		return getStringFromFile( file, null );
	}

	public static String getStringFromFile( File file, String def ) throws IOException
	{
		try
		{
			FileInputStream fin = new FileInputStream( file );
			String ret = inputStream2String( fin );
			fin.close();
			return ret;
		}
		catch ( FileNotFoundException e )
		{
			if ( def == null )
				throw e;
			return def;
		}
	}

	public static void gzFile( File source ) throws IOException
	{
		gzFile( source, new File( source + ".gz" ) );
	}

	public static void gzFile( File source, File dest ) throws IOException
	{
		byte[] buffer = new byte[1024];

		GZIPOutputStream gzos = new GZIPOutputStream( new FileOutputStream( dest ) );

		FileInputStream in = new FileInputStream( source );

		int len;
		while ( ( len = in.read( buffer ) ) > 0 )
			gzos.write( buffer, 0, len );

		in.close();

		gzos.finish();
		gzos.close();
	}

	public static ByteArrayOutputStream inputStream2ByteArray( InputStream is ) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		int len;
		byte[] buffer = new byte[4096];
		while ( -1 != ( len = is.read( buffer ) ) )
			bos.write( buffer, 0, len );

		bos.flush();
		return bos;
	}

	public static byte[] inputStream2Bytes( InputStream is ) throws IOException
	{
		return inputStream2ByteArray( is ).toByteArray();
	}

	public static String inputStream2String( InputStream is ) throws IOException
	{
		return new String( inputStream2Bytes( is ) );
	}

	public static boolean isAbsolute( String dir )
	{
		return dir.startsWith( "/" ) || dir.startsWith( ":\\", 1 );
	}

	public static boolean isDirectoryEmpty( File file )
	{
		Objs.notNull( file, "file is null" );

		return file.exists() && file.isDirectory() && file.list().length == 0;
	}

	public static String md5( File file )
	{
		return LibEncrypt.md5( file );
	}

	public static boolean putStringToFile( File file, String str ) throws IOException
	{
		return putStringToFile( file, str, false );
	}

	public static boolean putStringToFile( File file, String str, boolean append ) throws IOException
	{
		FileOutputStream fos = null;
		try
		{
			if ( !append && file.exists() )
				file.delete();

			fos = new FileOutputStream( file );
			fos.write( str.getBytes() );
		}
		finally
		{
			try
			{
				if ( fos != null )
					fos.close();
			}
			catch ( IOException ignore )
			{

			}
		}

		return true;
	}

	public static String readFileToString( File file ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream( file );

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = in.read( data, 0, data.length ) ) != -1 )
				buffer.write( data, 0, nRead );

			return new String( buffer.toByteArray(), Charset.defaultCharset() );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	public static List<File> recursiveFiles( final File dir )
	{
		return recursiveFiles( dir, 9999 );
	}

	private static List<File> recursiveFiles( final File start, final File current, final int depth, final int maxDepth, final String regexPattern )
	{
		final List<File> files = new ArrayList<>();

		current.list( new FilenameFilter()
		{
			@Override
			public boolean accept( File dir, String name )
			{
				dir = new File( dir, name );

				if ( dir.isDirectory() && depth < maxDepth )
					files.addAll( recursiveFiles( start, dir, depth + 1, maxDepth, regexPattern ) );

				if ( dir.isFile() )
				{
					String filename = dir.getAbsolutePath();
					filename = filename.substring( start.getAbsolutePath().length() + 1 );
					if ( regexPattern == null || filename.matches( regexPattern ) )
						files.add( dir );
				}

				return false;
			}
		} );

		return files;
	}

	public static List<File> recursiveFiles( final File dir, final int maxDepth )
	{
		return recursiveFiles( dir, maxDepth, null );
	}

	public static List<File> recursiveFiles( final File dir, final String regexPattern )
	{
		return recursiveFiles( dir, dir, 0, 9999, regexPattern );
	}

	public static List<File> recursiveFiles( final File dir, final int maxDepth, final String regexPattern )
	{
		return recursiveFiles( dir, dir, 0, maxDepth, regexPattern );
	}

	public static String relPath( File file, File relTo )
	{
		if ( file == null || relTo == null )
			return null;
		String root = relTo.getAbsolutePath();
		if ( file.getAbsolutePath().startsWith( root ) )
			return file.getAbsolutePath().substring( root.length() + 1 );
		else
			return file.getAbsolutePath();
	}

	public static String resourceToString( String resource ) throws IOException
	{
		return resourceToString( resource, App.class );
	}

	public static String resourceToString( String resource, Class<?> clz ) throws IOException
	{
		InputStream is = clz.getClassLoader().getResourceAsStream( resource );

		if ( is == null )
			return null;

		return new String( inputStream2Bytes( is ), "UTF-8" );
	}

	public static void writeStringToFile( File file, String data ) throws IOException
	{
		writeStringToFile( file, data, false );
	}

	public static void writeStringToFile( File file, String data, boolean append ) throws IOException
	{
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter( new FileWriter( file, append ) );
			out.write( data );
		}
		finally
		{
			if ( out != null )
				out.close();
		}
	}

	public static void zipDir( File src, File dest ) throws IOException
	{
		if ( dest.isDirectory() )
			dest = new File( dest, "temp.zip" );

		ZipOutputStream out = new ZipOutputStream( new FileOutputStream( dest ) );
		try
		{
			zipDirRecursive( src, src, out );
		}
		finally
		{
			out.close();
		}
	}

	private static void zipDirRecursive( File origPath, File dirObj, ZipOutputStream out ) throws IOException
	{
		File[] files = dirObj.listFiles();
		byte[] tmpBuf = new byte[1024];

		for ( int i = 0; i < files.length; i++ )
		{
			if ( files[i].isDirectory() )
			{
				zipDirRecursive( origPath, files[i], out );
				continue;
			}
			FileInputStream in = new FileInputStream( files[i].getAbsolutePath() );
			out.putNextEntry( new ZipEntry( relPath( files[i], origPath ) ) );
			int len;
			while ( ( len = in.read( tmpBuf ) ) > 0 )
				out.write( tmpBuf, 0, len );
			out.closeEntry();
			in.close();
		}
	}

	private LibIO()
	{

	}

	static class LibraryPath
	{
		private List<String> libPath;

		LibraryPath()
		{
			read();
		}

		void add( String path )
		{
			if ( path.contains( " " ) )
				path = "\"" + path + "\"";
			if ( libPath.contains( path ) )
				return;
			libPath.add( path );
		}

		void read()
		{
			libPath = new ArrayList<>( Arrays.asList( TextUtils.split( ":", System.getProperty( "java.library.path" ) ) ) );
		}

		void set()
		{
			System.setProperty( "java.library.path", TextUtils.join( ":", libPath ) );
		}
	}

	public static class SortableFile implements Comparable<SortableFile>
	{
		public File f;
		public long t;

		public SortableFile( File file )
		{
			f = file;
			t = file.lastModified();
		}

		@Override
		public int compareTo( SortableFile o )
		{
			long u = o.t;
			return t < u ? -1 : t == u ? 0 : 1;
		}
	}
}
