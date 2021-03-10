package ch1;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {// 소켓 채널이 최초 활성화 되었을 때 실행
        String sendMessage = "Hello netty";

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        StringBuilder builder = new StringBuilder();
        builder.append("전송한 문자열 [");
        builder.append(sendMessage);
        builder.append("]");

        System.out.println(builder.toString());
        ctx.writeAndFlush(messageBuffer);//writeAndFlush 메소드는 내부적으로 데이터 기록과 전송의 두가지 메소드를 호출한다. (write : 데이터 기록 메소드 / flush : 데이터를 서버로 전송하는 메소드)
    }
 
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {// 서버로 부터 수신된 데이터가 있을때 발생하는 이벤트
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());//문자열 데이터 추출

        StringBuilder builder = new StringBuilder();
        builder.append("수신한 문자열 [");
        builder.append(readMessage);
        builder.append("]");

        System.out.println(builder.toString());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {// 수신된 모든 데이터를 읽었을때 호출 channelRead메소드 수행이 완료되면 자동실행
        ctx.close();//데이터 채널 닫음
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}