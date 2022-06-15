package com.stc.sockets;

import org.apache.logging.log4j.*;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;

public class MyThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {
	
	private static Logger logger = LogManager.getLogger("Service");
			
	 public void serverErrorOccurred(JCoServer jcoServer, String connectionId, Error error){
         System.out.println(">>> Error occured on " + jcoServer.getProgramID() + " connection " + connectionId);
         error.printStackTrace();
     }

     public void serverExceptionOccurred(JCoServer jcoServer, String connectionId, Exception error){
    	 logger.error(">>> Error occured on " + jcoServer.getProgramID() + " connection " + connectionId);
         error.printStackTrace();
         logger.error(error.getMessage());
     }

		public void serverErrorOccurred(JCoServer arg0, String arg1,
				JCoServerContextInfo arg2, Error arg3) {
			// TODO Auto-generated method stub
			logger.error("Fehler" + arg3.getMessage());
			logger.error(arg3.getMessage());
			
		}

		public void serverExceptionOccurred(JCoServer arg0, String arg1,JCoServerContextInfo arg2, Exception arg3) {
			// TODO Auto-generated method stub
			logger.error("Fehler" + arg3.getMessage());
			logger.error(arg3.getMessage());			
		}

}
