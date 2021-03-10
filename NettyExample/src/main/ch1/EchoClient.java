package ch1;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

// 클라이언트 채널
public final class EchoClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)//서버와 다르게 이벤트루프 그룹이 1개만 존재한다. 
             .channel(NioSocketChannel.class) // NIO소캣채널 
             .handler(new ChannelInitializer<SocketChannel>() {//클라이언트 채널이므로 파이프라인의 설정에 일반 소켓 채널클레스 사용
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new EchoClientHandler());
                 }
             });

            ChannelFuture f = b.connect("localhost", 8888).sync();//비동기 입출력 메소드인 connect호출. 즉 connect메소드의 처리가 완료될 때까지 다음라인을 진행하지 않는다.

            f.channel().closeFuture().sync();
        }
        finally {
            group.shutdownGracefully();
        } 
    }
}