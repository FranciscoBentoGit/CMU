package pt.ulisboa.tecnico.cmov.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String myUrl = "http://192.168.1.143:8080/";
    private HttpCalls httpCalls = new HttpCalls(myUrl, "192.168.1.143", "", "false");
    private ArrayList<JSONObject> chatList = new ArrayList<>();

    private SharedPreferences sh1;
    private String username;
    private String password;
    private boolean isNightMode;

    private GpsTracker gpsTracker;

    private MainActivity act;
    private Activity loginActivity;

    private String privateChatLink = null;
    private int testNotifCount = 0;
    private Handler handler = null;
    private boolean onPause = false;
    private String loadedChat;
    private String chatPin;
    //private boolean userInChat;

    private Thread t;
    OkHttpClient client = new OkHttpClient();
    private String res = "";

    private RecyclerView recyclerViewChatList;
    private ChatListAdapter adapter;
    private SearchChatListAdapter search_adapter;
    private ArrayList<JSONObject> chatListSearch = new ArrayList<>();

    private SharedPreferences chatListCache;

    private HashMap<String, String> chatAndLastMessage = new HashMap<>();
    private HashMap<String, String> oldChatAndLastMessage = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        act = this;
        sh1 = getSharedPreferences("LOGIN", MODE_PRIVATE);

        username = sh1.getString("name", "");
        password = sh1.getString("password", "");
        isNightMode = sh1.getBoolean("DarkMode", false);

        /*if (isNightMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);*/

        httpCalls.LoginUser(username, password, loginActivity, getApplicationContext(), this);

        Uri data = getIntent().getData();
        if (data != null) {
            String privateChatName = data.toString().split("/")[3];
            privateChatLink = privateChatName + data.toString().split("/")[4];
            httpCalls.joinChat(username, privateChatName, getApplicationContext(), this, privateChatLink, true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsTracker = new GpsTracker(MainActivity.this);

        ChatRoomListByUser(username);

        //polling in every 30 seconds
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ChatRoomListByUser(username);
                if (handler != null)
                        handler.postDelayed(this, 30000);
                }
            }, 30000);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                String returnString = data.getStringExtra(Intent.EXTRA_TEXT);
                Thread t = httpCalls.createNewChat(username, data.getStringExtra("chatName"), getApplicationContext(), this,Boolean.parseBoolean(data.getStringExtra("isPublicToggleButtonState")), true, returnString, 100);
                try {
                    t.join(); //waits till thread terminates
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onPause = false;
        if (adapter != null)
            adapter.resetChatName();
        ChatRoomListByUser(username);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPause = true;
        String chat = null;
        if (adapter != null) {
            chat = adapter.getChatName();
        }
        loadedChat = chat;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    public void new_chat_fragment(MenuItem item) {
        getSupportFragmentManager().beginTransaction().replace(R.id.new_chatFL, new NewChatFragment()).commit();
    }

    public void search_chat_fragment(MenuItem item) throws InterruptedException {
        ChatRoomPublicList(username);
        getSupportFragmentManager().beginTransaction().replace(R.id.new_chatFL, new SearchChatFragment(search_adapter, chatListSearch)).commit();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public void createChat(View v) throws InterruptedException {
        EditText chatName = (EditText)findViewById(R.id.chat_name);
        if (chatName.getText().toString().isEmpty()) {
            Toast.makeText(this, "invalid chat name", Toast.LENGTH_SHORT).show();
            return;
        }

        ToggleButton isPublicToggleButton = (ToggleButton) findViewById(R.id.isPublicToggleButton); // initiate a toggle button
        Boolean isPublicToggleButtonState = isPublicToggleButton.isChecked();
        ToggleButton isGeoToggleButton = (ToggleButton) findViewById(R.id.isGeoToggleButton); // initiate a toggle button
        Boolean isGeoToggleButtonState = isGeoToggleButton.isChecked();
        if (isGeoToggleButtonState){
            httpCalls.runMapsAct(this, username, chatName.getText().toString(), getApplicationContext(), isPublicToggleButtonState);
            return;
        }
        Thread t = httpCalls.createNewChat(username, chatName.getText().toString(), getApplicationContext(), this, isPublicToggleButtonState, false,null, null);
        t.join(); //waits till thread terminates

    }

    public void closeFragment(View v) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.new_chatFL);
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    public void upgradeAccountFragment(MenuItem item) {
        getSupportFragmentManager().beginTransaction().replace(R.id.new_chatFL, new ChangePass(username)).commit();
    }

    public void upgradeAccount(View v) {
        EditText password = (EditText)findViewById(R.id.inputPassword);
        EditText confirmPassword = (EditText)findViewById(R.id.inputConfirmPassword);

        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Log.d("error", "passwords mismatch");
            CharSequence text = "Error: passwords mismatch";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
        else {
            httpCalls.changePass(username, password.getText().toString(), this, getApplicationContext());
            httpCalls.saveCredentials(username, password.getText().toString(), this, getApplicationContext());
        }
    }


    public String returnLocation() {
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            return "" + latitude + "," + longitude;
        }else{
            gpsTracker.showSettingsAlert();
        }
        return null;
    }

    public void goDarkMode(MenuItem item) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.new_chatFL);
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        sh1.edit().putBoolean("DarkMode", true).apply();
    }

    public void goLightMode(MenuItem item) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.new_chatFL);
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        sh1.edit().putBoolean("DarkMode", false).apply();
    }

    public void logoutUser(MenuItem item) {
        httpCalls.removeCredentials(username, loginActivity, getApplicationContext(), this);
    }

    public void ChatRoomListByUser(String name) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + name + "/location/" + returnLocation())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                UpdateChats(res, this);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void UpdateChats(String res, Activity activity){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                recyclerViewChatList = (RecyclerView) activity.findViewById(R.id.chatList);
                if (recyclerViewChatList == null) {
                    return;
                }

                chatList = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        chatList.add(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                LinearLayoutManager llm = new LinearLayoutManager(activity);
                recyclerViewChatList.setLayoutManager(llm);
                adapter = new ChatListAdapter(activity, chatList, httpCalls);
                recyclerViewChatList.setItemAnimator(new DefaultItemAnimator());
                recyclerViewChatList.setAdapter(adapter);
            }
        });
    }

    public void ChatRoomPublicList(String username) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/public")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                UpdateSearchChats(res, this, this, username);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void UpdateSearchChats(String res, Activity activity, MainActivity mainAct, String username) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                RecyclerView recyclerViewSearch = (RecyclerView) activity.findViewById(R.id.searchChatList);
                if (recyclerViewSearch == null) {
                    return;
                }

                chatListSearch = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        chatListSearch.add(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                LinearLayoutManager llm = new LinearLayoutManager(activity);
                recyclerViewSearch.setLayoutManager(llm);

                search_adapter = new SearchChatListAdapter(activity, chatListSearch, username, httpCalls, mainAct, recyclerViewSearch);
                recyclerViewSearch.setItemAnimator(new DefaultItemAnimator());
                recyclerViewSearch.setAdapter(search_adapter);
            }
        });
    }

    public void RefreshSearchChats(ArrayList<JSONObject> res, Activity activity, MainActivity mainAct, String username) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                RecyclerView recyclerViewSearch = (RecyclerView) activity.findViewById(R.id.searchChatList);
                if (recyclerViewSearch == null) {
                    return;
                }

                LinearLayoutManager llm = new LinearLayoutManager(activity);
                recyclerViewSearch.setLayoutManager(llm);

                SearchChatListAdapter refresh_search_adapter = new SearchChatListAdapter(activity, res, username, httpCalls, mainAct, recyclerViewSearch);
                recyclerViewSearch.setItemAnimator(new DefaultItemAnimator());
                recyclerViewSearch.setAdapter(refresh_search_adapter);
            }
        });
    }
}