package com.stc.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.StandardFilter;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.AbstractHttpServerAuthFilter;
import org.apache.hc.core5.http.io.support.BasicHttpServerRequestHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.URIAuthority;

import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;

import com.stc.service.TaskConnectionController;
import com.stc.service.TaskSendParameters;

public class TaskSapHttpServer extends TaskSapJcoServer {
	private String httpPort = "";
	private HttpServer httpServer;
	private SocketConfig socketConfig;
	private boolean isStarted = false;
	private TaskSapHtppServerHandler requestHandler;
	private TaskSapHtppServerHandlerAccept requestHandlerAccept;
	
	
	public TaskSapHttpServer( TaskConnectionController control ) {
		this.control = control;
	}
	
	@Override
	public boolean isConnected() {
		return isStarted;
	}
	
	@Override
	public void establishConnectionInternal() throws Exception {
		PropertiesConfiguration props = this.control.getConnectionManager().getServerConfiguration();
		
        try{
    		sapClientFile = "conf/" + props.getString("SAP_CLIENT");
    		httpPort = props.getString("HTTP_PORT");
    		createSapClientInstance();
    		createHttpServerInstance();
    		
        }catch (Exception e){
        	throw e;
        }
	}
	
	
	public String setOutput(TaskSendParameters params) throws Exception {
		// TODO Auto-generated method stub
    	String answer = null;
    	
    	try {
			answer = this.sendToSAPDestination(sapClientFile, params);			
			return answer;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		}
	}

	private SocketConfig createNewSocketConfig() {
		SocketConfig localSocketConfig = SocketConfig.custom()
											.setSoTimeout(15, TimeUnit.SECONDS)
								            .setTcpNoDelay(true)
								            .build();
		return localSocketConfig;
	}
	
	
	
	private void createHttpServerInstance() throws Exception{
		socketConfig = createNewSocketConfig();
		requestHandler = new TaskSapHtppServerHandler( control );
		requestHandlerAccept = new TaskSapHtppServerHandlerAccept( control );
		
		httpServer = ServerBootstrap.bootstrap() .setListenerPort(Integer.parseInt(httpPort))
							                 	 .register("/cloud", requestHandler )
							                 	 .register("/", requestHandlerAccept )
									                .replaceFilter(StandardFilter.EXPECT_CONTINUE.name(), new AbstractHttpServerAuthFilter<String>(false) {

									                    @Override
									                    protected String parseChallengeResponse(
									                            final String authorizationValue, final HttpContext context) throws HttpException {
									                        return "";
									                        //return authorizationValue;
									                    }

									                    @Override
									                    protected boolean authenticate(
									                            final String challengeResponse,
									                            final URIAuthority authority,
									                            final String requestUri,
									                            final HttpContext context) {
									                    	return true;
									                        //return "".equals(challengeResponse);
									                        //return "let me pass".equals(challengeResponse);
									                    }

									                    @Override
									                    protected String generateChallenge(
									                            final String challengeResponse,
									                            final URIAuthority authority,
									                            final String requestUri,
									                            final HttpContext context) {
									                        return "";
									                        //return "who goes there?";
									                    }

									                })

									                // Add a custom request filter at the beginning of the processing pipeline
									                .addFilterFirst("my-filter", new HttpFilterHandler() {

									                    @Override
									                    public void handle(final ClassicHttpRequest request,
									                                       final HttpFilterChain.ResponseTrigger responseTrigger,
									                                       final HttpContext context, final HttpFilterChain chain) throws HttpException, IOException {
									                        if (request.getRequestUri().equals("/cloud")) {
									                            final ClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_OK);
									                        	requestHandler.handle(request, response, context);
									                            responseTrigger.submitResponse(response);
									                            
									                        } else {
									                            chain.proceed(request, new HttpFilterChain.ResponseTrigger() {

									                                @Override
									                                public void sendInformation(final ClassicHttpResponse response) throws HttpException, IOException {
									                                    responseTrigger.sendInformation(response);
									                                }

									                                @Override
									                                public void submitResponse(final ClassicHttpResponse response) throws HttpException, IOException {
									                                    response.addHeader("X-Filter", "My-Filter");
									                                    responseTrigger.submitResponse(response);
									                                }

									                            }, context);
									                        }
									                    }
									                })
									                
							                	 .setSocketConfig(socketConfig)
							                	 .create();
		httpServer.start();
        
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                httpServer.close(CloseMode.GRACEFUL);
            }
        });
		
        control.setSapState("ALIVE");
        isStarted = true;
        System.out.println("------http port " + httpPort + " successfully set up------");
        //httpServer.awaitTermination(TimeValue.MAX_VALUE);
    }

}
