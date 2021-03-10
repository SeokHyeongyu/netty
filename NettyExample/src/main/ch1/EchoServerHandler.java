package ch1;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter { //ChannelInboundHandlerAdapter입력된 데이터를 처리하는 인벤트 핸들러 상속
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { //클라이언트로부터 데이터의 수신이 이루어졌을때 네티가 자동으로 호출하는 메소드
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset()); //수신된 데이터를 가지고 있는 네티의 바이트 버퍼 객체로부터 문자열 데이터를 잃어온다.

        StringBuilder builder = new StringBuilder();
        builder.append("수신한 문자열 [");
        builder.append(readMessage); //수신된 문자
        builder.append("]");
        System.out.println(builder.toString());

        ctx.write(msg); //ChannelHandlerContext 인터페이스의 객체로서 채널 파이프라인에 대한 이벤트 처리 / 클라이언트채널로 입력받은 데이터 전송
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {//channelRead이벤트 처리가 완료된 후 자동으로 수행되는 이벤트 메소드 
        ctx.flush();// 채널 파이프라인에 저장된 버퍼를 전송하는 flush메서드 호출
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
} 