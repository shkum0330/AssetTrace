package assettrace.node;

import assettrace.core.Block;
import assettrace.core.Transaction;
import assettrace.network.Message;
import assettrace.network.NetworkConfig;
import assettrace.network.NodeServer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static assettrace.network.Message.Type.TRANSACTION;

public class FullNode {
    private String nodeId;   // 예: F0
    private int port;        // 예: 9000

    // 블록체인 저장소 (Ledger)
    private List<Block> blockchain = new ArrayList<>();

    // 채굴 대기 중인 트랜잭션 풀 (Mempool)
    private List<Transaction> txPool = new ArrayList<>();

    public FullNode(String nodeId) {
        this.nodeId = nodeId;
        this.port = NetworkConfig.getPort(nodeId);

        // 제네시스 블록 초기화 (Block 0)
        // 모든 노드는 시작 시 동일한 제네시스 블록을 가짐
        loadGenesisBlock();
    }

    private void loadGenesisBlock() {
        // 이전 해시가 "0"인 0번 블록 생성
        Block genesis = new Block(0, "0");
        genesis.setNonce(0);
        genesis.calculateMerkleRoot(); // 트랜잭션 없어도 루트 계산
        genesis.setBlockHash(genesis.calculateBlockHash());

        blockchain.add(genesis);
        System.out.println("[" + nodeId + "] Genesis Block initialized: " + genesis.getBlockHash());
    }

    public void start() {
        // 서버 시작 시 "handleMessage" 메서드를 콜백으로 전달
        NodeServer server = new NodeServer(port, this::handleMessage);
        server.start();

        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        System.out.println("[" + nodeId + "] Neighbors: " + neighbors);
    }

    // 메시지 수신 시 호출되는 메서드
    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case TRANSACTION:
                handleTransaction(msg);
                break;
            // 추후 BLOCK, REQ_CHAIN 등 추가
            default:
                System.out.println("Unknown message type: " + msg.getType());
        }
    }

    private void handleTransaction(Message msg) {
        // Gson으로 인해 Object가 LinkedTreeMap으로 변환되었을 수 있으므로 다시 변환
        Gson gson = new Gson();
        String json = gson.toJson(msg.getData());
        Transaction tx = gson.fromJson(json, Transaction.class);

        // 1. 검증 (Valid Check) [cite: 20]
        if (isValid(tx)) {
            txPool.add(tx);
            System.out.println("[" + nodeId + "] Tx Verified & Added to Pool. Pool Size: " + txPool.size());
            // TODO: 이웃 노드에게 전파(Flooding) 로직 추
        } else {
            System.out.println("[" + nodeId + "] Tx Invalid! Discarded.");
        }
    }

    private boolean isValid(Transaction tx) {
        // TODO: 실제 서명 검증 로직 구현 필요 (현재는 무조건 true)
        // CryptoUtil.verifyECDSASig(...)
        return true;
    }

    public static void main(String[] args) {
        String id = (args.length > 0) ? args[0] : "F0";
        new FullNode(id).start();
    }
}