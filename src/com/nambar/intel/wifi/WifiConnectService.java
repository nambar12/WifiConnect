package com.nambar.intel.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class WifiConnectService extends Service
{
	private final String NETWORK_SSID = "Guest";
	private NotificationManager notificationManager;
	private String currentSSID = null;
	private String status;
	private final IBinder binder = new LocalBinder();
	private PeriodicLogin periodicLogin = new PeriodicLogin();
	private volatile boolean shouldRun = true;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		registerReceiver(receiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        check();
        scheduleLogin();
		return START_STICKY;
	}

	private void scheduleLogin()
	{
		try
		{
			synchronized(periodicLogin)
			{
				shouldRun = false;
				if(periodicLogin.isAlive())
				{
					periodicLogin.interrupt();
					periodicLogin.join();
				}
			}
		}
		catch (InterruptedException e)
		{
			ExceptionHandler.showException(e);
		}
		
		
		long delay = LoginInfo.getCheckInterval() * 60;
        if(delay > 0)
       	{
        	shouldRun = true;
        	periodicLogin = new PeriodicLogin();
        	periodicLogin.start();
       	}
	}

	public class LocalBinder extends Binder
	{
		String getCurrentSSID()
		{
			return currentSSID;
		}
		
		String getStatus()
		{
			return status;
		}
		
		void hup()
		{
			LoginInfo.load();
			scheduleLogin();
			check();
			
		}
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

    private BroadcastReceiver receiver  = new BroadcastReceiver()
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		check();
		}
    };
    
	public void check()
	{
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
		{
			return;
		}
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(netInfo.getState() == NetworkInfo.State.CONNECTED)
		{
		    WifiManager manager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		    if(manager.getConnectionInfo().getSSID().equals(NETWORK_SSID))
		    {
			    showNotification("Connected to " + manager.getConnectionInfo().getSSID(), R.drawable.icon);
		    	login();
		    }
		}
		else
		{
			currentSSID = null;
			hideNotification();
		}
	}

	private void login()
	{
		final NetworkLogin login = new NetworkLogin();
		showNotification(R.string.TryConnect, R.drawable.icon);
		try
		{
			new AsyncTask<Void, Void, Boolean>()
			{
				@Override
				protected Boolean doInBackground(Void... params)
				{
					if(login.test())
					{
						return true;
					}
					if(login.login())
					{
						if(login.test())
						{
							showNotification("Login successful, Internet access is available", R.drawable.icon);
							return true;
						}
						else
						{
							showNotification("Login failed, no Internet access", R.drawable.icon_disconnected);
							return false;
						}
					}
					else
					{
						showNotification("cannot login: " + login.getMessage(), R.drawable.icon_disconnected);
						return false;
					}
				}
				@Override
				protected void onPostExecute(Boolean result)
				{
					int icon = result ? R.drawable.icon : R.drawable.icon_disconnected;
					showNotification(login.getMessage(), icon);
				}
			}.execute();
		}
		catch(Exception e)
		{
			showNotification(e.getMessage(), R.drawable.icon_disconnected);
		}
	}
	
	 private void showNotification(int messageID, int icon)
	 {
		 showNotification(getText(messageID).toString(), icon);
	 }

	 private void showNotification(String message, int icon)
	 {
		 Notification notification = new Notification(icon, message, System.currentTimeMillis());
		 notification.flags |= Notification.FLAG_ONGOING_EVENT;
		 PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, WifiConnect.class), 0);
		 notification.setLatestEventInfo(this, getText(R.string.app_name), message, contentIntent);
		 notificationManager.notify(R.string.NOTIFICATION, notification);
	 }
	 
	 private void hideNotification()
	 {
		 notificationManager.cancel(R.string.NOTIFICATION);
	 }
	 
	 @Override
	public void onDestroy()
	 {
		try
		{
			synchronized(periodicLogin)
			{
				shouldRun = false;
				periodicLogin.interrupt();
				periodicLogin.join();
			}
		}
		catch (InterruptedException e) {}
		 hideNotification();
	}
	 
	 private class PeriodicLogin extends Thread
	 {
		 public PeriodicLogin()
		 {
			 super("PeriodicLogin");
		 }
		 public void run()
		 {
			 while(shouldRun)
			 {
				 try { Thread.sleep(LoginInfo.getCheckInterval() * 60 * 1000); }
				 catch(InterruptedException e) {}
				 if(!shouldRun) return;
				 login();
			 }
		}
	}
}
