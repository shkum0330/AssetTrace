package assettrace.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer extends Thread {
    private int port;
    private boolean running = true;

    public NodeServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(">> Server started on port: " + port);

            while (running) {
                // 이웃의 연결 요청 대기 (Blocking)
                Socket clientSocket = serverSocket.accept();

                // 새로운 연결이 오면 별도 스레드나 로직으로 처리
                // (여기서는 간단히 바로 읽어서 처리)
                handleMessage(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))
        ) {
            String jsonLine = reader.readLine();
            if (jsonLine == null) return;

            Gson gson = new Gson();
            Message message = gson.fromJson(jsonLine, Message.class);

            System.out.println(String.format("[RECV] From %s: %s", message.getSender(), message.getType()));

            // TODO: 여기서 실제 로직(블록 검증, 트랜잭션 풀 추가 등)을 수행할 핸들러를 호출해야 함


        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }
}