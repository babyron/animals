import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.*;

class Kind implements Comparable<Kind> {
    float e;
    int l;
    float r;
    int seq;

    public Kind(float e, float r) {
        this.e = e;
        this.r = r;
    }

    public int compareTo(Kind o) {
        if (this.e < o.e) {
            return -1;
        }

        return 1;
    }

    public int hashCode() {
        return seq;
    }

    public boolean equals(Kind k) {
        return this.seq == k.seq;
    }

    @Override
    public String toString() {
        return "Kind{" +
                "e=" + e +
                ", l=" + l +
                ", r=" + r +
                '}';
    }
}

class Matrix {
    int[][] matrix;
    int s;

    Matrix(int s) {
        matrix = new int[s][s];
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                matrix[i][j] = 0;
            }
        }
        this.s = s;
    }

    void set(int i, int j) {
        matrix[i][j] = 1;
    }

    /**
     * @param i
     * @return
     */
    int statL(int i) {
        int sum = 0;
        for (int j = 0; j < this.s; j++) {
            sum += this.matrix[i][j];
        }

        return sum;
    }

    boolean isHunted(int i) {
        for (int j = 0; j < this.s; j++) {
            if (this.matrix[j][i] == 1) {
                return true;
            }
        }

        return false;
    }

    boolean isHuntedByJ(int i, int j) {
        return matrix[j][i] == 1;
    }

    Set<Integer> HunterFoodSet(int i) {
        Set<Integer> set = new HashSet<Integer>();

        for (int m = 0; m < this.s; m++) {
            if (this.matrix[m][i] == 0) {
                continue;
            }

            for (int n = 0; n < s; n++) {
                if (this.matrix[m][n] == 1 && n != i) {
                    set.add(n);
                }
            }
        }

        return set;
    }

    public void print() {
        int sum = 0;
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                sum += matrix[i][j];
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("Sum:" + sum);
    }


    @Override
    public String toString() {
        return "Matrix{" +
                "matrix=" + Arrays.toString(matrix) +
                ", s=" + s +
                '}';
    }
}

public class Main {
    float c = 0.17f;
    int s = 30;
    int L = 200;
    Matrix matrix = new Matrix(s);

    // beta分布
    private float beta() {
        BetaDistribution beta = new BetaDistribution(1.0, (double) (s - 1) / (2 * s * c) - 1);
        return (float) beta.sample();
    }

    // 产生S个物种信息
    private Kind[] generateKinds() {
        Kind[] ret = new Kind[s];
        Set<Float> set = new HashSet<Float>();
        Random random = new Random();
        float sum = 0;
        for (int i = 0; i < s; i++) {
            Float t = random.nextFloat();
            while (set.contains(t)) {
                t = random.nextFloat();
            }
            float r = beta();
            ret[i] = new Kind(t, r);
            sum += r;
        }

        for (Kind k : ret) {
            k.l = Math.round(k.r * L / sum);
        }

        ret[0].l = 0;
        return ret;
    }

    // 不是一个严格意义上的真随机
    public int randomIndex(int length) {
        Random r = new Random();
        if (length == 0) {
            return 0;
        }
        return Math.abs(r.nextInt() % length);
    }

    private boolean finished(int index, Kind[] kinds) {
        return matrix.statL(index) >= kinds[index].l;
    }

    private int removeSetEle(Set<Kind> set, int index, int selected) {
        Kind k = (Kind) set.toArray()[selected];
        set.remove(k);
        matrix.set(index, k.seq);
        return randomIndex(set.size());
    }

    // 处理一个物种
    public void handleKind(Kind[] kinds, int index) {
        if (finished(index, kinds)) {
            return;
        }

        // case 1
        Set<Kind> set1 = new HashSet<Kind>();
        for (int i = 0; i < index; i++) {
            set1.add(kinds[i]);
        }
        int selected = randomIndex(set1.size());
        // 如果选择到的食物没有捕食者
        while (!matrix.isHunted(selected) && !set1.isEmpty()) {
            selected = removeSetEle(set1, index, selected);
            // 检查终止条件
            if (finished(index, kinds)) {
                return;
            }
        }

        // case 1: 选择到的食物有捕食者
        Set<Kind> set2 = new HashSet<Kind>();
        Set<Integer> foods = matrix.HunterFoodSet(selected);
        for (Integer i : foods) {
            set2.add(kinds[i]);
        }

        selected = randomIndex(set2.size());
        // case 2:
        while (!set2.isEmpty()) {
            selected = removeSetEle(set2, index, selected);
            // 检查终止条件
            if (finished(index, kinds)) {
                return;
            }
        }

        Set<Kind> set3 = new HashSet<Kind>();
        for (int i = 0; i < index; i++) {
            if (!matrix.isHunted(i)) {
                set3.add(kinds[i]);
            }
        }

        selected = randomIndex(set3.size());
        while (!set3.isEmpty()) {
            selected = removeSetEle(set3, index, selected);
            if (finished(index, kinds)) {
                return;
            }
        }

        Set<Kind> set4 = new HashSet<Kind>();
        for (int i = 0; i < kinds.length; i++) {
            if (!matrix.isHuntedByJ(i, index) && index != i) {
                set4.add(kinds[i]);
            }
        }

        selected = randomIndex(set4.size());
        while (!set4.isEmpty()) {
            selected = removeSetEle(set4, index, selected);
            if (finished(index, kinds)) {
                return;
            }
        }
    }

    public static void main(String[] args) {
        Main m = new Main();

        Kind[] kinds = m.generateKinds();
        Arrays.sort(kinds);
        int sum = 0;
        for (int s = 0; s < kinds.length; s++) {
            kinds[s].seq = s;
            sum += kinds[s].l;
        }

        for (int i = 1; i < kinds.length; i++) {
            m.handleKind(kinds, i);
        }
        m.matrix.print();
        System.out.println("总连接数:" + sum);
    }
}
