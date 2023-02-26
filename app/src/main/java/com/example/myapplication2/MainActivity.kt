package com.example.myapplication2

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.data.ReadData
import com.example.myapplication2.data.WordData
import com.example.myapplication2.databinding.ActivityMainBinding
import com.example.myapplication2.span.ClickHighLightSpan
import com.example.myapplication2.span.ClickSpan
import com.example.myapplication2.view.VerticalSeekBar.OnStateChangeListener
import com.google.gson.Gson
import com.google.gson.JsonArray
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null

    //当前播放时间
    private var curTime = 0L

    //高亮分组index
    private var highLightIndex = 0

    //总时长
    private var totalTime = 0L

    companion object {
        //间隔时间
        const val INTERVAL_DURATION = 100L
    }

    private var handler: Handler =
        @SuppressLint("HandlerLeak") object : Handler(Looper.myLooper()!!) {}
    private val list = arrayListOf<WordData>()


    private val runnable = object : Runnable {
        override fun run() {
            if (curTime >= totalTime) {
                curTime = 0L
            } else {
                curTime += INTERVAL_DURATION
            }
            changeProgress()
            val wordData = list[(highLightIndex) % list.size]
            if (curTime >= wordData.endTime) {
                highLightStr(list[(highLightIndex + 1) % list.size])
            }
            handler.postDelayed(this, INTERVAL_DURATION)
        }
    }

    private fun changeProgress() {
        mBinding?.progressBar?.setProgress(curTime * 100f / totalTime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding?.root)
        mBinding?.btnStartEnd?.setOnClickListener {
            if (mBinding?.btnStartEnd?.isSelected == false) {
                handler.post(runnable)
                mBinding?.btnStartEnd?.text = "暂停"
                mBinding?.btnStartEnd?.isSelected = true
            } else {
                mBinding?.btnStartEnd?.text = "开始"
                handler.removeCallbacks(runnable)
                mBinding?.btnStartEnd?.isSelected = false
            }
        }
        mBinding?.progressBar?.setOnStateChangeListener(object : OnStateChangeListener {
            override fun onStartTouch(view: View?) {}

            override fun onStateChangeListener(
                view: View?,
                progress: Float,
                indicatorOffset: Float
            ) {
                //拖动进度条
                curTime = (totalTime * 0.01 * progress).toLong()
                for (index in 0 until  list.size) {
                    if (curTime < list[index].endTime) {
                        highLightStr(list[index])
                        break
                    }
                }
            }

            override fun onStopTrackingTouch(view: View?, progress: Float) {
                //拖动进度条
                curTime = (totalTime * 0.01 * progress).toLong()
                for (index in 0 until  list.size) {
                    if (curTime < list[index].endTime) {
                        highLightStr(list[index])
                        break
                    }
                }
            }
        })
        val jsonStr = FileUtil.readAssets2String(this, "fakeData.json")
        var index = 0
        var wordIndex = 0
        Gson().fromJson(jsonStr, JsonArray::class.java).forEach { action ->
            val data = Gson().fromJson(action, ReadData::class.java)
            val wordData = WordData()
            wordData.word = data.word
            wordData.startTime = data.startTime
            wordData.endTime = data.endTime
            wordData.index = index
            wordData.startWordIndex = wordIndex
            list.add(wordData)
            index++
            wordIndex += data.word.length
        }
        totalTime = list.last().endTime
        initStr()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    //初始化str
    private fun initStr() {
        val builder = SpannableStringBuilder()
        for (index in 0 until list.size) {
            val data = list[index]
            //不需要高亮
            builder.append(
                data.word,
                ClickSpan {
                    //点击后高亮
                    curTime = data.startTime
                    changeProgress()
                    highLightStr(data)
                },
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        mBinding?.tvContent?.movementMethod = LinkMovementMethod.getInstance()
        mBinding?.tvContent?.text = builder
    }

    //高亮str
    private fun highLightStr(highLightData: WordData) {
        Log.d("word", "word:" + highLightData.word)
        val spannableString = SpannableStringBuilder(mBinding?.tvContent?.text)
        if (highLightIndex >= 0) {
            //将之前的高亮点击span重置为普通点击span
            val beforeHighLightData = list[highLightIndex]
            spannableString.setSpan(
                ClickSpan {
                    //点击后高亮
                    curTime = beforeHighLightData.startTime
                    changeProgress()
                    highLightStr(beforeHighLightData)
                },
                beforeHighLightData.startWordIndex,
                beforeHighLightData.endWordIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        //设置新的高亮点击span
        spannableString.setSpan(
            ClickHighLightSpan {
                //点击后高亮
                curTime = highLightData.startTime
                changeProgress()
                highLightStr(highLightData)
            },
            highLightData.startWordIndex,
            highLightData.endWordIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        highLightIndex = highLightData.index
        mBinding?.tvContent?.text = spannableString
    }
}