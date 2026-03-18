package com.xiaobai.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 小白聊天 - 主界面
 * 原生聊天界面，微信风格
 */
public class MainActivity extends AppCompatActivity {
    
    // 输入框
    private EditText inputMessage;
    
    // 发送按钮
    private Button sendButton;
    
    // 聊天列表
    private RecyclerView chatRecyclerView;
    
    // 消息适配器
    private MessageAdapter adapter;
    
    // 消息列表
    private List<Message> messageList;
    
    // 主线程 Handler
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化 Handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 绑定视图组件
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        
        // 初始化消息列表
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        
        // 配置 RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
        
        // 添加欢迎消息
        addMessage(Message.TYPE_BOT, "🤖 小白：你好，我是小白！有什么可以帮你的吗？");
        
        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    /**
     * 发送消息
     */
    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            // 添加用户消息（右边绿色气泡）
            addMessage(Message.TYPE_USER, message);
            
            // 清空输入框
            inputMessage.setText("");
            
            // 模拟小白回复（延迟 500ms）
            mainHandler.postDelayed(() -> {
                addMessage(Message.TYPE_BOT, "🤖 小白：收到你的消息：" + message);
            }, 500);
        }
    }
    
    /**
     * 添加消息到列表
     * @param type 消息类型（TYPE_USER 或 TYPE_BOT）
     * @param text 消息内容
     */
    private void addMessage(int type, String text) {
        messageList.add(new Message(type, text));
        adapter.notifyItemInserted(messageList.size() - 1);
        // 滚动到底部
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }
}
