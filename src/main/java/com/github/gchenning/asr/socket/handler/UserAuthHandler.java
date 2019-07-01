package com.github.gchenning.asr.socket.handler;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.github.gchenning.asr.socket.entity.UserInfo;
import com.github.gchenning.asr.socket.proto.ChatCode;
import com.github.gchenning.asr.socket.proto.ChatProto;
import com.github.gchenning.asr.socket.util.Constants;
import com.github.gchenning.asr.socket.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description 处理请求认证和分发消息
 */
public class UserAuthHandler extends SimpleChannelInboundHandler<Object> {

//    private volatile ApplicationContext applicationContext = SpringApplicationContextHolder.getApplicationContext();


    private static final Logger logger = LoggerFactory.getLogger(UserAuthHandler.class);

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx.channel() == null) {
            return;
        }
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocket(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            String eventType = null;

            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读空闲";
                    // 判断Channel是否读空闲, 读空闲时移除Channel
                    ChannelFuture channelFuture = ctx.channel().writeAndFlush(new PingWebSocketFrame());
                    IdleStateEvent evnet = (IdleStateEvent) evt;
                    // 判断Channel是否读空闲, 读空闲时移除Channel
                    if (evnet.state().equals(IdleState.READER_IDLE)) {
                        UserInfo userInfo = UserInfoManager.getUserInfo(ctx.channel());
                        if (userInfo == null) {
                            break;
                        }
//                        if (userInfo.getPingTime() > 2) {
//                            final String remoteAddress = NettyUtil.parseChannelRemoteAddr(ctx.channel());
//                            logger.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
//                            UserInfoManager.removeChannel(ctx.channel());
//                            UserInfoManager.broadCastInfo(ChatCode.SYS_USER_COUNT, UserInfoManager.getAuthUserCount());
//                            return;
//                        } else {
//                            userInfo.setPingTime(userInfo.getPingTime() + 1);
//                            System.out.println(userInfo.getPingTime() + "次");
//                        }
                    }

                    break;
                case WRITER_IDLE:
                    eventType = "写空闲";
                    ctx.channel().writeAndFlush(new PingWebSocketFrame());
                    break;
                case ALL_IDLE:
                    eventType = "读写空闲";
                    ctx.channel().writeAndFlush(new PingWebSocketFrame());
                    break;
                default:
                    ;
            }
            System.out.println(ctx.channel().remoteAddress() + " 超时事件： " + eventType);
        }

        ctx.fireUserEventTriggered(evt);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            logger.warn("protobuf don't support websocket");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(
                Constants.WEBSOCKET_URL, null, true);
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 动态加入websocket的编解码处理
            handshaker.handshake(ctx.channel(), request);
            UserInfo userInfo = new UserInfo();
            userInfo.setAddr(NettyUtil.parseChannelRemoteAddr(ctx.channel()));
            // 存储已经连接的Channel
//            UserInfoManager.addChannel(ctx.channel());
            UserInfoManager.addChannelNoAuth(ctx.channel(), ctx.channel().id().asLongText());
        }
    }

    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路命令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            UserInfoManager.removeChannel(ctx.channel());
            return;
        }
        // 判断是否Ping消息
        if (frame instanceof PingWebSocketFrame) {
            logger.info("ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否Pong消息
        if (frame instanceof PongWebSocketFrame) {
            logger.info("pong message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 本程序目前只支持文本消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(frame.getClass().getName() + " frame type not supported");
        }

        String message = ((TextWebSocketFrame) frame).text();

        if (StringUtils.isEmpty(message)) {
            return;
        }
        JSONObject json = null;
        try {
            json = JSONObject.parseObject(message);
        } catch (Exception e) {
            try {
                JSONObject.parseArray(message);
            } catch (JSONException ex1) {
//                //log.warn("不是JSONObject类型，类型可能非标准字符串" + message);
                return;
            }
            //log.warn("不是JSONObject类型，类型是JSONArray:" + message);
            return;
        }

        Integer code = json.getInteger("code");
        if (code == null) {
            code = -1;
        }

        Channel channel = ctx.channel();

        switch (code) {
            case ChatCode.PING_CODE:
                logger.info("收到心跳ping...");
//                channel.writeAndFlush(new PongWebSocketFrame());
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPongProto()));

                return;
            case ChatCode.PONG_CODE:
                UserInfoManager.updateUserTime(channel);
//                UserInfoManager.sendPong(ctx.channel());
                logger.info("receive pong message, address: {}", NettyUtil.parseChannelRemoteAddr(channel));
                return;
            case ChatCode.AUTH_CODE:
//                boolean isSuccess = UserInfoManager.saveUser(channel, json.getString("nick"));
//                UserInfoManager.sendInfo(channel,ChatCode.SYS_AUTH_STATE,isSuccess);
//                if (isSuccess) {
//                    UserInfoManager.broadCastInfo(ChatCode.SYS_USER_COUNT,UserInfoManager.getAuthUserCount());
//                }
                return;
            case ChatCode.MESS_CODE: //普通的消息留给MessageHandler处理
                break;
            default:
                logger.warn("The code [{}] can't be ping pong!!!", code);
                break;
        }
        //后续消息交给MessageHandler处理
        ctx.fireChannelRead(frame.retain());
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

//        boolean isSuccess = UserInfoManager.saveUser(channel, channel.id().asLongText());
        boolean isSuccess = UserInfoManager.addChannelNoAuth(channel, channel.id().asLongText());

        if (isSuccess) {
            System.out.println("handlerAdded: " + ctx.channel().id().asLongText());
        } else {
            logger.error("add user false");
            ctx.close();
        }

//        UserInfoManager.sendInfo(channel,ChatCode.SYS_AUTH_STATE,isSuccess);
//        if (isSuccess) {
//            UserInfoManager.broadCastInfo(ChatCode.SYS_USER_COUNT,UserInfoManager.getAuthUserCount());
//        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        UserInfoManager.removeChannel(channel);
        System.out.println("handlerRemoved: " + channel.id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常发生");
        cause.printStackTrace();

        ctx.close();
    }


//    public ApplicationContext getApplicationContext() {
//        if (applicationContext == null) {
//            synchronized (UserAuthHandler.class) {
//                if (applicationContext == null) {
//                    applicationContext = SpringApplicationContextHolder.getApplicationContext();
//                }
//            }
//        }
//
//        return applicationContext;
//    }
//
//    public void setApplicationContext(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }

}
