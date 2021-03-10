package ch1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

 
public class DiscardServerHandler extends SimpleChannelInboundHandler<Object> {
	
	//DiscardServer에서 지정된 포트로 접속한 클라이언트가 데이터를 전속하면 channelRead0메소드가 자동 실행된다
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 아무것도 하지 않음.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}