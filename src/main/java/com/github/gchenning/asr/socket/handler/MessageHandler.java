package com.github.gchenning.asr.socket.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.protocol.SpeechReqProtocol;
import com.github.gchenning.asr.aliAsr2.AliRealtimeNewTranslator;
import com.github.gchenning.asr.aliAsr2.Audio;
import com.github.gchenning.asr.socket.entity.UserInfo;
import com.github.gchenning.asr.socket.proto.ChatCode;
import com.github.gchenning.asr.system.SpeechRecognizerDemo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @description 广播返回用户的信息
 */
public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public static volatile boolean action1 = false;
    public static volatile boolean action2 = false;
    public static Audio mr = null;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame)
            throws Exception {

        JSONObject object = JSONObject.parseObject(frame.text());


        String action = object.getString("action");
        if (action != null) {
            UserInfo user = UserInfoManager.getUserInfos().get(ctx.channel());
            if (user == null) {
                logger.error("用户不存在");
                return;
            }
            if ("1".equals(action)) {
                action1 = true;
                action2 = false;
            } else if ("11".equals(action)) {
                action1 = false;
                action2 = false;
            } else if ("2".equals(action)) {
                action1 = false;
                action2 = true;
//                Thread.sleep(5000);
//                mr = new Audio();
//                mr.capture();
            } else if ("22".equals(action)) {
                action1 = false;
                action2 = false;
                AliRealtimeNewTranslator.state = SpeechReqProtocol.State.STATE_CLOSED;
////                //调用停止录音的方法
////                mr.stop();
//
//                //调用保存录音的方法
//                String save = mr.save();
//                try {
//                    SpeechRecognizerDemo.sendFile(save);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mr = null;
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadCastInfo(ChatCode.SYS_USER_COUNT, UserInfoManager.getAuthUserCount());
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection error and close the channel", cause);
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadCastInfo(ChatCode.SYS_USER_COUNT, UserInfoManager.getAuthUserCount());
    }

}
