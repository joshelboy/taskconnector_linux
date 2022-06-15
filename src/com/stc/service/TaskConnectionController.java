package com.stc.service;

import org.apache.logging.log4j.*;
import com.stc.sockets.ITaskCommunication;

public class TaskConnectionController {


    private TaskConnectionManager connectionManager=null;
    private static Logger logger=LogManager.getLogger("Control");
    private String sapState = "";

    public TaskConnectionController(TaskConnectionManager conMan){
        logger.info("Setzen der Manager in der Kontrollinstanz!");
        this.connectionManager = conMan;
    }

    public String getSapState() {
        return sapState;
    }

    public void setSapState(String sapState) {
        this.sapState = sapState;
    }


    public TaskConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

    public void setConnectionManager(TaskConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void start( ) throws Exception{
        try {
            logger.info("----Try to establish connection to SAP and devices----");
            System.out.println("----Try to establish connection to SAP and devices----");

            this.connectionManager.establishConnections(this);

            logger.info("----Connection to SAP and devices successfully established!----");
            System.out.println("----Connection to SAP and devices successfully established----");

        } catch (Exception e) {
            logger.error("----Error while establishing connections!----");
            //System.out.println("----Error while establishing connections!----");
            throw new Exception(e);
        }
    }

    public String sendToSAP(String telegram, ITaskCommunication client){
        String conId = client.getConId();

        try {
            TaskSendParameters params = new TaskSendParameters();
            params.setSendString(telegram);
            params.setConId(conId);
            params.setSapFunction(client.getSapFunction());
            params.setSapParam01(client.getSapParam01());
            params.setSapParam02(client.getSapParam02());
            params.setSapParam03(client.getSapParam03());
            params.setSapParam04(client.getSapParam04());
            params.setSapParam05(client.getSapParam05());
            params.setSapParam06(client.getSapParam06());
            params.setSapParam07(client.getSapParam07());
            params.setSapParam08(client.getSapParam08());
            params.setSapParam09(client.getSapParam09());
            params.setSapParam10(client.getSapParam10());
            params.setQueueName(client.getQueueName());

            System.out.println("S P S ==>> S A P " + conId + ": " + telegram);
            logger.info("S P S ==>> S A P " + conId + ": " + telegram);

            String answer = this.connectionManager.getSAPConnector().setOutput(params);

            System.out.println("S P S <<== S A P " + conId + ": " + answer);
            logger.info("S P S <<== S A P " + conId + ": " + answer);
            System.out.println("");

            return answer;

        } catch (Exception e) {
            logger.error(" Exception beim Senden an SAP " + e.getMessage() );
            //System.out.println("Exception beim Senden an SAP" + e.getMessage());
            return "";
        }
    }

    public String sendToSPS(String requestString) throws Exception{

        String answer = "";
        TaskSendParameters params = new TaskSendParameters(requestString);
        ITaskCommunication connection = this.connectionManager.getSPSConnector(params.getConId());

        if(connection == null){
            //es wurde keine Verbindung ermittelt
            //System.out.println("ConnectionID " + params.getConId() + " unknown");
            logger.error(" Exception when sending data: ConnectionID " + params.getConId() + " unknown");
            throw new Exception("ConnectionID " + params.getConId() + " unknown");
        }else{
            //Verbindung gefunden
            //versuche zu senden
            try {

                /**while(connection.isReceiveProcessActive()) {
                 //schleife, bis receiveProzss vorbei ist
                 }
                 connection.setSendProcessActive();**/

                System.out.println("S A P ==>> S P S " + connection.getConId() + ": " + requestString);
                logger.info("S A P ==>> S P S " + connection.getConId() + ": " + requestString);

                if (params.getCloseConnection() == true){
                    connection.disconnect();
                }else{
                    if (connection.getWithThread() == true) {
                        params.setNoRead(true);
                    }
                    answer = connection.setOutput(params);
                }

                System.out.println("S A P <<== S P S " + connection.getConId() + ": " + answer);
                logger.info("S A P <<== S P S " + connection.getConId() + ": " + answer);
                System.out.println("");

                //connection.setSendProcessInactive();
                return answer;

            }catch (Exception e) {
                // Fehler beim Senden
                //Loggen, Ausgeben und Exception hochreichen
                //System.out.println("Exception when sending data: " + e.getMessage());
                logger.error("Exception when sending data: " + e.getMessage() );
                //connection.setSendProcessInactive();

                throw e;
            }
        }
    }

}
