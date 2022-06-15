package com.stc.service;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration2.*;
import org.apache.logging.log4j.*;

import com.stc.sockets.*;


/**
 * Klasse, die Verbindungen des Clients verwaltet
 * Verwaltete Objekte erben von der abstrakten Klasse SpsClientConnector
 * @author status [C] GmbH & Co.KG
 * @version 1.0
 * @see PropertiesConfiguration
 * 
 */

public class TaskConnectionManager {
	
	private Logger logger = LogManager.getLogger("Service");
	private ITaskCommunication sapConnector=null;
	private PropertiesConfiguration spsConnfiguration = null;
	private PropertiesConfiguration parameterConfiguration = null;
	private TaskConnectionController control = null;
	private PropertiesConfiguration sapConfiguration = null;
	private TaskConnectionManagerThread reconnectThread = null;
	
	private HashMap<String, ITaskCommunication> connections = new HashMap<String, ITaskCommunication>( );
	
	/**
	 * Uebernahme der relevanten Vebindungseigenschaften in eigene Attribute
	 * @param taskProperties
	 * @throws Exception
	 */
	public TaskConnectionManager(TaskPropertiesManager taskProperties) throws Exception{
	   this.spsConnfiguration = taskProperties.getSpsConfiguration();
	   this.sapConfiguration = taskProperties.getSapConfiguration();
	   this.parameterConfiguration = taskProperties.getParamConfiguration();
	}
	
	public void establishSapConnection(){
		try{
			logger.info("------Try to connect to SAP system!------");
			System.out.println("------Try to connect to SAP system!------");
			
			this.initServerSide(this.sapConfiguration);
			
		}catch(Exception e){
			logger.error("------"+e.getMessage()+"------");
			//System.out.println("------"+e.getMessage()+"------");
			logger.error("------Connection to SAP system could not be established------");
			//System.out.println("------Connection to SAP system could not be established------");
			//throw e;
		}
	}
	
	public void establishSpsConnection(){
		try{
			logger.info("------Try to connect to SPS devices!------");
			System.out.println("------Try to connect to SPS devices!------");
			
			this.initRemoteConnections(this.spsConnfiguration);
			
			logger.info("------Connection to SPS devices successfully established!------");
			System.out.println("------Connection to SPS devices successfully established!------");
			
		}catch(Exception e){
			logger.error("------"+e.getMessage()+"------");
			//System.out.println("------"+e.getMessage()+"------");
			logger.error("------Connection to SPS devices could not be established------");
			//System.out.println("------Connection to SPS devices could not be established------");
			//throw e;
		}
	}
	
	
	public void establishConnections(TaskConnectionController control)throws Exception{
		this.control = control;	
		
		this.establishSapConnection();
		
		while(control.getSapState() != "ALIVE"){
			//solange warten, bis wir eine bestehende SAP-Verbindung haben
			Thread.sleep(500);
			
			if (control.getSapState() == "DEAD") {
				throw new RuntimeException("SAP Server dead, not reachable");
			}
			
		}
		
		logger.info("------Connection to SAP system successfully established!------");
		System.out.println("------Connection to SAP system successfully established!------");
		System.out.println("");
		
		this.establishSpsConnection();
		
		//Thread f�r die Reconnect-Steuerung starten
		this.reconnectThread = new TaskConnectionManagerThread(this);
		this.reconnectThread.start();
	}
	
	
	/**
	 * Initialisieren des zu verwaltenden Verbindungsobjektes
	 * Aus den Properties wird das System gelesen und dementsprechend das
	 * abhängige Verbindungsobjekt erzeugt.
	 * @param spsProperties Property-Datei
	 * @throws Exception
	 */
	private void initServerSide(PropertiesConfiguration connectionProperties) throws Exception{
		try{
			String httpPort = "";
			String rfcServer = "";
			PropertiesConfiguration props = this.control.getConnectionManager().getServerConfiguration();
			
	        try{
	    		httpPort = props.getString("HTTP_PORT");
	    		if (httpPort == null) {
	    			httpPort = "";
	    		}
	        }catch(Exception e) {
	        	httpPort = "";
	    	}try{
	    		rfcServer = props.getString("SAP_SERVER");
	        }catch(Exception e) {
	    	}
	    	
	    	if(httpPort != "" ) {
	    		//wir haben einen HTTP Port angegeben
				this.sapConnector=new TaskSapHttpServer(this.control); }
	    	else if(rfcServer != null) {
	    		//wir haben eine SAP RFC-Server-Datei angegeben
				this.sapConnector=new TaskSapJcoServer(this.control);}
	    	
			//Erzeugen einer Serverinstanz an dieses System
			this.sapConnector.establishConnectionInternal();
			
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * Gibt die Instanz des erzeugten ClientConnectors/Servers zur�ck.
	 * @return Instanz auf einen ClientConnector/Server
	 * @see TaskClientConnector
	 */
	public ITaskCommunication getSAPConnector(){
		return this.sapConnector;
	}
	
	public String getStringValueForKey(PropertiesConfiguration props, String key) {
		//ArrayList list = (ArrayList)props.getProperty(key);
		//return list.toString();
		return props.getString(key);
	}
	
   	
	/**
	 * Aufbauen der Verbindungen zu den einzelnen Ger�ten
	 * @param connectionProperties
	 * @throws Exception
	 */
	private void initRemoteConnections(PropertiesConfiguration connectionProperties) throws Exception{

		String comKey = null;
		String comData = null;
		Iterator<?> keyIterator=null;
		ITaskCommunication conn = null;
		String[] paramValues = null;
		String paramValue = null;
		
		//Es wird die Aufzählung der Schlüssel der Datei gelesen
		keyIterator=connectionProperties.getKeys();		

		//Zeilenweise �ber Schl�sseleintr�ge lesen und f�llen der HashMap
		while(keyIterator.hasNext()==true){
			//immer den n�chsten Konnektor lesen
			comKey=(String)keyIterator.next();
			//comData = (String)connectionProperties.getString(comKey);
			comData = this.getStringValueForKey(connectionProperties, comKey);
			

			try{
				//und die Verbindung aufbauen
				logger.info("------Try to establish connection to "+comKey+"------");
				System.out.println("------Try to establish connection to "+comKey+"------");
				
				conn =	this.createSocketConnection(comKey,comData);
				
				try {
					//finden wir noch zusätzliche Parameter
					paramValue = this.parameterConfiguration.getString(comKey);
					if(paramValue != null) {
					paramValues = paramValue.split(";");
						for(int i=0; i<10; i++) {
							try {
								switch(i) {
									case 0: conn.setSapParam01(paramValues[i].toString());
											break;
									case 1: conn.setSapParam02(paramValues[i].toString());
											break;
									case 2: conn.setSapParam03(paramValues[i].toString());
											break;
									case 3: conn.setSapParam04(paramValues[i].toString());
											break;
									case 4: conn.setSapParam05(paramValues[i].toString());
											break;
									case 5: conn.setSapParam06(paramValues[i].toString());
											break;
									case 6: conn.setSapParam07(paramValues[i].toString());
											break;
									case 7: conn.setSapParam08(paramValues[i].toString());
											break;
									case 8: conn.setSapParam09(paramValues[i].toString());
											break;
									case 9: conn.setSapParam10(paramValues[i].toString());
											break;
									}
								} catch(IndexOutOfBoundsException iob) {
									
								}
						}
					}
				}catch(Exception e) {
					System.out.println("Error in parameter settings");
				}
				
			    			    
		    }catch(Exception e){
		    	logger.error("------Error while establishing connection to "+comKey+"------");
				//System.out.println("------Error while establishing connection to "+comKey+"------");
			}
			
			this.connections.put(comKey, conn );
			
		}
		
		keyIterator=connectionProperties.getKeys();		

		//Zeilenweise �ber Schl�sseleintr�ge lesen und f�llen der HashMap
		while(keyIterator.hasNext()==true){
			//immer den n�chsten Konnektor lesen
			comKey=(String)keyIterator.next();
			ITaskCommunication socketEntry = this.connections.get(comKey);
			try {
				String array[] = this.getStringValueForKey(connectionProperties, comKey).split(";");
				String connectedSocketString = array[ 11 ];
				socketEntry.setConnectedSocket(this.connections.get(connectedSocketString));
				
			}catch (Exception e) {
				
			}
			
			
		}
	}
	
	private ITaskCommunication createSocketConnection(String conId,String ipString) throws Exception{ 
		
		ITaskCommunication TaskCommunication = null;	
		String array[] = ipString.split(";");
	    String ip = array[0].toString();
	    String port = array[1].toString();
	    String timeout = array[2].toString();
	    String asThread = "";
	    String asHex = "";
	    String sapFunction = "";
	    String openOnSend = "";
	    String simulation = "";
	    String emptyStream = "";
	    String queueName = "";
	    Integer noReadFail = 10000;
	    Integer waittimeThread = 500;
	    
	    try{
	    	asThread = array[3].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	asThread = new String("");
	    }
	    
	    try{
	    	asHex = array[4].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	asHex = new String("");
	    }
	    
	    try{
	    	sapFunction = array[5].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	sapFunction = new String("");
	    }
	    

	    try{
	    	noReadFail = Integer.parseInt(array[6].toString());
	    }catch (IndexOutOfBoundsException iobe){
	    }catch (NumberFormatException numb) {
	    	
	    }
	    
	    try{
	    	openOnSend = array[7].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	openOnSend = new String("");
	    }
	    
	    try{
	    	waittimeThread = Integer.parseInt(array[8].toString());
	    }catch (IndexOutOfBoundsException iobe){
	    	waittimeThread = 500;
	    }catch (NumberFormatException numb) {
	    	waittimeThread = 500;
	    }

	    try{
	    	simulation = array[9].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	simulation = new String("");
	    }

	    try{
	    	emptyStream = array[10].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	emptyStream = new String("");
	    }
	    
	    try{
	    	queueName = array[12].toString();
	    }catch (IndexOutOfBoundsException iobe){
	    	queueName = new String("");
	    }
	    
	    if (timeout == null){
	    	timeout = new String("5000");
	    }
	     
	    try{
		    if ( ip == null || ip.equals("") == true ){
		    	TaskCommunication = new TaskSimpleSocketServer(control);
			}else {
				if(this.isTcp(ip))
					//neue TcpIp-Verbindung
					TaskCommunication = new TaskSocketTcpIp(control);
				
				else if(this.isUdp(ip))
					//neue UDP-Verbindung
					TaskCommunication = new TaskSocketUdp(control);
				
				else if(this.isHttp(ip))
					//neue HTTP-Verbindung
					TaskCommunication = new TaskSocketHttp(control);

				else if(this.isSftp(ip))
					//neue HTTP-Verbindung
					TaskCommunication = new TaskSocketSftp(control);

				else if(this.isMqtt(ip))
					//neue HTTP-Verbindung
					TaskCommunication = new TaskSocketMqtt(control);


//				else if(this.ishttps(ip))
//					//neue HTTPS-Verbindung
//				else if(this.isftp(ip))
//					//neue FTP-Verbindung
		  	}
		    
		    if(asThread.equals("X") || asThread.equals("x") || asThread.equals("true"))
		    	TaskCommunication.setWithThread(true);

		    if(asHex.equals("X") || asHex.equals("x") || asHex.equals("true"))
		    	TaskCommunication.setWithHex(true);
		    
		    if(openOnSend.equals("X") || openOnSend.equals("x") || openOnSend.equals("true"))
		    	TaskCommunication.setOpenOnSend(true);
		    else
		    	TaskCommunication.setOpenOnSend(false);		    	
		    
		    //Stream vorm Senden abräumen
		    if(emptyStream.equals("X") || emptyStream.equals("x") || emptyStream.equals("true"))
		    	TaskCommunication.setEmptyStream(true);
		    else
		    	TaskCommunication.setEmptyStream(false);

		    
		    //aufzurufenden SAP-RFC-Baustein setzen
		    TaskCommunication.setConId(conId);
		    TaskCommunication.setSapFunction(sapFunction);
		    TaskCommunication.setNoReadFail(noReadFail);
		    TaskCommunication.setWaittimeThread(waittimeThread);
		    TaskCommunication.setQueueName(queueName);
		    
		    if(simulation.equals("") || simulation.equals("false")) {
			    TaskCommunication.establishConnection(ip, port, timeout);
			    
			    if(TaskCommunication.getWithThread()==true)
			    	TaskCommunication.start();
		    }
		 
		    return TaskCommunication;
		   
	    }catch(Exception e){
	    	throw e;
	    }
	}


	public PropertiesConfiguration getServerConfiguration(){
		return this.sapConfiguration;
	}
	
	public HashMap<String,ITaskCommunication> getSpsConnections(){
		return connections;
	}
	
	public ITaskCommunication getSPSConnector(String telegram){
	 String array[] = telegram.split(";");
	 ITaskCommunication con = null;
	 con = connections.get(array[0].toString( ));
	 return con;
	}
	
	private boolean isTcp(String ip){
		String[] protocol = ip.split("/");
		if(protocol[0].equals("tcp"))
			return true;
		else
			return false;
	}
	
	private boolean isUdp(String ip){
		String[] protocol = ip.split("/");
		if(protocol[0].equals("udp"))
			return true;
		else
			return false;
	}
	
	private boolean isHttp(String ip){
		String[] protocol = ip.split("/");
		if(protocol[0].equals("http"))
			return true;
		else
			return false;
	}
	
	private boolean isHttps(String ip){
		String[] protocol = ip.split("/");
		if(protocol[0].equals("https"))
			return true;
		else
			return false;
	}
	
	private boolean isFtp(String ip){
		String[] protocol = ip.split("/");
		if(protocol[0].equals("ftp"))
			return true;
		else
			return false;
	}
	private boolean isSftp(String ip){
		String[] protocol = ip.split("/");
		if(protocol[0].equals("sftp"))
			return true;
		else
			return false;
	}

	private boolean isMqtt(String ip) {
		String[] protocol = ip.split("/");
		if(protocol[0].equals("mqtt"))
			return true;
		else
			return false;
	}
}
