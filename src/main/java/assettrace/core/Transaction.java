package assettrace.core;

import assettrace.util.CryptoUtil;
import java.io.Serializable;

public class Transaction implements Serializable {
    // 1. Header
    private String trID; // 트랜잭션 해시 ID

    // 2. Participants
    private String input;  // 판매자 (Seller PK)
    private String output; // 구매자 (Buyer PK)

    // 3. Immutable Items
    private String identifier;       // 물품 ID
    private String modelNo;          // 모델명
    private String manufacturedDate; // 제조일

    // 4. Mutable Items
    private double price;            // 가격
    private String tradingDate;      // 거래일
    private String others;           // 기타

    // 5. Security
    private String signature; // 서명

    public Transaction() {} // JSON용 기본 생성자

    public Transaction(String input, String output, String identifier,
                       String modelNo, String manufacturedDate,
                       double price, String tradingDate, String others) {
        this.input = input;
        this.output = output;
        this.identifier = identifier;
        this.modelNo = modelNo;
        this.manufacturedDate = manufacturedDate;
        this.price = price;
        this.tradingDate = tradingDate;
        this.others = others;

        // 생성 시 ID 자동 계산
        this.trID = calculateHash();
    }

    public String calculateHash() {
        String dataToHash = input + output + identifier + modelNo +
                manufacturedDate + price + tradingDate + others;
        return CryptoUtil.applySha256(dataToHash);
    }

    // Getter
    public String getTrID() { return trID; }
    public String getInput() { return input; }
    public String getOutput() { return output; }
    public String getIdentifier() { return identifier; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    // 서명 대상 데이터 추출
    public String getDataForSigning() {
        return trID;
    }
}