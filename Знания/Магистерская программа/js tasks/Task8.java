/**
 * Найти медиану в массиве вещественных чисел.
 * Медиана: массив сортируется по возрастанию.
 * Нечётное количество — центральный элемент.
 * Чётное количество — среднее арифметическое двух центральных.
 */
public class Task8 {

    public static void main(String[] args) {
        double[] arr = { 5.2, 1.8, 9.4, 3.6, 7.1 };
        double result = median(arr);
        System.out.println(result);
    }

    public static double median(double[] arr) {
        if (arr == null || arr.length == 0) {
            return 0;
        }

        double[] sorted = arr.clone();

        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = 0; j < sorted.length - 1 - i; j++) {
                if (sorted[j] > sorted[j + 1]) {
                    double temp = sorted[j];
                    sorted[j] = sorted[j + 1];
                    sorted[j + 1] = temp;
                }
            }
        }

        int n = sorted.length;
        int mid = n / 2;

        if (n % 2 != 0) {
            return sorted[mid];
        } else {
            return (sorted[mid - 1] + sorted[mid]) / 2.0;
        }
    }
}