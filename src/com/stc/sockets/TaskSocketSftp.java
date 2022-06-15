package com.stc.sockets;

import com.jcraft.jsch.*;
import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;

public class TaskSocketSftp extends TaskSimpleSocketClient {
	

	// wir brauchen eine eigene UserInfo-Implementierung
	private class sftpUserInfo implements UserInfo{

		String pwd = null;
		String psp = null;

		public void setPassphrase( String psp ){
			this.psp = psp;
		}
		
		public String getPassphrase() {
			// TODO Auto-generated method stub
			return this.psp;
		}
		
		public void setPassword( String pwd ){
			this.pwd = pwd;
		}

		public String getPassword() {
			// TODO Auto-generated method stub
			return this.pwd;
		}

		public boolean promptPassphrase(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean promptPassword(String arg0) {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean promptYesNo(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public void showMessage(String arg0) {
			// TODO Auto-generated method stub

		}


	}

	
	private ChannelSftp sftpChannel;

	public TaskSocketSftp(TaskConnectionController control) {
		super(control);
	}
	
	@Override
	public void establishConnectionInternal() {
		if(this.getTimout() == 0 ){
			this.setTimout(1000);
		}
		
		try{
			logger.info("------Trying to establish connection to sftp Server (IP:"+ip+",Port:"+port+")------");
			System.out.println("------Trying to establish connection to sftp Server (IP:"+ip+",Port:"+port+")------");
			
			
			JSch jsch=new JSch();
		    Session session = null;
		    sftpUserInfo userInfo= new sftpUserInfo();
		    userInfo.setPassword(getSapParam01());
		    userInfo.setPassphrase(getSapParam03());
		    
		    session = jsch.getSession(getSapParam01(), getIp().toString(), getPort());
			session.setUserInfo(userInfo);
			
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			
			session.connect();
			Channel channel = session.openChannel("sftp");
		    channel.connect();
			sftpChannel = (ChannelSftp)channel;
			
			logger.info("------Connection to sftp Server (IP:"+ip+",Port:"+port+") successfully established------");
			System.out.println("------Connection to sftp Server (IP:"+ip+",Port:"+port+") successfully established------");
			
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
		
		try {
			if(this.sftpChannel != null){
				sftpChannel.disconnect();
			}
		} catch (Exception e) {
		}
		sftpChannel = null;
	}
	
	@Override
	public boolean isConnected(){
		if (this.getOpenOnSend())
			return true;
		
		if(this.sftpChannel == null){
			return false;
		}else{
			return true;
		}
	
	}

	@Override
	public String getInput() throws Exception {
		//das lesen einer Datei können wir noch nicht
		return "";
	} 
	
	@Override
	public String setOutput(TaskSendParameters params) throws Exception {
		
		try{
			if (this.getOpenOnSend())
				this.establishConnectionInternal();
		
			
			if(params.isNoSend() == false && params.getSendString() != null){
				//Sendevorgang ist gewünscht
				
				
			}
			

			if (this.getOpenOnSend())
				this.disconnect();
			
			return "successfull";
			
		}catch(Exception e){
			logger.error(e.getMessage().toString());
			this.disconnect();
			//this.establishConnectionInternal();
			throw e;
		}
	}
}
