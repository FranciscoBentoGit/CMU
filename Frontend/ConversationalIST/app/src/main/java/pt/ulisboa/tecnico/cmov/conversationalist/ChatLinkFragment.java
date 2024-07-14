package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ChatLinkFragment extends Fragment {

    private ChatMessageActivity activity;
    private String link;
    private HttpCalls httpCalls;
    private String chatName;

    public ChatLinkFragment(ChatMessageActivity activity, String link, HttpCalls httpCalls, String chat_name) {
        this.activity = activity;
        this.link = link;
        this.httpCalls = httpCalls;
        this.chatName = chat_name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_chat_link, container, false);
        TextView chat_link = (TextView) res.findViewById(R.id.chat_link);
        chat_link.setText(link);

        /*Button copy = (Button) res.findViewById(R.id.copy_chat_link);
        copy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newHtmlText("LINK", link, link);
                clipboard.setPrimaryClip(clip);
            }
        });*/

        Button share = (Button) res.findViewById(R.id.new_intent_chat);
        share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });
        return res;
    }
}