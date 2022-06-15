package com.stc.sockets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import com.sap.conn.jco.util.Codecs.Hex;

import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;

public class TaskSocketUdp extends TaskSimpleSocketClient {
	
	private DatagramSocket liftSocketUdp;

	public TaskSocketUdp(TaskConnectionController control) {
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
			
			this.liftSocketUdp=new DatagramSocket();
			this.liftSocketUdp.setSoTimeout(this.getTimout());
				
			logger.info("------Connection to device (IP:"+ip+",Port:"+port+") successfully established------");
			System.out.println("------Connection to device (IP:"+ip+",Port:"+port+") successfully established------");
			
		}catch(Exception e){
			logger.error("------Occured error: "+e.getMessage()+"------");
			//System.out.println("------Occured error: "+e.getMessage()+"------");
			
			logger.error("------Error while connecting to device (IP:"+ip+",Port:"+port+")------");
			//ystem.out.println("------Error while connecting to device (IP:"+ip+",Port:"+port+")------");
		}
	}
	
	@Override
	public void disconnect(){
		this.setFailedReads(0);
		
		if(this.liftSocketUdp != null)
			liftSocketUdp.close();
		liftSocketUdp = null;
	}
	
	@Override
	public boolean isConnected(){
		
		if(this.liftSocketUdp == null)
			return false;
		else
			return true;
	}

	@Override
	public String getInput() throws Exception {
		
		byte[] data = new byte[16384];
		
		try {
//			if(this.withHex == false){
//				this.noRead = 0;
//				DatagramPacket packet = new DatagramPacket(data, data.length,this.getIp(),this.getPort());
//				this.liftSocketUdp.receive(packet);
//				return new String(packet.getData(), 0, packet.getLength());
//				
//			}else{
				
				DatagramPacket packet = new DatagramPacket(data, data.length,this.getIp(),this.getPort());
				this.liftSocketUdp.receive(packet);

				ByteArrayOutputStream bBuffer = new ByteArrayOutputStream();
				bBuffer.write(packet.getData(), 0, packet.getLength());
				bBuffer.flush();
				
				if(this.withHex == false)
					return bBuffer.toString();
				else
					return Hex.encode(bBuffer.toByteArray());
				
//				return Hex.encode(bBuffer.toByteArray());
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
				//Sendevorgang ist gew�nscht
				
				if(this.getWithTHex() == true){
					//HEX-Konvertierung erw�nscht
					array = Hex.decode(params.getSendString());
				}else{
					//Sendestring versenden
					array = params.getSendString().getBytes();
				}
				
				if(this.liftSocketUdp==null)
					if(this.liftSocketUdp==null)
						throw new Exception("No connection to remote peer available");
					
					DatagramPacket packet = new DatagramPacket(array, array.length,this.getIp(),this.getPort());
					this.liftSocketUdp.send(packet);
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
