package com.stc.service;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.LoggerContext;
//import org.apache.log4j.PropertyConfigurator;

/**
 * Hauptklasse f�r den TaskConnector
 * Hier werden alle erforderlichen Manager geladen und Methoden ausgef�hrt,
 * um einen reibungslosen Ablauf des Dienstes zu erm�glichen
 * 
 * Es wird dazu die Konfigurationsdatei f�r die Logginginformationen gelsen.
 * Das bildet die Grundlage daf�r, dass alle anderen Klassen/Objekte auf diese
 * Informationen zugreifen k�nnen.
 * 
 * @author status AG, Berlin
 * @version 4.0
 */
public class TaskConnectorHighWay {

	static TaskPropertiesManager propertyManager=null;
	static TaskConnectionManager connectionManager=null;
	static Logger logger = null;

	/**
	 * Main-Klasse, aus der Dienst gestartet wird
	 * @param args
	 */
	public static void main(String[] args) {
		
		String date=DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMAN).format(new Date());
		String time=DateFormat.getTimeInstance(DateFormat.LONG, Locale.GERMAN).format(new Date());
		TaskConnectionController control=null;
		
		//Logger initialisieren
		//PropertyConfigurator.configureAndWatch( "log4j.properties", 60*1000 );
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		File log4jProperties = new File("./Log4j2.properties");
		System.out.println(log4jProperties);
		context.setConfigLocation(log4jProperties.toURI());
		logger = LogManager.getLogger("Service");
		
		//Versuchen, den SPSConnector zu initialisieren
		try{
			logger.info("========================================================");
			logger.info("=New run on "+date+" at "+time);
			logger.info("========================================================");
			
			logger.info("--Connector is beeing intitalized!--");
			System.out.println("--Connector is beeing initialized!--");
			initTaskConnector();
			
			logger.info("--Connector intitalized successfully!--");
			System.out.println("--Connector initialized successfully!--");
			
		}catch(Exception e){
			logger.error("--Connector could not be intitalized!--");
			//System.out.println("--Connector could not be intitalized!--");
			System.exit(1);
		}
		
		System.out.println();
		System.out.println();

		
		try{
			logger.info("--Connector is beeing started!--");
			System.out.println("--Connector is beeing started!--");
			
			control=new TaskConnectionController(connectionManager);
			control.start( );
			
			logger.info("--Connector is beeing started successfully!--");
			System.out.println("--Connector started successfully!--");
			
		}catch(Exception e){
			logger.error("--Connector started with errors!--");
			//System.out.println("--Connector started with errors!--");
			//System.exit(1);
		}
	}
    
	/**
	 * Initialisierung der ben�tigten Objekte im Service. 
	 * Es werden die Propertiesdaten ausgelesen und die, ConnectionManager aufgebaut.
	 * @throws Exception
	 */
	static void initTaskConnector() throws Exception{

		//Den PropertyManager initialisieren
		try{
			logger.info("----Read and init configuration files!----");
			System.out.println("----Read and init configuration files!----");
			
			propertyManager=new TaskPropertiesManager();
			propertyManager.readFiles();
			
			logger.info("----Configuration files successfully read!----");
			System.out.println("----Configuration files successfully read!----");
			
		}catch(Exception e){
			logger.error("----Error while reading configuration files----");
			//System.out.println("----Error while reading configuration files----");
			throw e;
		}
	
		//Den ConnectionManager initialisieren
		try{
			logger.info("----Init connection manager!----");
			System.out.println("----Init connection manager!----");
			
			connectionManager=new TaskConnectionManager(propertyManager);
			
			logger.info("----Connection manager successfully initalized!----");
			System.out.println("----Connection manager successfully initalized!----");
			
		}catch(Exception e){
			logger.error("----Error while initializing connection manager!----");
			//System.out.println("----Error while initializing connection manager!----");
			throw e;
		}
	}	
}
