package com.example.myapplication2

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException

class FileUtil {

    companion object {
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
}