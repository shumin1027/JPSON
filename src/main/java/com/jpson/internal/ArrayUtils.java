package com.jpson.internal;

public final class ArrayUtils {

    public static void reverse(final byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = startIndexInclusive < 0 ? 0 : startIndexInclusive;//等同与上面第二段代码中的head变量
        int j = Math.min(array.length, endIndexExclusive) - 1;//等同与上面第二段代码中的tail变量
        byte tmp;//等同与上面第二段代码中的temp变量
        while (j > i) {//使用while循环判断首尾的大小关系
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public static void reverse(final byte[] array) {
        if (array == null) {
            return;
        }
        for (int i = 0; i <= array.length / 2 - 1; i++) {
            byte temp1 = array[i];
            byte temp2 = array[array.length - i - 1];
            array[i] = temp2;
            array[array.length - i - 1] = temp1;
        }
    }

}