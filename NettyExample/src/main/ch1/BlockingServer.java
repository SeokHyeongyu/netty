package ch1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {
    public static void main(String[] args) throws Exception {
        BlockingServer server = new BlockingServer();
        server.run();
    }

    private void run() throws IOException {
        ServerSocket server = new ServerSocket(8888);//1 클라이언트가 접속하기전까지 무한 대기
        System.out.println("접속 대기중");

        while (true) {
            Socket sock = server.accept();//2 클라이언트가 접속하면 소켓생성
            System.out.println("클라이언트 연결됨");

            OutputStream out = sock.getOutputStream();//3
            InputStream in = sock.getInputStream();//4

            while (true) {
                try {
                    int request = in.read();//5클라이언트 입력대기
                    out.write(request);
                }
                catch (IOException e) {
                    break;
                }
            }
        }
    }
}