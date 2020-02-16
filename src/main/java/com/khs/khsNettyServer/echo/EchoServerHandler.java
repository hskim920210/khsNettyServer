package com.khs.khsNettyServer.echo;

import java.nio.charset.Charset;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

// ChannelInboundHandlerAdapter : 수신한 데이터를 처리하는 이벤트를 제공한다.
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
	private Logger logger = LogManager.getLogger(EchoServerHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// super.channelRead(ctx, msg);
		String readMessage = ( (ByteBuf) msg ).toString(Charset.defaultCharset());
		logger.info(String.format("수신한 문자열 [%s]", readMessage));
		
		ctx.write(msg);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// super.channelReadComplete(ctx);
		logger.info("channelReadComplete");
		ctx.flush();
	}
	
}
