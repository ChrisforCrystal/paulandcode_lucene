package test;

import java.text.NumberFormat;

/**
 * @Description: 测试
 * @Author: paulandcode
 * @Email: paulandcode@gmail.com
 * @Date: 2018/9/26 17:44
 */
public class Test {
    public static void main(String[] args) {
        double i = 1234.134534566500;

        NumberFormat nf = NumberFormat.getInstance();
        // 是否以逗号隔开, 默认true以逗号隔开,如[123,456,789.128]
        nf.setMaximumFractionDigits(100);
        nf.setGroupingUsed(false);
        System.out.println(nf.format(i));
    }
}
