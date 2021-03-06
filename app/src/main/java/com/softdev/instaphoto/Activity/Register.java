package com.softdev.instaphoto.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
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

public class Register extends AppCompatActivity {

    EditText txtUsername, txtName, txtEmail, txtPassword;
    Button btnRegister, btnLogin;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    _Register();
                }
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });
    }

    private void _Register() {
        pDialog = new ProgressDialog(Register.this, R.style.AppTheme_Dark_Dialog);
        pDialog.setIndeterminate(true);
        pDialog.setMessage("Authenticating...");
        pDialog.show();
        final String name = txtName.getText().toString();
        final String email = txtEmail.getText().toString().trim();
        final String username = txtUsername.getText().toString().trim();
        final String password = txtPassword.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST, Config.REGISTER, new Response.Listener<String>() {
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
                        startActivity(new Intent(Register.this, MainActivity.class));
                        finish();
                    } else {
                        int code = obj.getInt("code");
                        if (code == Config.USER_INVALID) {
                            txtUsername.setError("Username is not valid.");
                        } else if (code == Config.USER_ALREADY_EXISTS) {
                            Toast.makeText(Register.this, "Username or email address already in use.", Toast.LENGTH_SHORT).show();
                        } else if (code == Config.PASSWORD_INCORRECT) {
                            txtPassword.setError("Password incorrect.");
                        } else if (code == Config.EMAIL_INVALID) {
                            txtEmail.setError("Email address is not valid.");
                        } else if (code == Config.UNKNOWN_ERROR) {
                            Log.e(AppHandler.TAG, "Login() Unknown error returned by server.");
                        }
                    }
                } catch (JSONException ex) {
                    Log.e("Register", "JSON Parse error: " + ex.getMessage() + "\nResponse: " + response);
                    pDialog.dismiss();
                    Toast.makeText(Register.this, "Unable to connect to server.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Register", "Unexpected error: " + error.getMessage());
                pDialog.dismiss();
                Toast.makeText(Register.this, "Unable to connect to server.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("name", name);
                params.put("email", email);
                return params;
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        AppHandler.getInstance().addToRequestQueue(request);
    }

    boolean validate() {
        boolean valid = true;
        String strUsername = txtUsername.getText().toString();
        String strPassword = txtPassword.getText().toString();
        String strEmail = txtEmail.getText().toString();
        String strName = txtName.getText().toString();
        if (strName.isEmpty()) {
            txtName.setError("Please enter your name.");
            valid = false;
        } else {
            txtName.setError(null);
        }
        if (strUsername.isEmpty()) {
            txtUsername.setError("Please enter a valid username.");
            valid = false;
        } else {
            txtUsername.setError(null);
        }
        if (strEmail.isEmpty()) {
            txtEmail.setError("Please enter a valid email address.");
            valid = false;
        } else {
            txtEmail.setError(null);
        }
        if (strPassword.isEmpty() || strPassword.length() < 6) {
            txtPassword.setError("Password incorrect.");
            valid = false;
        } else {
            txtPassword.setError(null);
        }
        return valid;
    }
}
