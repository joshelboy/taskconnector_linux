package com.stc.service;

public class TaskSendParameters {
	
	private String conId = null;
	private String sendString = null;

	private boolean noSend = false;
	private boolean noRead = false;
	private String sapFunction = null;
	private boolean closeConnection = false;
	private boolean emptyStream = false;
	
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
	private String queueName  = null;
	
	public void setEmptyStream( boolean emptyStream ) {
		this.emptyStream = emptyStream;
	}

	public boolean getEmptyStream( ) {
		return this.emptyStream;
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
	public String getConId() {
		return conId;
	}
	public void setConId(String conId) {
		this.conId = conId;
	}
	
	
	
	public String getSendString() {
		return sendString;
	}
	public void setSendString(String sendString) {
		this.sendString = sendString;
	}
	public boolean isNoSend() {
		return noSend;
	}
	public void setNoSend(boolean noSend) {
		this.noSend = noSend;
	}
	public boolean isNoRead() {
		return noRead;
	}
	public void setNoRead(boolean noRead) {
		this.noRead = noRead;
	}
	public void setSapFunction( String function){
		this.sapFunction = function;
	}
	public String getSapFunction(){
		return this.sapFunction;
	}
	
	public TaskSendParameters( ){
		super();
	}
	
	public TaskSendParameters(String paramString) {
		
		super();
		String requestArray[] = paramString.split(";");
		
		//Aufbau der Sendeparameter
		try{
			this.setConId(requestArray[0]);
			this.setSendString(requestArray[1]);
		
			if(requestArray[2].equals("NO_SEND"))
				this.setNoSend(true);
			
			if(requestArray[3].equals("NO_READ"))
				this.setNoRead(true);
		
			if(requestArray[4].equals("CLOSE_CONNECTION"))
				this.setCloseConnection(true);
			
		}catch(IndexOutOfBoundsException e){	
		}
	}
	public boolean getCloseConnection() {
		return this.closeConnection;
	}
	public void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
	}

	public String getQueueName() {
		return this.queueName;
	}

	public void setQueueName(String queueNameIn) {
		this.queueName = queueNameIn;
	}
}
