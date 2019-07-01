package com.github.gchenning.asr.model;

/**
 * 语音转文本结果
 */
public class TranslateResult {

    /**
     * 是否成功
     */
    private boolean isSuccess;

    /**
     * 转义结果
     */
    private String text;

    /**
     * 语音流
     */
    private byte[] soundBuffer;

    private String flowId;

    public boolean isSuccess() {
        return isSuccess;
    }

    public TranslateResult setSuccess(boolean success) {
        isSuccess = success;
        return this;
    }

    public byte[] getSoundBuffer() {
        return soundBuffer;
    }

    public TranslateResult setSoundBuffer(byte[] soundBuffer) {
        this.soundBuffer = soundBuffer;
        return this;
    }

    public String getText() {
        return text;
    }

    public TranslateResult setText(String text) {
        this.text = text;
        return this;
    }

    public String getFlowId() {
        return flowId;
    }

    public TranslateResult setFlowId(String flowId) {
        this.flowId = flowId;
        return this;
    }
}
