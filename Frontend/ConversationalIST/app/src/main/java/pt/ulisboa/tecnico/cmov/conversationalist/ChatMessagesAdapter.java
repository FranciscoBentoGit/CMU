package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ChatMessageActivity messageActivity;
    private ArrayList<JSONObject> messageList = new ArrayList<>();

    public ChatMessagesAdapter(Context context, ArrayList<JSONObject> objects, ChatMessageActivity act) {
        mInflater = LayoutInflater.from(context);
        messageList = objects;
        messageActivity = act;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sender;
        TextView message;
        ImageView image;
        TextView time;

        ViewHolder(View itemView) {
            super(itemView);
            sender = (TextView) itemView.findViewById(R.id.chatroomName);
            message = (TextView) itemView.findViewById(R.id.chatroomLastMessage);
            image = (ImageView) itemView.findViewById(R.id.openedImage);
            time = (TextView) itemView.findViewById(R.id.timestamp);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject json = new JSONObject(messageList.get(position).get("sender").toString());
            holder.sender.setText(json.get("username").toString());

            String message_obj = messageList.get(position).get("message").toString();
            if (!message_obj.equals("null"))
                holder.message.setText(message_obj);
            if (message_obj.equals("null")) {
                holder.message.setText("");
                holder.image.setImageBitmap(StringToBitMap(messageList.get(position).get("image").toString()));
            }

            String message_time = messageList.get(position).get("time").toString();
            holder.time.setText(message_time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
