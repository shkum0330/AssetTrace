package assettrace.core;

import assettrace.util.CryptoUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable {
    // Header
    private int blockNo;
    private String prevHash;
    private long nonce;
    private String merkleRoot;

    // Body
    private List<Transaction> transactions;

    private String blockHash; // 블록 자체의 해시

    public Block(int blockNo, String prevHash) {
        this.blockNo = blockNo;
        this.prevHash = prevHash;
        this.transactions = new ArrayList<>();
        this.nonce = 0;
    }

    public void addTransaction(Transaction tx) {
        this.transactions.add(tx);
    }

    // 머클 루트 계산
    public void calculateMerkleRoot() {
        StringBuilder sb = new StringBuilder();
        for(Transaction tx : transactions) {
            sb.append(tx.getTrID());
        }
        this.merkleRoot = CryptoUtil.applySha256(sb.toString());
    }

    // 블록 해시 계산 (PoW용)
    public String calculateBlockHash() {
        String headerData = blockNo + prevHash + nonce + merkleRoot;
        return CryptoUtil.applySha256(headerData);
    }

    // Getters & Setters
    public List<Transaction> getTransactions() { return transactions; }
    public void setNonce(long nonce) { this.nonce = nonce; }
    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }
    public int getBlockNo() { return blockNo; }
    public String getPrevHash() { return prevHash; }
}