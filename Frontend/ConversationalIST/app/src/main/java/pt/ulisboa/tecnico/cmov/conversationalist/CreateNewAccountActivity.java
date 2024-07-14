package pt.ulisboa.tecnico.cmov.conversationalist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreateNewAccountActivity extends AppCompatActivity {

    //HttpCalls httpCalls = new HttpCalls();
    private HttpCalls httpCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);

        Bundle b = getIntent().getExtras();
        if(b != null)
            httpCalls = b.getParcelable("httpCallsObject");
    }

    public void signUp(View view) {
        EditText username = (EditText)findViewById(R.id.inputUsername);
        EditText password = (EditText)findViewById(R.id.inputPassword);
        EditText confirmPassword = (EditText)findViewById(R.id.inputConfirmPassword);

        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Log.d("error", "passwords mismatch");
            CharSequence text = "Error: passwords mismatch";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
        else {
            httpCalls.createUser(username.getText().toString(), password.getText().toString(), this, getApplicationContext());
        }
    }
}