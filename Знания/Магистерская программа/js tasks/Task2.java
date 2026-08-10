/**
 * Найти среднее арифметическое всех элементов, находящихся одновременно
 * слева от побочной и главной диагонали целочисленной квадратной матрицы.
 */
public class Task2 { // сложность алгоритма: время O(n²), память O(1) — один проход по матрице

    public static void main(String[] args) {
        int[][] matrix = {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 },
                { 9, 10, 11, 12 },
                { 13, 14, 15, 16 }
        };

        double result = function(matrix);
        System.out.println(result);
    }

    public static double function(int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix.length != matrix[0].length) {
            return 0;
        }

        int n = matrix.length;
        double sum = 0;
        int count = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j < i && j < n - 1 - i) {
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