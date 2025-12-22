package assettrace.node;

import assettrace.core.Block;
import assettrace.core.Transaction;
import assettrace.network.Message;
import assettrace.network.NetworkConfig;
import assettrace.network.NodeClient; // 전송용 클라이언트 추가
import assettrace.network.NodeServer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class FullNode {
    private String nodeId;
    private int port;
    private List<Block> blockchain = new ArrayList<>();
    private List<Transaction> txPool = new ArrayList<>();
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
        System.out.println("[" + nodeId + "] Genesis Block initialized.");
    }

    public void start() {
        NodeServer server = new NodeServer(port, this::handleMessage);
        server.start();

        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        System.out.println("[" + nodeId + "] Neighbors: " + neighbors);

        miningLoop();
    }

    private void miningLoop() {
        while (true) {
            try {
                Thread.sleep(2000);
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
        Block prevBlock = blockchain.get(blockchain.size() - 1);
        int newBlockNo = blockchain.size();
        Block newBlock = new Block(newBlockNo, prevBlock.getBlockHash());

        List<Transaction> txsToMine = new ArrayList<>(txPool);
        for (Transaction tx : txsToMine) newBlock.addTransaction(tx);

        System.out.println("[" + nodeId + "] Start Mining Block #" + newBlockNo + "...");
        newBlock.mine(DIFFICULTY);

        blockchain.add(newBlock);
        txPool.clear();

        System.out.println("[" + nodeId + "] Block #" + newBlockNo + " Mined & Added! Broadcast to neighbors...");

        // 이웃 노드들에게 블록 전파
        broadcastBlock(newBlock);
    }

    // 블록 전파 메서드
    private void broadcastBlock(Block block) {
        List<String> neighbors = NetworkConfig.getNeighbors(nodeId);
        Message msg = new Message(Message.Type.BLOCK, nodeId, block);
        NodeClient client = new NodeClient();

        for (String neighbor : neighbors) {
            // Full Node에게만 블록을 전파
            if (neighbor.startsWith("F")) {
                int targetPort = NetworkConfig.getPort(neighbor);
                client.send(targetPort, msg);
            }
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case TRANSACTION:
                handleTransaction(msg);
                break;
            case BLOCK:
                handleBlock(msg);
                break;
            default:
                System.out.println("Unknown message type: " + msg.getType());
        }
    }

    // 수신한 블록 처리 및 검증 로직
    private void handleBlock(Message msg) {
        Gson gson = new Gson();
        String json = gson.toJson(msg.getData());
        Block receivedBlock = gson.fromJson(json, Block.class);

        System.out.println("[" + nodeId + "] Received Block #" + receivedBlock.getBlockNo() + " from " + msg.getSender());

        // 블록 번호가 내 다음 순서인지 검증
        Block lastBlock = blockchain.get(blockchain.size() - 1);
        if (receivedBlock.getBlockNo() != lastBlock.getBlockNo() + 1) {
            System.out.println("[" + nodeId + "] Block Rejected: Incorrect Height (My: " + lastBlock.getBlockNo() + ", Recv: " + receivedBlock.getBlockNo() + ")");
            return;
        }

        // 이전 해시가 일치하는지 검증
        if (!receivedBlock.getPrevHash().equals(lastBlock.getBlockHash())) {
            System.out.println("[" + nodeId + "] Block Rejected: PrevHash mismatch");
            return;
        }

        // 해시값이 유효한지 검증
        String target = new String(new char[DIFFICULTY]).replace('\0', '0');
        if (!receivedBlock.getBlockHash().startsWith(target)) {
            System.out.println("[" + nodeId + "] Block Rejected: Invalid PoW");
            return;
        }

        // 검증 통과 -> 내 체인에 추가
        blockchain.add(receivedBlock);
        System.out.println("[" + nodeId + "] Valid Block #" + receivedBlock.getBlockNo() + " Added to Chain! (Sync Complete)");

        // 이미 처리된 트랜잭션은 내 풀에서 제거 (Double Spending 방지)
        // 실제로는 트랜잭션 ID를 비교해서 지워야 함. 여기서는 싹 비우는 것으로 단순화.
        txPool.clear();
    }

    private void handleTransaction(Message msg) {
        Gson gson = new Gson();
        String json = gson.toJson(msg.getData());
        Transaction tx = gson.fromJson(json, Transaction.class);

        if (isValid(tx)) {
            txPool.add(tx);
            System.out.println("[" + nodeId + "] Tx Verified & Added. Pool: " + txPool.size());
        }
    }

    private boolean isValid(Transaction tx) { return true; }

    public static void main(String[] args) {
        String id = (args.length > 0) ? args[0] : "F0";
        new FullNode(id).start();
    }
}