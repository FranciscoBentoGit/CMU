package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddUserFragment extends Fragment {

    private HttpCalls mHttpCalls;
    private String mChatName;
    private ChatMessageActivity messageActivity;

    public AddUserFragment(HttpCalls httpCalls, String chat_name, ChatMessageActivity act) {
        // Required empty public constructor
        this.mHttpCalls = httpCalls;
        this.mChatName = chat_name;
        this.messageActivity = act;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View res = inflater.inflate(R.layout.fragment_add_user, container, false);
        EditText username = (EditText) res.findViewById(R.id.user_search);
        mHttpCalls.listUsersPrivateChat(mChatName, messageActivity);

        Button add = (Button) res.findViewById(R.id.add_user);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mHttpCalls.addUserToPrivateChat(mChatName, username.getText().toString(), messageActivity);
            }
        });

        return res;
    }
}