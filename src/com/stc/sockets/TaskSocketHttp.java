package com.stc.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.sap.conn.jco.util.Codecs.Hex;

import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;

public class TaskSocketHttp extends TaskSimpleSocketClient {
	
	private HttpURLConnection connection = null;
	private URL url = null;

	public TaskSocketHttp(TaskConnectionController control) {
		super(control);
	}
	
	@Override
	public void establishConnectionInternal() {
		if(this.getTimout() == 0 ){
			this.setTimout(1000);
		}
		
		try{
			logger.info("------Trying to establish connection to device (IP:"+ip+",Port:"+port+")------");
			System.out.println("------Trying to establish connection to device (IP:"+ip+",Port:"+port+")------");
			
			String urlString = this.getProtocol() + "://" + this.getIp().getHostAddress() + ":" + this.getPort() + "/";
			url = new URL(urlString);
			
			connection = (HttpURLConnection) url.openConnection();
			connection.disconnect();

			logger.info("------Connection to device (IP:"+ip+",Port:"+port+") successfully established------");
			System.out.println("------Connection to device (IP:"+ip+",Port:"+port+") successfully established------");
			
		}catch(Exception e){
			logger.error("------Occured error: "+e.getMessage()+"------");
			//System.out.println("------Occured error: "+e.getMessage()+"------");
			
			logger.error("------Error while connecting to device (IP:"+ip+",Port:"+port+")------");
			//System.out.println("------Error while connecting to device (IP:"+ip+",Port:"+port+")------");
		}
	}
	
	@Override
	public void disconnect(){

		this.setFailedReads(0);
		if(this.connection != null)
			this.connection.disconnect();
		this.connection = null;
	}
	
	@Override
	public boolean isConnected(){
		
		if(this.connection == null){
			return false;
		}else{
			return true;
		}
	
	}

	@Override
	public String getInput() throws Exception {
		
		try {
			InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
//			}
			
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
	
	@Override
	public String setOutput(TaskSendParameters params) throws Exception {
		byte[] array = null;
		
		try{
			if(params.isNoSend() == false && params.getSendString() != null){
				//Sendevorgang ist gewünscht
				String urlString = null;
				
				if(this.getWithTHex() == true){
					//HEX-Konvertierung erwünscht
					array = Hex.decode(params.getSendString());
					urlString = this.getProtocol()            +
							   "://" 						 +
							   this.getIp().getHostAddress() +
							   ":" 							 +
							   this.getPort() 				 +
							   "/"							 +
							   array.toString();
				}else{
					//wurde schon korrekt konvertiert übergeben
					urlString = this.getProtocol()            +
							   "://" 						 +
							   this.getIp().getHostAddress() +
							   ":" 							 +
							   this.getPort() 				 +
							   "/"							 +
							   params.getSendString();
				}
								
				url = new URL(urlString);
				
				connection = (HttpURLConnection) url.openConnection();
			    connection.setRequestMethod("GET");

			    //Send request
			    int responseCode = connection.getResponseCode();
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

}
