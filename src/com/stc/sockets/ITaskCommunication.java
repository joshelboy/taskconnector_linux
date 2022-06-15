package com.stc.sockets;

import com.stc.service.TaskSendParameters;

/**
 * 
 * @author status C AG
 * @see SpsClientConnector
 * @see SpsLiftConnector
 * 
 * Integererface bietet Routinen, um Verbindungen zu einem Client aufzubauen.
 * Bei Bedarf können aber die Methoden auch so angepasst werden, dass Serververbindungen
 * aufgebaut werden.
 *
 */

public interface ITaskCommunication {
	
	public void establishConnection(String ip, String port, String timeout) throws Exception;
	public void establishConnectionInternal() throws Exception;
	public String getInput() throws Exception;
	public String setOutput(TaskSendParameters params) throws Exception;
	public void disconnect() throws Exception;
	public void setWithThread(boolean withThread);
	public boolean getWithThread();
	public void setWithHex(boolean withHex);
	public boolean getWithTHex();
	public void setSapFunction(String sapFunction);
	public String getSapFunction();
	public void start() throws Exception;
	public void setConId(String conID);
	public String getConId();
	public boolean isConnected();
	public void setNoReadFail( Integer i);
	public Integer getNoReadFail();
	public void setFailedReads( Integer i);
	public void addFailedReads();
	public Integer getFailedReads();
	public void setOpenOnSend(boolean open);
	public boolean getOpenOnSend();
	public void setWaittimeThread(Integer waittime);
	public Integer getWaittimeThread();
	public boolean getEmptyStream();
	public void setEmptyStream(boolean emptyStream);
	public ITaskCommunication getConnectedSocket();
	public void setConnectedSocket(ITaskCommunication connectedSocket);
	public boolean isSendProcessActive();
	public void setSendProcessActive();
	public void setSendProcessInactive();
	public boolean isReceiveProcessActive();
	public void setReceiveProcessActive();
	public void setReceiveProcessInactive();
	
	public String getSapParam01();
	public void setSapParam01(String sapParam);
	public String getSapParam02();
	public void setSapParam02(String sapParam);
	public String getSapParam03();
	public void setSapParam03(String sapParam);
	public String getSapParam04();
	public void setSapParam04(String sapParam);
	public String getSapParam05();
	public void setSapParam05(String sapParam);
	public String getSapParam06();
	public void setSapParam06(String sapParam);
	public String getSapParam07();
	public void setSapParam07(String sapParam);
	public String getSapParam08();
	public void setSapParam08(String sapParam);
	public String getSapParam09();
	public void setSapParam09(String sapParam);
	public String getSapParam10();
	public void setSapParam10(String sapParam);
	public String getQueueName();
	public void setQueueName(String queueName);
	

}
