package com.nambar.intel.wifi;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.net.http.AndroidHttpClient;

public class NetworkLogin
{
	final String URL = "https://apc.aptilo.com/cgi-bin/login?acceptedurl=http%3A%2F%2Fwww.google.co.il%2F&deniedpage=%2Fpas%2Fstart%3Fmodule%3Dstart%26command%3Dstart%26key%3Duserlogin-fa0b9652b5946bf135fde09e3c9b28c3%26denied%3Dtrue&showsession=true&key=userlogin-fa0b9652b5946bf135fde09e3c9b28c3";
	final String TEST_URL = "http://www.google.com";
	private String message;

	public boolean login()
	{
		try
		{
			String url = URL + "&term1=ON&username=" + LoginInfo.getUsername() + "&password=" + LoginInfo.getPassword();
			HttpClient client = AndroidHttpClient.newInstance("android");
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = client.execute(httpget);
			return response.getStatusLine().getStatusCode() == 200;
		}
		catch(Exception e)
		{
			message = e.getMessage();
			return false;
		}
	}

	public boolean test()
	{
		try
		{
			HttpClient client = AndroidHttpClient.newInstance("android");
			HttpGet httpget = new HttpGet(TEST_URL);
			HttpResponse response = client.execute(httpget);
			if(response.getStatusLine().getStatusCode() == 302)
			{
				message = "Internet access is available";
				return true;
			}
			return false;
		}
		catch(Exception e)
		{
			message = e.getMessage();
			return false;
		}
	}
	
	public String getMessage()
	{
		return message;
	}
}
