package com.penoaks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Files
{
	public static String convertStreamToString(InputStream is) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null)
			sb.append(line).append("\n");
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws IOException
	{
		return getStringFromFile(new File(filePath));
	}

	public static String getStringFromFile(File file) throws IOException
	{
		return getStringFromFile(file, null);
	}

	public static String getStringFromFile(File file, String def) throws IOException
	{
		try
		{
			FileInputStream fin = new FileInputStream(file);
			String ret = convertStreamToString(fin);
			fin.close();
			return ret;
		}
		catch (FileNotFoundException e)
		{
			if (def == null)
				throw e;
			return def;
		}
	}

	public static boolean putStringToFile(File file, String str) throws IOException
	{
		return putStringToFile(file, str, false);
	}

	public static boolean putStringToFile(File file, String str, boolean append) throws IOException
	{
		FileOutputStream fos = null;
		try
		{
			if (!append && file.exists())
				file.delete();

			fos = new FileOutputStream(file);
			fos.write(str.getBytes());
		}
		finally
		{
			try
			{
				if (fos != null)
					fos.close();
			}
			catch (IOException ignore)
			{

			}
		}

		return true;
	}
}
