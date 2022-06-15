package com.stc.sockets;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.*;

import com.sap.conn.jco.util.Codecs.Hex;

import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;
import com.stc.service.TaskThreadServerSide;



/**
 * Klasse f�r einfache Socketverbindungen, der Server h�rt dann auf einen bestimmten
 * Port und kann von diesem Anfragen von Clients entgegennehmen
 * @author piotter
 * @see TaskClientConnector
 */

public class TaskSimpleSocketServer implements ITaskCommunication {
	
	private ServerSocket serverSocket=null;
	private TaskConnectionController control = null;
	static Logger logger = LogManager.getLogger("Service");
    private TaskThreadServerSide sst=null;
    private boolean withThread=false;
    private boolean withHex=false;
    private String sapFunction = null;
    private Integer port;
	private Integer timout = 0;private String conId = null;
	protected Integer noReadCount = 0;
	protected boolean emptyStream = false;
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
	private String queueName = null;
	private boolean sendProcessActive = false;
	private boolean receiveProcessActive = false;
	private ITaskCommunication connectedSocket = null;
	
	public void setConId(String conId){
		this.conId = conId;
	}
	
	public String getConId(){
		return this.conId;
	}
    
	public TaskSimpleSocketServer(TaskConnectionController control) {	
		this.control = control;
	}
	
	public TaskSimpleSocketServer() {		
	}
	
	
	/**
	 * Baut die Verbindung des Serversockets ab, es wird dann also nicht mehr auf
	 * einen Port geh�rt und der Dienst ist dann nicht mehr ansprechbar
	 */
	public void disconnect() throws Exception{
		try {
			logger.info("Verbindung des SimpleSockets abbauen");
			this.serverSocket.close();
		} catch (IOException e) {
			logger.error("Fehler beim Abbauen der SimpleSocket-Verbindung!");
			logger.error(e.getMessage());
			throw e;
		}
	}
	
	
	/**
	 * Methode startet den eigentlichen Dienst. Daf�r wird hier eine Endlosschleife
	 * benutzt. Bei Anfrage eines Clients/Sockets wird ein neuer Thread erzeugt, der
	 * diese Anfrage behandelt. Diesem Threat wird das Control-Objekt und der angemeldete
	 * Client �bergeben.
	 * Durch die Thread-Methode start() wird die Verarbeitung begonnen und intern
	 * die run()-Methode des Thread-Objektes aufgerufen.
	 * @param control
	 * @see TaskThreadServerSide
	 */
	public void start( ) throws Exception{			
//	    while(true){
	    	if ( sst == null ) {
	    		sst= new TaskThreadServerSide(serverSocket, control, this );
	    		logger.info("Neue Anfrage eines Clients, neuer Thread ge�ffnet!");
	    		sst.start();
	    	}
	    	
//		    if ( sst.isCanceled( ) == true ){
//		    	// Alten Thread nun abbrechen
//		    	sst.interrupt();
//		    	System.out.println("Thread abgebrochen!");
//	    	   
//		    	// Neuen Thread starten
//		    	sst= new TaskThreadServerSide(serverSocket, control, this );
//		    	System.out.println("Neue Anfrage eines Clients, neuer Thread ge�ffnet!");
//				logger.info("Neue Anfrage eines Clients, neuer Thread ge�ffnet!");
//				
//				sst.start(); 
//			}
//		    
//		    if ( sst.isInterrupted() == true ){    	   
//		    	// Alten Thread nun abbrechen
//		    	System.out.println("Thread abgebrochen durch Server!");
//		    	
//		    	// Neuen Thread starten
//		    	sst= new TaskThreadServerSide(serverSocket, control, this );
//		    	System.out.println("Neue Anfrage eines Clients, neuer Thread ge�ffnet!");
//				logger.info("Neue Anfrage eines Clients, neuer Thread ge�ffnet!");
//				
//				sst.start(); 
//		    }
//	    }	
	}
			
	
	public String getLiftAcknowledge(){
		
		return sst.getLiftAcknowledge();
	}
	
	public String setOutput(TaskSendParameters params) throws Exception{
		try {
			sst.setOutput(params.getSendString());
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
		return null;
		
	}

	public void shutdownOutput() throws Exception{
		try {
			sst.getLiftSocket().shutdownOutput();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public void shutdownInput() throws Exception{
	   try {
		sst.getLiftSocket().shutdownInput();
	   } catch (Exception e) {
		// TODO Auto-generated catch block
	     throw e;
	  }	
	}
	
	public Socket getLiftSocket( ){
		
	   return sst.getLiftSocket();	
	}
	
	public void close( ) throws Exception{
		
	  try {
		sst.getLiftSocket().close();
		this.disconnect();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		throw e;
	}
	  sst = null;
	}

	
	public void establishConnection(String ip, String port, String timeout) throws Exception{
		this.setPort(Integer.parseInt(port));
		this.setTimout(Integer.parseInt(timeout));
		
		try{
			this.establishConnectionInternal();
		}catch(Exception e){
			throw e;
		}
	}

	
	public void setWithThread(boolean withThread) {
		this.withThread = withThread;
	}

	
	public boolean getWithThread() {
		return this.withThread;
	}

	
	public String getInput() throws Exception {
		return null;
	}

	
	public void setWithHex(boolean withHex) {
		this.withHex = withHex;
	}

	
	public boolean getWithTHex() {
		return this.withHex;
	}
	public String getSapFunction(){
		return this.sapFunction;
	}
	
	public void setSapFunction(String sapFunction){
		this.sapFunction = sapFunction;
	}
	public Integer getTimout() {
		return timout;
	}

	public void setTimout(Integer timout) {
		this.timout = timout;
	}
	
	public Integer getPort(){
		return this.port;
	}
	
	public void setPort(Integer port){
		this.port = port;
	}
	
	public boolean isConnected(){
		if(this.serverSocket != null)
			return true;
		else
			return false;
	}

	
	public void establishConnectionInternal() throws Exception {
		try{
			logger.info("------Trying to set up port"+port+")------");
			System.out.println("------Trying to set up port"+port+")------");
			
			this.serverSocket=new ServerSocket(this.getPort());
			this.start();
			
			logger.info("------Port "+port+" successfully set up)------");
			System.out.println("------Port "+port+" successfully set up)------");
			
		}catch(Exception e){
			logger.error("------Occured error: "+e.getMessage()+"------");
			//System.out.println("------Occured error: "+e.getMessage()+"------");
			
			logger.info("------Error while setting up port "+port+"------");
			//System.out.println("------Error while setting up port "+port+"------");
		}
		
	}
	
	public String getClientInput(Socket client) throws Exception {
		
		Integer length = 1024;
		char[] cbuffer = new char[1024];
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			length = bufferedReader.read(cbuffer);
			
			if (length == -1) {
				this.disconnect();
				throw new Exception("Connection closed by remote peer");
			} else {
				// Nachricht aus dem Puffer lesen
				return new String(cbuffer, 0, length);
			}
			
		} catch (SocketTimeoutException e1) {
			//ReadTimeOut
			//Gegenstelle hat keine Daten gesendet
			//wir fangen das nur ab
			return "";

		} catch (IOException e) {
			logger.error(e.getMessage().toString());
			if(e.getMessage().toString().equals("Software caused connection abort: recv failed") ||
			   e.getMessage().toString().equals("Connection reset") ){
				this.disconnect();
			}
			
			throw e;

		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage().toString());
			throw e;

		} catch (Exception e) {
			logger.error(e.getMessage().toString());
			if(e.getMessage().toString().equals("Software caused connection abort: recv failed")){
				this.disconnect();
			}
			 throw e;
		}
	}
	
public String setClientOutput(Socket client, TaskSendParameters params) throws Exception {
		
		byte[] array = null;
		BufferedOutputStream bos=null;
		
		try{
			if(params.isNoSend() == false && params.getSendString() != null){
				//Sendevorgang ist gew�nscht
				
				if(this.getWithTHex() == true){
					//HEX-Konvertierung erw�nscht
					//ab der 4.Stelle konvertieren
					//array = javax.xml.bind.DatatypeConverter.parseHexBinary(params.getSendString());
					array = Hex.decode(params.getSendString());
				}else{
					//Sendestring versenden
					//wurde schon korrekt konvertiert �bergeben
					array = params.getSendString().getBytes();
				}
				
					//Logik für TCP/IP-Kommunikation
					if(client==null)
						//this.establishConnectionInternal();
					if(client==null)
						throw new Exception("No connection to remote peer available");
					
					bos = new BufferedOutputStream(client.getOutputStream());
					bos.write(array);
					bos.flush();
			}

			
			if(params.isNoRead() == false){
				//Lesevorgang ist gewünscht
				return this.getInput();
			}else{
				return "";
			}
			
		}catch(Exception e){
			logger.error(e.getMessage().toString());
			this.disconnect();
			//this.establishConnectionInternal();
			throw e;
		}
		
	}

	public ServerSocket getServerSocket(){
		return this.serverSocket;
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
		return this.emptyStream;
	}

	
	public void setEmptyStream(boolean emptyStream) {
		this.emptyStream = emptyStream;
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

	public void establishConnectionIntegerernal() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public ITaskCommunication getConnectedSocket() {
		return connectedSocket;
	}
	public void setConnectedSocket(ITaskCommunication connectedSocket) {
		this.connectedSocket = connectedSocket;
	}

	@Override
	public String getQueueName() {
		// TODO Auto-generated method stub
		return this.queueName;
	}

	@Override
	public void setQueueName(String queueName) {
		this.queueName = queueName;
		
	}
	
}
