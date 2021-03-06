package one.inve.localfullnode2.utilities.http;

import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: {@code port} allow us to customize the listener port.
 * @author: Francis.Deng
 * @date: May 13, 2019 1:36:05 AM
 * @version: V1.0
 */
public class NettyHttpServer {
	private ChannelFuture channel;
	private final EventLoopGroup masterGroup;
	private final EventLoopGroup slaveGroup;

	private int port;

	public NettyHttpServer() {
		masterGroup = new NioEventLoopGroup();
		slaveGroup = new NioEventLoopGroup();
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void start(ChannelInitializer channelInitializer) {
		start(channelInitializer, -1);
	}

	public void start(ChannelInitializer channelInitializer, int howManyNettyThreads) {
		// Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
		if (howManyNettyThreads > 0)
			System.setProperty("io.netty.eventLoopThreads", String.valueOf(howManyNettyThreads));

		try {
			final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup, slaveGroup)
					.channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, false).childHandler(channelInitializer);

			channel = bootstrap.bind(port).sync();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		slaveGroup.shutdownGracefully();
		masterGroup.shutdownGracefully();

//		try {
//			channel.channel().closeFuture().sync();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		try {
			slaveGroup.terminationFuture().sync();
			masterGroup.terminationFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static NettyHttpServer boostrap(HttpServiceImplsDependent dep, int port) {
		return boostrap(dep, port, -1);
	}

	public static NettyHttpServer boostrap(HttpServiceImplsDependent dep, int port, int threadSize) {
		NettyHttpServer httpServer = new NettyHttpServer();
		httpServer.setPort(port);

		new Thread(() -> httpServer.start(new HttpChannelInitializer(dep), threadSize)).start();

		return httpServer;
	}

	@Test
	public static void main(String[] args) {
		new NettyHttpServer().start(new DefaultChannelInitializer());
	}
}