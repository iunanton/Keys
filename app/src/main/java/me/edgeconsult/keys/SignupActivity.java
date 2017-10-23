package me.edgeconsult.keys;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by yun on 10/19/17.
 */

public class SignupActivity extends AccountAuthenticatorActivity {

    private static final String SIGNUP_ACTIVITY_TAG = SignupActivity.class.getSimpleName();

    private AccountManager accountManager = null;

    private TextInputLayout registrationUsernameLayout;
    private TextInputLayout registrationPasswordLayout;
    private TextInputLayout registrationCodeLayout;
    private EditText registrationUsername;
    private EditText registrationCode;
    private EditText registrationPassword;
    private Button registrationSubmitButton;

    private TextView login;

    final String targetURL = "https://owncloudhk.net/oauth";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        accountManager = AccountManager.get(this);
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
                registrationSubmit();
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

    void registrationSubmit() {
        if (!validateRegistration()) {
            // onLoginFailed();
            return;
        }

        new AsyncTask<Void, Void, Intent>() {

            private String username;
            private String code;
            private String password;

            @Override
            protected void onPreExecute() {
                registrationUsernameLayout.setEnabled(false);
                registrationCodeLayout.setEnabled(false);
                registrationPasswordLayout.setEnabled(false);
                registrationSubmitButton.setEnabled(false);

                username = registrationUsername.getText().toString();
                code = registrationCode.getText().toString();
                password = registrationPassword.getText().toString();
            }

            @Override
            protected Intent doInBackground(Void... voids) {
                HttpsURLConnection httpsURLConnection = null;
                try {
                    URL url = new URL(targetURL);
                    String urlParameters = "grant_type=permanent&username="
                            + URLEncoder.encode(username, "UTF-8") + "&code="
                            + URLEncoder.encode(code, "UTF-8") + "&password="
                            + URLEncoder.encode(password, "UTF-8");
                    Log.i(SIGNUP_ACTIVITY_TAG, urlParameters);
                    httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.setRequestMethod("POST");
                    httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:55.0) Gecko/20100101 Firefox/55.0");
                    httpsURLConnection.setRequestProperty("Accept", "*/*");
                    httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpsURLConnection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                    httpsURLConnection.setUseCaches(false);
                    httpsURLConnection.setDoInput(true);
                    httpsURLConnection.setDoOutput(true);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpsURLConnection.getOutputStream()));
                    bufferedWriter.write(urlParameters);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    JSONObject json = new JSONObject(stringBuilder.toString());
                    if (json.has("access_token")) {
                        final Intent i = new Intent();
                        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.account_type));
                        i.putExtra(AccountManager.KEY_AUTHTOKEN, json.getString("access_token"));
                        return i;
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this, "JSON could not be parsed", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this, "URL could not be parsed", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (UnknownHostException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this, "Unknown host", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this, "Invalid grant", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(httpsURLConnection != null) {
                        httpsURLConnection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                registrationUsernameLayout.setEnabled(true);
                registrationCodeLayout.setEnabled(true);
                registrationPasswordLayout.setEnabled(true);
                registrationSubmitButton.setEnabled(true);
                if (intent != null) {
                    String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
                    String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
                    String authtokenType = getString(R.string.auth_token_type);
                    accountManager.addAccountExplicitly(account, null, null);
                    accountManager.setAuthToken(account, authtokenType, authtoken);
                    setAccountAuthenticatorResult(intent.getExtras());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }.execute();
    }

    public boolean validateRegistration() {
        boolean valid = true;

        String username = registrationUsername.getText().toString();
        String code = registrationCode.getText().toString();
        String password = registrationPassword.getText().toString();

        if (username.isEmpty()) {
            registrationUsername.setError(getString(R.string.error_field_required));
        } else if (username.length() < 4) {
            registrationUsername.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else {
            registrationUsername.setError(null);
        }

        if (code.isEmpty()) {
            registrationCode.setError(getString(R.string.error_field_required));
        } else if (code.length() < 4) {
            registrationCode.setError(getString(R.string.error_invalid_code));
            valid = false;
        } else {
            registrationCode.setError(null);
        }

        if (password.isEmpty()) {
            registrationPassword.setError(getString(R.string.error_field_required));
        } else if (password.length() < 4) {
            registrationPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            registrationPassword.setError(null);
        }

        return valid;
    }
}
