package com.jpson;

import java.util.ArrayList;
import java.util.List;

public class PSONTest {

    public static void main(String[] args) {
        Man man = new Man("shumin", 30, 165, 50);

        List<String> dic = new ArrayList<>();

        dic.add("name");
        dic.add("age");
        dic.add("high");
        dic.add("weigh");

        byte[] datas = PSON.encode(man, dic);

        Object obj = PSON.decode(datas, dic);

        System.out.println();

    }

}


class Man {
    String name;
    int age;
    long high;
    int weigh;

    public Man(String name, int age, long high, int weigh) {
        this.name = name;
        this.age = age;
        this.high = high;
        this.weigh = weigh;
    }

    public String getName() {
        return name;
    }

    public Man setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Man setAge(int age) {
        this.age = age;
        return this;
    }

    public long getHigh() {
        return high;
    }

    public Man setHigh(long high) {
        this.high = high;
        return this;
    }

    public int getWeigh() {
        return weigh;
    }

    public Man setWeigh(int weigh) {
        this.weigh = weigh;
        return this;
    }
}