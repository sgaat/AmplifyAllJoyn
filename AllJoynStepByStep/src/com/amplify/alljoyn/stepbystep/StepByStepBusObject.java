package com.amplify.alljoyn.stepbystep;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;

public class StepByStepBusObject implements StepByStepBusInterface, BusObject {

	@Override
	public void buttonClicked(int id) throws BusException {
		// TODO Auto-generated method stub
		System.out.println(id);
	}

	@Override
	public void playerPosition(int x, int y, int z) throws BusException {
		// TODO Auto-generated method stub

	}

}
