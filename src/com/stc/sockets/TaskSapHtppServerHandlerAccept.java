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

public class TaskSapHtppServerHandlerAccept implements HttpRequestHandler {
	protected TaskConnectionController control=null;
	
	public TaskSapHtppServerHandlerAccept( TaskConnectionController controller ) {
		this.control = controller; 
	}
	
	@Override
	public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
			throws HttpException, IOException {
        
        		response.setCode(HttpStatus.SC_ACCEPTED);
                response.setEntity(new StringEntity("Missing Parameter"));
                response.close();
        	
        
	}

}
