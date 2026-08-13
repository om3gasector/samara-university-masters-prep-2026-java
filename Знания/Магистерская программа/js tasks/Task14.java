/**
 * Найти самую длинную неубывающую последовательность в массиве целых чисел
 * (последовательность не обязательно должна быть непрерывной).
 */
public class Task14 { // сложность алгоритма: время O(n²), память O(n) — динамическое программирование

    public static void main(String[] args) {
        int[] arr = { 3, 1, 4, 2, 5 };
        int[] result = longest(arr);
        System.out.println("Длина: " + result.length);
        for (int num : result)
            System.out.print(num + " ");
    }

    public static int[] longest(int[] arr) {
        if (arr == null || arr.length == 0)
            return new int[0];

        int n = arr.length;
        int[] dp = new int[n];
        int[] prev = new int[n];
        int maxIdx = 0;

        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            prev[i] = -1;
            for (int j = 0; j < i; j++) {
                if (arr[j] <= arr[i] && dp[j] + 1 > dp[i]) {
                    dp[i] = dp[j] + 1;
                    prev[i] = j;
                }
            }
            if (dp[i] > dp[maxIdx])
                maxIdx = i;
        }

        int length = dp[maxIdx];
        int[] result = new int[length];
        int cur = maxIdx;

        for (int i = length - 1; i >= 0; i--) {
            result[i] = arr[cur]; // записываем текущий элемент цепочки
            cur = prev[cur]; //

        }
        return result;
    }
}