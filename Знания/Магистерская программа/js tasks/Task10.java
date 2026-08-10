/**
 * Найти среднее арифметическое всех элементов, находящихся одновременно
 * над побочной и главной диагональю целочисленной квадратной матрицы
 * (включая прилегающие элементы диагоналей).
 */
public class Task10 { // сложность алгоритма: время O(n²), память O(1) — один проход по матрице

    public static void main(String[] args) {
        int[][] matrix = {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 },
                { 9, 10, 11, 12 },
                { 13, 14, 15, 16 }
        };

        double result = onBothDiagonalsUp(matrix);
        System.out.println(result);
    }

    public static double onBothDiagonalsUp(int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix.length != matrix[0].length) {
            return 0;
        }

        int n = matrix.length;
        double sum = 0;
        int count = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i <= j && i <= n - 1 - j) {
                    sum += matrix[i][j];
                    count++;
                }
            }
        }

        if (count == 0)
            return 0;
        return sum / count;
    }
}