package org.jys.example.common.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.jys.example.common.concurrent.NamedThreadFactory;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YueSong Jiang
 * @date 2019/3/13
 * Tcp server use netty
 */
public class TcpServer {

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private int eventLoopPoolSize = 15;

    private int eventLoopPoolBossSize = 5;

    private int eventLoopPoolWorkerSize = 10;


    public TcpServer() {

        NamedThreadFactory factory = new NamedThreadFactory("tcp-server-worker");
        // The core size must be equal to the max size
        Executor executor = new ThreadPoolExecutor(eventLoopPoolSize, eventLoopPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(), factory);

        bossGroup = new NioEventLoopGroup(eventLoopPoolBossSize, executor);
        workerGroup = new NioEventLoopGroup(eventLoopPoolWorkerSize, executor);
    }

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(30 * 1024, 4, 4));
                pipeline.addLast("byteArrayDecoder", new ByteArrayDecoder());
                pipeline.addLast("byteArrayEncoder", new ByteArrayEncoder());
                pipeline.addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        byte[] rawData = (byte[]) msg;
                        System.out.println(Arrays.toString(rawData));
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        cause.printStackTrace();
                    }
                });
            }
        });
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = serverBootstrap.bind("0.0.0.0", 8086);
        if (future.isSuccess()) {
            System.out.println("tcp server start success");
        } else {
            System.out.println("tcp server start failed");
        }
        future.channel().closeFuture().addListener(future1 -> System.out.println("tcp server stop"));
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
