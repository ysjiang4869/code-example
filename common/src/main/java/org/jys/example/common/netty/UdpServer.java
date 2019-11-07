package org.jys.example.common.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.jys.example.common.concurrent.NamedThreadFactory;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YueSong Jiang
 * @date 2019/3/13
 * @description <p> </p>
 */
public class UdpServer {

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private int eventLoopPoolCoreSize = 10;

    private int eventLoopPoolMaxSize = 15;

    private int eventLoopPoolBossSize = 5;

    private int eventLoopPoolWorkerSize = 10;


    public UdpServer() {

        NamedThreadFactory factory = new NamedThreadFactory("udp-server-worker");
        Executor executor = new ThreadPoolExecutor(eventLoopPoolCoreSize, eventLoopPoolMaxSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(), factory);

        bossGroup = new NioEventLoopGroup(eventLoopPoolBossSize, executor);
        workerGroup = new NioEventLoopGroup(eventLoopPoolWorkerSize, executor);
    }

    public void start() {
        Bootstrap bootStrap = new Bootstrap();
        bootStrap.group(bossGroup);
        bootStrap.channel(NioDatagramChannel.class);
        bootStrap.option(ChannelOption.SO_BROADCAST, true);
        bootStrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                        ByteBuf buf = msg.content();
                        byte[] rawData = new byte[buf.readableBytes()];
                        buf.readBytes(rawData);
                        System.out.println(Arrays.toString(rawData));
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        cause.printStackTrace();
                    }
                });
            }
        });
        ChannelFuture future = bootStrap.bind("0.0.0.0", 8086);
        if (future.isSuccess()) {
            System.out.println("udp server start success");
        } else {
            System.out.println("udp server start failed");
        }
        future.channel().closeFuture().addListener(future1 -> System.out.println("udp server stop"));
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
