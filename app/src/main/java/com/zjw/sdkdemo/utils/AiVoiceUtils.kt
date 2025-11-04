package com.zjw.sdkdemo.utils

import android.media.MediaPlayer
import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.zh.opus.OpusBridge
import java.io.File
import java.io.FileOutputStream

object AiVoiceUtils {
    private val tag: String = AiVoiceUtils::class.java.simpleName

    private var opusDecoder: Long = 0
    private val pcmBuffer = ShortArray(1920)
    private const val OPUS_BLOCK_SIZE = 80

    private const val SAMPLE_RATE = 16000
    private const val CHANNELS = 1
    private const val BITS_RATE = 16

    private val cachePath = PathUtils.getExternalAppCachePath() + "/audio"

    private fun createDecoder() {
        opusDecoder = OpusBridge.createDecoder(SAMPLE_RATE, CHANNELS)
    }

    private fun destroyDecoder() {
        if (opusDecoder != 0L) {
            OpusBridge.destroyDecoder(opusDecoder)
            opusDecoder = 0
        }
    }

    @JvmStatic
    fun playVoice(receivedData: ByteArray?) {
        createDecoder()
        val decodedData = decodeOpusData(receivedData)
        if (decodedData == null) {
            ToastUtils.showToast("playVoice decodedData = null")
            return
        }
        playWithData(decodedData)
        destroyDecoder()
    }

    /**
     * 播放解码后的PCM数据
     * Play the decoded PCM data
     *
     * @param pcmData 解码后的PCM数据 Decoded PCM data
     */
    @JvmStatic
    private fun playWithData(pcmData: ShortArray) {
        Log.i(tag, "playWithData")
        try {
            val pcmBytes = shortArrayToByteArray(pcmData)
            val wavHead = buildWavHead(pcmBytes.size)
            val wavData = ByteArray(wavHead.size + pcmBytes.size)
            System.arraycopy(wavHead, 0, wavData, 0, wavHead.size)
            System.arraycopy(pcmBytes, 0, wavData, wavHead.size, pcmBytes.size)
            playAudio(wavData)
        } catch (e: Exception) {
            Log.e(tag, "playWithData Exception e=${e.message}")
        }
    }

    /**
     * 播放WAV音频数据 Play WAV audio data
     *
     * @param wavData WAV格式的音频数据 Audio data in WAV format
     */
    @JvmStatic
    private fun playAudio(wavData: ByteArray) {
        Log.i(tag, "playAudio")
        try {
            // 使用Android MediaPlayer播放音频
            FileUtils.createOrExistsDir(cachePath)
            val tempFile = File(cachePath, "temp_audio.wav")
            FileOutputStream(tempFile).use { fos ->
                fos.write(wavData)
            }

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
            }

            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }

        } catch (e: Exception) {
            Log.e(tag, "playAudio Exception e=${e.message}")
        }
    }

    /**
     * 将ShortArray转换为ByteArray
     *
     * @param shortArray Short数组
     * @return 字节数组
     */
    private fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteArray = ByteArray(shortArray.size * 2)
        for (i in shortArray.indices) {
            val shortValue = shortArray[i].toInt()
            byteArray[i * 2] = (shortValue and 0xFF).toByte()
            byteArray[i * 2 + 1] = ((shortValue shr 8) and 0xFF).toByte()
        }
        return byteArray
    }

    /**
     * 构建WAV文件头
     *
     * @param audioDataSize PCM数据大小
     * @return WAV文件头字节数组
     */
    private fun buildWavHead(audioDataSize: Int): ByteArray {
        val head = ByteArray(44)

        // RIFF header
        head[0] = 'R'.code.toByte()
        head[1] = 'I'.code.toByte()
        head[2] = 'F'.code.toByte()
        head[3] = 'F'.code.toByte()
        // File size (data size + 36)
        val fileSize = audioDataSize + 36
        head[4] = (fileSize and 0xff).toByte()
        head[5] = ((fileSize shr 8) and 0xff).toByte()
        head[6] = ((fileSize shr 16) and 0xff).toByte()
        head[7] = ((fileSize shr 24) and 0xff).toByte()
        // WAVE header
        head[8] = 'W'.code.toByte()
        head[9] = 'A'.code.toByte()
        head[10] = 'V'.code.toByte()
        head[11] = 'E'.code.toByte()
        // fmt chunk marker
        head[12] = 'f'.code.toByte()
        head[13] = 'm'.code.toByte()
        head[14] = 't'.code.toByte()
        head[15] = ' '.code.toByte()
        // Length of format data (16 for PCM)
        head[16] = 16
        head[17] = 0
        head[18] = 0
        head[19] = 0
        // Type of format (1 is PCM)
        head[20] = 1
        head[21] = 0
        // Number of channels
        head[22] = CHANNELS.toByte()
        head[23] = 0
        // Sample rate
        head[24] = (SAMPLE_RATE and 0xff).toByte()
        head[25] = ((SAMPLE_RATE shr 8) and 0xff).toByte()
        head[26] = ((SAMPLE_RATE shr 16) and 0xff).toByte()
        head[27] = ((SAMPLE_RATE shr 24) and 0xff).toByte()
        // Byte rate (SampleRate * NumChannels * BitsPerSample/8)
        val byteRate = SAMPLE_RATE * CHANNELS * BITS_RATE / 8
        head[28] = (byteRate and 0xff).toByte()
        head[29] = ((byteRate shr 8) and 0xff).toByte()
        head[30] = ((byteRate shr 16) and 0xff).toByte()
        head[31] = ((byteRate shr 24) and 0xff).toByte()
        // Block align (NumChannels * BitsPerSample/8)
        head[32] = (CHANNELS * BITS_RATE / 8).toByte()
        head[33] = 0
        // Bits per sample
        head[34] = BITS_RATE.toByte()
        head[35] = 0
        // Data chunk header
        head[36] = 'd'.code.toByte()
        head[37] = 'a'.code.toByte()
        head[38] = 't'.code.toByte()
        head[39] = 'a'.code.toByte()
        // Data chunk size (PCM data size)
        head[40] = (audioDataSize and 0xff).toByte()
        head[41] = ((audioDataSize shr 8) and 0xff).toByte()
        head[42] = ((audioDataSize shr 16) and 0xff).toByte()
        head[43] = ((audioDataSize shr 24) and 0xff).toByte()
        return head
    }


    /**
     * 参考iOS实现的Opus解码方法
     *
     * @param opusData 待解码的Opus数据
     * @return 解码后的PCM数据，失败时返回null
     */
    private fun decodeOpusData(opusData: ByteArray?): ShortArray? {
        Log.i(tag, "decodeOpusData")
        if (opusData == null || opusData.isEmpty() || opusDecoder == 0L) {
            return null
        }
        try {
            val opusPtr = 0
            val opusSize = opusData.size
            val opusEnd = opusSize
            val decodedData = mutableListOf<Short>()
            var currentPtr = opusPtr
            while (currentPtr < opusEnd) {
                // 确定当前块大小
                val nBytes = minOf(OPUS_BLOCK_SIZE, opusEnd - currentPtr)
                // 提取当前块数据
                val blockData = opusData.copyOfRange(currentPtr, currentPtr + nBytes)
                // 解码当前块
                val decodedSamples = OpusBridge.decode(opusDecoder, blockData, pcmBuffer)
                if (decodedSamples <= 0) {
                    Log.e(tag, "decodeOpusData error decodedSamples = $decodedSamples")
                    return null
                }
                // 将解码后的数据添加到结果中
                for (i in 0 until decodedSamples) {
                    decodedData.add(pcmBuffer[i])
                }
                // 移动到下一个块
                currentPtr += nBytes
            }
            return if (decodedData.isNotEmpty()) {
                decodedData.toShortArray()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "decodeOpusData Exception e=${e.message}")
            return null
        }
    }
}