package com.stc.service;

import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.*;

import com.stc.sockets.TaskSimpleSocketServer;

/**
 * @author status[C] GmbH & Co. KG Klasse, die einen einen eigenen Thread f�r
 *         die SimpleSocketVerbindung repr�sentiert. Diese wird dann
 *         instanziiert, wenn eine Anfrage von einem SocketClient kommt. Diese
 *         Klasse l�uft dann in einem eigenen Thread.
 * 
 */

public class TaskThreadServerSide extends Thread {

	private Socket clientSocket = null;
	private ServerSocket serverSocket = null;
	private String liftAcknowledge = null;
	private OutputStreamWriter output;
	private TaskConnectionController control = null;
	private static Logger logger = LogManager.getLogger("Service");
	private boolean canceled = false; 
	private TaskSimpleSocketServer server=null;

	/**
	 * �berschriebener Konstruktor, der erst den Konstruktor der Klasse Threat
	 * aufruft und dann das �bergeben Control-Objekt und das
	 * Socket-Objekt(Client) in seine Attribute �bernimmt. Auf diese wird in der
	 * Methode run() sp�ter zugrgriffen
	 * 
	 * @param control
	 * @param socket
	 * @see Socket
	 */
	public TaskThreadServerSide(ServerSocket socket,
			TaskConnectionController control, TaskSimpleSocketServer server ) {

		super();
		logger.info("Neuer Thread wird erzeugt!");
		this.serverSocket = socket;
		this.control = control;
		this.server = server;
		
	}

	/**
	 * Synchronisierte Methode, die die Anfrage eines Clients behandelt. Es
	 * werden die In- und Outputstreams referenziert und �ber das Control-Objekt
	 * der Eingabestring verifiziert und an den Lift gesendet. Bei Fehlern,
	 * seien es Fehler beim Initialisieren der Streams oder beim Konvertieren
	 * bzw. bei der Liftanfrage im Controlobjekt, werden Fehler geloggt und die
	 * entsprechende Nachricht auch an den Client/Socket zur�ckgesendet. Wurde
	 * alles ordnungsgem�� durchgef�hrt, werden die Streams und die Verbindung
	 * zum Client wieder abgebaut.
	 */
	public synchronized void run(){
    
		int timeout;
		timeout = this.server.getTimout();
		
	    if (timeout == 0 ){
	    	timeout = 20000;
	    }
	    
        this.canceled = false;
	   	 
	    while(true){
			try{
				if(this.canceled==true){
					server.establishConnectionInternal();
					this.serverSocket = server.getServerSocket();
				}
				
	            this.canceled = false;
	            
				this.clientSocket = serverSocket.accept(); // warten auf Client-Anforderung

	    	 	System.out.println("Neuer Client");
			    clientSocket.setKeepAlive(true);
			 	clientSocket.setSoTimeout(timeout);
			 
				// starte den Handler-Thread zur Realisierung der Client-Anforderung
				//Handler handler = new Handler(serverSocket, clientSocket, control);
				 
			    handleInput( );

			} catch (Exception ex) {
				System.out.println("--- Interrupt NetworkService au�en ");
				
				//ex.printStackTrace();
				//logger.error(ex.getMessage());
				this.canceled = true ;
			}
	    }
	}

    
	public boolean isCanceled( ){
		
		return this.canceled;
	}
	
	public String getLiftAcknowledge() {

		return this.liftAcknowledge;
	}

	public void setOutput(String outputString) throws Exception {
		try {
			if (this.clientSocket.isClosed() == false) {
				logger.info("Telegram sent to device: " + outputString);
				this.output = new OutputStreamWriter(
						this.clientSocket.getOutputStream());
				this.output.write(outputString);
				this.output.flush();
			}

		} catch (NullPointerException e) {
			throw e;
		}

		catch (Exception e) {
			logger.error("Error on transmitting telegram to device" + e.getMessage());
			// TODO Auto-generated catch block
			throw e;
		}

	}

	public Socket getLiftSocket() {
		return this.clientSocket;
	}
	
	
	private void handleInput( ) throws Exception{
		String nachricht = null;
		String answer = null;
		TaskSendParameters params;
		
		while(true){
			try {
				nachricht = this.server.getClientInput(clientSocket);
				
				if(control.getSapState() != "ALIVE"){
					System.out.println("----No alive connection to sap, read input not neccessary----");
				
				}else if(nachricht != ""){
					//answer = control.sendToSAP(nachricht, this.server.getSapFunction(), this.server.getConId());
					answer = control.sendToSAP(nachricht, this.server);
					
					//if ( answer != null && answer.length() > 0 ) {
						params = new TaskSendParameters( );
						params.setSendString(answer);
						params.setNoRead(true);
						this.server.setClientOutput(clientSocket, params);
					//}
				}	
			} catch (Exception e1) {
				throw e1;
			}
		}

	}

	} // Ende run
		