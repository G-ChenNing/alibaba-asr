package com.github.gchenning.asr.aliAsr2;

import com.github.gchenning.asr.model.TranslateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;

/**
 * 对话，对于实时语音转义文本之后，会回调此类#onMessage
 * <P>处理具体的文本流程</P>
 * 1. 通过各种匹配规则，匹配文本
 * 2. 根据提取规则，做出对应的处理
 * 3. 如果有人工介入流程，会进行人工介入
 * 4. 做出最终的播报
 */
public class CommunicateTranslateListener implements TranslateListener {
    protected EventListenerList translateListenerList = new EventListenerList();
    protected EventListenerList analyzListenerList = new EventListenerList();

    public static int noanswer =1;

    public static long noanswertime =2;
    public static long previousNoAnswerTime = 0;
    private static int alivoiceExceptionTimeSetting = 10;
    private static int alivoiceExceptionTime = 0;
    private static int pushAlikeanswer = 1;
    private Logger logger = LoggerFactory.getLogger(CommunicateTranslateListener.class);
    private Translator translator;

    public CommunicateTranslateListener(Translator translator) {
        this.translator = translator;
        this.translator.addListener(this);
    }


    @Override
    public void onMessage(TranslateResult translateResult) {

    }
    private void stopMusic() {

    }
    @Override
    public void onException() {
        translator.shutdown();
    }
    @Override
    public void onStateChange(Translator.State state) {

    }


    private void setOperateStack() {

    }

   /* private void setTimestamp(){
        long timestamp = System.currentTimeMillis();
        JSONObject json = new JSONObject();
        json.put("type","voice");
        json.put("timestamp",timestamp);
        OperateStack.getInstance().setJsonStack(json);
    }*/
    /**
     * 发声
     *
     * @param text
     */
    private void textToSound(String text) {

    }
}