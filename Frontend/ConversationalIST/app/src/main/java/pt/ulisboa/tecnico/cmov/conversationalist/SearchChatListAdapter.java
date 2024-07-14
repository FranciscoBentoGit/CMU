package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchChatListAdapter extends RecyclerView.Adapter<SearchChatListAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;
    private String mUsername;
    private HttpCalls mHttpCalls;
    private MainActivity mActivity;
    private RecyclerView mRecycler;
    private ArrayList<JSONObject> chatList = new ArrayList<>();

    public SearchChatListAdapter(Context context, ArrayList<JSONObject> objects, String username, HttpCalls httpCalls, MainActivity activity, RecyclerView recyclerViewSearch) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        chatList = objects;
        mUsername = username;
        mHttpCalls = httpCalls;
        mActivity = activity;
        mRecycler = recyclerViewSearch;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatName;

        ViewHolder(View itemView) {
            super(itemView);
            chatName = (TextView) itemView.findViewById(R.id.searchChatroomName);
        }
    }

    @NonNull
    @Override
    public SearchChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_search_view_layout, parent, false);
        return new SearchChatListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchChatListAdapter.ViewHolder holder, int position) {
        try {
            holder.chatName.setText(chatList.get(position).get("name").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button button1 = (Button) holder.itemView.findViewById(R.id.joinChat);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mHttpCalls.joinChat(mUsername, holder.chatName.getText().toString(), mContext, mActivity, null, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void filterList(ArrayList<JSONObject> filteredList, SearchChatListAdapter adapter) {
        chatList.clear();
        chatList = filteredList;
        mActivity.RefreshSearchChats(chatList, mActivity, mActivity, mUsername);
    }

}
