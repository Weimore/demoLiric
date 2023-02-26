package com.example.myapplication2.data;

public class WordData {

    //文本
    public String word;
    //起始时间
    public long startTime;
    //结束时间
    public long endTime;
    //总的分组index
    public int index;
    //文字起始index
    public int startWordIndex;

    public int getEndWordIndex() {
        return startWordIndex + word.length();
    }

}
