package pt.ulisboa.tecnico.cmov.conversationalist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchChatFragment extends Fragment {

    private EditText inputSearch;
    private SearchChatListAdapter mAdapter;
    private ArrayList<JSONObject> mChatList;

    public SearchChatFragment(SearchChatListAdapter adapter, ArrayList<JSONObject> chatListSearch) {
        this.mAdapter = adapter;
        this.mChatList = chatListSearch;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View res = inflater.inflate(R.layout.fragment_search_chat_fragment, container, false);
        inputSearch = (EditText) res.findViewById(R.id.search_by_chat_name);

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    filter(s.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return res;
    }

    private void filter(String text) throws JSONException {
        ArrayList<JSONObject> filteredList = new ArrayList<>();

        if (!mChatList.isEmpty() && mAdapter != null) {
            for (JSONObject item : mChatList) {
                if (item.getString("name").toLowerCase().contains(text.toLowerCase()))
                    filteredList.add(item);
            }
            mAdapter.filterList(filteredList, mAdapter);
        }
    }
}