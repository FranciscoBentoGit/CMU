package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.Context;
import android.graphics.Bitmap;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<JSONObject> chatList = new ArrayList<>();
    private ArrayList<JSONObject> messageList = new ArrayList<>();
    private HttpCalls mHttpCalls;
    private String chat_name;

    public ChatListAdapter(Context context, ArrayList<JSONObject> objects, HttpCalls httpCalls) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        chatList = objects;
        mHttpCalls = httpCalls;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatName;
        TextView chatLastMessage;

        ViewHolder(View itemView) {
            super(itemView);
            chatName = (TextView) itemView.findViewById(R.id.chatroomName);
            chatLastMessage = (TextView) itemView.findViewById(R.id.chatroomLastMessage);
        }
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_view_layout, parent, false);
        return new ChatListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        try {
            holder.chatName.setText(chatList.get(position).get("name").toString());

            messageList = handleMessages(chatList.get(position).get("messages").toString());
            String lastMessage = "No messages yet.";
            if (messageList.size() != 0)
                lastMessage = messageList.get(messageList.size()-1).get("message").toString();
            if (lastMessage.equals("null"))
                holder.chatLastMessage.setText(":photo:");
            else
                holder.chatLastMessage.setText(lastMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat_name = holder.chatName.getText().toString();
                createNewChatIntent(chat_name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void createNewChatIntent(String chat_name) {
        Intent intent = new Intent(mContext, ChatMessageActivity.class);
        intent.putExtra("chat_name", chat_name);
        intent.putExtra("httpCallsObject", mHttpCalls);

        mContext.startActivity(intent);
    }

    public ArrayList<JSONObject> handleMessages(String messages) throws JSONException {
        ArrayList<JSONObject> messageList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(messages);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            messageList.add(jsonObject);
        }
        return messageList;
    }

    public void resetChatName() {
        chat_name = null;
    }

    public String getChatName() {
        return chat_name;
    }
}
