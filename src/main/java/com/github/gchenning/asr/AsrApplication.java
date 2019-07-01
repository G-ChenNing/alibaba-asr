package com.github.gchenning.asr;

import com.alibaba.nls.client.protocol.SpeechReqProtocol;
import com.github.gchenning.asr.aliAsr2.*;
import com.github.gchenning.asr.socket.handler.MessageHandler;
import com.github.gchenning.asr.system.CustomThreadFactory;
import com.github.gchenning.asr.system.FileUitl;
import com.github.gchenning.asr.system.SpeechRecognizerDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class AsrApplication {
//    public static TranslateListener translateListener;
//    public static StandardNewSoundInitializer standardNewSoundInitializer;
private static final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1, new CustomThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
private static final ScheduledExecutorService timer2 = new ScheduledThreadPoolExecutor(1, new CustomThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    public static void main(String[] args) {




        SpringApplication.run(AsrApplication.class, args);
        //模拟语音识别结果命令行
        Translator translator = new AliRealtimeNewTranslator();
        CommunicateTranslateListener communicateTranslateListener = new CommunicateTranslateListener(translator);

        StandardNewSoundInitializer standardNewSoundInitializer = new StandardNewSoundInitializer(translator, communicateTranslateListener);
        standardNewSoundInitializer.startAli();


        timer.scheduleAtFixedRate(() -> {
            if (MessageHandler.action2) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (MessageHandler.action1) {
                AliRealtimeNewTranslator.state = SpeechReqProtocol.State.STATE_CLOSED;
            }else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }, 5, 5, TimeUnit.SECONDS);

//        timer2.execute(() -> {
//            while (true) {
//                if (MessageHandler.action2) {
//                    Audio mr = new Audio();
//                    //调用录音的方法
//                    mr.capture();
//                    loop2:while (true) {
//                        if (!MessageHandler.action2) {
//                            //调用停止录音的方法
//                            mr.stop();
//
//                            //调用保存录音的方法
//                            String save = mr.save();
//                            try {
//                                SpeechRecognizerDemo.sendFile(save);
//                                break loop2;
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    }
//                }else {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//
//        });
    }

}
