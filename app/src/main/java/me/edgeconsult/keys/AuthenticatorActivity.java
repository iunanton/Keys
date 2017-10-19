package me.edgeconsult.keys;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TextInputLayout;
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
 * Created by yun on 10/13/17.
 */

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private static final String AUTHENTICATOR_ACTIVITY_TAG = AuthenticatorActivity.class.getSimpleName();
    private static final int REQUEST_SIGNUP = 0;

    private AccountManager accountManager = null;

    private TextInputLayout guestUsernameLayout;
    private TextInputLayout guestCodeLayout;
    private EditText guestUsername;
    private EditText guestCode;
    private Button guestSubmitButton;

    private TextInputLayout loginUsernameLayout;
    private TextInputLayout loginPasswordLayout;
    private EditText loginUsername;
    private EditText loginPassword;
    private Button loginSubmitButton;

    private TextView signup;

    final String targetURL = "https://owncloudhk.net/oauth";

    private WebSocketService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(ServiceConnection.class.getSimpleName(), "onServiceConnected");
            mBoundService = ((WebSocketService.MyBinder)service).getService();
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_authenticator);
        accountManager = AccountManager.get(this);
        guestUsernameLayout = findViewById(R.id.guest_username_layout);
        guestCodeLayout = findViewById(R.id.guest_code_layout);
        guestUsername = findViewById(R.id.guest_username);
        guestCode = findViewById(R.id.guest_code);
        guestCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    guestSubmitButton.performClick();
                    return true;
                }
                return false;
            }
        });
        guestSubmitButton = findViewById(R.id.guest_submit);
        guestSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guestSubmit();
            }
        });
        loginUsernameLayout = findViewById(R.id.login_username_layout);
        loginPasswordLayout = findViewById(R.id.login_password_layout);
        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    loginSubmitButton.performClick();
                    return true;
                }
                return false;
            }
        });
        loginSubmitButton = findViewById(R.id.login_submit);
        loginSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginSubmit();
            }
        });

        signup = findViewById(R.id.link_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                //startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        bindService(new Intent(this, WebSocketService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private void guestSubmit() {
        if (!validateGuest()) {
            // onLoginFailed();
            return;
        }

        new AsyncTask<Void, Void, Intent>() {

            private String username;
            private String code;

            @Override
            protected void onPreExecute() {
                guestUsernameLayout.setEnabled(false);
                guestCodeLayout.setEnabled(false);
                guestSubmitButton.setEnabled(false);

                username = guestUsername.getText().toString();
                code = guestCode.getText().toString();
            }

            @Override
            protected Intent doInBackground(Void... voids) {
                HttpsURLConnection httpsURLConnection = null;
                try {
                    URL url = new URL(targetURL);
                    String urlParameters = "grant_type=temporary&username="
                            + URLEncoder.encode(username, "UTF-8") + "&code="
                            + URLEncoder.encode(code, "UTF-8");
                    Log.i(AUTHENTICATOR_ACTIVITY_TAG, urlParameters);
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
                            Toast.makeText(AuthenticatorActivity.this, "JSON could not be parsed", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuthenticatorActivity.this, "URL could not be parsed", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (UnknownHostException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuthenticatorActivity.this, "Unknown host", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuthenticatorActivity.this, "Invalid grant", Toast.LENGTH_LONG).show();
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
                guestUsernameLayout.setEnabled(true);
                guestCodeLayout.setEnabled(true);
                guestSubmitButton.setEnabled(true);
                if (intent != null) {
                    if (mIsBound) {
                        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
                        Log.i(AUTHENTICATOR_ACTIVITY_TAG, authtoken);
                        mBoundService.connect(authtoken);
                        setResult(RESULT_OK);
                        finish();
                        startActivity(new Intent(AuthenticatorActivity.this, MainActivity.class));
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AuthenticatorActivity.this, "Error: service was not binded", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }.execute();
    }

    private void loginSubmit() {
        if (!validateLogin()) {
            // onLoginFailed();
            return;
        }

        new AsyncTask<Void, Void, Intent>() {

            private String username;
            private String password;

            @Override
            protected void onPreExecute() {
                loginUsernameLayout.setEnabled(false);
                loginPasswordLayout.setEnabled(false);
                loginSubmitButton.setEnabled(false);

                username = loginUsername.getText().toString();
                password = loginPassword.getText().toString();
            }

            @Override
            protected Intent doInBackground(Void... voids) {
                HttpsURLConnection httpsURLConnection = null;
                try {
                    URL url = new URL(targetURL);
                    String urlParameters = "grant_type=password&username="
                            + URLEncoder.encode(username, "UTF-8") + "&password="
                            + URLEncoder.encode(password, "UTF-8");
                    Log.i(AUTHENTICATOR_ACTIVITY_TAG, urlParameters);
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
                            Toast.makeText(AuthenticatorActivity.this, "JSON could not be parsed", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuthenticatorActivity.this, "URL could not be parsed", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (UnknownHostException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuthenticatorActivity.this, "Unknown host", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuthenticatorActivity.this, "Invalid grant", Toast.LENGTH_LONG).show();
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
                loginUsernameLayout.setEnabled(true);
                loginPasswordLayout.setEnabled(true);
                loginSubmitButton.setEnabled(true);
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
                    startActivity(new Intent(AuthenticatorActivity.this, MainActivity.class));
                }
            }
        }.execute();
    }

    public boolean validateGuest() {
        boolean valid = true;

        String username = guestUsername.getText().toString();
        String code = guestCode.getText().toString();

        if (username.isEmpty()) {
            guestUsername.setError(getString(R.string.error_field_required));
        } else if (username.length() < 4) {
            guestUsername.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else {
            guestUsername.setError(null);
        }

        if (code.isEmpty()) {
            guestCode.setError(getString(R.string.error_field_required));
        } else if (code.length() < 4) {
            guestCode.setError(getString(R.string.error_invalid_code));
            valid = false;
        } else {
            guestCode.setError(null);
        }

        return valid;
    }

    public boolean validateLogin() {
        boolean valid = true;

        String username = loginUsername.getText().toString();
        String password = loginPassword.getText().toString();

        if (username.isEmpty()) {
            loginUsername.setError(getString(R.string.error_field_required));
        } else if (username.length() < 4) {
            loginUsername.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else {
            loginUsername.setError(null);
        }

        if (password.isEmpty()) {
            loginPassword.setError(getString(R.string.error_field_required));
        } else if (password.length() < 4) {
            loginPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            loginPassword.setError(null);
        }

        return valid;
    }
}
