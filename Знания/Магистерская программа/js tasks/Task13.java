/**
 * Развернуть квадратную матрицу в одномерный массив по «улитке»
 * против часовой стрелки, начиная с левого верхнего угла матрицы.
 */
public class Task13 { // сложность алгоритма: время O(n²), память O(n²) — каждый элемент ровно один
                      // раз

    public static void main(String[] args) {
        int[][] matrix = {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 },
                { 9, 10, 11, 12 },
                { 13, 14, 15, 16 }
        };

        int[] result = spiral(matrix);
        for (int num : result) {
            System.out.print(num + " ");
        }
    }

    public static int[] spiral(int[][] matrix) {
        int n = matrix.length;
        int[] result = new int[n * n];
        int index = 0;

        // обходим слой за слоем, всего n / 2 (для чётной матрицы), для нечётной
        // останется центр
        for (int layer = 0; layer < n / 2; layer++) {

            // 1. ВНИЗ по левой стороне слоя
            for (int i = layer; i < n - 1 - layer; i++)
                result[index++] = matrix[i][layer];

            // 2. ВПРАВО по нижней стороне слоя
            for (int j = layer; j < n - 1 - layer; j++)
                result[index++] = matrix[n - 1 - layer][j];

            // 3. ВВЕРХ по правой стороне слоя
            for (int i = n - 1 - layer; i > layer; i--)
                result[index++] = matrix[i][n - 1 - layer];

            // 4. ВЛЕВО по верхней стороне слоя
            for (int j = n - 1 - layer; j > layer; j--)
                result[index++] = matrix[layer][j];
        }

        // если матрица нечётного размера — добавляем центральный элемент
        if (n % 2 == 1)
            result[index] = matrix[n / 2][n / 2];

        return result;
    }
}