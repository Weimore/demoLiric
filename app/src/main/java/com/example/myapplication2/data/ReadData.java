package com.example.myapplication2.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReadData implements Serializable {

    @SerializedName("w")
    public String word;
    @SerializedName("ws")
    public long startTime;
    @SerializedName("we")
    public long endTime;

}
