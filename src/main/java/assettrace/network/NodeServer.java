package assettrace.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class NodeServer extends Thread {
    private int port;
    private boolean running = true;

    // 메시지가 오면 실행할 함수
    private Consumer<Message> onMessageReceived;

    public NodeServer(int port, Consumer<Message> onMessageReceived) {
        this.port = port;
        this.onMessageReceived = onMessageReceived;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(">> Server started on port: " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                handleMessage(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Socket socket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            String jsonLine = reader.readLine();
            if (jsonLine == null) return;

            // JSON 파싱 시 Interface/Object 타입 문제 해결을 위해 커스텀 어댑터 필요할 수 있음.
            // 일단 간단하게 Gson 처리 (LinkedTreeMap으로 변환될 수 있음)
            Gson gson = new Gson();
            Message message = gson.fromJson(jsonLine, Message.class);

            // 콜백 호출 -> FullNode나 UserNode가 처리
            if (onMessageReceived != null) {
                onMessageReceived.accept(message);
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }
}