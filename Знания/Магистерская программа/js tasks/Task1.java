/**
 * Найти индекс начала наиболее короткой (не менее двух) и непрерывной
 * последовательности одинаковых чисел в целочисленном массиве.
 */
public class Task1 { // сложность алгоритма: время О(n); память О(1) - один проход по массиву

    public static void main(String[] args) {
        int[] arr = { 3, 3, 3, 1, 1, 1, 2, 2, 5, 5, 5, 5 };
        int result = function(arr);
        System.out.println(result);
    }

    public static int function(int[] arr) {
        if (arr == null || arr.length < 2) {
            return -1;
        }

        int minLength = Integer.MAX_VALUE;
        int startIndex = -1;
        int curStart = 0;

        for (int i = 1; i <= arr.length; i++) {
            if (i == arr.length || arr[i] != arr[i - 1]) {
                int len = i - curStart;
                if (len >= 2 && len < minLength) {
                    minLength = len;
                    startIndex = curStart;
                }
                curStart = i;
            }
        }

        return startIndex;
    }
}