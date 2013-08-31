package org.alljoyn.bus.sample.chat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class StatisticsActivity extends Activity implements Observer {
    private static final String TAG = "chat.StatisticsActivity";
    
     
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
              
        /*
         * Keep a pointer to the Android Appliation class around.  We use this
         * as the Model for our MVC-based application.  Whenever we are started
         * we need to "check in" with the application so it can ensure that our
         * required services are running.
         */
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.checkin();
        mChatApplication.addObserver(this);
        
        mSentCounter = (TextView)findViewById(R.id.textSentCounter);
        mSentCounter.setText(ChatApplication.localMessageCounter+"");
        
        mReceivedCounter = (TextView)findViewById(R.id.textRecvCounter);
        mReceivedCounter.setText(ChatApplication.remoteMessageCounter+"");
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	mSentCounter.setText(ChatApplication.localMessageCounter+"");
		mReceivedCounter.setText(ChatApplication.remoteMessageCounter+"");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mSentCounter.setText(ChatApplication.localMessageCounter+"");
		mReceivedCounter.setText(ChatApplication.remoteMessageCounter+"");
    }
    
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.deleteObserver(this);
        super.onDestroy();
 	}
	
    private ChatApplication mChatApplication = null;
    
    
    private TextView mSentCounter;
    private TextView mReceivedCounter;


	@Override
	public void update(Observable o, Object arg) {
		String qualifier = (String)arg;
		  
		  if (qualifier.equals(ChatApplication.HISTORY_CHANGED_EVENT)) {
			  final MessageObject message = mChatApplication.getHistory().get(mChatApplication.getHistory().size()-1);
		      if ( !message.isSelf ){  	
		    	  Handler handle = new Handler(getMainLooper());
	        		handle.post(new Runnable() {
						
						@Override
						public void run() {
							mSentCounter.setText(ChatApplication.localMessageCounter+"");
							mReceivedCounter.setText(ChatApplication.remoteMessageCounter+"");
						}
					});
			  }
		  }
	}
    
}