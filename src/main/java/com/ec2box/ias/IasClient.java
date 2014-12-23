package com.ec2box.ias;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class IasClient {
	
	private static WebResource resource;
	
	public static WebResource getClient(String serverUrl){
		if(resource==null){
			ClientConfig config = new DefaultClientConfig();
	        Client client = Client.create(config);
	        resource = client.resource(UriBuilder.fromUri(serverUrl).build());
		}
		return resource;
	}
}
