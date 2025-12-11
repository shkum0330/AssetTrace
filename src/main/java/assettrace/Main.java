package assettrace;

import assettrace.node.FullNode;
import assettrace.node.UserNode;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <NodeID> (e.g., F0, U1)");
            return;
        }

        String nodeId = args[0];

        if (nodeId.startsWith("F")) {
            System.out.println(">>> Starting Full Node: " + nodeId);
            new FullNode(nodeId).start();
        } else if (nodeId.startsWith("U")) {
            System.out.println(">>> Starting User Node: " + nodeId);
            new UserNode(nodeId).start();
        } else {
            System.err.println("Invalid Node ID. Must start with 'F' or 'U'.");
        }
    }
}