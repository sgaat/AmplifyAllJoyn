package com.amplify.alljoyn.stepbystep;

import org.alljoyn.bus.BusAttachment;

import com.amplify.alljoyntest.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String LOG_TAG = MainActivity.class.getName();
	
	public static final int SERVICE_STARTED_CODE = 1;
	public static final int SESSION_DISCONNECTED = 2;
	
	BusAttachment mBus;
	AllJoynService remoteService;
	private ServiceConnection serviceConnection = new AllJoynServiceConnection();
	private AllJoynServiceReceiver resultReceiver;
	private Button buttonStartService;
	private Button buttonLeaveSession;
	
	static {
	    System.loadLibrary("alljoyn_java");
	    Log.d(LOG_TAG, "alljoyn_java lib has been loaded!");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonStartService = (Button)findViewById(R.id.buttonStartService);
		buttonStartService.setOnClickListener(startServiceListener);
		
		buttonLeaveSession = (Button)findViewById(R.id.buttonLeaveSession);
		buttonLeaveSession.setOnClickListener(leaveSessionListener);
		
		
		resultReceiver = new AllJoynServiceReceiver(new Handler());
        Intent intent = new Intent(MainActivity.this, AllJoynService.class);
        intent.putExtra("resultReceiver", resultReceiver);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private View.OnClickListener startServiceListener = new View.OnClickListener() {
        public void onClick(View v) {            
          remoteService.startServer();
          setUIForServiceStarted();
          Toast.makeText(MainActivity.this, "AllJoyn Service was started", Toast.LENGTH_LONG).show();
        }
    };
    
    private View.OnClickListener leaveSessionListener = new View.OnClickListener() {
        public void onClick(View v) {            
        	remoteService.leaveSession();
            setUIForSessionDisconnected();
            Toast.makeText(MainActivity.this, "Disconnected from session", Toast.LENGTH_LONG).show();
        }
    };
    
    public void setUIForServiceStarted() {
        buttonStartService.setVisibility(View.GONE);
        buttonLeaveSession.setVisibility(View.VISIBLE);
    }
    
    public void setUIForSessionDisconnected() {
        buttonLeaveSession.setVisibility(View.GONE);
        buttonStartService.setVisibility(View.VISIBLE);
    }
    
    private class AllJoynServiceConnection implements ServiceConnection {
    	public void onServiceConnected(ComponentName className, IBinder service) {
            remoteService = ((AllJoynService.ServiceBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            remoteService = null;
        }
    
    };
    
    public class AllJoynServiceReceiver extends ResultReceiver{

    	public AllJoynServiceReceiver(Handler handler) {
    		super(handler);
    	}
    	
    	@Override
    	public void onReceiveResult(final int resultCode, Bundle resultData){
    		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					switch(resultCode) {
						case SERVICE_STARTED_CODE: setUIForServiceStarted(); break;
						case SESSION_DISCONNECTED: setUIForSessionDisconnected(); break;
						default: Log.e(LOG_TAG, "Result code not handled: " + resultCode);
					}
					
				}
			});
    	}

    }
}
