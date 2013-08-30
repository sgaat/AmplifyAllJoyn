package com.amplify.alljoyn.stepbystep;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

@BusInterface (name="com.amplify.alljoyn.stepbystep.StepByStepBusInterface")
public interface StepByStepBusInterface {
	
    @BusSignal
    public void buttonClicked(int id) throws BusException;
    
    @BusSignal
    public void playerPosition(int x, int y, int z) throws BusException;
}
