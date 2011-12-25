package com.nambar.intel.wifi;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

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

			
		    HttpPost post = new HttpPost(new URI(url));
//		    post.setEntity(new StringEntity(BODY));

		    KeyStore trusted = KeyStore.getInstance("BKS");
		    trusted.load(null, "".toCharArray());
		    SSLSocketFactory sslf = new MySSLSocketFactory(trusted);
		    sslf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		    SchemeRegistry schemeRegistry = new SchemeRegistry();
		    schemeRegistry.register(new Scheme ("https", sslf, 443));
		    SingleClientConnManager cm = new SingleClientConnManager(post.getParams(),
		            schemeRegistry);

		    HttpClient client = new DefaultHttpClient(cm, post.getParams());
			
//			HttpClient client = AndroidHttpClient.newInstance("android");
//			HttpGet httpget = new HttpGet(url);
//			HttpResponse response = client.execute(httpget);
//			
			HttpResponse response = client.execute(post);
			return response.getStatusLine().getStatusCode() == 200;
		}
		catch(Exception e)
		{
			message = e.getMessage();
			return false;
		}
	}

	public class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
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
