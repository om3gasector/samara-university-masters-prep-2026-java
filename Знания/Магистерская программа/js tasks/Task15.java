/**
 * Разбить массив целых положительных чисел на два массива так,
 * чтобы разность сумм элементов двух получившихся массивов была минимальна.
 */
public class Task15 {

    public static void main(String[] args) {
        int[] arr = { 1, 6, 11, 5 };
        System.out.println(minDiff(arr));
    }

    public static int minDiff(int[] arr) {
        if (arr == null || arr.length == 0)
            return 0;

        int total = 0;
        for (int num : arr)
            total += num;

        int target = total / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;

        for (int num : arr) {
            for (int s = target; s >= num; s--) {
                if (dp[s - num])
                    dp[s] = true;
            }
        }

        int sum1 = 0;
        for (int s = target; s >= 0; s--) {
            if (dp[s]) {
                sum1 = s;
                break;
            }
        }

        return Math.abs(total - 2 * sum1);
    }
}