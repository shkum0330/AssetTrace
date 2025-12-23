# 📦 AssetTrace: P2P Blockchain Asset Tracking System

## 🚀 Overview
* Java로 구현한 **P2P 기반의 자산 추적 블록체인 시스템**
* 외부 블록체인 프레임워크 없이, **P2P 네트워크 통신, 합의 알고리즘(PoW), 암호화 서명(ECDSA), 블록 및 트랜잭션 구조**를 설계하고 구현하여 블록체인의 핵심 원리를 시뮬레이션

<br/>

## 🚀 Key Features
* **Core Blockchain Structure**: 블록 헤더/바디, 머클 루트, 체인 연결 구조 구현
* **Proof of Work (PoW)**: 난이도(Difficulty) 기반의 작업 증명 마이닝 알고리즘 구현
* **P2P Network**: Java Socket을 이용한 노드 간 양방향 통신 및 메시지 브로드캐스팅
* **Security & Crypto**:
* **SHA-256**: 블록 해시 및 머클 트리 계산
* **ECDSA (secp256k1)**: 트랜잭션 전자 서명 및 검증 (BouncyCastle 라이브러리 활용)

<br/>

* **Node Architecture**:
* **Full Node**: 트랜잭션 수집, 검증, 블록 채굴, 원장 관리
* **User Node**: 지갑 생성, 트랜잭션 생성 및 서명, 전파

<br/>

## 🛠 Tech Stack
* **Language**: Java
* **Build Tool**: Gradle
* **Library**:
* `Gson`: JSON 직렬화/역직렬화 (네트워크 메시지 통신용)
* `BouncyCastle`: 암호화 알고리즘 (ECDSA) provider

<br/>

## 📂 Project Structure
```bash
src/main/java/assettrace
├── core          # 핵심 데이터 구조 (Block, Transaction)
├── network       # P2P 통신 계층 (Server, Client, Message, Config)
├── node          # 노드 구현체 (FullNode, UserNode)
└── util          # 암호화 유틸리티 (CryptoUtil: SHA-256, ECDSA)
```

<br/>

## ⚙️ Configuration & Topology
네트워크 연결 정보는 프로젝트 루트의 `topology.dat` 파일에 정의되어 있음
* **Full Node (Fx)**: `9000`번대 포트 사용 (예: F0 -> 9000, F1 -> 9001)
* **User Node (Ux)**: `8000`번대 포트 사용 (예: U0 -> 8000)

<br/>

## 🏃 How to Run
### 1. Build
```bash
./gradlew build

```


### 2. Run Nodes
터미널 탭을 여러 개 열어 각각의 노드를 실행하여 네트워크를 시뮬레이션

**Step 1: Full Node 실행 (채굴자)**
F1 노드를 실행하여 9001번 포트에서 수신 대기 및 채굴 준비
```bash
./gradlew run --args="F1"

```


**Step 2: User Node 실행 (사용자)**
U0 노드를 실행하면 자동으로 F1에 접속하여 트랜잭션을 생성하고 전송
```bash
./gradlew run --args="U0"

```

### 3. Execution Flow (Logs)
정상적으로 실행되면 아래와 같은 로그 흐름을 볼 수 있음
1. **[U0]** 트랜잭션 생성(서명 포함) -> **[F1]**으로 전송
2. **[F1]** 트랜잭션 수신 -> 서명 검증 -> Mempool에 추가
3. **[F1]** 채굴 시작 (Mining...) -> Nonce 탐색
4. **[F1]** 블록 생성 성공 (**Block Mined!**) -> 체인 연결
5. **[F1]** 이웃 노드들에게 블록 전파 (Broadcasting)

<br/>

## 📝 Implementation Steps
* **Step 1**: 기본 데이터 구조 설계 (Block, Transaction, SHA-256)
* **Step 2**: JSON 기반 P2P 네트워크 통신 프로토콜 구현
* **Step 3**: 노드(Full/User) 서버 구동 및 `topology.dat` 파싱
* **Step 4**: 전자 서명(ECDSA) 적용 및 트랜잭션 전파
* **Step 5**: 작업 증명(PoW) 마이닝 로직 및 블록 생성 구현
* **Step 6**: 채굴된 블록의 네트워크 전파 및 노드 간 동기화
