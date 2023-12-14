package com.stc.fugitive.test.io.netty.demo1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

public class Server {

    //http://www.easyswoole.com/wstool.html
    public static void main(String[] args) throws InterruptedException {
        // 用来接收客户端传进来的连接
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用来处理已被接收的连接
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        // 创建 netty 服务
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    // 设置 NIO 模式
                    .channel(NioServerSocketChannel.class)
                    // 设置 tcp 缓冲区
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 设置发送缓冲区数据大小
                    .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
                    // 设置接收缓冲区数据大小
                    .option(ChannelOption.SO_RCVBUF, 64 * 1024)
                    // 保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // HttpClient编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 设置最大内容长度
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            // WebSocket 数据压缩扩展
                            pipeline.addLast(new WebSocketServerCompressionHandler());
                            // WebSocket 握手、控制帧处理
                            pipeline.addLast(new WebSocketServerProtocolHandler("/myWebSocket/path", null, true));
                            // 通道的初始化，数据传输过来进行拦截及执行
                            pipeline.addLast(new ServerHandler());
                        }
                    });
            // 绑定端口启动服务
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}