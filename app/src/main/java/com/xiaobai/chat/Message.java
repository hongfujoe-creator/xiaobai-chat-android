package com.xiaobai.chat;

/**
 * 消息数据模型
 */
public class Message {
    // 消息类型：用户
    public static final int TYPE_USER = 1;
    
    // 消息类型：机器人（小白）
    public static final int TYPE_BOT = 2;
    
    // 消息类型
    private int type;
    
    // 消息内容
    private String text;
    
    /**
     * 构造函数
     * @param type 消息类型（TYPE_USER 或 TYPE_BOT）
     * @param text 消息内容
     */
    public Message(int type, String text) {
        this.type = type;
        this.text = text;
    }
    
    /**
     * 获取消息类型
     * @return TYPE_USER 或 TYPE_BOT
     */
    public int getType() {
        return type;
    }
    
    /**
     * 获取消息内容
     * @return 消息文本
     */
    public String getText() {
        return text;
    }
}
