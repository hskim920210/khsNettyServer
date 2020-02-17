package com.khs.khsNettyServer.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class BlockingServer {
	private Logger logger = LogManager.getLogger(BlockingServer.class);
	public static void main(String[] args) {
		BlockingServer server = new BlockingServer();
		try {
			server.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void run() throws IOException {
		ServerSocket server = new ServerSocket(8888);
		logger.info("Wait for connect ...");
		
		while(true) {
			// 클라이언트의 연결을 기다리며 대기한다. (Blocking)
			Socket sock = server.accept();
			logger.info("Client connected !");
			
			OutputStream out = sock.getOutputStream();
			InputStream in = sock.getInputStream();
			
			while(true) {
				try {
					// read 할 것이 있을 때 까지 기다린다. (Blocking)
					int request = in.read();
					out.write(request);
				} catch (IOException e) {
					e.printStackTrace();
					server.close();
					break;
				}
			}
		}
		
	}
}
