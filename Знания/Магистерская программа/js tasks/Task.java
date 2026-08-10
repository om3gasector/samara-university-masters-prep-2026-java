public class Task {
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
            for (int i = layer; i < n - 1 - layer; i++)
                result[index++] = matrix[i][layer];

            for (int j = layer; j < n - 1 - layer; j++)
                result[index++] = matrix[n - 1 - layer][j];

            for (int i = n - 1 - layer; i > layer; i--)
                result[index++] = matrix[i][n - 1 - layer];

            for (int j = n - 1 - layer; j > layer; j--)
                result[index++] = matrix[layer][j];

        }

        if (n % 2 == 1)
            result[index] = matrix[n / 2][n / 2];

        return result;
    }
}