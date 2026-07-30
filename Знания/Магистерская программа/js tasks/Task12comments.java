
// Импортируем HashSet — коллекцию для хранения уникальных элементов
import java.util.HashSet;

public class Task12comments {

    // Класс узла бинарного дерева
    static class Node {
        int value; // числовое значение, которое хранит узел
        Node left, right; // ссылки на левый и правый дочерние узлы
        Node parent; // ссылка на родительский узел (у корня — null)

        // Конструктор узла — создаёт узел с заданным значением
        Node(int v) {
            value = v;
        }
    }

    public static void main(String[] args) {
        // Создаём корень дерева со значением 1
        Node n1 = new Node(1);

        // Создаём узел 2, говорим что его родитель — n1,
        // и что n1.left теперь указывает на n2
        Node n2 = new Node(2);
        n2.parent = n1;
        n1.left = n2;

        // Создаём узел 3, родитель — n1, это правый ребёнок n1
        Node n3 = new Node(3);
        n3.parent = n1;
        n1.right = n3;

        // Узел 4, родитель — n2, левый ребёнок n2
        Node n4 = new Node(4);
        n4.parent = n2;
        n2.left = n4;

        // Узел 5, родитель — n2, правый ребёнок n2
        Node n5 = new Node(5);
        n5.parent = n2;
        n2.right = n5;

        // Узел 6, родитель — n3, правый ребёнок n3
        Node n6 = new Node(6);
        n6.parent = n3;
        n3.right = n6;

        // Ищем ближайшего общего родителя для n4 и n5 и выводим значение
        System.out.println(lca(n4, n5).value); // 2

        // Ищем для n4 и n6 и выводим
        System.out.println(lca(n4, n6).value); // 1
    }

    // Метод поиска ближайшего общего родителя (LCA)
    static Node lca(Node a, Node b) {
        // Создаём пустое множество для хранения предков узла a
        HashSet<Node> set = new HashSet<>();

        // Цикл: идём от узла a вверх до корня
        while (a != null) {
            set.add(a); // добавляем текущий узел в множество
            a = a.parent; // переходим к родителю
        }

        // Цикл: идём от узла b вверх до корня
        while (b != null) {
            if (set.contains(b)) { // если текущий узел уже был у предков a —
                return b; // это общий родитель, возвращаем его
            }
            b = b.parent; // иначе поднимаемся выше
        }

        // Если не нашли (оба null или разорванные ссылки) — возвращаем null
        return null;
    }
}