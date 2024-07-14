package pt.ulisboa.tecnico.cmov.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.Activity;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import tech.gusavila92.websocketclient.WebSocketClient;

public class ChatMessageActivity extends AppCompatActivity {

    private HttpCalls httpCalls;
    private String chat_name;
    private String username;
    private WebSocketClient webSocketClient;
    private ChatMessageActivity activity = this;
    private MainActivity mainActivity;

    private ChatMessagesAdapter chat_adapter;
    private RecyclerView recyclerViewInsideChat;
    private ArrayList<JSONObject> chatInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            chat_name = b.getString("chat_name");
            httpCalls = b.getParcelable("httpCallsObject");
        }

        setTitle("Chat: " + chat_name);

        SharedPreferences sh = getSharedPreferences("LOGIN", MODE_PRIVATE);
        username = sh.getString("name", "");

        createWebSocketClient();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat, menu);

        try {
            Thread t = httpCalls.isChatRoomPublic(chat_name);
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MenuItem item = menu.findItem(R.id.share_private_link);
        if (httpCalls.getIsPublic().equals("true"))
            item.setVisible(false);
        return true;
    }

    public void chat_link_fragment(MenuItem item) throws InterruptedException {
        Thread t = httpCalls.getPrivateChatLink(chat_name);
        t.join();
        String processed = httpCalls.getPrivateChatLinkString(chat_name).replace(chat_name, "");
        String link = "http://tecnico.ulisboa.cmu.pt/" + chat_name + "/" + processed;
        getSupportFragmentManager().beginTransaction().replace(R.id.new_chatLinkFragment, new ChatLinkFragment(this, link, httpCalls, chat_name)).commit();
    }

    public void add_user_fragment(MenuItem item) throws InterruptedException {
        getSupportFragmentManager().beginTransaction().replace(R.id.new_chatLinkFragment, new AddUserFragment(httpCalls, chat_name, this)).commit();
    }

    public void closeChatLinkFragment(View v) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.new_chatLinkFragment);
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    public void leaveChat(MenuItem item) {
        httpCalls.leaveChat(chat_name, username);
        this.finish();
    }

    public void sendMessage(View view) {
        Log.e("WebSocket", "Button was clicked");

        SharedPreferences sh1 = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String user = sh1.getString("name", "");

        EditText message = (EditText)findViewById(R.id.sendMessage);
        ImageView image = (ImageView)findViewById(R.id.openedImage);
        String text = message.getText().toString();
        if (text.isEmpty())
            text = null;
        if (text == null && image == null)
            return;

        String toSend = "chatroom/" + chat_name + "/" + user + "/message/" + text + "/image/" + convertImageViewToString(image);

        webSocketClient.send(toSend);

        image.setImageDrawable(null);
        message.setText(null);
    }

    public String convertImageViewToString(ImageView imageView){
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable == null)
            return null;
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // In case you want to compress your image, here it's at 40%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void loadChat() {
        Log.i("WebSocket", "Session is starting");
        String toSend = "chatroom/" + chat_name;
        webSocketClient.send(toSend);
    }

    private void createWebSocketClient() {
        URI uri;
        try {
            String websocket = "ws://" + httpCalls.getIpAddress() + ":8080/websocket";
            uri = new URI(websocket);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                UpdateChatMessages("", activity, false);
                loadChat();
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateChatMessages(message, activity, true);
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
            }

            @Override
            public void onCloseReceived() {
                webSocketClient.close();
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.connect();
    }

    public void openImage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Toast.makeText(getApplicationContext(), "Take Photo", Toast.LENGTH_SHORT).show();
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        ((ImageView) findViewById(R.id.openedImage)).setImageBitmap(thumbnail);
        sendMessage(null);
    }

    public void UpdateChatMessages(String res, Activity activity, boolean isAppend){
        if (isAppend) {
            try {
                JSONArray jsonArray = new JSONArray(res);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                chatInfo.add(jsonObject);

                chat_adapter.notifyItemInserted(chatInfo.size());
                recyclerViewInsideChat.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerViewInsideChat.smoothScrollToPosition(chatInfo.size()-1);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            recyclerViewInsideChat = (RecyclerView) activity.findViewById(R.id.chatMessageList);
            if (recyclerViewInsideChat == null){
                return;
            }

            chatInfo = new ArrayList<>();

            LinearLayoutManager llm = new LinearLayoutManager(activity);
            llm.setStackFromEnd(true);
            recyclerViewInsideChat.setLayoutManager(llm);
            chat_adapter = new ChatMessagesAdapter(activity, chatInfo, this);
            recyclerViewInsideChat.setItemAnimator(new DefaultItemAnimator());
            recyclerViewInsideChat.setAdapter(chat_adapter);
        }

    }
}