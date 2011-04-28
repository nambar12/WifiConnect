package com.nambar.intel.wifi;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WifiConnect extends Activity
{
	private static final String FILE = "login_info";
	private EditText username;
	private EditText password;
	private EditText checkInterval;
	private WifiConnectService.LocalBinder binder;
	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			binder = (WifiConnectService.LocalBinder)service;
			
		}
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			binder = null;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }
    
	private void init()
	{
		ExceptionHandler.setContext(this, new Handler());
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
		LoginInfo.init(getFilesDir() + File.separator + FILE);
		username = (EditText)findViewById(R.id.EditTextUsername);
		password = (EditText)findViewById(R.id.EditTextPassword);
		checkInterval = (EditText)findViewById(R.id.EditTextRecheck);
        final Intent serviceIntent = new Intent(this, WifiConnectService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        final Button terminateButton = (Button)findViewById(R.id.ButtonTerminate);
		terminateButton.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v)
        	{
               	stopService(serviceIntent);
               	finish();
        	}
        });

        final Button closeButton = (Button)findViewById(R.id.ButtonClose);
        closeButton.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v)
        	{
               	finish();
        	}
        });

        final Button saveButton = (Button)findViewById(R.id.ButtonSave);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v)
        	{
        		LoginInfo.setUsername(username.getText().toString());
        		LoginInfo.setPassword(password.getText().toString());
        		LoginInfo.setCheckInterval(Integer.valueOf(checkInterval.getText().toString()));
               	LoginInfo.save();
               	if(binder !=  null)
               	{
               		binder.hup();
               	}
        	}
        });
        
        username.setText(LoginInfo.getUsername());
        password.setText(LoginInfo.getPassword());
        checkInterval.setText(String.valueOf(LoginInfo.getCheckInterval()));
	}
	
}