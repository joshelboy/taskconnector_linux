package com.stc.sockets;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;

import com.stc.service.TaskConnectionController;

public class TaskSapHtppServerHandler implements HttpRequestHandler {
	protected TaskConnectionController control=null;
	
	public TaskSapHtppServerHandler( TaskConnectionController controller ) {
		this.control = controller; 
	}
	
	@Override
	public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
			throws HttpException, IOException {
        
        Header[] headers = request.getHeaders();
        String requestString = "";
        String connectionId = "";
        try {
        	try {
        	for ( Integer i = 0; i < 100 ; i++ ) {
				if (headers[ i ].getName().toLowerCase().equals("requeststring")){
					requestString = headers[ i ].getValue();
				}if (headers[ i ].getName().toLowerCase().equals("connectionid")){
					connectionId = headers[ i ].getValue();
				}
			}
        	}catch (IndexOutOfBoundsException iob){
        		
        	}

        	try {	
	        List<NameValuePair> parameter = new URIBuilder(request.getRequestUri()).getQueryParams();
	        for ( Integer i = 0; i < 100 ; i++ ) {
				if (parameter.get(i).getName().toLowerCase().equals("requeststring")){
					requestString = parameter.get(i).getValue();
				}if (parameter.get(i).getName().toLowerCase().equals("connectionid")){
					connectionId = parameter.get(i).getValue();
				}
				
			}
	    	}catch (IndexOutOfBoundsException iob){
	    	}
        	
        	if (connectionId != "" && requestString != "") {
        		String answer = control.sendToSPS(connectionId + ";" + requestString );
        		response.setCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity(answer));
                response.close();
        	}else {
        		response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setEntity(new StringEntity("Missing Parameter"));
                response.close();
        	}
        	
        } catch (Exception e) {
			// TODO Auto-generated catch block
        	response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.toString()));
            response.close();
		}
        
	}

}
