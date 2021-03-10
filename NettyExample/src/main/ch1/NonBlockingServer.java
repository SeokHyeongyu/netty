package ch1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

    private void startEchoServer() {
       try (//1
          Selector selector = Selector.open();//2 Selector 자신에게 등록된 채널에 변경 사항이 발생했는지 검사 하고 변경사항이 발생한 채널에 대한 접근을 가능하게 해준다.
          ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()//3 논블록킹 소켓의 서버 소켓 채널 생성 브로킹 소켓과 다르게 소켓 채널을 먼저 생성하고 사용한 포트를 바인딩
        ) {

          if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {//4 selector와 serverSocketChannel 객체가 정상적으로 생성되었는지 확인
             serverSocketChannel.configureBlocking(false);//5 기본값은 true이며 serverSocketChannel객체를 논블로킹 모드로 설정하기 위해서 false로 설정한다.
             serverSocketChannel.bind(new InetSocketAddress(8888));//6 클라이언트의 연결을 대기할 포트를 지정 serverSocketChannel객체가 지정된 포트로부터 클라이언트의 연결을 생성할 수 있다.

             serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);//7 selector객체에 등록 연결요청 이벤트 감지 SelectionKey.OP_ACCEPT
             System.out.println("접속 대기중");

             while (true) {
                selector.select();//8 selector에 등록된 채널에서 변경사항이 있는지 검사한다. 아무런 I/O이벤트도 발생하지 않으면 블로킹된다. 블로킹을 피하고 싶다면 selectNow 메소드를 사용
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();//9 등록된 채널중 이벤트가 발생한 채널 목록을 조회

                while (keys.hasNext()) {
                   SelectionKey key = (SelectionKey) keys.next();
                   keys.remove();//10 I/O이벤트가 발생한 채널중 중복된 이벤트가 감지 되는걸 방지하고 조회된 목록에서 제거

                   if (!key.isValid()) {
                      continue;
                   }

                   if (key.isAcceptable()) {//11 조회된 I/O 이벤트가 연결요청 이벤트인지 확인 / 연결요청이면 연결처리 메소드 실행
                      this.acceptOP(key, selector);
                   }
                   else if (key.isReadable()) {//12 조회된 I/O 이벤트가 데이터 수신인지 확인 / 데이터 수신 이벤트면 데이터 읽기 처리 메소드 실행
                      this.readOP(key);
                   }
                   else if (key.isWritable()) {//13 조회된 I/O 이벤트가 데이터 쓰기 가능인지 확인 / 데이터 쓰기 기능 이벤트 실행
                      this.writeOP(key);
                   }
                }
             }
          }
          else {
             System.out.println("서버 소캣을 생성하지 못했습니다.");
          }
       }
       catch (IOException ex) {
          System.err.println(ex);
       }
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException { // 연결요청 메소드
       ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();//14 ServerSocketChannel 채널에서 발생된 이벤트 이므로 ServerSocketChannel로 변환
       SocketChannel socketChannel = serverChannel.accept();//15 ServerSocketChannel 을 사용하여 클라이언트 연결 수락하고 연결된 채널을 가져온다.
       socketChannel.configureBlocking(false);//16 연결된 소켓 채널을 논블로킹 모드로 설정

       System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());

       keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
       socketChannel.register(selector, SelectionKey.OP_READ);//17 연결된 클라이언트 소켓 채널을 Selector에 등록하여 I/O이벤트 감시
    }

    private void readOP(SelectionKey key) {
       try {
          SocketChannel socketChannel = (SocketChannel) key.channel();
          buffer.clear();
          int numRead = -1;
          try {
             numRead = socketChannel.read(buffer);
          }
          catch (IOException e) {
             System.err.println("데이터 읽기 에러!");
          }

          if (numRead == -1) {
             this.keepDataTrack.remove(socketChannel);
             System.out.println("클라이언트 연결 종료 : "+ socketChannel.getRemoteAddress());
             socketChannel.close();
             key.cancel();
             return;
          }

          byte[] data = new byte[numRead];
          System.arraycopy(buffer.array(), 0, data, 0, numRead);
          System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());

          doEchoJob(key, data);
       }
       catch (IOException ex) {
          System.err.println(ex);
       }
    }

    private void writeOP(SelectionKey key) throws IOException {
       SocketChannel socketChannel = (SocketChannel) key.channel();

       List<byte[]> channelData = keepDataTrack.get(socketChannel);
       Iterator<byte[]> its = channelData.iterator();

       while (its.hasNext()) {
          byte[] it = its.next();
          its.remove();
          socketChannel.write(ByteBuffer.wrap(it));
       }

       key.interestOps(SelectionKey.OP_READ);
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
       SocketChannel socketChannel = (SocketChannel) key.channel();
       List<byte[]> channelData = keepDataTrack.get(socketChannel);
       channelData.add(data);

       key.interestOps(SelectionKey.OP_WRITE);
    }

    public static void main(String[] args) {
       NonBlockingServer main = new NonBlockingServer();
       main.startEchoServer();
    }
 }