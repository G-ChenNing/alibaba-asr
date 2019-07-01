package com.github.gchenning.asr.aliAsr2;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerResponse;
import com.github.gchenning.asr.aliAsr2.session.TranslateSession;
import com.github.gchenning.asr.model.TranslateResult;
import com.github.gchenning.asr.socket.handler.UserInfoManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class StreamingSignSeparateNewConnListener extends AbstractNewTranslateConnListener{

    public StreamingSignSeparateNewConnListener(TranslateListener translateListener) {
        super(translateListener);
    }

    //sessionMap
    private Map<Integer, TranslateSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 断句符号
     */
    private static final String SIGN = "，";




    private Logger logger = LoggerFactory.getLogger(StreamingSignSeparateNewConnListener.class);

//    //识别的中间结果
//    @Override
//    public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
//
//    }
//
//    //一句话结束的识别结果
//    @Override
//    public void onSentenceEnd(SpeechTranscriberResponse response) {
//
//
//
//    }
//
//    //识别完成
//    @Override
//    public void onTranscriptionComplete(SpeechTranscriberResponse response) {
//        super.onTranscriptionComplete(response);
//    }


    /**
     * 处理消息
     * @param message
     */
    private void onMessages(String message){

        String newFlowId = StandardNewSoundInitializer.soundFlowIdGenerator.generate();

        logger.info(newFlowId, "接收到阿里实时语音2.0转义文本:{}", message);

        TranslateResult translateResult = new TranslateResult();
        translateResult.setSuccess(true);
        translateResult.setText(message);
        translateResult.setFlowId(newFlowId);
        translateListener.onMessage(translateResult);
        /*try {
            translateListener.onMessage(translateResult);
        } catch (Exception e) {
            logger.error("语义处理逻辑存在异常！请检查", e.getMessage());
            RobotRuntime.getRuntime().tip(1, "语义处理过程异常");
        }*/


    }

    //返回值 > -1 出现校准

    /**
     * 对话中计算当前文本中是否出现了校准
     * @param hisCount
     * @param text
     * @return
     */
    private int computeStringBegin(int hisCount, String text){

        int begin = -1;

        int currentCount = StringUtils.countMatches(text, SIGN);

        if (hisCount == currentCount) {
            //出现校准了
            if (currentCount == 1) {
                begin = 0;
            } else {
                //count > 1
                //多次出现定位到倒数第二个
                int c = 0;
                int index = 0;
                while (c != currentCount - 1) {
                    index = text.indexOf(SIGN, index);
                    c++;
                }
                begin = index;
            }

        }

        return begin;

    }

    /**
     * 对话完成后判断是否，有调整。
     * @param hisCount
     * @param text
     * @return
     */
    private int computeFragmentBeginForWhole(int hisCount, String text){
        int begin = -1;

        int currentCount = StringUtils.countMatches(text, SIGN);

        if (hisCount > currentCount) {
            //历史的大于现在的，滚回倒数第二个
            if (hisCount <= 1) {
                begin = 0;
            } else {
                begin = text.lastIndexOf(SIGN);
            }
        }

        return begin;

    }
// begin != hisSignIndex 出现了校准


    private void setOperateStack() {
        JSONObject json = new JSONObject();
        json.put("type", "voice");
        //Plugin wakePlugin = FeaturePluginManager.getPlugin("utry.robot.wake.WakePlugin");
    }


    @Override
    public void onRecognitionResultChanged(SpeechRecognizerResponse response) {
        //事件名称 RecognitionResultChanged
        System.out.println("name: " + response.getName() +
                //状态码 20000000 表示识别成功
                ", status: " + response.getStatus() +
                //语音识别文本
                ", result: " + response.getRecognizedText());
    }

    @Override
    public void onRecognitionCompleted(SpeechRecognizerResponse response) {
        //事件名称 RecognitionCompleted
        System.out.println("name: " + response.getName() +
                //状态码 20000000 表示识别成功
                ", status: " + response.getStatus() +
                //语音识别文本
                ", result: " + response.getRecognizedText());
        UserInfoManager.broadcastMesswcn(response.getRecognizedText());
    }

    @Override
    public void onStarted(SpeechRecognizerResponse response) {
        System.out.println(
                "task_id: " + response.getTaskId());

//        logger.info("name: " + response.getName() +
//                // 状态码 20000000 表示正常识别
//                ", status: " + response.getStatus() +
//                // 句子编号，从1开始递增
//                ", index: " + response.getTransSentenceIndex() +
//                // 当前句子的完整识别结果
//                ", result: " + response.getTransSentenceText() +
//                // 当前已处理的音频时长，单位是毫秒
//                ", time: " + response.get());
//
//        if (response != null && StringUtils.isNotBlank(response.getTransSentenceText())) {
//            int sentenceId = response.getTransSentenceIndex();
//            TranslateSession session = null;
//
//            String text = response.getTransSentenceText();
//
//            logger.info("#######阿里实时语音识别（一句话识别结束），响应code： " + response.getStatus()
//                    + " \n响应内容： " + text);
//
//            if (response.getStatus() == 20000000) {
//                //转义结束了   status_code = 20000000
//                session = sessionMap.remove(sentenceId);
//                //是否处理了
//
//                if (session != null) {
//                    //是否有已经处理过的语句了
//                    int allLength = text.length();
//
//                    int begin;
//
//                    if ((begin = computeFragmentBeginForWhole(session.signCount, text)) == -1) {
//                        begin = session.signIndex;
//                    }
//
//                    if (begin > 0 && begin < allLength) {
//                        text = text.substring(begin + 1, allLength);
//                    }
//
//                    session = null;
//                }
//
//                onMessages(text);
//
//
//                return;
//            }
//
//            //识别过程中异常处理~~~
//            //misclassification(response);
//
//            if (text.contains(SIGN)) {
//
//                //含有标记符号了
//                //转义中       status_code = 1
//                if (!sessionMap.containsKey(sentenceId)) {
//                    synchronized (this) {
//                        if (!sessionMap.containsKey(sentenceId)) {
//                            session = new TranslateSession();
//                            sessionMap.put(sentenceId, session);
//                        }
//                    }
//                }
//
//                if (session == null) {
//                    session = sessionMap.get(sentenceId);
//                }
//
//                //当前最新的断点下标 20
//                int currentLastIndex = text.lastIndexOf(SIGN);
//
//                if (currentLastIndex > session.signIndex ) {
//
//                    int begin = computeStringBegin(session.signCount, text);
//
//                    synchronized (this) {
//                        if (begin == -1) {
//                            //正常
//                            //这么做实际上是线程不安全的，volatile并不能保证原子性，但是在这里对并发要求并不高。
//                            session.signCount++;
//                            begin = session.signIndex == 0 ? 0 : session.signIndex + 1;
//                        }
//
//                        session.signIndex = currentLastIndex;
//                    }
//
//                    String fragment = text.substring(begin, currentLastIndex);
//
//                    onMessages(fragment);
//
//                }
//
//            }
//        }
    }

    @Override
    public void onFail(SpeechRecognizerResponse response) {
        System.out.println(
                "task_id: " + response.getTaskId() +
                        //状态码 20000000 表示识别成功
                        ", status: " + response.getStatus() +
                        //错误信息
                        ", status_text: " + response.getStatusText());
    }

}