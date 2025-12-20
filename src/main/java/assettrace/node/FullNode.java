package assettrace.node;

import assettrace.core.Block;
import assettrace.core.Transaction;
import assettrace.network.Message;
import assettrace.network.NetworkConfig;
import assettrace.network.NodeServer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class FullNode {
    private String nodeId;
    private int port;

    // 블록체인 저장소 (Ledger)
    private List<Block> blockchain = new ArrayList<>();

    // 채굴 대기 중인 트랜잭션 풀 (Mempool)
    private List<Transaction> txPool = new ArrayList<>();

    // 채굴 난이도
    private static final int DIFFICULTY = 5;

    public FullNode(String nodeId) {
        this.nodeId = nodeId;
        this.port = NetworkConfig.getPort(nodeId);
        loadGenesisBlock();
    }

    private void loadGenesisBlock() {
        Block genesis = new Block(0, "0");
        genesis.setNonce(0);
        genesis.calculateMerkleRoot();
        genesis.setBlockHash(genesis.calculateBlockHash());

        blockchain.add(genesis);
        System.out.println("[" + nodeId + "] Genesis Block initialized: " + genesis.getBlockHash());
    }

    public void start() {
        // 1. 서버 시작 (수신 대기 - 별도 스레드)
        NodeServer server = new NodeServer(port, this::handleMessage);
        server.start();

        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        System.out.println("[" + nodeId + "] Neighbors: " + neighbors);

        // 메인 스레드는 채굴 루프 실행
        miningLoop();
    }

    // 끊임없이 트랜잭션을 감시하고 채굴하는 루프
    private void miningLoop() {
        while (true) {
            try {
                Thread.sleep(2000); // 2초 대기 (CPU 과부하 방지)

                // 트랜잭션이 있으면 채굴 시작
                if (!txPool.isEmpty()) {
                    mineBlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void mineBlock() {
        // 1. 이전 블록 정보 가져오기
        Block prevBlock = blockchain.get(blockchain.size() - 1);
        int newBlockNo = blockchain.size();

        // 2. 새 블록 객체 생성
        Block newBlock = new Block(newBlockNo, prevBlock.getBlockHash());

        // 3. 풀에 있는 트랜잭션을 블록에 담기 (동시성 문제를 피하기 위해 복사본 사용)
        List<Transaction> txsToMine = new ArrayList<>(txPool);
        for (Transaction tx : txsToMine) {
            newBlock.addTransaction(tx);
        }

        // 4. 채굴 시작 (PoW)
        System.out.println("[" + nodeId + "] Start Mining Block #" + newBlockNo + " with " + txsToMine.size() + " txs...");
        newBlock.mine(DIFFICULTY);

        // 5. 체인에 추가 및 풀 비우기
        blockchain.add(newBlock);
        txPool.clear(); // 채굴된 트랜잭션 삭제

        System.out.println("[" + nodeId + "] Block #" + newBlockNo + " Added to Chain! Total Height: " + blockchain.size());

        // TODO: 채굴된 블록 전파(Broadcasting) 로직 추가
    }

    // 메시지 처리 핸들러
    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case TRANSACTION:
                handleTransaction(msg);
                break;
            default:
                System.out.println("Unknown message type: " + msg.getType());
        }
    }

    private void handleTransaction(Message msg) {
        Gson gson = new Gson();
        // Object -> Json -> Transaction 변환
        String json = gson.toJson(msg.getData());
        Transaction tx = gson.fromJson(json, Transaction.class);

        // 유효성 검증 후 풀에 추가
        if (isValid(tx)) {
            txPool.add(tx);
            System.out.println("[" + nodeId + "] Tx Verified & Added to Pool. Pool Size: " + txPool.size());
        } else {
            System.out.println("[" + nodeId + "] Tx Invalid! Discarded.");
        }
    }

    private boolean isValid(Transaction tx) {
        // 서명 검증 로직 (구현되었다고 가정)
        return true;
    }

    public static void main(String[] args) {
        String id = (args.length > 0) ? args[0] : "F0";
        new FullNode(id).start();
    }
}