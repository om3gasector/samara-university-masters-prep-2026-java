/**
 * Разбить массив целых положительных чисел на два массива так,
 * чтобы разность сумм элементов двух получившихся массивов была минимальна.
 */
public class Task15 { // сложность алгоритма: время O(n × sum), память O(sum) — задача о рюкзаке
    public static void main(String[] args) {
        int[] arr = { 1, 6, 11, 5 };
        System.out.println(function(arr));
    }

    public static int function(int[] arr) {

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

        int sum = 0;
        for (int s = target; s >= 0; s--) {
            if (dp[s]) {
                sum = s;
                break;
            }
        }

        return total - 2 * sum;
    }
}