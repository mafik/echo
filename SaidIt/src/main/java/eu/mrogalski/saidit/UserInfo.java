package eu.mrogalski.saidit;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Patterns;

import java.util.regex.Pattern;

public class UserInfo {

    public static String getUserPhoneNumber(Context c) {
        final TelephonyManager tMgr = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    /*
    First - try phone ID
    Second - try user email
    Third - try system ID
     */
    public static String getUserID(Context c) {
        final TelephonyManager tMgr = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        String id = tMgr.getDeviceId();
        if(id != null) {
            return "device-id:" + id;
        }

        AccountManager accountManager = AccountManager.get(c);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        for (Account a: accounts) {
            if (a.name.contains("@gmail.com")) {
                return "email:" + a.name;
            }
        }

        return "android-id:" + Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getUserCountryCode(Context c) {

        final TelephonyManager manager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        final String countryLetterCode = manager.getSimCountryIso().toUpperCase();
        String[] rl = c.getResources().getStringArray(R.array.country_codes);
        for (String aRl : rl) {
            String[] g = aRl.split(",");
            if (g[1].trim().equals(countryLetterCode.trim())) {
                return g[0];
            }
        }
        return "";
    }

    public static String getUserEmail(Context c) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(c).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return "";
    }
}
