package assettrace.node;

import assettrace.core.Transaction;
import assettrace.network.Message;
import assettrace.network.NetworkConfig;
import assettrace.network.NodeClient;
import assettrace.network.NodeServer;
import assettrace.util.CryptoUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

public class UserNode {
    private String nodeId;
    private int port;

    // 내 지갑 (키 쌍)
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public UserNode(String nodeId) {
        this.nodeId = nodeId;
        this.port = NetworkConfig.getPort(nodeId);

        // 키 쌍 생성
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public void start() {
        NodeServer server = new NodeServer(port, null); // 콜백 null 처리
        server.start();

        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        if (neighbors.isEmpty()) {
            System.err.println("[" + nodeId + "] No connected Full Node found!");
            return;
        }
        String targetFullNode = neighbors.get(0);
        int targetPort = NetworkConfig.getPort(targetFullNode);

        System.out.println("[" + nodeId + "] Connected to " + targetFullNode);

        // 트랜잭션 생성 및 전송 테스트
        try {
            Thread.sleep(2000); // 서버들이 켜질 시간 약간 대기
            sendRandomTransaction(targetFullNode, targetPort);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRandomTransaction(String targetNodeName, int targetPort) {
        // 1. 더미 데이터 생성
        String myPkStr = CryptoUtil.getStringFromKey(publicKey);
        String receiverPkStr = "DUMMY_RECEIVER_PK"; // 예시

        Transaction tx = new Transaction(
                myPkStr, receiverPkStr,
                "ITEM-" + System.currentTimeMillis(), // Identifier (Random)
                "Tesla Model X", "2025-12-19",
                100.0, "2025-12-20", "Brand New"
        );

        // 2. 트랜잭션 ID에 서명
        String signature = CryptoUtil.applyECDSASig(privateKey, tx.getDataForSigning());
        tx.setSignature(signature);

        // 3. 메시지 패키징 및 전송
        Message msg = new Message(Message.Type.TRANSACTION, nodeId, tx);

        System.out.println("[" + nodeId + "] Sending Tx (" + tx.getTrID().substring(0, 6) + "...) to " + targetNodeName);

        new NodeClient().send(targetPort, msg);
    }

    public static void main(String[] args) {
        String id = (args.length > 0) ? args[0] : "U0";
        new UserNode(id).start();
    }
}