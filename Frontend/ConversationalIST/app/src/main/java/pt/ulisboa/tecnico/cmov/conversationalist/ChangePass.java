package pt.ulisboa.tecnico.cmov.conversationalist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChangePass extends Fragment {

    public String Username;

    public ChangePass(String username) {
        Username = username;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_change_pass, container, false);
        TextView textView = (TextView) res.findViewById(R.id.Username);
        textView.setText(Username);
        return res;
    }
}