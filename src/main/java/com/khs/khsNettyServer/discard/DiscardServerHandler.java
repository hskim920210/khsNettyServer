package com.khs.khsNettyServer.discard;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// SimpleChannelInboundHandler : 수신한 데이터를 처리하는 이벤트를 제공한다.
public class DiscardServerHandler extends SimpleChannelInboundHandler<Object> {
	private Logger logger = LogManager.getLogger(DiscardServerHandler.class);
	
	// 5.0에서 messageReceived로 바뀔 예정
	// 지정한 포트로 접속한 클라이언트가 데이터를 전송하면 자동으로 실행되는 메소드
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		logger.info("channelRead0");
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// super.exceptionCaught(ctx, cause);
		logger.info("exceptionCaught");
		cause.printStackTrace();
		ctx.close();
	}
}
