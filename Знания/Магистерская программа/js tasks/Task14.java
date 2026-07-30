/**
 * Найти самую длинную неубывающую последовательность в массиве целых чисел
 * (последовательность не обязательно должна быть непрерывной).
 */
public class Task14 {

    public static void main(String[] args) {
        int[] arr = { 3, 1, 4, 2, 5, 3, 7 };
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

        int[] result = new int[dp[maxIdx]];
        for (int i = result.length - 1, cur = maxIdx; i >= 0; i--, cur = prev[cur])
            result[i] = arr[cur];

        return result;
    }
}