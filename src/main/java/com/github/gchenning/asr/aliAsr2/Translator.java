package com.github.gchenning.asr.aliAsr2;


import com.github.gchenning.asr.system.AudioRecord;

public interface Translator {

    void init() throws Exception;

    /**
     * 讯飞听写初始化
     * @param accent 传入方言类型
     * @throws Exception
     */
    void init(String accent) throws Exception;

    boolean working();

    boolean isShutdown();

    void shutdown();

    /**
     * 阿里语音1.0转义
     * @param input
     * @param off
     * @param len
     */
    void translate(byte[] input, int off, int len);

    /**
     * 阿里语音2.0转义
     * @param record
     * @param buffer
     * @param bufSize
     */
    void translate(AudioRecord record, byte[] buffer, int bufSize) throws Exception;

    /**
     * 讯飞听写识别
     * @param type
     */
    void recongnize(String type);

    void addListener(TranslateListener listener);

    void close();

    enum State {
        INITIALIZE,
        TRANSLATING,
        FINISHED,
        ERROR
    }

}
