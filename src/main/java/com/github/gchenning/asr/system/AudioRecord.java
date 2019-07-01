package com.github.gchenning.asr.system;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * 系统音频采集
 */
public class AudioRecord {

    private TargetDataLine targetDataLine;

    public int read(byte[] tempBuffer, int off, int len) {
        return targetDataLine.read(tempBuffer, off, len);
    }

    public AudioRecord(){
        try {
            AudioFormat audioFormat = AudioFormatConfiguer.getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
        } catch (Exception e) {
            throw new IllegalStateException("音频输入设备异常，请确认正常工作", e);
        }
    }

    public void close(){

        if (targetDataLine != null && targetDataLine.isOpen()) {
            targetDataLine.close();
        }

    }

}

