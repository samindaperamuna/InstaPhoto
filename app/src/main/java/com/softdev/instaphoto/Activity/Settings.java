package com.softdev.instaphoto.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
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
import com.softdev.instaphoto.Configuration.AppHelper;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {

    Toolbar toolbar;
    EditText txtName;
    EditText txtUsername;
    EditText txtEmail;
    EditText txtPassword;
    EditText txtBio;
    Button btnLogout, btnDeactivate, btnBlockList;
    CircleImageView icon;
    ProgressDialog progressDialog;
    Bitmap bitmap;
    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        txtName = (EditText) findViewById(R.id.txtName);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtBio = (EditText) findViewById(R.id.txtBio);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnDeactivate = (Button) findViewById(R.id.btnDeactivate);
        btnBlockList = (Button) findViewById(R.id.btnBlockedList);
        icon = (CircleImageView) findViewById(R.id.icon);
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("Delete your account?")
                        .setMessage("Do you really want to logout?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setMessage("Please wait...");
                                progressDialog.setIndeterminate(true);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                // Logout
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        AppHandler.getInstance().getDBHandler().resetDatabase();
                                        AppHandler.getInstance().getDataManager().clear();
                                        startActivity(new Intent(Settings.this, AppHelper.class));
                                    }
                                }, 2000);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        btnDeactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("Delete your account?")
                        .setMessage("Are you sure you want to delete your account? Please note that this action cannot be undone and all your data (including photos, comments, likes) will be deleted.")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setIndeterminate(true);
                                progressDialog.setCancelable(false);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.setMessage("Deleting your account...");
                                progressDialog.show();
                                StringRequest request = new StringRequest(Request.Method.POST, Config.DELETE_ACCOUNT, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        progressDialog.dismiss();
                                        try {
                                            JSONObject obj = new JSONObject(response);
                                            if (!obj.getBoolean("error")) {
                                                Toast.makeText(Settings.this, "Your account is successfully deleted.", Toast.LENGTH_LONG).show();
                                                AppHandler.getInstance().getDBHandler().resetDatabase();
                                                AppHandler.getInstance().getDataManager().clear();
                                                startActivity(new Intent(Settings.this, AppHelper.class));
                                            } else {
                                                Log.e("Settings", "Server response: " + obj.getString("code"));
                                                Toast.makeText(Settings.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException ex) {
                                            Log.e("Settings", "Unexpected error: " + ex.getMessage());
                                            Toast.makeText(Settings.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        progressDialog.dismiss();
                                        Log.e("Settings", "Unexpected error: " + error.getMessage());
                                        Toast.makeText(Settings.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        return AppHandler.getInstance().getAuthorization();
                                    }
                                };
                                int socketTimeout = 0;
                                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                                request.setRetryPolicy(policy);
                                AppHandler.getInstance().addToRequestQueue(request);
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
        btnBlockList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this, BlockList.class));
            }
        });
//        txtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    if (txtPassword.getText().toString().trim().equals("")) {
//                        txtPassword.setText(AppHandler.getInstance().getDataManager().getString("password", "1233"));
//                        return;
//                    }
//                    if (!txtPassword.getText().toString().equals(AppHandler.getInstance().getDataManager().getString("password", "1233"))) {
//                        new AlertDialog.Builder(Settings.this).setTitle("Change Password")
//                                .setMessage("Do you want to edit your password with the new one?")
//                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                })
//                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        txtPassword.setText(AppHandler.getInstance().getDataManager().getString("password", "1233"));
//                                    }
//                                }).show();
//                    } else {
//                        txtPassword.setText("");
//                    }
//                }
//            }
//        });
        loadSettings();
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            photoUri = filePath;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                loadPhoto();
                invalidateOptionsMenu();
            }
            catch (IOException ex) {
                finish();
            }
        }
    }

    private void loadPhoto() {
        icon.setScaleX(0);
        icon.setScaleY(0);
        Picasso.with(this)
                .load(photoUri)
                .into(icon, new Callback() {
                    @Override
                    public void onSuccess() {
                        icon.animate()
                                .scaleX(1.f).scaleY(1.f)
                                .setInterpolator(new OvershootInterpolator())
                                .setDuration(400)
                                .setStartDelay(200)
                                .start();
                    }

                    @Override
                    public void onError() {
                    }
                });
    }
    
    private void loadSettings() {
        txtName.setText(AppHandler.getInstance().getDataManager().getString("name", ""));
        txtUsername.setText(AppHandler.getInstance().getDataManager().getString("username", ""));
        txtEmail.setText(AppHandler.getInstance().getDataManager().getString("email", ""));
        txtBio.setText(AppHandler.getInstance().getDataManager().getString("bio", ""));
        Picasso.with(this)
                .load(AppHandler.getInstance().getDataManager().getString("icon", ""))
                .error(R.drawable.ic_people)
                .placeholder(R.drawable.ic_people)
                .into(icon);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (bitmap == null) {
            menu.getItem(0).setVisible(false);
        } else {
            menu.getItem(0).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            updateSettings();
            return true;
        } else if (item.getItemId() == R.id.action_cancel) {
            bitmap = null;
            photoUri = null;
            Picasso.with(this)
                    .load(AppHandler.getInstance().getDataManager().getString("icon", ""))
                    .error(R.drawable.ic_people)
                    .placeholder(R.drawable.ic_people)
                    .into(icon);
            invalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateSettings() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Updating your account settings...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Config.UPDATE_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        AppHandler.getInstance().getDataManager().setString("name", txtName.getText().toString());
                        AppHandler.getInstance().getDataManager().setString("bio", txtBio.getText().toString());
                        if (obj.has("icon")) {
                            AppHandler.getInstance().getDataManager().setString("icon", obj.getString("icon"));
                        }
                        bitmap = null;
                        invalidateOptionsMenu();
                        Toast.makeText(Settings.this, "Your account settings has been updated.", Toast.LENGTH_SHORT).show();
                        icon.requestFocus();
                    }
                } catch (JSONException ex) {
                    Log.e("Settings", "error: " + ex.getMessage() + "\nResponse: " + response);
                    Toast.makeText(Settings.this, "Unable to update settings due to server-side error.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e("Settings", "Error: " + error.getMessage());
                Toast.makeText(Settings.this, "Unable to update settings due to server-side error.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", txtName.getText().toString());
                params.put("password", getPassword());
                params.put("bio", txtBio.getText().toString());
                params.put("icon", getStringImage(bitmap));
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

    private String getPassword() {
        if (txtPassword.getText().toString().trim().equals("")) {
            return "test";
        } else {
            return txtPassword.getText().toString();
        }
    }

    public String getStringImage(Bitmap bmp){
        if (bmp == null)
            return "";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        byte[] imageBytes = output.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
