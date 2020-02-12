package com.khs.khsNettyServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Logger logger = LogManager.getLogger(ServerHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // super.handlerAdded(ctx);
        logger.info("[SERVER] handler added.");
        Channel in = ctx.channel();
        for(Channel channel : channelGroup) {
            // 사용자가 추가되었을 때 기존 client 들에게 알림
            channel.write(String.format("[SERVER] - %s has joined\n", in.remoteAddress()));
        }
        channelGroup.add(in);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // super.channelActive(ctx);
        // 사용자가 접속했을 때, 서버에 알림
        logger.info("User Access !");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // super.handlerRemoved(ctx);
        logger.info("[SERVER] handler removed.");
        Channel in = ctx.channel();
        for(Channel channel : channelGroup) {
            // 사용자가 나갔을 때 기존 client 들에게 알림
            channel.write(String.format("[SERVER] - %s has left\n", in.remoteAddress()));
        }
        channelGroup.remove(in);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // super.channelRead(ctx, msg);
        String message = null;
        message = (String) msg;
        logger.info(String.format("[SERVER] channelRead - ", message));
        Channel in = ctx.channel();
        for(Channel channel : channelGroup) {
            if(channel != in) {
                channel.writeAndFlush(String.format("[%s] %s\n", in.remoteAddress(), message));
            }
        }
        if("exit".equalsIgnoreCase(message)) {
            ctx.close();
        }




    }
}
