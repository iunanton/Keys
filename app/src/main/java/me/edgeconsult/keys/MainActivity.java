package me.edgeconsult.keys;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.content.Intent;
import android.os.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnAccountsUpdateListener {

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();
    private AccountManager accountManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accountManager = AccountManager.get(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        accountManager.addOnAccountsUpdatedListener(this, null, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        accountManager.removeOnAccountsUpdatedListener(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (accounts.length == 0) {
            accountManager.addAccount(getString(R.string.account_type), null, null,null, null, new AccountManagerCallback<Bundle>() {
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
