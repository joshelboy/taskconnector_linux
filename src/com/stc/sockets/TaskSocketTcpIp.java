package com.stc.sockets;

import com.sap.conn.jco.util.Codecs.Hex;
import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TaskSocketTcpIp extends TaskSimpleSocketClient {

    private Socket liftSocketTcp;

    public TaskSocketTcpIp(TaskConnectionController control) {
        super(control);
    }

    @Override
    public void establishConnectionInternal() {
        if (this.getTimout() == 0) {
            this.setTimout(1000);
        }

        try {
            logger.info("------Trying to establish connection to device (IP:" + ip + ",Port:" + port + ")------");
            System.out.println("------Trying to establish connection to device (IP:" + ip + ",Port:" + port + ")------");

            this.liftSocketTcp = new Socket(this.getIp(), this.getPort());
            this.liftSocketTcp.setSoTimeout(this.getTimout());

            logger.info("------Connection to device (IP:" + ip + ",Port:" + port + ") successfully established------");
            System.out.println("------Connection to device (IP:" + ip + ",Port:" + port + ") successfully established------");

        } catch (Exception e) {
            logger.error("------Occured error: " + e.getMessage() + "------");
            //System.out.println("------Occured error: " + e.getMessage() + "------");

            logger.error("------Error while connecting to device (IP:" + ip + ",Port:" + port + ")------");
            //System.out.println("------Error while connecting to device (IP:" + ip + ",Port:" + port + ")------");
        }
    }

    @Override
    public void disconnect() {

        this.setFailedReads(0);

        try {
            if (this.liftSocketTcp != null) {
                liftSocketTcp.close();
            }
        } catch (IOException e) {
        }
        liftSocketTcp = null;
    }

    @Override
    public boolean isConnected() {
        if (this.getOpenOnSend())
            return true;

        if (this.liftSocketTcp == null) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public String getInput() throws Exception {

        Integer length = 1024;
        int nRead;
        char[] cbuffer = new char[1024];
        byte[] data = new byte[16384];

        try {
            if (this.withHex == false) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.liftSocketTcp.getInputStream()));
                length = bufferedReader.read(cbuffer);

                if (length == -1) {
                    this.disconnect();
                    throw new Exception("Connection closed by remote peer");
                } else {
                    // Nachricht aus dem Puffer lesen
                    return new String(cbuffer, 0, length);
                }

            } else {

                length = (nRead = this.liftSocketTcp.getInputStream().read(data, 0, data.length));

                if (length == -1) {
                    this.disconnect();
                    throw new Exception("Connection closed by remote peer");

                } else {
                    // Nachricht aus dem Puffer lesen
                    ByteArrayOutputStream bBuffer = new ByteArrayOutputStream();
                    bBuffer.write(data, 0, nRead);
                    bBuffer.flush();

                    if (this.withHex == false)
                        return bBuffer.toString();
                    else
                        return Hex.encode(bBuffer.toByteArray());
                }
            }

        } catch (SocketTimeoutException e1) {
            //ReadTimeOut
            //Gegenstelle hat keine Daten gesendet
            //wir fangen das nur ab
            return "";

        } catch (IOException e) {
            logger.error(e.getMessage().toString());
            if (e.getMessage().toString().equals("Software caused connection abort: recv failed") ||
                    e.getMessage().toString().equals("Connection reset")) {
                this.disconnect();
            }

            throw e;

        } catch (IndexOutOfBoundsException e) {
            logger.error(e.getMessage().toString());
            throw e;

        } catch (Exception e) {
            logger.error(e.getMessage().toString());
            if (e.getMessage().toString().equals("Software caused connection abort: recv failed")) {
                this.disconnect();
            }

            throw e;
        }
    }

    @Override
    public String setOutput(TaskSendParameters params) throws Exception {

        byte[] array = null;
        BufferedOutputStream bos = null;
        String returnValue = "";

        try {
            if (this.getOpenOnSend())
                this.establishConnectionInternal();

            if (this.getEmptyStream()) {
                try {
                    this.getInput();
                } catch (Exception e) {

                }
            }

            if (params.isNoSend() == false && params.getSendString() != null) {
                //Sendevorgang ist gewünscht

                if (this.getWithTHex() == true) {
                    //HEX-Konvertierung erwünscht
                    array = Hex.decode(params.getSendString());
                } else {
                    //wurde schon korrekt konvertiert übergeben
                    array = params.getSendString().getBytes();
                }

                //Logik für TCP/IP-Kommunikation
                if (this.liftSocketTcp == null)
                    throw new Exception("No connection to remote peer available");

                bos = new BufferedOutputStream(this.liftSocketTcp.getOutputStream());
                bos.write(array);
                bos.flush();
            }

            if (params.isNoRead() == false)
                //Lesevorgang ist gewünscht
                returnValue = this.getInput();

            if (this.getOpenOnSend())
                this.disconnect();

            return returnValue;

        } catch (Exception e) {
            logger.error(e.getMessage().toString());
            this.disconnect();
            //this.establishConnectionInternal();
            throw e;
        }
    }
}
