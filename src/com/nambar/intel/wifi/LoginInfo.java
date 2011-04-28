package com.nambar.intel.wifi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

public class LoginInfo
{
	private static String file = null;
	private static String username;
	private static String password;
	private static int checkInterval;
	
	public static void init(String file)
	{
		LoginInfo.file = file;
		load();
	}
	public static void load()
	{
		try
		{
			if(!new File(file).exists()) return;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			setUsername(reader.readLine());
			setPassword(reader.readLine());
			setCheckInterval(Integer.valueOf(reader.readLine()));
			reader.close();
		}
		catch(Exception e)
		{
			ExceptionHandler.showException(e);
		}
	}
	
	public static void save()
	{
		try
		{
			String filename = file;
			PrintWriter writer = new PrintWriter(filename);
			writer.println(getUsername());
			writer.println(getPassword());
			writer.println(getCheckInterval());
			writer.close();
		}
		catch(Exception e)
		{
			ExceptionHandler.showException(e);
		}
	}

	public static String getUsername()
	{
		return username;
	}
	public static void setUsername(String username)
	{
		LoginInfo.username = username;
	}
	public static String getPassword()
	{
		return password;
	}
	public static void setPassword(String password)
	{
		LoginInfo.password = password;
	}

	public static int getCheckInterval()
	{
		return checkInterval;
	}
	public static void setCheckInterval(int checkInterval)
	{
		LoginInfo.checkInterval = checkInterval;
	}
}
