package com.stc.service;

import org.apache.logging.log4j.*;
import com.stc.sockets.ITaskCommunication;


/**
 * @author status C AG
 * 		   Klasse, die einen einen eigenen Thread für
 *         die SimpleSocketVerbindung repr�sentiert. Diese wird dann
 *         instanziiert, wenn eine Anfrage von einem SocketClient kommt. Diese
 *         Klasse l�uft dann in einem eigenen Thread.
 * 
 */

public class TaskThreadClientSide extends Thread {

	private TaskConnectionController control = null;
	private static Logger logger = LogManager.getLogger("Service");
	private ITaskCommunication client = null;
	
	/**
	 * überschriebener Konstruktor, der erst den Konstruktor der Klasse Threat
	 * aufruft und dann das �bergeben Control-Objekt und das
	 * Socket-Objekt(Client) in seine Attribute übernimmt. Auf diese wird in der
	 * Methode run() später zugrgriffen
	 * 
	 * @param control
	 * @param socket
	 * @see Socket
	 */

	public TaskThreadClientSide(TaskConnectionController control, ITaskCommunication client) {
		super();
		logger.info("Neuer Thread wird erzeugt!");
		this.control = control;
		this.client = client;
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
	public synchronized void run() {
		//Endlosschleife um den Inputstream der Gegenstelle zu lesen
		while (true) {
			try {
				if (this.client.isConnected() == false) {
					// Verbindung zum Client herstellen, falls verloren gegangen
					// inklusive WaitTime
					logger.error("----Connection to remote peer "+client.getConId()+" not available! Wait for reconnection!----");
					//System.out.println("----Connection to remote peer "+client.getConId()+" not available! Wait for reconnection!----");
					Thread.sleep(5000);
				}else{
					//Verbindung besteht
					//wir laufen in die Methode zur Behandlung des Inputs
					/**while(client.isSendProcessActive()) {
						
					}
					client.setReceiveProcessActive();**/
					handleInput();
					//client.setReceiveProcessInactive();
					
					//definierte Wartezeit warten
					//default 500 Milisekunden
					Thread.sleep(client.getWaittimeThread());
				}
			} catch (Exception ex) {
				client.setReceiveProcessInactive();
				logger.error(this.toString() + ":" + ex.getMessage());
				 logger.error(ex.getMessage());
				 try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
			//Prüfen, ob noch eine Verbindung besteht
			if( this.client.getNoReadFail() > 0 &&
				this.client.getFailedReads() > this.client.getNoReadFail()){
				try {
					this.client.disconnect();
					
					if (this.client.getConnectedSocket() != null){
						this.client.getConnectedSocket().disconnect();
					}
					
				} catch (Exception e) {
				}
				
			}
			
		}
	}

	private void handleInput() throws Exception {

		String nachricht = null;
		String answer = null;
		TaskSendParameters params;
		
		try {
			if (this.client.getOpenOnSend())
				//bei Bedarf öffnen...
				client.establishConnectionInternal();
			
			//versuche zu lesen
			nachricht = this.client.getInput();

			
			if(control.getSapState() != "ALIVE"){
				System.out.println("----No alive connection to sap, read input not neccessary----");
			
			}else if(nachricht != ""){
				this.client.setFailedReads(0);
				//answer = control.sendToSAP(nachricht, this.client.getSapFunction(), this.client.getConId());
				answer = control.sendToSAP(nachricht, this.client);
				
				params = new TaskSendParameters( );
				params.setSendString(answer);
				params.setNoRead(true);
				
				this.client.setOutput(params);
				
				//bei Bedarf wieder schließen
				if (client.getOpenOnSend())
					client.disconnect();
				
			}else{
				//nichts gelesen
				this.client.addFailedReads();
				
				//bei Bedarf wieder schließen
				if (client.getOpenOnSend())
					client.disconnect();
				
			}
		} catch (Exception e1) {
			//nichts gelesen
			this.client.addFailedReads();
			
			try {
				//bei Bedarf wieder schließen
				if (client.getOpenOnSend())
					client.disconnect();
			}catch (Exception e2) {
				
			}
			
			throw e1;
		}
	}
}

