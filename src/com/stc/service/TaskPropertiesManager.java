package com.stc.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.logging.log4j.*;

/**
 * Einlesen der Property Dateien und Speichern in den Objekten
 * Es werden hier sowohl dir grundlegenden Konfigurationen gelsesen,
 * sowie die Konfigurationsdatei f�r die Treiber als auch die Datei f�r
 * den Aufbau des Befehlsstrings, der aus dem Fremdsystem kommt
 * @author status [C] GmbH & Co.KG
 * @version 1.0
 * @see Logger
 */

public class TaskPropertiesManager {
    
	private static Logger logger = LogManager.getLogger("Property");
	//Objekt mit Informationen der Config-Datei/SAP-Verbindungsdaten
	private PropertiesConfiguration sapConnection = null;
	
	//Objekt mit Informationen zu den einzelnen Verbindungen
	private PropertiesConfiguration spsConnection = null;
	
	//Objekt mit Informationen zu den einzelnen Verbindungen
	private PropertiesConfiguration spsConnectionParam = null;


	/**
	 * Methode zum Einlesen der beiden Dateien f�r SAP und SPS-Verbindung
	 * @throws Ausnahme, wenn das Einlesen einer Datei fehlschl�gt
	 */
	public void readFiles() throws Exception{
		try {
			logger.info("------Reading config file!------");
			System.out.println("------Reading config file!------");
			
			this.sapConnection = readFile("conf/task_config.ini");
			
			logger.info("------Config file was read successfully!------");
			System.out.println("------Config file was read successfully!------");
			
		} catch (Exception e) {
			logger.error("------Error while reading config file------");
			//System.out.println("------Error while reading config file------");
			logger.error("------"+e.getMessage()+"------");
			//System.out.println("------"+e.getMessage()+"------");
			throw e;
		}
		
		try {
			logger.info("------Reading connections file!------");
			System.out.println("------Reading connections file!------");
			
			this.spsConnection = this.readFile("conf/"+this.sapConnection.getString("CONNECTIONS"));
			
			logger.info("------Connections file was read successfully!------");
			System.out.println("------Connections file was read successfully!------");
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("------Error while reading connections file------");
			//System.out.println("------Error while reading connections file------");
			logger.error("------"+e.getMessage()+"------");
			//System.out.println("------"+e.getMessage()+"------");
			throw e;
		}
		
		try {
			logger.info("------Reading connections parameter file!------");
			System.out.println("------Reading connections parameter file!------");
			
			this.spsConnectionParam = this.readFile("conf/"+this.sapConnection.getString("CONNECTIONS_PARAM"));
			
			logger.info("------Connections parameter file was read successfully!------");
			System.out.println("------Connections file parameter was read successfully!------");
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("------Error while reading connections parameter file------");
			//System.out.println("------Error while reading connections parameter file------");
			logger.error("------"+e.getMessage()+"------");
			//System.out.println("------"+e.getMessage()+"------");
			throw e;
		}
	}
	
	/**
	 * Eine einzelne Datei einlesen
	 * @param Pfad der Konfigurationsdatei
	 * @return Instanz der Konfiguration
	 * @throws Ausnahme beim Fehler w�hrend des Einlesens
	 */
	public PropertiesConfiguration readFile(String path) throws Exception{
		try{
			
			//PropertiesConfiguration props = new PropertiesConfiguration();
			//props.
			//return props;
			
			
			FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
				    new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
				    .configure(new Parameters().properties()
				        .setFileName(path));
				        //.setThrowExceptionOnMissing(false)
				        //.setListDelimiterHandler(new DefaultListDelimiterHandler('*'))
				        //.setIncludesAllowed(false));
				PropertiesConfiguration config = builder.getConfiguration();
				return config;
			
		}catch(Exception e){
			throw e;
		}
	}	

	/**
	 * Zur�ckgeben des Objektes mit allen Infos aus der Konfigurationsdatei. 
	 * @return Informationen aus der Konfigurationsdatei
	 */
	public PropertiesConfiguration getSapConfiguration() {
		return this.sapConnection;
	}	

	/**
	 * Zur�ckgeben des Objektes mit allen Infos zu den Connectiosn aus der Konfigurationsdatei. 
	 * @return Informationen aus der Konfigurationsdatei
	 */
	public PropertiesConfiguration getSpsConfiguration() {
		return this.spsConnection;
	}
	
	public PropertiesConfiguration getParamConfiguration() {
		return this.spsConnectionParam;
	}
}
