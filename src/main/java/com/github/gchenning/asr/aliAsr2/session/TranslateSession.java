package com.github.gchenning.asr.aliAsr2.session;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 实时转义会话对象
 */
public class TranslateSession {

    //一次语音转义流程
    private String sessionId;

    //实时转义结果
    private LinkedList<StreamingResult> realtimeResults = new LinkedList<>();

    public volatile int signCount = 0;

    public volatile int signIndex = 0;

    public String getSessionId() {
        return sessionId;
    }

    public TranslateSession setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public List<StreamingResult> getRealtimeResults() {
        return Collections.unmodifiableList(realtimeResults);
    }

//    public TranslateSession addRealtimeResult(NlsResponse.Result result){
//        this.realtimeResults.add(new StreamingResult(result));
//        return this;
//    }

    public StreamingResult getLastestRealtimeResult(){
        try {
            return this.realtimeResults.getLast();
        } catch (Exception e) {
            return null;
        }

    }

    public static class StreamingResult {

        private final long timestamp = System.currentTimeMillis();

//        private NlsResponse.Result result;
//
//        public StreamingResult(NlsResponse.Result result) {
//            this.result = result;
//        }
//
//        public long getTimestamp() {
//            return timestamp;
//        }
//
//        public NlsResponse.Result getResult() {
//            return result;
//        }
    }
}
