package com.github.gchenning.asr.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizer;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerListener;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerResponse;
import com.github.gchenning.asr.socket.handler.UserInfoManager;

/**
 * 一句话识别Demo
 */
public class SpeechRecognizerDemo {
    private String appKey;
    private String accessToken;
    NlsClient client;

    public SpeechRecognizerDemo(String appKey, String token) {
        this.appKey = appKey;
        this.accessToken = token;
        //创建NlsClient实例,应用全局创建一个即可,默认服务地址为阿里云线上服务地址
        client = new NlsClient(accessToken);
    }

    public SpeechRecognizerDemo(String appKey, String token, String url) {
        this.appKey = appKey;
        this.accessToken = token;
        //创建NlsClient实例,应用全局创建一个即可,用户指定服务地址
        client = new NlsClient(url, accessToken);
    }

    private static SpeechRecognizerListener getRecognizerListener() {
        SpeechRecognizerListener listener = new SpeechRecognizerListener() {
            //识别出中间结果.服务端识别出一个字或词时会返回此消息.仅当setEnableIntermediateResult(true)时,才会有此类消息返回
            @Override
            public void onRecognitionResultChanged(SpeechRecognizerResponse response) {
                //事件名称 RecognitionResultChanged
                System.out.println("name: " + response.getName() +
                        //状态码 20000000 表示识别成功
                        ", status: " + response.getStatus() +
                        //语音识别文本
                        ", result: " + response.getRecognizedText());
            }

            //识别完毕
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
        };
        return listener;
    }

    public void process(InputStream ins) {
        SpeechRecognizer recognizer = null;
        try {
            //创建实例,建立连接
            recognizer = new SpeechRecognizer(client, getRecognizerListener());
            recognizer.setAppKey(appKey);
            //设置音频编码格式
            recognizer.setFormat(InputFormatEnum.PCM);
            //设置音频采样率
            recognizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
            //设置是否返回中间识别结果
            recognizer.setEnableIntermediateResult(true);
            //此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
            recognizer.start();
            //语音数据来自声音文件用此方法,控制发送速率;若语音来自实时录音,不需控制发送速率直接调用 recognizer.sent(ins)即可
            recognizer.send(ins, 3200, 100);
            //通知服务端语音数据发送完毕,等待服务端处理完成
            recognizer.stop();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            //关闭连接
            if (null != recognizer) {
                recognizer.close();
            }
        }
    }

    public void shutdown() {
        client.shutdown();
    }


    public static volatile AccessToken tokens;
    public static volatile SpeechRecognizerDemo demos;

    public static AccessToken getToken() throws IOException {
        if (tokens == null) {
            synchronized (SpeechRecognizerDemo.class) {
                if (tokens == null) {
                    String akId = "LTAI2A7cZhaStf1T";
                    String akSecrete = "okr4MYMMjfNxz6jHy77VfOV1V3dHB7";

                    AccessToken token = new AccessToken(akId, akSecrete);
                    token.apply();
                    tokens = token;
                    return token;
                }
            }

        }
        return tokens;
    }

    public static SpeechRecognizerDemo getInstance() throws IOException {
        if (demos == null) {
            synchronized (SpeechRecognizerDemo.class) {
                if (demos == null) {
                    String appKey = "vpn3oRW9lKMYpW8G";
                    AccessToken token = getToken();
                    SpeechRecognizerDemo speechRecognizerDemo = new SpeechRecognizerDemo(appKey, token.getToken());
                    demos = speechRecognizerDemo;
                    return speechRecognizerDemo;
                }
            }
        }
        return demos;
    }

    public static void sendFile(String path) throws IOException {
        SpeechRecognizerDemo instance = getInstance();
        InputStream ins = new FileInputStream(new File(path));
        instance.process(ins);
//        instance.shutdown();

    }

    public static void main(String[] args) throws Exception {

        //default url is wss://nls-gateway.cn-shanghai.aliyuncs.com/ws/v1

        getToken();
        getInstance();

//        InputStream ins = SpeechRecognizerDemo.class.getResourceAsStream("1.wav");
        sendFile("audio/1.wav");
        sendFile("audio/1.wav");
//        if (null == ins) {
//            System.err.println("open the audio file failed!");
//            System.exit(-1);
//        }

    }
}