import java.util.HashSet;

public class Task12 {

    static class Node {
        int value;
        Node left, right;
        Node parent;

        Node(int v) {
            value = v;
        }
    }

    public static void main(String[] args) {
        // 1 — корень
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        n2.parent = n1;
        n1.left = n2;
        Node n3 = new Node(3);
        n3.parent = n1;
        n1.right = n3;
        Node n4 = new Node(4);
        n4.parent = n2;
        n2.left = n4;
        Node n5 = new Node(5);
        n5.parent = n2;
        n2.right = n5;
        Node n6 = new Node(6);
        n6.parent = n3;
        n3.right = n6;

        System.out.println(lca(n4, n5).value); // 2
        System.out.println(lca(n4, n6).value); // 1
    }

    static Node lca(Node a, Node b) {
        HashSet<Node> set = new HashSet<>();
        while (a != null) {
            set.add(a);
            a = a.parent;
        }
        while (b != null) {
            if (set.contains(b))
                return b;
            b = b.parent;
        }
        return null;
    }
}