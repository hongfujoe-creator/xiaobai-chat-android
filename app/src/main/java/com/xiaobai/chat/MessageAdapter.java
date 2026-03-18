package com.xiaobai.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 消息列表适配器
 * 用于 RecyclerView 显示聊天气泡
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    
    // 消息列表
    private List<Message> messages;
    
    /**
     * 构造函数
     * @param messages 消息列表
     */
    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }
    
    /**
     * 创建 ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载布局文件
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }
    
    /**
     * 绑定数据到 ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        // 根据消息类型显示不同的布局
        if (message.getType() == Message.TYPE_USER) {
            // 用户消息：显示右边绿色气泡
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.botLayout.setVisibility(View.GONE);
            holder.userText.setText(message.getText());
        } else {
            // 机器人消息：显示左边白色气泡
            holder.userLayout.setVisibility(View.GONE);
            holder.botLayout.setVisibility(View.VISIBLE);
            holder.botText.setText(message.getText());
        }
    }
    
    /**
     * 获取消息数量
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    /**
     * ViewHolder 内部类
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        // 用户消息布局
        LinearLayout userLayout;
        // 机器人消息布局
        LinearLayout botLayout;
        // 用户头像
        ImageView userAvatar;
        // 机器人头像
        ImageView botAvatar;
        // 用户消息文本
        TextView userText;
        // 机器人消息文本
        TextView botText;
        
        ViewHolder(View itemView) {
            super(itemView);
            // 绑定视图组件
            userLayout = itemView.findViewById(R.id.userMessageLayout);
            botLayout = itemView.findViewById(R.id.botMessageLayout);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            botAvatar = itemView.findViewById(R.id.botAvatar);
            userText = itemView.findViewById(R.id.userMessageText);
            botText = itemView.findViewById(R.id.botMessageText);
        }
    }
}
