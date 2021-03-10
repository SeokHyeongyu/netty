package ch1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

//서버
public class EchoServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);// NioEventLoopGroup 클래스 객체 할당 생성자에 입력된 스레드 수는 1이므로 단일 스레드
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 생성자에 입력된 수가 없으므로 CPU 코어 수에 따른 스레드 수가 설정된다.
        try {
            ServerBootstrap b = new ServerBootstrap(); //ServerBootstrap 생성
            b.group(bossGroup, workerGroup) // group함수에 (부모스레드-클라이언트 연결 요청 수락을 담당 / 자식스레드- I/O처리를 담당)를 담는다
             .channel(NioServerSocketChannel.class)// 부모스레드가 사용할 입출력 모드설정 NioServerSocketChannel클래스로 설정 했기에 NIO모드로 동작한다.
             .childHandler(new ChannelInitializer<SocketChannel>() {// 자식채널의 초기화 방법을 설정 
                @Override
                public void initChannel(SocketChannel ch) { // 클라이언트로 부터 연결된 채널이 초기화 될때 기본동작이 지정된 추상클래스
                    ChannelPipeline p = ch.pipeline();// 채널 파이프라인 생성
                    p.addLast(new EchoServerHandler()); // 채널 파이프라인에 EchoServerHandler클래스 등록 (EchoServerHandler클래스는 이후 클라이언트와 연결 되었을때 데이터 처리를 담당)
                }
            });

            ChannelFuture f = b.bind(8888).sync();//접속할 포트지정

            f.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    } 
}