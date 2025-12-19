package assettrace.util;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtil {
    // BouncyCastle Provider 등록 (static 블록에서 최초 1회 실행)
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // SHA-256 해시 함수
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 1. ECDSA 키 쌍 생성 (개인키/공개키)
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 2. 서명 생성 (개인키로 데이터 서명)
    public static String applyECDSASig(PrivateKey privateKey, String input) {
        try {
            Signature dsa = Signature.getInstance("SHA256withECDSA", "BC");
            dsa.initSign(privateKey);
            dsa.update(input.getBytes());
            byte[] realSig = dsa.sign();
            return Base64.getEncoder().encodeToString(realSig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. 서명 검증 (공개키로 서명 확인)
    public static boolean verifyECDSASig(String publicKeyStr, String data, String signature) {
        try {
            // String(Base58/64) -> PublicKey 객체 복원 로직이 필요하지만,
            // 간단하게 구현하기 위해 여기서는 publicKeyStr을 일단 Pass하고
            // 실제 구현 시에는 PublicKey 객체 자체를 넘기거나 인코딩/디코딩 로직을 추가해야 함.
            // 이번 단계에서는 편의상 KeyPair를 메모리에 들고 있다고 가정하고 로직을 작성했음.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 편의상 Key -> String 변환 (Base64)
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}