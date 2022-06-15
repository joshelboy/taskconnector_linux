package com.stc.sockets;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;

import java.util.LinkedList;

public class TaskSocketMqtt extends TaskSimpleSocketClient {
    String[] topics = {"TASK"};     //todo: get from config
    int qos = 2;
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client;
    LinkedList<String> queue = new LinkedList<>();
    TaskConnectionController controller;

    public TaskSocketMqtt(TaskConnectionController control) {
        super(control);
        this.controller = control;
    }

    @Override
    public String getInput() throws Exception {
        return queue.poll();
    }

    @Override
    public String setOutput(TaskSendParameters params) throws Exception {
    	String[] array = params.getSendString().split("_");
    	switch (array[0] ) {
    	case "SUBSCRIBE":	client.subscribe(array[1]);
    						break;
    	case "UNSUBSCRIBE":	client.unsubscribe(array[1]);
							break;
    	case "PUBLISH": array[1] = array[1].toLowerCase();
    					array[2] = array[2].toLowerCase();
    					client.publish(array[1], new MqttMessage(array[2].getBytes()));
    					break;
    	}
//    	params.getse
//
//        if (params.isNoSend() == false && params.getSendString() != null) {
//            MqttMessage message = new MqttMessage(params.getSendString().getBytes());  // todo get message Str
//            message.setQos(qos);
//            client.publish("topic", message);// todo get topic  Str
//        }
        return null; // todo: correct?
    }

    @Override
    public void establishConnectionInternal() {
        try {
        	
//            sampleClient = new MqttClient(broker, clientId, persistence);
            String brokerAdress = "tcp://" + getIp().getHostAddress() + ":" + getPort();
        	client = new MqttClient(brokerAdress, getConId(), persistence);
        	
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            ITaskCommunication taskconnectorclient = this;

//            // subscribe to topiccs
//            for (String topic : topics) {
//                sampleClient.subscribe(topic);
//            }

            // subscribe callbacks
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                	controller.sendToSAP(message.toString(), taskconnectorclient );
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (client == null) return;
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client = null;
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
}
