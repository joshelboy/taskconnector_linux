package com.stc.service;

import java.util.Iterator;

import org.apache.logging.log4j.*;

import com.stc.service.TaskConnectionManager;
import com.stc.sockets.ITaskCommunication;

public class TaskConnectionManagerThread extends Thread {
	private TaskConnectionManager conMan;
	private Logger logger= LogManager.getLogger("Service");
	
	public TaskConnectionManagerThread(TaskConnectionManager conMan){
		this.conMan = conMan;
	}

	public synchronized void run() {
		boolean conBroke = false;
		
		while(true){
			
			conBroke = false;
			Iterator<ITaskCommunication> clientIterator1 = conMan.getSpsConnections().values().iterator();
			while (clientIterator1.hasNext()) {
				ITaskCommunication client = (ITaskCommunication) clientIterator1.next();
				if(client == null || client.isConnected()==false){
					conBroke = true;
					break;
				}
			}
			
			if(conBroke == true){
				System.out.println();
				System.out.println();
				System.out.println("--------------------------------------------");
				System.out.println("ReconnectionThread starting, check connection status");
				if(conMan.getSAPConnector().isConnected()==false){
					conMan.establishSapConnection();
				}
				
				Iterator<ITaskCommunication> clientIterator2 = conMan.getSpsConnections().values().iterator();
				while (clientIterator2.hasNext()) {
					ITaskCommunication client = (ITaskCommunication) clientIterator2.next();
					if(client.isConnected()==false){
						try {
							logger.error("----Connection to "+client.getConId()+" is broken! Try reconnecting!----");
							//System.out.println("----Connection to "+client.getConId()+" is broken! Try reconnecting!----");
							
							client.establishConnectionInternal();
						} catch (Exception e) {
						}
					}
				}
				System.out.println("ReconnectionThread finished");
				System.out.println("--------------------------------------------");
				System.out.println();
				System.out.println();
			}
				
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
