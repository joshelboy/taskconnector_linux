package com.stc.sockets;

import java.net.InetAddress;
import org.apache.logging.log4j.*;
import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;
import com.stc.service.TaskThreadClientSide;

/**
 * 
 * @author status[C] GmbH & Co. KG
 * @see ITaskCommunication
 * 
 * Diese Klasse implementiert die Verbindung zu einem Lift und bietet Routinen,
 * um mit dem Lift zu kommunizieren.
 */
public abstract class TaskSimpleSocketClient implements ITaskCommunication {
	
//	private Socket liftSocketTcp;
//	private DatagramSocket liftSocketUdp;
	protected Logger logger=LogManager.getLogger("Service");
	protected TaskThreadClientSide thread = null;
	protected TaskConnectionController control = null;
	protected InetAddress ip;
	protected Integer port;
	protected Integer timout = Integer.valueOf(0);
	protected String protocol;
	protected boolean withThread = false;
	protected boolean withHex = false;
	protected String sapFunction = null;
	protected String conId = null;
	protected Integer noReadFail = Integer.valueOf(0);
	protected Integer noReadCount = Integer.valueOf(0);
	protected boolean openOnSend = false;
	protected boolean emptyStream = false;
	protected Integer waittimeThread = Integer.valueOf(500);
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
	
	public abstract String getInput() throws Exception;
	public abstract String setOutput(TaskSendParameters params) throws Exception;
	public abstract void establishConnectionInternal();
	public abstract void disconnect();
	public abstract boolean isConnected();
	
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
	
	public void setConId(String conId){
		this.conId = conId;
	}
	
	public void setEmptyStream( boolean emptyStream ) {
		this.emptyStream = emptyStream;
	}

	public boolean getEmptyStream( ) {
		return this.emptyStream;
	}
	
	public String getConId(){
		return this.conId;
	}
   
	public void setOpenOnSend(boolean open) {
		this.openOnSend = open;
	}
	
	public boolean getOpenOnSend() {
		return this.openOnSend;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public TaskThreadClientSide getThread() {
		return thread;
	}

	public void setThread(TaskThreadClientSide thread) {
		this.thread = thread;
	}

	public String getSapFunction(){
		return this.sapFunction;
	}
	
	public void setSapFunction(String sapFunction){
		this.sapFunction = sapFunction;
	}

	public TaskConnectionController getControl() {
		return control;
	}

	public void setControl(TaskConnectionController control) {
		this.control = control;
	}

	public Integer getTimout() {
		return timout;
	}

	public void setTimout(Integer timout) {
		this.timout = timout;
	}

	public Integer getWaittimeThread() {
		return this.waittimeThread;
	}

	public void setWaittimeThread(Integer waittime) {
		this.waittimeThread = waittime;
	}
	
	public InetAddress getIp(){
		return this.ip;
	}
	
	public Integer getPort(){
		return this.port;
	}
	
	public String getProtocol(){
		return this.protocol;
	}
	
	public void setIp(InetAddress ip){
		this.ip = ip;
	}
	
	public void setProtocol( String protocol){
		this.protocol = protocol;
	}
	
	public void setPort(Integer port){
		this.port = port;
	}
	
	public TaskSimpleSocketClient(TaskConnectionController control) {
		this.control = control;
	}
	
	public void establishConnection(String ip, String port, String timeout) throws Exception {
		
		String[] destination = ip.split("/");
		
		this.setProtocol(destination[0]);
		this.setIp(InetAddress.getByName(destination[1]));
		this.setPort(Integer.valueOf(port));
		this.setTimout(Integer.valueOf(timeout));
		
		if (this.getOpenOnSend()==false)
			this.establishConnectionInternal();
	}

	public Integer getNoReadCount() {
		return noReadCount;
	}
	public void setNoReadCount(Integer noReadCount) {
		this.noReadCount = noReadCount;
	}

	public boolean isWithHex() {
		return withHex;
	}
	
	public void setWithThread(boolean withThread) {
		this.withThread = withThread;
	}

	public boolean getWithThread() {
		return this.withThread;
	}

	public void start() throws Exception {
		
		if(this.withThread==true && this.thread == null){
			//Thread starten, um eine 2-Wege-Kommunikation aufzubauen
			this.thread = new TaskThreadClientSide(control, this);
			this.thread.start( );
		}
		
	}

	public void setWithHex(boolean withHex) {
		this.withHex = withHex;
	}

	public boolean getWithTHex() {
		return this.withHex;
	}

	public void setNoReadFail(Integer i) {
		this.noReadFail = i;
		
	}

	public Integer getNoReadFail() {
		return this.noReadFail;
	}
	
	public Integer getFailedReads( ){
		return this.noReadCount;
	}
	
	public void setFailedReads( Integer noRead ){
		this.noReadCount = noRead;
	}

	public void addFailedReads( ){
		this.noReadCount = this.noReadCount + 1;
		//System.out.prIntegerln("Anzahl fehlerhafter Lesevorgänge" + this.noReadCount);
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
	public ITaskCommunication getConnectedSocket() {
		return connectedSocket;
	}
	public void setConnectedSocket(ITaskCommunication connectedSocket) {
		this.connectedSocket = connectedSocket;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueNameIn) {
		this.queueName = queueNameIn;
	}

}

