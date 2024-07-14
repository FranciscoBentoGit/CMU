package pt.ulisboa.tecnico.cmov.conversationalist;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class HttpCalls implements Parcelable {

    private HttpCalls httpCallsObject = this;
    private String myUrl;
    private String IP;

    private static final int MAPS_ACTIVITY_REQUEST_CODE = 0;

    OkHttpClient client = new OkHttpClient();
    Thread t;
    String res = "";

    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static Random RANDOM = new Random();

    private HashMap<String, String> privateChatLink = new HashMap();
    private String isPublic;

    public HttpCalls(String url, String ip, String start, String isBoolean) {
        myUrl = url;
        IP = ip;
        res = start;
        isPublic = isBoolean;
    }

    protected HttpCalls(Parcel in) {
        myUrl = in.readString();
        IP = in.readString();
        res = in.readString();
        isPublic = in.readString();
    }

    public static final Creator<HttpCalls> CREATOR = new Creator<HttpCalls>() {
        @Override
        public HttpCalls createFromParcel(Parcel in) {
            return new HttpCalls(in);
        }

        @Override
        public HttpCalls[] newArray(int size) {
            return new HttpCalls[size];
        }
    };

    public String getIpAddress() {
        return IP;
    }

    public String getPrivateChatLinkString(String chatName) {
        return privateChatLink.get(chatName);
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void createUser(String name, String password, Activity createUserActivity, Context context) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "users/create?username=" + name + "&passwd=" + password)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                runToast(res, createUserActivity, context);
                if(res.split(":")[0].equals("Account created")){
                    runNewActivity(createUserActivity);
                }
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void LoginUser(String name, String password, Activity loginActivity, Context context, Activity currentAct) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "users/login?username=" + name + "&passwd=" + password)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                if (!res.equals("Successful login")) {
                    if (currentAct.equals(loginActivity))
                        runToast(res, loginActivity, context);
                    else {
                        requestUserLogin(currentAct);
                    }
                } else {
                    saveCredentials(name, password, currentAct, context);
                }
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void LoginGuest(String name, Activity loginActivity, Context context, Activity currentAct) {
        t = new Thread(() -> {
            String password = randomString(15);
            Request request = new Request.Builder()
                    .url(myUrl + "users/create?username=" + name + "&passwd=" + password)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                runToast(res, currentAct, context);
                if(res.split(":")[0].equals("Account created"))
                    saveCredentials( name, password, currentAct, context);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    //AUXILIARY FUNCTIONS
    public void runToast(String res, Activity activity, Context context) {
        String finalRes = res;
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                CharSequence text = finalRes;
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void runNewActivity(Activity activity) {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("httpCallsObject", httpCallsObject);
                activity.startActivity(intent);
                activity.finish();  // finish login activity to prevent coming back to login screen
            }
        });
    }

    public void runMapsAct(Activity currentAct, String username,String chatName, Context context, boolean isPublicToggleButtonState){
        currentAct.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Intent intent = new Intent(currentAct, MapsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("chatName", chatName);
                intent.putExtra("isPublicToggleButtonState", String.valueOf(isPublicToggleButtonState));

                currentAct.startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE);

            }
        });
    }


    public void requestUserLogin(Activity currentAct) {
        currentAct.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Intent intent = new Intent(currentAct, LoginActivity.class);
                intent.putExtra("httpCallsObject", httpCallsObject);
                currentAct.startActivity(intent);
                currentAct.finish();  // finish main activity
            }
        });
    }

    public Thread createNewChat(String username, String chatName, Context context, Activity currentAct, boolean isPublic, boolean isGeo, String latlng, Integer size) {
        t = new Thread(() -> {
            Request request;
            if(!isGeo){
                request = new Request.Builder()
                        .url(myUrl + "chatroom/create/" + chatName + "/" + isPublic + "/" + isGeo)
                        .build();
            }else{
                request = new Request.Builder()
                        .url(myUrl + "chatroom/create/" + chatName + "/" + isPublic + "/" + isGeo + "/" + latlng + "/" + size)
                        .build();
            }

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                runToast(res, currentAct, context);
                if (res.split(":")[0].equals("Chatroom created")) {
                    if (!isPublic) {
                        Thread t2 = getPrivateChatLink(chatName);
                        t2.join();
                        joinChat(username, chatName, context, currentAct, getPrivateChatLinkString(chatName), false);
                    }
                    else
                       joinChat(username, chatName, context, currentAct, null, false);
                    runNewActivity(currentAct);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
        return t;
    }

    public void joinChat(String userName, String chatName, Context context, Activity currentAct, String link, boolean newActivityNeeded) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + chatName + "/join?user=" + userName + "&link=" + link)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                runToast(res, currentAct, context);
                if (res.split(":")[0].equals("You successfully joined the chatroom") && newActivityNeeded)
                    runNewActivity(currentAct);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public Thread getPrivateChatLink(String chatName) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + chatName + "/link")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                privateChatLink.put(chatName, res.toString());
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
        return t;
    }

    public Thread isChatRoomPublic(String chatName) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + chatName + "/isPublic")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                isPublic = res.toString();
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
        return t;
    }

    public void addUserToPrivateChat(String chatName, String username, ChatMessageActivity messageActivity) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + chatName + "/add_user?user=" + username)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                if (res.startsWith("Added"))
                    listUsersPrivateChat(chatName, messageActivity);
                else
                    runToast(res, messageActivity, messageActivity);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void removeUserFromPrivateChat(String chatName, String username, ChatMessageActivity messageActivity) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + chatName + "/remove_user?user=" + username)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                if (res.startsWith("Removed"))
                    listUsersPrivateChat(chatName, messageActivity);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void listUsersPrivateChat(String chatName, ChatMessageActivity activity) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/users/" + chatName)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        RecyclerView recyclerViewUsersList = (RecyclerView) activity.findViewById(R.id.usersList);
                        if (recyclerViewUsersList == null) {
                            return;
                        }
                        String result = res.substring(1, res.length() - 1);
                        ArrayList usersList = new ArrayList<String>(Arrays.asList(result.split(",")));

                        LinearLayoutManager llm = new LinearLayoutManager(activity);
                        recyclerViewUsersList.setLayoutManager(llm);
                        UsersListAdapter users_adapter = new UsersListAdapter(activity, usersList, httpCallsObject, activity, chatName);
                        recyclerViewUsersList.setItemAnimator(new DefaultItemAnimator());
                        recyclerViewUsersList.setAdapter(users_adapter);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public Thread leaveChat(String chatName, String username) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "chatroom/" + chatName + "/leave/" + username)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                //runToast(res, currentAct, context);
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
        return t;
    }

    public void changePass(String name, String password, Activity currentAct, Context context) {
        t = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(myUrl + "users/upgrade?username=" + name + "&passwd=" + password)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                res = response.peekBody(2048).string();
                runToast(res, currentAct, context);
                if(res.split(":")[0].equals("Successful modified")){
                    runNewActivity(currentAct);
                }
            } catch (IOException e) {
                e.printStackTrace();
                t.interrupt();
            }
        });
        t.start();
    }

    public void saveCredentials(String name, String password, Activity currentAct, Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("LOGIN", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("name", name);
        myEdit.putString("password", password);
        myEdit.apply();

        if (currentAct.getClass().getSimpleName().equals("LoginActivity")) {
            runNewActivity(currentAct);
        }
    }

    public void removeCredentials(String name, Activity loginActivity, Context context, Activity currentAct){
        if (currentAct.equals(loginActivity))
            runToast(res, currentAct, context);

        SharedPreferences sharedPreferences = context.getSharedPreferences("LOGIN", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("name", "");
        myEdit.putString("password", "");
        myEdit.apply();

        runNewActivity(currentAct);
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(myUrl);
        dest.writeString(IP);
        dest.writeString(res);
        dest.writeString(isPublic);
    }
}
