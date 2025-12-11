package assettrace.network;

import com.google.gson.Gson;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeClient {
    // 특정 노드(targetPort)에게 메시지 전송
    public void send(int targetPort, Message message) {
        // IPC 방식: localhost 소켓 연결
        try (Socket socket = new Socket("127.0.0.1", targetPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // 객체 -> JSON 변환
            Gson gson = new Gson();
            String json = gson.toJson(message);

            // 전송
            writer.println(json);
             System.out.println("[SEND] To " + targetPort + ": " + message.getType());

        } catch (Exception e) {
            // 상대방 노드가 아직 안 켜져 있을 수 있음
            System.err.println("Failed to send to " + targetPort + ": " + e.getMessage());
        }
    }
}