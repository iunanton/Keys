package me.edgeconsult.keys;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by yun on 10/13/17.
 */

public class AuthenticatorService extends Service {

    private static AccountAuthenticator accountAuthenticator = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IBinder iBinder = null;
        if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            iBinder = getAuthenticator().getIBinder();
        }
        return iBinder;
    }

    private AccountAuthenticator getAuthenticator() {
        if (AuthenticatorService.accountAuthenticator == null) {
            AuthenticatorService.accountAuthenticator = new AccountAuthenticator(this);
        }

        return AuthenticatorService.accountAuthenticator;
    }

    private class AccountAuthenticator extends AbstractAccountAuthenticator {

        private Context context;

        AccountAuthenticator(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public String getAuthTokenLabel(String s) {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            return null;
        }
    }
}