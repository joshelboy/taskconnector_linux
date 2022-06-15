package com.stc.sockets;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTT_PLAYGROUND {
    //source: https://www.eclipse.org/paho/index.php?page=clients/java/index.php
    public static void main(String[] args) {
        String topic = "MQTT Examples";
        String content = "Message from MqttPublishSample";
        int qos = 2;
        String broker = "tcp://localhost:1883"; //192.168.177.13
        String clientId = "TaskConnector";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            //todo: authentication https://stackoverflow.com/questions/42378119/how-to-subscribe-to-a-mqtt-topic-and-print-received-messages-on-eclipse-java#:~:text=MqttClient%20client%20%3D%20new,client.connect(options)%3B
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");

            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("*** new Message: ");
                    System.out.println("Topic: " + topic);
                    System.out.println("Message: " + message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
            sampleClient.subscribe(topic);

            // sampleClient.disconnect();
            //  System.out.println("Disconnected");
            // System.exit(0);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }


    }
}

