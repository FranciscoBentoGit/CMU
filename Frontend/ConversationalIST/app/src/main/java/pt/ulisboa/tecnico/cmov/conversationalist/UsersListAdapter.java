package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder>{

    private LayoutInflater mInflater;
    private Context mContext;
    private HttpCalls mHttpCalls;
    private ChatMessageActivity messageActivity;
    private ArrayList<String> usersList = new ArrayList<>();
    private String mChatName;

    public UsersListAdapter(Context context, ArrayList<String> objects, HttpCalls httpCalls, ChatMessageActivity act, String chat_name) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mHttpCalls = httpCalls;
        usersList = objects;
        messageActivity = act;
        mChatName = chat_name;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user;
        ViewHolder(View itemView) {
            super(itemView);
            user = (TextView) itemView.findViewById(R.id.searchUserName);
        }
    }

    @NonNull
    @Override
    public UsersListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_user_list_view_layout, parent, false);
        return new UsersListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersListAdapter.ViewHolder holder, int position) {
        String res = usersList.get(position);
        if (!res.equals("")) {
            String result = res.substring(1, res.length() - 1);
            holder.user.setText(result);
        } else
            messageActivity.finish();

        Button rm = (Button) holder.itemView.findViewById(R.id.removeUserFromList);
        rm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView user = (TextView) holder.itemView.findViewById(R.id.searchUserName);
                mHttpCalls.removeUserFromPrivateChat(mChatName, user.getText().toString(), messageActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
}
