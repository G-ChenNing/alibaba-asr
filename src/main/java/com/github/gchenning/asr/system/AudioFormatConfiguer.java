package com.github.gchenning.asr.system;

import javax.sound.sampled.AudioFormat;

/**
 * 声音采样率配置
 *
 */
public class AudioFormatConfiguer {

    public static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
