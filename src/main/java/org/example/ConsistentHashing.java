package org.example;

import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Collection;

public class ConsistentHashing {

        private final TreeMap<Long, String> hashRing = new TreeMap<>();
        private final int numberOfReplicas;  // Number of virtual nodes (replicas)
        private Hashing hashing;

        public ConsistentHashing(Hashing hashing, int numberOfReplicas, Collection<String> nodes) {
            this.hashing = hashing;
            this.numberOfReplicas=numberOfReplicas;

            // Add initial nodes to the hash ring
            for (String node : nodes) {
                addNode(node);
            }
        }

        // Adds a node with its virtual replicas to the hash ring
        public void addNode(String node) {
            for (int i = 0; i < numberOfReplicas; i++) {
                byte[] nodeHash = hashing.compute((node + i).getBytes());
                long hash = toLong(nodeHash);
                hashRing.put(hash, node);
            }
        }

        // Removes a node and its replicas from the hash ring
        public void removeNode(String node) {
            for (int i = 0; i < numberOfReplicas; i++) {
                byte[] nodeHash = hashing.compute((node + i).getBytes());
                long hash = toLong(nodeHash);
                hashRing.remove(hash);
            }
        }

    // Gets the node responsible for the given key
    public String getNodeForKey(String key) {
        if (hashRing.isEmpty()) {
            return null;
        }
        byte[] keyHash = hashing.compute(key.getBytes());
        long hash = toLong(keyHash);

        // Find the first node that comes after the key's hash (or loop around)
        Entry<Long, String> entry = hashRing.ceilingEntry(hash);
        if (entry == null) {
            entry = hashRing.firstEntry();  // Wrap around the ring
        }
        return entry.getValue();
    }

    // Converts the first 8 bytes of the MD5 hash to a long value
    private long toLong(byte[] digest) {
        return ((long) (digest[7] & 0xFF) << 56)
                | ((long) (digest[6] & 0xFF) << 48)
                | ((long) (digest[5] & 0xFF) << 40)
                | ((long) (digest[4] & 0xFF) << 32)
                | ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | ((long) (digest[0] & 0xFF));
    }

    public static void main(String[] args) {
        // Example usage
        Collection<String> nodes = List.of("NodeA", "NodeB", "NodeC");
        ConsistentHashing consistentHashing = new ConsistentHashing(new MD5Implementation(),3, nodes);

        // Add a node
        consistentHashing.addNode("NodeD");

        // Find which node a key maps to
        String key = "XYZ";
        System.out.println("Key " + key + " is mapped to node " + consistentHashing.getNodeForKey(key));

        // Remove a node
        consistentHashing.removeNode("NodeB");
        System.out.println("After removing NodeB, key " + key + " is mapped to node " + consistentHashing.getNodeForKey(key));
    }

}
