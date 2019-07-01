package com.github.gchenning.asr.aliAsr2;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.SpeechReqProtocol;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizer;
import com.github.gchenning.asr.RobotRuntime;
import com.github.gchenning.asr.socket.handler.MessageHandler;
import com.github.gchenning.asr.system.AudioRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AliRealtimeNewTranslator implements Translator {
    private static final Logger logger = LoggerFactory.getLogger(AliRealtimeNewTranslator.class);

    private TranslateListener translateListener;

    private static String appKey = "vpn3oRW9lKMYpW8G";
    private static String akId = "LTAI2A7cZhaStf1T";
    private static String akSecrete = "okr4MYMMjfNxz6jHy77VfOV1V3dHB7";
    private static int sign = 1;


    NlsClient client;

    SpeechRecognizer transcriber;

    public volatile static SpeechReqProtocol.State state;

    //获取阿里云实时语音 token, 有效期24小时（阿里反馈）
    private String getToken() throws Exception {
        AccessToken token = new AccessToken(akId, akSecrete);
        if (StringUtils.isBlank(akId) || StringUtils.isBlank(akSecrete) || StringUtils.isBlank(appKey)) {
            akId = "LTAI2A7cZhaStf1T";
            akSecrete = "okr4MYMMjfNxz6jHy77VfOV1V3dHB7";
            appKey = "vpn3oRW9lKMYpW8G";
        }
        token.apply();
        if (token == null) {
            return null;
        } else {
            return token.getToken();
        }
    }


    @Override
    public void init() throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("init Nls client...");
        }

        if (translateListener == null) {
            throw new IllegalArgumentException("转义结果监听器不能为空");
        }

        //Step0 创建NlsClient实例,应用全局创建一个即可,默认服务地址为阿里云线上服务地址
        if (StringUtils.isBlank(getToken())) {
            throw new RuntimeException("获取阿里云实时语音2.0的token为空");
        }
        client = new NlsClient(getToken());

        // Step1 创建实例,建立连接
        transcriber = new SpeechRecognizer(client, new StreamingSignSeparateNewConnListener(translateListener));
        transcriber.setAppKey(appKey);
        // 输入音频编码方式
        transcriber.setFormat(InputFormatEnum.PCM);
        // 输入音频采样率
        transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
        // 是否返回中间识别结果
        transcriber.setEnableIntermediateResult(true);
        // 是否生成并返回标点符号
        if (sign == 1) {
            transcriber.setEnablePunctuation(true);
        } else {
            transcriber.setEnablePunctuation(false);
        }
        // 是否将返回结果规整化,比如将一百返回为100
        transcriber.setEnableITN(false);

        // Step2 此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
        transcriber.start();
        //此处需要设置当前状态为初始化状态，当识别过程中发生错误，需要重新初始化识别流程
        this.state = SpeechReqProtocol.State.STATE_INIT;
        translateListener.onStateChange(State.INITIALIZE);

    }

    @Override
    public void init(String accent) throws Exception {

    }

    @Override
    public boolean working() {
        return !isShutdown();
    }

    @Override
    public boolean isShutdown() {
        return this.state == SpeechReqProtocol.State.STATE_CLOSED
                || this.state == SpeechReqProtocol.State.STATE_FAIL;
    }

    @Override
    public void shutdown() {
        //client.shutdown();
        close();
        //this.state = SpeechReqProtocol.State.STATE_CLOSED;
    }

    @Override
    public void translate(byte[] input, int off, int len) {

    }

    @Override
    public void translate(AudioRecord record, byte[] buffer, int bufSize) throws Exception {
        int nByte;
        if (MessageHandler.action1 && !MessageHandler.action2) {
            while ((nByte = record.read(buffer, 0, bufSize)) > 0) {

                //logger.info("~~~~~~~~~~~~``我在运行过程中~~~~~~~~~~~~~~~·");
                // Step4 直接发送麦克风数据流
                if (this.state == SpeechReqProtocol.State.STATE_CLOSED || !RobotRuntime.checkNetState()) {
                    break;
                }
                transcriber.send(buffer);
            }
            // Step5 通知服务端语音数据发送完毕,等待服务端处理完成
            transcriber.stop();
        } else if (MessageHandler.action2) {
            while ((nByte = record.read(buffer, 0, bufSize)) > 0) {

                //logger.info("~~~~~~~~~~~~``我在运行过程中~~~~~~~~~~~~~~~·");
                // Step4 直接发送麦克风数据流
                if (!MessageHandler.action2) {
                    break;
                }
                transcriber.send(buffer);
            }
            // Step5 通知服务端语音数据发送完毕,等待服务端处理完成
            transcriber.stop();
        }


    }

    @Override
    public void recongnize(String type) {

    }

    @Override
    public void addListener(TranslateListener listener) {
        this.translateListener = listener;
    }

    @Override
    public void close() {
        this.state = SpeechReqProtocol.State.STATE_CLOSED;
        if (logger.isDebugEnabled()) {
            logger.debug("close Nls client...");
        }
        if (transcriber != null) {
            transcriber.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }


    public String getAkId() {
        return akId;
    }

    public void setAkId(String akId) {
        this.akId = akId;
    }

    public String getAkSecrete() {
        return akSecrete;
    }

    public void setAkSecrete(String akSecrete) {
        this.akSecrete = akSecrete;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

}
