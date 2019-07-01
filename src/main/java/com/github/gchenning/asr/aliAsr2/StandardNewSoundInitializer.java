package com.github.gchenning.asr.aliAsr2;

import com.github.gchenning.asr.RobotRuntime;
import com.github.gchenning.asr.flow.FlowIdGenerator;
import com.github.gchenning.asr.flow.PrefixAutoIncrementFlowIdGenerator;
import com.github.gchenning.asr.socket.handler.MessageHandler;
import com.github.gchenning.asr.system.AudioRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 阿里实时语音识别 2.0
 * 标准对话机
 * <P>new StandardSoundInitializer(new Translator(), new TranslateListener()).soundFlow();</P>
 */
public class StandardNewSoundInitializer {


    public static FlowIdGenerator soundFlowIdGenerator = new PrefixAutoIncrementFlowIdGenerator("sound");
    /**
     * 对于流程日志，需要初始化一个日志流程id生成器
     */
    private static Logger logger = LoggerFactory.getLogger(StandardNewSoundInitializer.class);
    /**
     * 语音转义器
     */
    private final Translator translator;
    /**
     * 转义回调监听器
     */
    private final TranslateListener translateListener;

    /**
     * 作为启停实时语音识别的标志信息
     */
    public static boolean flag = true;

    private StandardNewSoundInitializer() {
        throw new IllegalStateException("private constructor.");
    }

    /**
     * 初始化一个语音流程
     *
     * @param translator
     * @param translateListener
     */
    public StandardNewSoundInitializer(Translator translator, TranslateListener translateListener) {
        this.translator = translator;
        this.translateListener = translateListener;
    }

    public Translator getTranslator() {
        return translator;
    }

    public TranslateListener getTranslateListener() {
        return translateListener;
    }

    public void close() {
        flag = false;
        translator.shutdown();
    }

    public void start() {
        flag = true;
        soundFlow();
    }

    public void startAli() {
        flag = true;
        new Thread(() -> {
            soundFlow();
        }, "aliRealtime").start();
    }


    /**
     * 标准语音流程
     */
    public void soundFlow() {

        while (flag) {
            while (true) {
                if (RobotRuntime.checkNetState()) {
                    break;
                }

                logger.info("~~~~~~~~~~~~~~~~·当前网络环境断开咯~~~~~~~~~~~~~~~~~~~~~···");
                //等待网络重新连接成功
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //logger.info("标准语音流程处发生错误，Thread.sleep(1000)。。。");
                    e.printStackTrace();
                }
            }


            //收声
            AudioRecord record = new AudioRecord();

            int notReadyCount = 0;

            try {
                //logger.info("~~~~~~~~~~~~语音开始初始化~~~~~~~~~~~~·");

                translator.init();

                while (true) {

                    if (translator.isShutdown()) {
                        break;
                    }

                    //当前关闭此循环，退出本次循环，交由上层循环处理
                    if (!flag) {
                        break;
                    }

//                    if (!MessageHandler.action1 ) {
//                        System.out.println("wait");
//                        Thread.sleep(200);
//                        continue;
//                }
                    if (!translator.working()) {
                        if (notReadyCount++ < 30) {
                            Thread.sleep(500);
                            continue;
                        } else {
                            break;
                        }
                    }
                    int bufSize = 6400;
                    byte[] buffer = new byte[bufSize];
                    translator.translate(record, buffer, bufSize);
                }

            } catch (IllegalStateException e1) {
                logger.error("没有有效长连接,请等连接建立后调用。 {}", e1.getMessage(), e1);
            } catch (Exception e) {
                logger.error("语音服务异常. {}", e.getMessage(), e);
            } finally {
                translator.close();
                record.close();
            }

        }
    }


}
