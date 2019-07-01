package com.github.gchenning.asr.aliAsr2;


import com.github.gchenning.asr.model.TranslateResult;

import java.util.EventListener;

/**
 * 转义监听器
 */
public interface TranslateListener extends EventListener {

    void onMessage(TranslateResult result);

    void onException();

    void onStateChange(Translator.State state);

}
