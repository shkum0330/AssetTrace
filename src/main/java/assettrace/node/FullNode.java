package assettrace.node;

import assettrace.core.Block;
import assettrace.core.Transaction;
import assettrace.network.Message;
import assettrace.network.NetworkConfig;
import assettrace.network.NodeClient;
import assettrace.network.NodeServer;

import java.util.ArrayList;
import java.util.List;

public class FullNode {
    private String nodeId;   // 예: F0
    private int port;        // 예: 5000

    // 블록체인 저장소 (Ledger)
    private List<Block> blockchain = new ArrayList<>();

    // 채굴 대기 중인 트랜잭션 풀 (Mempool)
    private List<Transaction> txPool = new ArrayList<>();

    public FullNode(String nodeId) {
        this.nodeId = nodeId;
        this.port = NetworkConfig.getPort(nodeId);

        // 1. 제네시스 블록 초기화 (Block 0)
        // 명세서에 따라 모든 노드는 시작 시 동일한 제네시스 블록을 가짐
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
        // 1. 서버 시작 (수신 대기)
        NodeServer server = new NodeServer(port);
        server.start();

        // 2. 이웃 노드 확인 (topology.dat)
        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        System.out.println("[" + nodeId + "] Neighbors: " + neighbors);

        // TODO: 이웃들에게 연결 시도 및 블록 동기화 로직 추가
    }

    // 이 노드를 단독 실행할 때 사용하는 진입점
    public static void main(String[] args) {
        // 인자가 없으면 기본값 F0으로 테스트
        String id = (args.length > 0) ? args[0] : "F0";
        new FullNode(id).start();
    }
}