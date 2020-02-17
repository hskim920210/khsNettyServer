package com.khs.khsNettyServer.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class NonBlockingServer {
	private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<SocketChannel, List<byte[]>>();
	private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
	
	private Logger logger = LogManager.getLogger(NonBlockingServer.class);
	
	private void startEchoServer() {
		try (
			// 자신에게 등록된 채널에 변경사항이 발생했는지 검사하고 변경한 채널에 대한 접근을 가능하게 해준다.
			Selector selector = Selector.open();
			// 블로킹 소켓의 ServerSocket에 대응되는 논블로킹 소켓의 서버 소켓 채널.
			// 블로킹 소켓과 다르게 소켓채널을 먼저 생성하고 사용할 포트를 바인딩
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		){
			// 두 객체가 정상적으로 생성된지 확인
			if( (serverSocketChannel.isOpen()) && (selector.isOpen()) ) {
				// 소켓채널의 블로킹모드 기본은 ture이다.
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.bind(new InetSocketAddress(8888));
				
				// 현재 서버 소켓 채널 객체를 셀렉터 객체에 등록하고 셀렉터가 감지할 이벤트는 연결요청에 해당하는 OP_ACCEPT
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				
				logger.info("Wait for connection ...");
				
				while(true) {
					// selector에 등록된 채널에서 변경사항이 발생했는지 검사.
					// selector에 아무런 io 이벤트도 발생하지 않으면 스레드는 이부분에서 블로킹 된다.
					// io이벤트가 발생하지 않았을 때 블로킹을 피하려면 selectNow 메소드를사용.
					selector.select();
					// selector에 등록된 채널 중 io이벤트가 발생한 채널의 목록을 조회
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					
					while(keys.hasNext()) {
						SelectionKey key = (SelectionKey) keys.next();
						// io이벤트가 발생한 채널에서 동일한 이벤트가 감지되는 것을 방지하기 위해 조회된 목록에서 제거.
						keys.remove();
						
						if(!key.isValid()) {
							continue;
						}
						
						
						if(key.isAcceptable()) {				// 조회된 io이벤트 종류가 연결 요청인지 확인.
							this.acceptOP(key, selector);
						} else if (key.isReadable()) {			// 조회된 io이벤트 종류가 데이터 수신인지 확인.
							this.readOP(key);
						} else if (key.isWritable()) {			// 조회된 io이벤트 종류가 데이터 쓰기인지 확인.
							this.writeOP(key);
						}
					}
				}
			} else {
				logger.info("Cannot create server socket ...");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	private void acceptOP(SelectionKey key, Selector selector) throws IOException {
		// 연결 요청 이벤트가 발생한 채널은 항상 ServerSocketChannel이므로 이벤트가 발생한 채널을 형변환 한다.
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverChannel.accept();
		socketChannel.configureBlocking(false);
		
		logger.info(String.format("Client connected : %s", socketChannel.getRemoteAddress().toString()));
		
		keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
		
		// io 이벤트를 감시하기 위한 등록
		socketChannel.register(selector, SelectionKey.OP_READ);
	}
	
	private void readOP(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		buffer.clear();
		int numRead = -1;
		
		try {
			numRead = socketChannel.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failed to read data ...");
		}
		
		if(numRead == -1) {
			this.keepDataTrack.remove(socketChannel);
			try {
				logger.info(String.format("Client connection terminated : ", socketChannel.getRemoteAddress().toString()));
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			key.cancel();
			return;
		}
		
		byte[] data = new byte[numRead];
		System.arraycopy(buffer.array(), 0, data, 0, numRead);
		try {
			logger.info(String.format("%s from %s", new String(data, "UTF-8"), socketChannel.getRemoteAddress().toString()));
			doEchoJob(key, data);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	private void writeOP(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		Iterator<byte[]> its = channelData.iterator();
		
		while(its.hasNext()) {
			byte[] it = its.next();
			its.remove();
			socketChannel.write(ByteBuffer.wrap(it));
		}
		
		key.interestOps(SelectionKey.OP_READ);
	}
	
	private void doEchoJob(SelectionKey key, byte[] data) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		channelData.add(data);
		
		key.interestOps(SelectionKey.OP_WRITE);
	}
	
	public static void main(String[] args) {
		NonBlockingServer main = new NonBlockingServer();
		main.startEchoServer();
	}
}
