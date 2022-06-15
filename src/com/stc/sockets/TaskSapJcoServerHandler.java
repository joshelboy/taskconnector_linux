package com.stc.sockets;

import org.apache.logging.log4j.*;

import com.stc.service.TaskConnectionController;


import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;


public class TaskSapJcoServerHandler implements JCoServerFunctionHandler{
	
	private static Logger logger = LogManager.getLogger("Service");
	private TaskConnectionController control = null;
	
	public TaskSapJcoServerHandler(TaskConnectionController control){
		this.control = control;
	}
	
	public TaskSapJcoServerHandler(){
	}

	public void handleRequest(JCoServerContext serverCtx, JCoFunction function){
		JCoParameterList input = function.getImportParameterList();
        JCoParameterList output = function.getExportParameterList();
        String answer = new String("");
        String returnCode = new String("999");
 	        
        if (function.getName().equals("/STC/TSK07_RFC_CALL_JCO") || 
        	function.getName().equals("STFC_CONNECTION") ) {
	        String inputString = input.getString("REQUTEXT");
 	            
	        try {
	        	
	        	/**if(inputString.equals("RESTART")){
	        		control.getConnectionManager().getSAPConnector().disconnect();
	        		control.getConnectionManager().getSAPConnector().establishConnectionInternal();
	        		control.getConnectionManager().getSpsConnections().get("CONVEYER").disconnect();
	        		control.start();

		            
		     	    output.setValue("ECHOTEXT", "000"); 
		     	    output.setValue("RESPTEXT", answer);
	        	}else {**/

		            answer = control.sendToSPS(inputString);
		            if(answer == null){
		            	answer = "";
		            }
		            
		        	logger.info("Behandlung des Befehlsstrings");
		     		logger.info("Befehlsstring erfolgreich verarbeitet!");
		     		
		     		returnCode = "000";
		     	    output.setValue("ECHOTEXT", returnCode); 
		     	    output.setValue("RESPTEXT", answer);
	        	//}
	        	
	     	    
			} catch (Exception e){
				System.out.println("Error when sending data to remote peer! ");
				output.setValue("ECHOTEXT", returnCode);
				output.setValue("RESPTEXT", e.getMessage());	
			}
        }
	}
}
