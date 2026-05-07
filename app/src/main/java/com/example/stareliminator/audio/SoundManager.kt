package com.example.stareliminator.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileInputStream
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(context: Context) {

    enum class SoundType {
        ELIMINATE,
        COMBO,
        BOARD_CLEAR,
        LEVEL_COMPLETE,
        GAME_OVER,
        INVALID_TAP
    }

    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<SoundType, Int>()
    private val cacheDir: File = File(context.cacheDir, "sounds").also { it.mkdirs() }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        for (type in SoundType.entries) {
            try {
                val wavFile = File(cacheDir, "${type.name.lowercase()}.wav")
                wavFile.writeBytes(generateWav(type))
                val fis = FileInputStream(wavFile)
                val soundId = soundPool.load(fis.fd, 0, wavFile.length(), 1)
                fis.close()
                if (soundId != 0) {
                    soundIds[type] = soundId
                } else {
                    Log.e("SoundManager", "Failed to load sound: $type")
                }
            } catch (e: Exception) {
                Log.e("SoundManager", "Error loading sound $type", e)
            }
        }
    }

    fun play(type: SoundType) {
        val soundId = soundIds[type] ?: return
        soundPool.play(soundId, 0.7f, 0.7f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }

    private fun generateWav(type: SoundType): ByteArray = when (type) {
        SoundType.ELIMINATE -> generateToneWav(
            durationMs = 200,
            startFreq = 440.0,
            endFreq = 880.0
        )
        SoundType.COMBO -> generateToneWav(
            durationMs = 200,
            startFreq = 660.0,
            endFreq = 1320.0
        )
        SoundType.BOARD_CLEAR -> generateVictoryJingle()
        SoundType.LEVEL_COMPLETE -> generateLevelCompleteFanfare()
        SoundType.GAME_OVER -> generateToneWav(
            durationMs = 400,
            startFreq = 440.0,
            endFreq = 220.0
        )
        SoundType.INVALID_TAP -> generateBuzz()
    }

    private fun generateToneWav(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double
    ): ByteArray {
        val sampleRate = 44100
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ByteArray(numSamples * 2 + 44)

        writeWavHeader(buffer, numSamples, sampleRate)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = startFreq + (endFreq - startFreq) * (i.toDouble() / numSamples)
            val envelope = 1.0 - (i.toDouble() / numSamples) * 0.3
            val sample = (sin(2.0 * PI * freq * t) * envelope * 0.5 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val offset = 44 + i * 2
            buffer[offset] = (sample and 0xFF).toByte()
            buffer[offset + 1] = (sample shr 8 and 0xFF).toByte()
        }

        return buffer
    }

    private fun generateVictoryJingle(): ByteArray {
        val sampleRate = 44100
        val noteDuration = 150
        val notes = intArrayOf(523, 659, 784, 1047) // C5, E5, G5, C6
        val samplesPerNote = noteDuration * sampleRate / 1000
        val totalSamples = notes.size * samplesPerNote
        val buffer = ByteArray(totalSamples * 2 + 44)

        writeWavHeader(buffer, totalSamples, sampleRate)

        for ((noteIdx, freq) in notes.withIndex()) {
            val startSample = noteIdx * samplesPerNote
            val endSample = startSample + samplesPerNote
            for (i in startSample until endSample) {
                val t = i.toDouble() / sampleRate
                val noteProgress = (i - startSample).toDouble() / samplesPerNote
                val envelope = (1.0 - noteProgress) * 0.6
                val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                val offset = 44 + i * 2
                buffer[offset] = (sample and 0xFF).toByte()
                buffer[offset + 1] = (sample shr 8 and 0xFF).toByte()
            }
        }

        return buffer
    }

    private fun generateLevelCompleteFanfare(): ByteArray {
        val sampleRate = 44100
        val noteDuration = 200
        val notes = intArrayOf(523, 659, 784, 1047, 784, 1047) // C5 E5 G5 C6 G5 C6
        val samplesPerNote = noteDuration * sampleRate / 1000
        val totalSamples = notes.size * samplesPerNote
        val buffer = ByteArray(totalSamples * 2 + 44)

        writeWavHeader(buffer, totalSamples, sampleRate)

        for ((noteIdx, freq) in notes.withIndex()) {
            val startSample = noteIdx * samplesPerNote
            val endSample = startSample + samplesPerNote
            for (i in startSample until endSample) {
                val t = i.toDouble() / sampleRate
                val noteProgress = (i - startSample).toDouble() / samplesPerNote
                val envelope = (1.0 - noteProgress * 0.5) * 0.5
                val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                val offset = 44 + i * 2
                buffer[offset] = (sample and 0xFF).toByte()
                buffer[offset + 1] = (sample shr 8 and 0xFF).toByte()
            }
        }

        return buffer
    }

    private fun generateBuzz(): ByteArray {
        val sampleRate = 44100
        val durationMs = 100
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ByteArray(numSamples * 2 + 44)

        writeWavHeader(buffer, numSamples, sampleRate)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = (if (sin(2.0 * PI * 150.0 * t) > 0) 0.3 else -0.3 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val offset = 44 + i * 2
            buffer[offset] = (sample and 0xFF).toByte()
            buffer[offset + 1] = (sample shr 8 and 0xFF).toByte()
        }

        return buffer
    }

    private fun writeWavHeader(buffer: ByteArray, numSamples: Int, sampleRate: Int) {
        val dataSize = numSamples * 2
        val fileSize = 36 + dataSize
        val byteRate = sampleRate * 2

        buffer[0] = 'R'.code.toByte(); buffer[1] = 'I'.code.toByte()
        buffer[2] = 'F'.code.toByte(); buffer[3] = 'F'.code.toByte()
        buffer[4] = (fileSize and 0xFF).toByte()
        buffer[5] = (fileSize shr 8 and 0xFF).toByte()
        buffer[6] = (fileSize shr 16 and 0xFF).toByte()
        buffer[7] = (fileSize shr 24 and 0xFF).toByte()
        buffer[8] = 'W'.code.toByte(); buffer[9] = 'A'.code.toByte()
        buffer[10] = 'V'.code.toByte(); buffer[11] = 'E'.code.toByte()

        buffer[12] = 'f'.code.toByte(); buffer[13] = 'm'.code.toByte()
        buffer[14] = 't'.code.toByte(); buffer[15] = ' '.code.toByte()
        buffer[16] = 16; buffer[17] = 0; buffer[18] = 0; buffer[19] = 0
        buffer[20] = 1; buffer[21] = 0  // PCM
        buffer[22] = 1; buffer[23] = 0  // mono
        buffer[24] = (sampleRate and 0xFF).toByte()
        buffer[25] = (sampleRate shr 8 and 0xFF).toByte()
        buffer[26] = (sampleRate shr 16 and 0xFF).toByte()
        buffer[27] = (sampleRate shr 24 and 0xFF).toByte()
        buffer[28] = (byteRate and 0xFF).toByte()
        buffer[29] = (byteRate shr 8 and 0xFF).toByte()
        buffer[30] = (byteRate shr 16 and 0xFF).toByte()
        buffer[31] = (byteRate shr 24 and 0xFF).toByte()
        buffer[32] = 2; buffer[33] = 0  // block align
        buffer[34] = 16; buffer[35] = 0 // bits per sample

        buffer[36] = 'd'.code.toByte(); buffer[37] = 'a'.code.toByte()
        buffer[38] = 't'.code.toByte(); buffer[39] = 'a'.code.toByte()
        buffer[40] = (dataSize and 0xFF).toByte()
        buffer[41] = (dataSize shr 8 and 0xFF).toByte()
        buffer[42] = (dataSize shr 16 and 0xFF).toByte()
        buffer[43] = (dataSize shr 24 and 0xFF).toByte()
    }
}
