package com.jpson;

import com.jpson.internal.ArrayUtils;

public class ArrayUtilsTest {
    public static void main(String[] args) {
        byte[] arr = new byte[]{1, 2, 3, 4};
//        ArrayUtils.reverse(arr, 0, 4);
        StringBuffer sb = new StringBuffer();
//
//        for (int i = 0; i < arr.length; i++) {
//            sb.append(arr[i]);
//            if (i < arr.length - 1) {
//                sb.append(" ");
//            }
//        }
//        System.out.println(sb.toString());

        ArrayUtils.reverse(arr);

        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(" ");
            }
        }
        System.out.println(sb.toString());

    }
}
