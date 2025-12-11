package assettrace.node;

import assettrace.network.NetworkConfig;
import assettrace.network.NodeServer;
import java.util.List;

public class UserNode {
    private String nodeId; // 예: U0
    private int port;      // 예: 6000

    public UserNode(String nodeId) {
        this.nodeId = nodeId;
        this.port = NetworkConfig.getPort(nodeId);
    }

    public void start() {
        NodeServer server = new NodeServer(port);
        server.start();

        // 연결된 Full Node 확인 (사용자 노드는 하나의 Full Node에만 연결된다고 가정)
        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        if (neighbors.isEmpty()) {
            System.err.println("[" + nodeId + "] No connected Full Node found in topology!");
            return;
        }

        String targetFullNode = neighbors.get(0);
        System.out.println("[" + nodeId + "] Connected to Full Node: " + targetFullNode);

        // TODO: 주기적으로 트랜잭션 생성(판매) 로직 추가
    }

    public static void main(String[] args) {
        String id = (args.length > 0) ? args[0] : "U0";
        new UserNode(id).start();
    }
}