package me.edgeconsult.keys;

import android.accounts.AccountAuthenticatorActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    private TextInputLayout guestUsernameLayout;
    private TextInputLayout guestCodeLayout;
    private EditText guestUsername;
    private EditText guestCode;
    private Button guestSubmitButton;

    private TextInputLayout loginUsernameLayout;
    private TextInputLayout loginPasswordLayout;
    private EditText loginUsername;
    private EditText loginPasssword;
    private Button loginSubmitButton;

    final String targetURL = "https://owncloudhk.net/oauth";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_authenticator);
        guestUsernameLayout = findViewById(R.id.guest_username_layout);
        guestCodeLayout = findViewById(R.id.guest_code_layout);
        guestUsername = findViewById(R.id.guest_username);
        guestCode = findViewById(R.id.guest_code);
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
        loginPasssword = findViewById(R.id.login_password);
        loginSubmitButton = findViewById(R.id.login_submit);
        loginSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginSubmit();
            }
        });
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
                    String urlParameters = "grant_type=guest&username="
                            + URLEncoder.encode(username, "UTF-8") + "&code="
                            + URLEncoder.encode(code, "UTF-8");
                    Log.i("guestSubmit", urlParameters);
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
                    Log.i("guestSubmit", stringBuilder.toString());
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

                }
                return null;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                guestUsernameLayout.setEnabled(true);
                guestCodeLayout.setEnabled(true);
                guestSubmitButton.setEnabled(true);
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
                password = loginPasssword.getText().toString();
            }

            @Override
            protected Intent doInBackground(Void... voids) {
                HttpsURLConnection httpsURLConnection = null;
                try {
                    URL url = new URL(targetURL);
                    String urlParameters = "grant_type=password&username="
                            + URLEncoder.encode(username, "UTF-8") + "&password="
                            + URLEncoder.encode(password, "UTF-8");
                    Log.i("guestSubmit", urlParameters);
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
                    Log.i("loginSubmit", stringBuilder.toString());
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

                }
                return null;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                loginUsernameLayout.setEnabled(true);
                loginPasswordLayout.setEnabled(true);
                loginSubmitButton.setEnabled(true);
            }
        }.execute();
    }

    public boolean validateGuest() {
        boolean valid = true;

        String username = guestUsername.getText().toString();
        String code = guestCode.getText().toString();

        if (username.isEmpty() || username.length() < 4) {
            guestUsername.setError("more than 4 alphanumeric characters");
            valid = false;
        } else {
            guestUsername.setError(null);
        }

        if (code.isEmpty() || code.length() < 4 || code.length() > 10) {
            guestCode.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            guestCode.setError(null);
        }

        return valid;
    }

    public boolean validateLogin() {
        boolean valid = true;

        String username = loginUsername.getText().toString();
        String password = loginPasssword.getText().toString();

        if (username.isEmpty() || username.length() < 4) {
            loginUsername.setError("more than 4 alphanumeric characters");
            valid = false;
        } else {
            loginUsername.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            loginPasssword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            loginPasssword.setError(null);
        }

        return valid;
    }
}