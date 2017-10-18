package me.edgeconsult.keys;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MyListener {

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();
    private boolean mIsResumed = false;

    private AccountManager accountManager = null;

    private ArrayList<Message> messagesList;
    private ArrayAdapter<Message> messagesAdapter;

    private static class ViewHolder{
        TextView username;
        TextView time;
        TextView body;
    }

    private ListView mListView;
    private EditText mEditText;
    private ImageButton mImageButton;
    private ProgressBar mProgressBar;

    private boolean activityOnPause = true;

    private NotificationManager mNotificationManager;
    private static int notificationID = 1;
    private PendingIntent mPendingIntent;

    private Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private WebSocketService mBoundService;
    private boolean mIsBound = false;

    private String token;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(ServiceConnection.class.getSimpleName(), "onServiceConnected");
            mBoundService = ((WebSocketService.MyBinder)service).getService();
            mBoundService.registerListener(MainActivity.this);
            mIsBound = true;
            startSession();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MAIN_ACTIVITY_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountManager = AccountManager.get(this);

        mListView = (ListView) findViewById(R.id.messages_wrapper);
        mEditText = (EditText) findViewById(R.id.input);
        mImageButton = (ImageButton) findViewById(R.id.send_button);
        mProgressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        showProgressBar();

        messagesList = new ArrayList<>();
        messagesAdapter =
                new ArrayAdapter<Message>(this,
                        R.layout.item_message,
                        messagesList) {
                    @NonNull
                    public View getView(int position,
                                        View convertView,
                                        @NonNull ViewGroup parent) {
                        Message currentMessage = messagesList.get(position);
                        if (convertView == null) {
                            convertView = getLayoutInflater()
                                    .inflate(R.layout.item_message, parent, false);
                            ViewHolder viewHolder = new ViewHolder();
                            viewHolder.username = convertView.findViewById(R.id.message_username);
                            viewHolder.time = convertView.findViewById(R.id.message_time);
                            viewHolder.body = convertView.findViewById(R.id.message_body);
                            convertView.setTag(viewHolder);
                        }
                        TextView username = ((ViewHolder) convertView.getTag()).username;
                        TextView time = ((ViewHolder) convertView.getTag()).time;
                        TextView body =  ((ViewHolder) convertView.getTag()).body;
                        username.setText(currentMessage.getUsername());
                        Date date = new Date(currentMessage.getTime());
                        time.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date));
                        body.setText(currentMessage.getBody());
                        return convertView;
                    }
                };

        mListView.setAdapter(messagesAdapter);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ed_text = mEditText.getText().toString().trim().replaceAll("\\r|\\n", " ").replaceAll("\'", "&apos;").replaceAll("\"", "&quot;");
                if (ed_text.isEmpty() || ed_text.length() == 0 || ed_text.equals("")) {
                    //EditText is empty
                } else {
                    String msg = "{ \"type\": \"message\", \"data\": { \"messageBody\": \"" + ed_text + "\" } }";
                    mBoundService.send(msg);
                    mEditText.setText("");
                }
            }
        });

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mPendingIntent = PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

        bindService(new Intent(this, WebSocketService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void startSession() {
        if (!mBoundService.connected()) {
            Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type));
            if (accounts != null && accounts.length > 0) {
                accountManager.getAuthToken(accounts[0], getString(R.string.auth_token_type), null, null, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        Bundle b;
                        try {
                            b = accountManagerFuture.getResult();
                            if (b.containsKey(AccountManager.KEY_INTENT)) {
                                Intent intent = b.getParcelable(AccountManager.KEY_INTENT);
                                startActivity(intent);
                                finish();
                            } else {
                                token = b.getString(AccountManager.KEY_AUTHTOKEN);
                                mBoundService.connect(token);
                            }
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (android.accounts.OperationCanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
            } else {
                accountManager.addAccount(getString(R.string.account_type), null, null, null, null, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        Bundle bundle;
                        try {
                            bundle = accountManagerFuture.getResult();
                            Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                            startActivity(intent);
                            finish();
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (android.accounts.OperationCanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
            }
        }
    }

    private void showProgressBar() {
        mListView.setVisibility(View.GONE);
        mEditText.setVisibility(View.GONE);
        mImageButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mListView.setVisibility(View.VISIBLE);
        mEditText.setVisibility(View.VISIBLE);
        mImageButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void launchNotification(CharSequence title, CharSequence contentText) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setColor(0xFF00CCCC);
        mBuilder.setLights(0xFF00CCCC, 500, 1500);
        mBuilder.setSound(uri);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(contentText);
        mBuilder.setContentIntent(mPendingIntent);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(notificationID, mBuilder.build());
    }

    @Override
    public void onMessage(String text) {
        try {
            final JSONObject message = new JSONObject(text);
            final String type = message.getString("type");
            JSONObject data = message.getJSONObject("data");
            switch (type) {
                case "context": {
                    final JSONArray users = data.getJSONArray("users");
                    final JSONArray messages = data.getJSONArray("messages");
                    for (int i = 0; i < messages.length(); ++i) {
                        JSONObject item = messages.getJSONObject(i);
                        final String username = item.getString("username");
                        final Long timestamp = item.getLong("timestamp");
                        final String messageBody = item.getString("messageBody").replaceAll("&apos;", "\'").replaceAll("&quot;", "\"");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messagesAdapter.add(new Message(username, timestamp, messageBody));
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBar();
                        }
                    });
                    break;
                }
                case "userJoined":
                    final String username = data.getString("username");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activityOnPause)
                                launchNotification("New user joined", username + " joined! Say \"Hi\" to him!");
                            Toast.makeText(getApplicationContext(), username + " joined", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case "userLeft":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activityOnPause)
                                launchNotification("User left", "One user just left chat..");
                            Toast.makeText(getApplicationContext(), "user left", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case "messageAdd":
                    final String message_body = data.getString("messageBody").replaceAll("&apos;", "\'").replaceAll("&quot;", "\"");
                    final Long message_timestamp = data.getLong("timestamp");
                    final String message_username = data.getString("username");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activityOnPause)
                                launchNotification("New message", message_username + ": " + message_body);
                            messagesAdapter.add(new Message(message_username, message_timestamp, message_body));
                        }
                    });
                    break;
                default:
                    break;
            }
        } catch (final JSONException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "JSON could not be parsed", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
