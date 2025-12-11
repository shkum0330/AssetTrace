package assettrace.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NetworkConfig {

    // 노드 이름을 포트 번호로 변환
    public static int getPort(String nodeName) {
        if (nodeName.startsWith("F")) {
            int id = Integer.parseInt(nodeName.substring(1));
            return 9000 + id;
        } else if (nodeName.startsWith("U")) {
            int id = Integer.parseInt(nodeName.substring(1));
            return 8000 + id;
        }
        throw new IllegalArgumentException("알 수 없는 노드 타입: " + nodeName);
    }

    // topology.dat 파일을 읽어서 나의 이웃 노드 리스트를 반환
    public static List<String> getNeighbors(String myName) {
        List<String> neighbors = new ArrayList<>();
        String filename = "topology.dat";

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("link")) {
                    // "link U0-F1, U1-F2, ..." 형태 파싱
                    String content = line.substring(4).trim(); // "link" 제거
                    String[] links = content.split(",");

                    for (String link : links) {
                        String[] pair = link.trim().split("-");
                        if (pair.length == 2) {
                            String nodeA = pair[0].trim();
                            String nodeB = pair[1].trim();

                            // 내가 링크에 포함되어 있으면 상대방을 이웃으로 추가
                            if (nodeA.equals(myName)) {
                                neighbors.add(nodeB);
                            } else if (nodeB.equals(myName)) {
                                neighbors.add(nodeA);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("topology.dat을 읽을 수 없습니다.");
        }
        return neighbors;
    }
}