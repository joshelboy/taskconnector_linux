package com.stc.sockets;

import com.stc.service.TaskConnectionController;

import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;


public class MyStateChangedListener implements JCoServerStateChangedListener {
	private JCoServer server;
	private TaskConnectionController control = null;
	
    public void serverStateChangeOccurred(JCoServer server, JCoServerState oldState, JCoServerState newState){
       // Defined states are: STARTED, DEAD, ALIVE, STOPPED;
        // see JCoServerState class for details. 
        // Details for connections managed by a server instance
        // are available via JCoServerMonitor
        System.out.println("------Server state changed: " + oldState.toString() + " --> " + newState.toString() );
        this.control.setSapState(newState.toString());
    }

	public TaskConnectionController getControl() {
		return control;
	}

	public void setControl(TaskConnectionController control) {
		this.control = control;
	}

	public JCoServer getServer() {
		return server;
	}

	public void setServer(JCoServer server) {
		this.server = server;
	}
    
    
}
