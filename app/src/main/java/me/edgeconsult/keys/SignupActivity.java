package me.edgeconsult.keys;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by yun on 10/19/17.
 */

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout registrationUsernameLayout;
    private TextInputLayout registrationPasswordLayout;
    private TextInputLayout registrationCodeLayout;
    private EditText registrationUsername;
    private EditText registrationCode;
    private EditText registrationPassword;
    private Button registrationSubmitButton;

    private TextView login;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        registrationUsernameLayout = (TextInputLayout) findViewById(R.id.registration_username_layout);
        registrationCodeLayout = (TextInputLayout) findViewById(R.id.registration_code_layout);
        registrationPasswordLayout = (TextInputLayout) findViewById(R.id.registration_password_layout);
        registrationUsername = (EditText) findViewById(R.id.registration_username);
        registrationCode = (EditText) findViewById(R.id.registration_code);
        registrationPassword = (EditText) findViewById(R.id.registration_password);
        registrationPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    registrationSubmitButton.performClick();
                    return true;
                }
                return false;
            }
        });
        registrationSubmitButton = (Button) findViewById(R.id.registration_submit);
        registrationSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //registrationSubmit();
            }
        });

        login = (TextView) findViewById(R.id.link_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
