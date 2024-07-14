package pt.ulisboa.tecnico.cmov.conversationalist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    private HttpCalls httpCalls; //= new HttpCalls();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle b = getIntent().getExtras();
        if(b != null)
            httpCalls = b.getParcelable("httpCallsObject");
    }

    public void createNewAccount(View view) {
        Intent intent = new Intent(this, CreateNewAccountActivity.class);
        intent.putExtra("httpCallsObject", httpCalls);
        startActivity(intent);
    }

    public void signIn(View view) {
        EditText username = (EditText)findViewById(R.id.inputUsername);
        EditText password = (EditText)findViewById(R.id.inputPassword);

        httpCalls.LoginUser(username.getText().toString(), password.getText().toString(), this, getApplicationContext(), this);
    }

    public void guests(View view) {
        EditText username = (EditText)findViewById(R.id.inputUsername);
        httpCalls.LoginGuest(username.getText().toString(), this, getApplicationContext(), this);
    }
}