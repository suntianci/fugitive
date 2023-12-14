package com.stc.fugitive.test.io.netty.demo1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道激活（回调）");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        System.out.println("通道关闭（回调）");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 仅处理 TextWebSocketFrame
        if (msg instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) msg).text();
            System.out.println("收到请求：" + request);
            ctx.writeAndFlush(new TextWebSocketFrame("PONG"));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("数据读取完成");
    }
}