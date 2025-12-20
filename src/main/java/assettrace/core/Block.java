package assettrace.core;

import assettrace.util.CryptoUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable {
    private int blockNo;
    private String prevHash;
    private String merkleRoot;
    private int nonce = 0;
    private String blockHash;
    private List<Transaction> transactions = new ArrayList<>();

    public Block(int blockNo, String prevHash) {
        this.blockNo = blockNo;
        this.prevHash = prevHash;
    }

    public void addTransaction(Transaction tx) {
        this.transactions.add(tx);
    }

    // 채굴 로직
    public void mine(int difficulty) {
        // 목표: 해시값 앞에 difficulty 개수만큼의 '0'이 와야 함 (예: "00000...")
        String target = new String(new char[difficulty]).replace('\0', '0');

        System.out.println(">> Mining Block " + blockNo + "... (Target: " + target + ")");

        // 1. 머클 루트 계산
        this.merkleRoot = calculateMerkleRoot();

        // 2. PoW 수행 (Nonce 증가시키며 Target보다 작은 해시 찾기)
        while (true) {
            this.blockHash = calculateBlockHash();
            if (this.blockHash.substring(0, difficulty).equals(target)) {
                System.out.println(">> Block Mined! : " + this.blockHash);
                break;
            }
            nonce++;
        }
    }

    // 머클 루트 계산 (반환 타입을 void -> String으로 수정함)
    public String calculateMerkleRoot() {
        if (transactions.isEmpty()) return "0";

        StringBuilder sb = new StringBuilder();
        for(Transaction tx : transactions) {
            sb.append(tx.getTrID());
        }
        return CryptoUtil.applySha256(sb.toString());
    }

    // 블록 해시 계산
    public String calculateBlockHash() {
        // 헤더 구성 요소: 번호 + 이전해시 + 머클루트 + nonce
        String headerData = blockNo + prevHash + merkleRoot + nonce;
        return CryptoUtil.applySha256(headerData);
    }


    public int getBlockNo() { return blockNo; }
    public String getPrevHash() { return prevHash; }
    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setNonce(int nonce) { this.nonce = nonce; }
}