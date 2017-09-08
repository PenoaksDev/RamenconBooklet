package io.amelia.android.files;

/**
 * Created by amelia on 9/8/17.
 */
public class FileBuilderException extends Exception
{
	private FileBuilder fileBuilder;

	public FileBuilderException( FileBuilder fileBuilder, String message )
	{
		super( message );
		this.fileBuilder = fileBuilder;
	}

	public FileBuilderException( FileBuilder fileBuilder, String message, Exception exception )
	{
		super( message, exception );
		this.fileBuilder = fileBuilder;
	}

	public FileBuilder getFileBuilder()
	{
		return fileBuilder;
	}

	public String getId()
	{
		return getFileBuilder().id;
	}
}
