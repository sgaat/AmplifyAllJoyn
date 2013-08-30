package com.amplify.alljoyn.stepbystep;

import org.alljoyn.bus.annotation.BusSignalHandler;

import android.util.Log;

public class SignalHandler {
	
	private static final String LOG_TAG = SignalHandler.class.getName();
	
	@BusSignalHandler(iface="com.amplify.alljoyntest.DrawingBusInterface", signal="buttonClicked")
	public void buttonClicked(int id) {
	    Log.i(LOG_TAG, "************** button Clicked signal received **************");
	}

	@BusSignalHandler(iface="com.amplify.alljoyntest.DrawingBusInterface", signal="playerPosition")
	public void playerPosition(int x, int y, int z) {
	    Log.i(LOG_TAG, "************** player position signal received **************");
	}

}
