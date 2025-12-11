package assettrace.network;

import java.io.Serializable;

// 노드 간 통신에 사용되는 객체
public class Message implements Serializable { // 직렬화 가능해야 전송할 수 있음
    // 메시지 타입
    public enum Type {
        TRANSACTION,    // 트랜잭션 전파
        BLOCK,          // 채굴된 블록 전파
        REQ_CHAIN,      // 블록체인 데이터 요청 (동기화용)
        RES_CHAIN       // 블록체인 데이터 응답
    }

    private Type type;
    private String sender; // 보낸 노드 이름 (예: "U0", "F1")
    private Object data;   // 실제 데이터 (Block, Transaction, List<Block> 등)

    public Message(Type type, String sender, Object data) {
        this.type = type;
        this.sender = sender;
        this.data = data;
    }

    public Type getType() { return type; }
    public String getSender() { return sender; }
    public Object getData() { return data; }
}