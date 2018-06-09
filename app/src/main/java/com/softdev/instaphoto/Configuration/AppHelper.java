package com.softdev.instaphoto.Configuration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.softdev.instaphoto.Activity.MainActivity;
import com.softdev.instaphoto.Activity.Register;

public class AppHelper extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppHandler.getInstance().getDataManager().getString("id", null) != null) {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(this, Register.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        overridePendingTransition(0, 0);
    }
}
