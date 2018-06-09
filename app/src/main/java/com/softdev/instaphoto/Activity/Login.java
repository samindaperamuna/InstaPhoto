package com.softdev.instaphoto.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Configuration.DataStorage;
import com.softdev.instaphoto.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    EditText txtUsername;
    EditText txtPassword;
    Button btnLogin;
    Button btnRegister;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Binding Views
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        // Event Handling
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    _Login();
                }
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
                finish();
            }
        });
    }

    private void _Login() {
        pDialog = new ProgressDialog(Login.this, R.style.AppTheme_Dark_Dialog);
        pDialog.setIndeterminate(true);
        pDialog.setMessage("Authenticating...");
        pDialog.show();
        final String username = txtUsername.getText().toString();
        final String password = txtPassword.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST, Config.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONObject accountObj = obj.getJSONObject("account");
                        DataStorage d = AppHandler.getInstance().getDataManager();
                        d.setString("id", accountObj.getString("id"));
                        d.setString("username", accountObj.getString("username"));
                        d.setString("name", accountObj.getString("name"));
                        d.setString("email", accountObj.getString("email"));
                        d.setString("api", obj.getString("api"));
                        d.setString("icon", accountObj.getString("icon"));
                        d.setString("created_At", accountObj.getString("created_At"));
                        d.setString("bio", accountObj.getString("bio"));
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        int code = obj.getInt("code");
                        if (code == Config.USER_INVALID) {
                            txtUsername.setError("Username is not valid.");
                        } else if (code == Config.PASSWORD_INCORRECT) {
                            txtPassword.setError("Password incorrect.");
                        } else if (code == Config.UNKNOWN_ERROR) {
                            Log.e(AppHandler.TAG, "Login() Unknown error returned by server.");
                        }
                    }
                } catch (JSONException ex) {
                    pDialog.dismiss();
                    Log.e("Login", "Error:" + ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Unable to connect to server.", Toast.LENGTH_SHORT).show();
                Log.e("Login", "" + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }

    public boolean validate() {
        boolean valid = true;
        String strUsername = txtUsername.getText().toString();
        String strPassword = txtPassword.getText().toString();
        if (strUsername.isEmpty()) {
            txtUsername.setError("Please enter a valid username.");
            valid = false;
        } else {
            txtUsername.setError(null);
        }
        if (strPassword.isEmpty() || strPassword.length() < 8) {
            txtPassword.setError("Password incorrect.");
            valid = false;
        } else {
            txtPassword.setError(null);
        }
        return valid;
    }
}
