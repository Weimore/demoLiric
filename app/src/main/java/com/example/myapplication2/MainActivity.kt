package com.example.myapplication2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.util.*


class MainActivity : AppCompatActivity() {

    interface TimeChangeListener {
        fun timeChange(duration: Int)
    }

    private var mBinding: ActivityMainBinding? = null
    private var curTime = 0L

    companion object {
        const val INTERVAL_DURATION = 100L
    }

    private var handler: Handler =
        @SuppressLint("HandlerLeak") object : Handler(Looper.myLooper()!!) {}
    private val list = arrayListOf<ReadData>()


    private val runnable = object : Runnable {
        override fun run() {
            if (curTime >= FakerData.duration) {
                curTime = 0L
            } else {
                curTime += INTERVAL_DURATION
            }
            highLightStr()
            handler.postDelayed(this, INTERVAL_DURATION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding?.root)
        val jsonStr = readAssets2String(this, "fakeData.json")
        Gson().fromJson(jsonStr, JsonArray::class.java).forEach { action ->
            val data = Gson().fromJson(action, ReadData::class.java)
            list.add(data)
        }
        FakerData.duration = list.last().time
        mBinding?.tvContent?.text = FakerData.content
        mBinding?.btnSearch?.setOnClickListener {
            handler.post(runnable)
        }
        highLightStr()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }


    private fun highLightStr() {
        mBinding?.tvContent?.text = ""
        val builder = SpannableStringBuilder()
        var highLightIndex = -1
        for (index in 0 until list.size) {
            val data = list[index]
            val spanString = SpannableString(data.text)
            if (curTime < list[index].time && highLightIndex == -1) {
                highLightIndex = 0
                //需要高亮
                spanString.setSpan(
                    ClickHighLightSpan {
                        Toast.makeText(this, data.text, Toast.LENGTH_SHORT).show()
                    },
                    0,
                    data.text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                //不需要高亮
                spanString.setSpan(
                    ClickSpan {
                        Toast.makeText(this, data.text, Toast.LENGTH_SHORT).show()
                        //点击后高亮
                        curTime = data.time
                        highLightStr()
                    },
                    0,
                    data.text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            builder.append(spanString)
        }
        mBinding?.tvContent?.movementMethod = LinkMovementMethod.getInstance()
        mBinding?.tvContent?.text = builder
    }

    private fun highStr(hlStr: String, srcString: String) {
        mBinding?.tvContent?.text = ""
        val hlLower = hlStr.lowercase(Locale.getDefault())
        val spannableString = SpannableString(srcString)
        for (i in 0..srcString.length - hlStr.length) {
            if (srcString.substring(i, i + hlStr.length)
                    .lowercase(Locale.getDefault()) == hlLower
            ) {
                spannableString.setSpan(
                    ForegroundColorSpan(Color.RED),
                    i,
                    i + hlStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    i,
                    i + hlStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        mBinding?.tvContent?.append(spannableString)
    }

    fun readAssets2String(context: Context, assetsFilePath: String): String? {
        return try {
            val `is`: InputStream = context.resources.assets.open(assetsFilePath)
            val bytes: ByteArray = input2OutputStream(`is`)?.toByteArray() ?: return ""
            try {
                String(bytes)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    private fun input2OutputStream(`is`: InputStream?): ByteArrayOutputStream? {
        return if (`is` == null) null else try {
            val os = ByteArrayOutputStream()
            val b = ByteArray(8192)
            var len: Int
            while (`is`.read(b, 0, 8192).also {
                    len = it
                } != -1) {
                os.write(b, 0, len)
            }
            os
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}