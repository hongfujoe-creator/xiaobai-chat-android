package com.xiaobai.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

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
    private static final String PREFS_NAME = "xiaobai_prefs";
    private static final String KEY_WEBSOCKET_URL = "websocket_url";
    
    // 默认 WebSocket 地址
    private static final String DEFAULT_WEBSOCKET_URL = "ws://192.168.0.112:18789";
    
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
    
    // 连接状态文本
    private TextView statusText;
    
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
        statusText = findViewById(R.id.statusText);
        
        // 初始化消息列表
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        
        // 配置 RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
        
        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());
        
        // 显示设置对话框
        showSettingsDialog();
    }
    
    /**
     * 显示设置对话框
     */
    private void showSettingsDialog() {
        // 获取保存的地址
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = prefs.getString(KEY_WEBSOCKET_URL, DEFAULT_WEBSOCKET_URL);
        
        // 创建输入框
        EditText input = new EditText(this);
        input.setText(savedUrl);
        input.setHint("例如：ws://192.168.0.112:18789");
        input.setPadding(50, 30, 50, 30);
        
        // 创建对话框
        new AlertDialog.Builder(this)
            .setTitle("⚙️ 服务器设置")
            .setMessage("请输入 OpenClaw WebSocket 地址：\n\n• 本机：ws://127.0.0.1:18789\n• 局域网：ws://192.168.x.x:18789")
            .setView(input)
            .setPositiveButton("连接", (dialog, which) -> {
                String url = input.getText().toString().trim();
                if (!url.isEmpty()) {
                    // 保存地址
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_WEBSOCKET_URL, url);
                    editor.apply();
                    
                    // 连接服务器
                    connectWebSocket(url);
                } else {
                    Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
                    showSettingsDialog();
                }
            })
            .setNegativeButton("取消", (dialog, which) -> {
                addMessage(Message.TYPE_BOT, "⚠️ 未设置服务器地址，无法聊天");
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 连接 WebSocket 服务器
     */
    private void connectWebSocket(String url) {
        // 更新状态
        updateStatus("连接中...");
        
        // 配置 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        // 创建 WebSocket 监听器
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket 连接成功");
                webSocket = ws;
                isConnected = true;
                mainHandler.post(() -> {
                    updateStatus("已连接");
                    Toast.makeText(MainActivity.this, "✅ 已连接到服务器", Toast.LENGTH_SHORT).show();
                    addMessage(Message.TYPE_BOT, "🤖 小白：已连接！有什么可以帮你的吗？");
                });
            }
            
            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "收到消息：" + text);
                mainHandler.post(() -> handleServerMessage(text));
            }
            
            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket 连接失败：" + t.getMessage());
                isConnected = false;
                mainHandler.post(() -> {
                    updateStatus("连接失败");
                    Toast.makeText(MainActivity.this, "❌ 连接失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    addMessage(Message.TYPE_BOT, "⚠️ 连接服务器失败，请检查：\n1. OpenClaw Gateway 是否运行\n2. 地址是否正确\n3. 防火墙是否阻止");
                });
            }
            
            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket 关闭：" + reason);
                isConnected = false;
                mainHandler.post(() -> {
                    updateStatus("已断开");
                });
            }
        };
        
        // 启动连接
        client.newWebSocket(request, listener);
    }
    
    /**
     * 更新连接状态
     */
    private void updateStatus(String status) {
        if (statusText != null) {
            statusText.setText("状态：" + status);
        }
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
                addMessage(Message.TYPE_BOT, "⚠️ 未连接到服务器，点击右上角重新设置");
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
