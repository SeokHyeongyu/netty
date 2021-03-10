package ch1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

public class BlockingEchoServer {
	
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new OioEventLoopGroup(1); //EchoServer와 다르게 OioEventLoopGroup 지정하면 블로킹 모드로 전환
        EventLoopGroup workerGroup = new OioEventLoopGroup(); // EchoServer와 다르게 OioEventLoopGroup 지정하면 블로킹 모드로 전환
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
            .channel(OioServerSocketChannel.class) //EchoServer와 다르게 OioEventLoopGroup 지정하면 논블로킹 모드로 전환
            .childHandler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel ch) {
                   ChannelPipeline p = ch.pipeline();
                   p.addLast(new EchoServerHandler());
               }
            });

            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}