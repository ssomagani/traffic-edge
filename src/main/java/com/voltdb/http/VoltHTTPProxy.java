package com.voltdb.http;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;

import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

public class VoltHTTPProxy {
	
    public static void main(String[] args) throws IOException {
        // Create a server listening on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Create a context for "/" path
        server.createContext("/index.html", new FileHandler());
        
        server.createContext("/traffic", new TrafficHandler());

        // Start the server
        server.start();

        System.out.println("Server is running on port 8000");
    }
}

class TrafficHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
    	System.out.println("Serving /traffic");
    	String response = "";
    	Client client = ClientFactory.createClient();
    	try {
			client.createConnection("localhost");
		} catch (Exception e1) {
			System.out.println("can't connect");
			e1.printStackTrace();
		}
    	try {
    		Object[] params = new Object[] {};
    		System.out.println("Calling proc");
			ClientResponse resp = client.callProcedure("GET_TRAFFIC", params);
			if(resp.getStatus() == ClientResponse.SUCCESS) {
				VoltTable results = resp.getResults()[0];
				response = results.toJSONString();
			} else {
				response = "Got no response";
			}
		} catch (IOException | ProcCallException e) {
			e.printStackTrace();
			response = "Exception " + e.getMessage();
		}
    	try {
    		response = "Something";
			exchange.sendResponseHeaders(200, response.getBytes().length);
	        OutputStream os = exchange.getResponseBody();
	        os.write(response.getBytes());
	        os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

class FileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            // Get the requested URI path
            String uriPath = exchange.getRequestURI().getPath();
            
            // If the path is "/", serve index.html
            if (uriPath.equals("/")) {
            	System.out.println("fetching index.html");
                uriPath = "/index.html";
            }

            // Load the requested file
            String filePath = "." + uriPath; // Assuming files are in the current directory
            File file = new File(filePath);

            if (file.exists() && !file.isDirectory()) {
                // Prepare response headers
                exchange.sendResponseHeaders(200, file.length());
                
                // Write the file contents to the response body
                OutputStream os = exchange.getResponseBody();
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                fis.close();
                os.close();
            } else {
                // File not found, return 404
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            // Method not allowed, return 405
            String response = "Method not allowed";
            exchange.sendResponseHeaders(405, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
