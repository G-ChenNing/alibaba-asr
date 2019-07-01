package com.github.gchenning.asr.model;

/**
 * 文本解析结果
 */
public class ParserResult {

    /**
     * 收音转义文本
     */
    private TranslateResult translateResult;

    /**
     * 有效文本
     */
    private String validText;

    /**
     * 类型
     */
    private Type type = Type.QUESTION;

    public TranslateResult getTranslateResult() {
        return translateResult;
    }

    public ParserResult setTranslateResult(TranslateResult translateResult) {
        this.translateResult = translateResult;
        return this;
    }

    public String getValidText() {
        return validText;
    }

    public ParserResult setValidText(String validText) {
        this.validText = validText;
        return this;
    }

    public Type getType() {
        return type;
    }

    public ParserResult setType(Type type) {
        this.type = type;
        return this;
    }

    public boolean isStopCommand(){
        return is(Type.STOP);
    }

    public boolean isIgnore(){
        return is(Type.IGNORE);
    }

    public boolean isQuestion(){
        return is(Type.QUESTION);
    }

    public boolean isBasicCmd(){
        return is(Type.BASIC_CMD);
    }

    public boolean is(Type type){
        return type == this.type;
    }

    public enum Type {

        /**
         * 停止
         */
        STOP,

        /**
         * 有效问题
         */
        QUESTION,

        /**
         * 忽略
         */
        IGNORE,

        /**
         * 基本指令
         */
        BASIC_CMD

    }

}
