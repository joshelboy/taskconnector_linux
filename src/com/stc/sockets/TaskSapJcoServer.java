package com.stc.sockets;


import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.logging.log4j.*;

import com.sap.conn.jco.JCo;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoCustomRepository;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoListMetaData;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;


/**
 * 
 * Klasse stellt Verbindung zu einem SAP-System �ber eine SAP-JCo-Verbindung her.
 * Die Informationen f�r die Verbindung werden aus der Datei CONFIG.INI ausgelesen.
 * Darin m�ssen alle ben�tigten Informationen hinterlegt sein:
 * Systemname, SAP-GatewayServivce("SAPGW"+Systemnummer), Programm-ID(die der RFC-Verbindung),
 * Mandant, User, Passwort, Spoache, Hostname, Systemnummer und Anzahl der maximal
 * zugelassenen Verbindungen.
 * 
 * @author status[C] GmbH & Co. KG
 * @see SpsClientConnector
 * 
 */

public class TaskSapJcoServer implements ITaskCommunication{

	protected TaskConnectionController control=null;
	protected static Logger logger = LogManager.getLogger("Service");
    private JCoServer server;
    protected String sapServerFile = "";
	protected String sapClientFile = "";
    private boolean running=false;
	protected Integer noReadCount = 0;
	private String sapParam01 = null;
	private String sapParam02 = null;
	private String sapParam03 = null;
	private String sapParam04 = null;
	private String sapParam05 = null;
	private String sapParam06 = null;
	private String sapParam07 = null;
	private String sapParam08 = null;
	private String sapParam09 = null;
	private String sapParam10 = null;
	private boolean sendProcessActive = false;
	private boolean receiveProcessActive = false;
    
    public TaskSapJcoServer(TaskConnectionController control){
    	this.control = control;
    }
    
    public TaskSapJcoServer() {
		// TODO Auto-generated constructor stub
	}
    
    public String setOutput(TaskSendParameters params) throws Exception {
		// TODO Auto-generated method stub
    	String answer = null;
    	
    	try {
			answer = this.sendToSAP(params);			
			return answer;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		}
	}
	
	public void disconnect() throws Exception {
		this.server.stop();
	}
	
	protected String sendToSAPDestination( String destinationName, TaskSendParameters parameter ) throws Exception{
		
		JCoDestination destination = null;
    	JCoFunction functionDest = null;
    	String answer = null;
    	
		try {
			 destination = JCoDestinationManager.getDestination(destinationName);
		} catch (JCoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			functionDest  = destination.getRepository().getFunction(parameter.getSapFunction());
		} catch (JCoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    
        JCoParameterList input = functionDest.getImportParameterList();
        input.setValue("IV_MESSAGE", parameter.getSendString());
        
      //versuche, optionale Verbindungs-ID zu setzen
        try {
        	input.setValue("IV_CONID", parameter.getConId());
        }catch(Exception e) {
        }
        
        //versuche, optionale Parameter zu setzen
        try {
        	input.setValue("IV_PARAM01", parameter.getSapParam01());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM02", parameter.getSapParam02());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM03", parameter.getSapParam03());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM04", parameter.getSapParam04());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM05", parameter.getSapParam05());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM06", parameter.getSapParam06());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM07", parameter.getSapParam07());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM08", parameter.getSapParam08());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM09", parameter.getSapParam09());
        }catch(Exception e) {
        }
        try {
        	input.setValue("IV_PARAM10", parameter.getSapParam10());
        }catch(Exception e) {
        }
     
	     try {
	    	 //Tatsächlichen Aufruf durchführen
	    	 if (parameter.getQueueName().isEmpty() == false) {
	    		 //hier mit queuedRFC
	    		 String transactionID = destination.createTID();
	    		 JCoContext.begin(destination);
	    		 functionDest.execute(destination, transactionID, parameter.getQueueName());
	    		 JCoContext.end(destination);
	    	 }else {
	    		 //oder hier ganz normal
	    		 functionDest.execute(destination);
	    	 }
	         logger.info("String an SAP gesendet " + parameter.getSendString());
	         
	         //Rueckgabewert ermitteln und an den Aufrufer versorgen
	         answer = functionDest.getExportParameterList().getString("EV_ANSWER");
	         
	         try {
		         JCoStructure bapiRet = functionDest.getExportParameterList().getStructure("ES_RETURN");
		         if (bapiRet.getString("TYPE").equals("E"))   {
		            answer = bapiRet.getString("MESSAGE");
		         }
		         }catch(Exception e) {
	         }
	         
	         logger.info("Antwort von SAP erhalten" + answer);
	         return answer;
	         
	     }catch(Exception e){   
	         //System.out.println(e.getMessage());
	         logger.error("Fehler bei Uebergabe an SAP" + e.getMessage());
	         return "";  
	    }finally {
			JCoContext.end(destination);
		}
	}

    private String sendToSAP(TaskSendParameters parameter) throws Exception{
    	//String receivedString = sendToSAPDestination(server.getRepositoryDestination(), parameter);
    	if(sapClientFile != "") {
	    	String receivedString = sendToSAPDestination(sapClientFile, parameter);
	    	return receivedString;
    	}else {
    		System.out.println("No SAP destination availbale, check configuration files!");
    		logger.warn("No SAP destination availbale, check configuration files!");
    		return "";
    	}
    }


	protected void createSapClientInstance() throws Exception {
		try {
			//wir simulieren einen initialien Systemaufruf
			JCoDestination destination = JCoDestinationManager.getDestination(sapClientFile);
			JCoFunction functionDest  = destination.getRepository().getFunction("RFC_PING");
			functionDest.execute(destination);

		} catch (JCoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally {
			//JCoContext.end(destination);
		}
	}
    
    private void createServerInstance() throws Exception{
        try{
            server = JCoServerFactory.getServer(sapServerFile);
            this.createAndRegisterJcoFunction();
            
        }catch(Exception ex){
        	ex.printStackTrace();
            throw new RuntimeException("Unable to create the server " + sapServerFile + ", because of " + ex.getMessage(), ex);
        }
       
        //Ereignisbehandler f�r Fehler und Status�nderungen erzeugen und setzen
        MyThrowableListener eListener = new MyThrowableListener();
        MyStateChangedListener slistener = new MyStateChangedListener();
        
        server.addServerErrorListener(eListener);
        server.addServerExceptionListener(eListener);
        server.addServerStateChangedListener(slistener);
        
        //dem State-Listener die SERVER-Instanz geben, damit dieser einen
        //Reconnect initieren kann
        slistener.setServer(server);
        slistener.setControl(control);
        
        server.start();
        this.running = true;
     
    }
    
    private void createAndRegisterJcoFunction() throws Exception{
    	
    	JCoCustomRepository cR = JCo.createCustomRepository("MyCustomRepository");
        JCoListMetaData impList = JCo.createListMetaData("IMPORTS");
        JCoListMetaData expList = JCo.createListMetaData("EXPORTS");
        
        impList.add("REQUTEXT", JCoMetaData.TYPE_CHAR, 5000, 10000, 0, null, null, JCoListMetaData.IMPORT_PARAMETER, null, null);
        expList.add("ECHOTEXT", JCoMetaData.TYPE_CHAR, 3, 6, 0, null, null, JCoListMetaData.EXPORT_PARAMETER, null, null);
        expList.add("RESPTEXT", JCoMetaData.TYPE_CHAR, 5000, 10000, 0, null, null, JCoListMetaData.EXPORT_PARAMETER, null, null);
        impList.lock();
        expList.lock();
        
        JCoFunctionTemplate fT1 = JCo.createFunctionTemplate("/STC/TSK07_RFC_CALL_JCO", impList, expList, null, null, null);
        JCoFunctionTemplate fT2 = JCo.createFunctionTemplate("STFC_CONNECTION", impList, expList, null, null, null);
        try {
        	cR.addFunctionTemplateToCache(fT1);
        }catch (Exception e) {
        }
        
        try {
        	cR.addFunctionTemplateToCache(fT2);
        }catch (Exception e) {
        }
        
        server.setRepository(cR);
    	
    	JCoServerFunctionHandler stfcConnectionHandler = new TaskSapJcoServerHandler(this.control);
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerHandler(fT1.getName(), stfcConnectionHandler);
        factory.registerHandler(fT2.getName(), stfcConnectionHandler);
        server.setCallHandlerFactory(factory);
        
    	
    }

	public void establishConnection(String ip, String port, String timeout)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String getInput() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void setWithThread(boolean withThread) {
		// TODO Auto-generated method stub
		
	}

	public boolean getWithThread() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setWithHex(boolean withThread) {
		// TODO Auto-generated method stub
		
	}

	public boolean getWithTHex() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setSapFunction(String sapFunction) {
		// TODO Auto-generated method stub
		
	}

	public String getSapFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	public void start( ) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void setConId(String conID) {
		// TODO Auto-generated method stub
		
	}

	public String getConId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isConnected(){
		return this.running;
	}

	public void establishConnectionInternal() throws Exception{
		PropertiesConfiguration props = this.control.getConnectionManager().getServerConfiguration();
		
        try{
    		sapServerFile = "conf/" + props.getString("SAP_SERVER");
        	createServerInstance();

        	if(props.getString("SAP_CLIENT") != null) {
	        	sapClientFile = "conf/" + props.getString("SAP_CLIENT");
	        	createSapClientInstance();
        	}
        }catch (Exception e){
        	throw e;
        }
		
	}

	public void setNoReadFail(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public Integer getNoReadFail() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Integer getFailedReads( ){
		return this.noReadCount;
	}
	
	public void setFailedReads( Integer noRead ){
		this.noReadCount = noRead;
	}
	
	public void addFailedReads( ){
		this.noReadCount = this.noReadCount + 1;
	}

	public void setOpenOnSend(boolean open) {
		// TODO Auto-generated method stub
		
	}

	public boolean getOpenOnSend() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setWaittimeThread(Integer waittime) {
		// TODO Auto-generated method stub
		
	}

	public Integer getWaittimeThread() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getSapParam01() {
		return sapParam01;
	}
	public void setSapParam01(String sapParam01) {
		this.sapParam01 = sapParam01;
	}
	public String getSapParam02() {
		return sapParam02;
	}
	public void setSapParam02(String sapParam02) {
		this.sapParam02 = sapParam02;
	}
	public String getSapParam03() {
		return sapParam03;
	}
	public void setSapParam03(String sapParam03) {
		this.sapParam03 = sapParam03;
	}
	public String getSapParam04() {
		return sapParam04;
	}
	public void setSapParam04(String sapParam04) {
		this.sapParam04 = sapParam04;
	}
	public String getSapParam05() {
		return sapParam05;
	}
	public void setSapParam05(String sapParam05) {
		this.sapParam05 = sapParam05;
	}
	public String getSapParam06() {
		return sapParam06;
	}
	public void setSapParam06(String sapParam06) {
		this.sapParam06 = sapParam06;
	}
	public String getSapParam07() {
		return sapParam07;
	}
	public void setSapParam07(String sapParam07) {
		this.sapParam07 = sapParam07;
	}
	public String getSapParam08() {
		return sapParam08;
	}
	public void setSapParam08(String sapParam08) {
		this.sapParam08 = sapParam08;
	}
	public String getSapParam09() {
		return sapParam09;
	}
	public void setSapParam09(String sapParam09) {
		this.sapParam09 = sapParam09;
	}
	public String getSapParam10() {
		return sapParam10;
	}
	public void setSapParam10(String sapParam10) {
		this.sapParam10 = sapParam10;
	}

	public boolean getEmptyStream() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEmptyStream(boolean emptyStream) {
		// TODO Auto-generated method stub
		
	}

	public boolean isSendProcessActive() {
		return sendProcessActive;
	}
	public void setSendProcessActive(){
		sendProcessActive = true;
	}
	public void setSendProcessInactive(){
		sendProcessActive = false;
	}
	public boolean isReceiveProcessActive(){
		return receiveProcessActive;
	}
	public void setReceiveProcessActive(){
		receiveProcessActive = true;
	}
	public void setReceiveProcessInactive(){
		receiveProcessActive = false;
	}


	public ITaskCommunication getConnectedSocket() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setConnectedSocket(ITaskCommunication connectedSocket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getQueueName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueueName(String queueName) {
		// TODO Auto-generated method stub
		
	}

}

   

   