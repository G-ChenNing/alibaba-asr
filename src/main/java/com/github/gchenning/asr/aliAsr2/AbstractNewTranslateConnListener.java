package com.github.gchenning.asr.aliAsr2;

import com.alibaba.nls.client.protocol.SpeechReqProtocol;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractNewTranslateConnListener extends SpeechRecognizerListener {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNewTranslateConnListener.class);

    protected final TranslateListener translateListener;
    private volatile String reason;
    private volatile int status;

    protected AbstractNewTranslateConnListener(TranslateListener translateListener) {
        if (translateListener == null) {
            throw new IllegalArgumentException("转义结果监听器不能为空");
        }
        this.translateListener = translateListener;
    }

    @Override
    public void onOpen() {
        logger.info("阿里云智能语音2.0开始监听");
        translateListener.onStateChange(Translator.State.TRANSLATING);
    }



//    public void onError(Throwable throwable) {
//        logger.error("阿里云智能语音2.0监听异常！", throwable);
//        if ("40000004".equals(reason) || status == 4404) {
//            return;
//        }
//        translateListener.onStateChange(Translator.State.ERROR);
//    }


//    public void onFail(int status, String reason) {
//        this.reason = reason;
//        this.status = status;
//        logger.error("阿里云智能语音2.0识别失败！失败信息： status：" + String.valueOf(status) + ", reason: " + reason);
//        this.reason = reason;
//        this.status = status;
//        //misclassification(status);
//    }

    @Override
    public void onClose(int closeCode, String reason) {
        logger.info("连接被关闭，关闭信息：closeCode: " + String.valueOf(closeCode) + ", reason: " + reason);
        //关闭连接，设置其状态为关闭状态
        AliRealtimeNewTranslator.state = SpeechReqProtocol.State.STATE_CLOSED;
        /*//空闲超时	确认是否长时间没有发送数据到服务端
        if (closeCode == 4404 && "40000004".equals(reason)) {
            return;
        }*/
        translateListener.onStateChange(Translator.State.FINISHED);
    }


    /**
     * 处理识别过程中可能发生的异常，对其错误码进行信息提示
     * @param response 传入SpeechTranscriberResponse对象
     */
    /*public void misclassification(SpeechTranscriberResponse response) {
        if (response != null) {
            int statusCode = response.getStatus();
            misclassification(statusCode);
        }
    }*/

    /**
     *  处理识别过程中可能发生的异常，对其错误码进行信息提示
     * @param statusCode 传入statusCode码
     */
    /*private void misclassification(int statusCode) {
        if (statusCode != 20000000) {
            //40000005	请求数量过多  超过了并发连接数或者每秒钟请求数
            if (statusCode == 40000005) {
                translateListener.onStateChange(Translator.State.MANY_CONCURRENCY);
            }
            //40010004	客户端提前断开连接
            if (statusCode == 40010004) {
                translateListener.onStateChange(Translator.State.CLIENT_DISCONNECT);
            }
            //51040101	服务端内部错误
            if (statusCode == 51040101) {
                translateListener.onStateChange(Translator.State.SERVICE_ERROR);
            }
            //41040201	客户端10s内停止发送数据, 检查网络
            if (statusCode == 41040201) {
                translateListener.onStateChange(Translator.State.STOP_SEND_DATA);
            }
            //40000004	空闲超时	确认是否长时间没有发送数据到服务端
            if (statusCode == 40000004) {
                translateListener.onStateChange(Translator.State.IDLE_TIMEOUT);
            }
        }
    }*/
}
