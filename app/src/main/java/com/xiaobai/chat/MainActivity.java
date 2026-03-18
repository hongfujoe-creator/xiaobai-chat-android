package com.xiaobai.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 小白聊天 - 主界面
 * 连接 OpenClaw WebSocket 服务器
 */
public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "XiaoBaiChat";
    
    // ⚠️ 本地 WebSocket 地址
    // 模拟器用：ws://10.0.2.2:18789
    // 真机调试用：ws://你的电脑 IP:18789
    private static final String WEBSOCKET_URL = "ws://10.0.2.2:18789";
    
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
    
    // WebSocket 客户端
    private WebSocket webSocket;
    
    // 连接状态
    private boolean isConnected = false;
    
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
        
        // 连接 WebSocket 服务器
        connectWebSocket();
        
        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    /**
     * 连接 WebSocket 服务器
     */
    private void connectWebSocket() {
        // 配置 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        
        // 构建请求
        Request request = new Request.Builder()
                .url(WEBSOCKET_URL)
                .build();
        
        // 创建 WebSocket 监听器
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket 连接成功");
                isConnected = true;
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "✅ 已连接到小白服务器", Toast.LENGTH_SHORT).show();
                    addMessage(Message.TYPE_BOT, "🤖 小白：已连接！有什么可以帮你的吗？");
                });
            }
            
            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "收到消息：" + text);
                // 解析服务器消息
                mainHandler.post(() -> handleServerMessage(text));
            }
            
            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket 连接失败：" + t.getMessage());
                isConnected = false;
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "❌ 连接失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    addMessage(Message.TYPE_BOT, "⚠️ 连接服务器失败，请检查网络设置");
                });
            }
            
            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket 关闭：" + reason);
                isConnected = false;
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "连接已关闭", Toast.LENGTH_SHORT).show();
                });
            }
        };
        
        // 启动连接
        client.newWebSocket(request, listener);
    }
    
    /**
     * 处理服务器消息
     */
    private void handleServerMessage(String text) {
        try {
            // 尝试解析 JSON
            JSONObject json = new JSONObject(text);
            
            // 根据消息类型显示
            String content = json.optString("content", text);
            String role = json.optString("role", "bot");
            
            if ("assistant".equals(role) || "bot".equals(role)) {
                addMessage(Message.TYPE_BOT, content);
            } else {
                addMessage(Message.TYPE_BOT, text);
            }
        } catch (Exception e) {
            // 不是 JSON，直接显示
            addMessage(Message.TYPE_BOT, text);
        }
    }
    
    /**
     * 发送消息
     */
    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            // 显示用户消息（右边绿色气泡）
            addMessage(Message.TYPE_USER, message);
            
            // 清空输入框
            inputMessage.setText("");
            
            // 通过 WebSocket 发送到服务器
            if (isConnected && webSocket != null) {
                // 发送 JSON 格式消息
                try {
                    JSONObject json = new JSONObject();
                    json.put("type", "chat.send");
                    json.put("content", message);
                    webSocket.send(json.toString());
                    Log.d(TAG, "发送消息：" + json.toString());
                } catch (Exception e) {
                    // 发送纯文本
                    webSocket.send(message);
                    Log.d(TAG, "发送纯文本：" + message);
                }
            } else {
                // 未连接，显示提示
                addMessage(Message.TYPE_BOT, "⚠️ 未连接到服务器，请检查网络设置");
            }
        }
    }
    
    /**
     * 添加消息到列表
     */
    private void addMessage(int type, String text) {
        messageList.add(new Message(type, text));
        adapter.notifyItemInserted(messageList.size() - 1);
        // 滚动到底部
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }
    
    @Override
    protected void onDestroy() {
        // 关闭 WebSocket 连接
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
        super.onDestroy();
    }
}
