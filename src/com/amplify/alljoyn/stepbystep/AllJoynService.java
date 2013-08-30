package com.amplify.alljoyn.stepbystep;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.OnJoinSessionListener;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

public class AllJoynService extends Service {

	private static final String LOG_TAG = AllJoynService.class.getName();
	private static final String WELL_KNOWN_NAME = "com.amplify.alljoyn.stepbystep.AllJoynService";
	private static final short CONTACT_PORT = 42;

	private BusAttachment mBus;
	private StepByStepBusObject stepByStepBusObject = new StepByStepBusObject();
	private boolean isServer = false;
	private ResultReceiver resultReceiver;
	private int mSessionId;
	
	
	private ServiceBinder mbinder = new ServiceBinder();
	
	public class ServiceBinder extends Binder{
		AllJoynService getService(){
			return AllJoynService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mbinder;
	}

	@Override
	public void onCreate() {
		// If using ICE (Interactive Connectivity Establishment), need to uncomment the line below
		// org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());
		
		//STEP 1. ATTACH BUS
		mBus = new BusAttachment("AllJoynTest", BusAttachment.RemoteMessage.Receive);
		
		//STEP 2. REGISTER OBJECT
		Log.d(LOG_TAG, "Registering Bus Object...");
		
		Status status = mBus.registerBusObject(stepByStepBusObject, "/StepByStepBusObject");
		if (status != Status.OK) {
			Log.e(LOG_TAG, "Error registering Bus Object. Status = " + status.name());
			System.exit(0);
			return;
		}
				
		//STEP 3. CONNECT 
		Log.d(LOG_TAG, "Connecting to bus...");
		 status = mBus.connect();
		if (Status.OK != status) {
		    System.out.println("BusAttachment.connect() failed:" + status);
		    System.exit(0);
		    return;
		}
		
		//STEP 4. REGISTER LISTENERS
		Log.d(LOG_TAG, "Registering listener...");
		mBus.registerBusListener(new BusListener() {
			
			public void foundAdvertisedName(String name, short transport, String namePrefix) {
	            Log.i(LOG_TAG, "Found advertised name: " + name);
	            if(name.equals(WELL_KNOWN_NAME)) {
	            	if (isServer) {
	            		Log.i(LOG_TAG, "This is the server side - will not attempt to join session");
	            		return;
	            	}
	            	// JOIN SESSION WHEN ADVERTISED NAME IS FOUND
	            	joinSession();
	            }
	           
			}
			
	   		public void lostAdvertisedName(String name, short transport, String namePrefix) {
	            Log.i(LOG_TAG, "Lost advertised name: " + name);
	            if(WELL_KNOWN_NAME.equals(name)){
	            	//TODO: disconnect from session;
	            }
			}
		});
		
		Log.d(LOG_TAG, "Finding advertised name...");
		//STEP 5. FIND ADVERTISED NAME
		status = mBus.findAdvertisedName(WELL_KNOWN_NAME);
		if (status != Status.OK) {
			System.out.println("Error finding advertised name. Status = " + status);
		    System.exit(0);
		    return;
		}
		
		//STEP 6. REGISTER SIGNAL HANDLER
		Log.d(LOG_TAG, "Registering signal handler...");
		status = mBus.registerSignalHandlers(new SignalHandler());
		if (status != Status.OK) {
			System.out.println("Error registering signal handler. Status = " + status);
		    System.exit(0);
		    return;
		}
		
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		resultReceiver = intent.getParcelableExtra("resultReceiver");
		return START_STICKY;
	}

	private void joinSession() {
		
		Log.d(LOG_TAG, "Joinning session...");

	    short contactPort = CONTACT_PORT;
	    SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);
/* ******** We are not joining session synchronously because it gets stuck and the method never returns ******  
	    Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
	    
	    Log.d(LOG_TAG, "sessionId created when joining session: " + sessionId.value);
	                
	    Status status = mBus.joinSession(WELL_KNOWN_NAME, contactPort, sessionId, sessionOpts, new SessionListener());
*/    
	    Status status = mBus.joinSession(WELL_KNOWN_NAME, contactPort, sessionOpts, new SessionListener(), new MyOnJoinSessionListener(), "Something important");
	    
		if (status != Status.OK) {
			Log.e(LOG_TAG, "Error joining session. Status = " + status.name());
		} else {
			Log.d(LOG_TAG, "After calling joinSession - assynchronous now, which means at this point it's not joined");
		}
	}
		
    private class MyOnJoinSessionListener extends OnJoinSessionListener {
    	
		@Override
		public void onJoinSession(Status status, int sessionId, SessionOpts sessionOpts, Object context) {
			Log.d(LOG_TAG, "Session joined - status=" + status.name() + " , sessionId=" + sessionId);
			mSessionId = sessionId;
			//update UI
			resultReceiver.send(MainActivity.SERVICE_STARTED_CODE, new Bundle());
			//start sending signal
			startSignalEmitter(sessionId);
		}
    }

	public void bindSession(){
		
		Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);

		SessionOpts sessionOpts = new SessionOpts();
		sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
		sessionOpts.isMultipoint = false;
		sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
		sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

		Log.d(LOG_TAG, "Binding session port...");
		
		Status status = mBus.bindSessionPort(contactPort, sessionOpts,
				new SessionPortListener() {
			@Override
			public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
				Log.d(LOG_TAG, "Accept session from joiner=" + joiner + " , sessionPort=" + sessionPort);		
				if (sessionPort == CONTACT_PORT) {
					return true;
				} else {
					return false;
				}
			}
			@Override
			 public void sessionJoined(short sessionPort, int id, String joiner) {
				Log.d(LOG_TAG, "Session joined from joiner=" + joiner + " , sessionPort=" + sessionPort + " , sessionId=" + id);
				mSessionId = id;
				startSignalEmitter(id);

			 }
		});
		
		if (status != Status.OK) {
			Log.e(LOG_TAG, "Error binding session. Status = " + status.name());
		} else {
			Log.d(LOG_TAG, "Session port is bound!");
		}	
		
	}
	
	public void leaveSession(){
		Log.d(LOG_TAG, "I'm server? <" + isServer + ">. My session ID is [" + mSessionId + "].");
		if(isServer){
			Log.d(LOG_TAG, "Shutting down BUS...");
			//STEP 1. CANCEL ADVERTISING
	        Status status = mBus.cancelAdvertiseName(WELL_KNOWN_NAME, SessionOpts.TRANSPORT_ANY);
	        if (status != Status.OK) {
	        	Log.e(LOG_TAG, "Failed to cancel advertise name!");
	        	return;
	        }
	        //STEP 2. UNBIND PORT
	        status = mBus.unbindSessionPort(CONTACT_PORT);
	        if (status != Status.OK) {
	        	Log.e(LOG_TAG, "Failed to cancel advertise name!");
	        	return;
	        }
	        //STEP 3. RELEASE NAME
	        mBus.releaseName(WELL_KNOWN_NAME);
		}
		else{
			Log.d(LOG_TAG, "Disconnecting session...");
			Status status = mBus.leaveSession(mSessionId);
			if(status != Status.OK){
				Log.e(LOG_TAG, "Failed to leaving session");
				System.exit(0);
			}
		}

		//update UI
		resultReceiver.send(MainActivity.SESSION_DISCONNECTED, new Bundle());
	}
	
	public void startServer(){
		isServer = true;

		//STEP 1.REQUEST WELLKNOWN NAME
		Log.d(LOG_TAG, "Requesting name...");
		Status status = mBus.requestName(WELL_KNOWN_NAME, BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE);
		if (status != Status.OK) {
			Log.e(LOG_TAG, "Error requesting name. Status = " + status.name());
			System.exit(0);
			return;
		}
		
		//STEP 2. BIND SESSION PORT
		bindSession();	

		
		//STEP 3. ADVERTISE WELL KNOWN NAME
		Log.d(LOG_TAG, "Advertising name...");
		status = mBus.advertiseName(WELL_KNOWN_NAME, SessionOpts.TRANSPORT_ANY);
		if (status != Status.OK) {
			Log.e(LOG_TAG, "Error advertising name. Status = " + status.name());
			mBus.releaseName(WELL_KNOWN_NAME);
			System.exit(0);
			return;
		}
		
	}
	
	private void startSignalEmitter(int joinedSessionId) {
		// CREATE SIGNAL EMITTER AND GET BUS INTERFACE
		Log.d(LOG_TAG, "Creating signal emitter, then getting bus interface...");
		SignalEmitter emitter = new SignalEmitter(stepByStepBusObject, joinedSessionId, SignalEmitter.GlobalBroadcast.Off);
		StepByStepBusInterface myInterface = emitter.getInterface(StepByStepBusInterface.class);

		// Emitting signals
		try {
			myInterface.buttonClicked(1);
			myInterface.playerPosition(12, 1, -24);
			Log.d(LOG_TAG, "Called methods on interface");

		} catch (BusException ex) {
			System.out.println("Bus Exception: " + ex.toString());
		}
	}

}
