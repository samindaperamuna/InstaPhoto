package com.softdev.instaphoto.Activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
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
import com.softdev.instaphoto.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Publish extends AppCompatActivity {

    Toolbar toolbar;
    Switch toggleComments;
    ImageView photo;
    TextView txtDescription;
    Uri photoUri;
    Bitmap bitmap;
    ProgressDialog pDialog;
    HashTagHelper hashTagHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_primary);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        pDialog = new ProgressDialog(Publish.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(false);
        toggleComments = (Switch) findViewById(R.id.toggleComments);
        photo = (ImageView) findViewById(R.id.photo);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        hashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimary), null);
        hashTagHelper.handle(txtDescription);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            photoUri = Uri.parse(b.getString("photoUri"));
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
            }
            catch (IOException ex) {
                finish();
            }
            loadPhoto();
        } else {
            finish();
        }
    }

    private void loadPhoto() {
        photo.setScaleX(0);
        photo.setScaleY(0);
        Picasso.with(this)
                .load(photoUri)
                .into(photo, new Callback() {
                    @Override
                    public void onSuccess() {
                        photo.animate()
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_publish) {
            publishPhoto();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void publishPhoto() {
        pDialog.setMessage("Uploading your post...");
        pDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Config.PUBLISH_PHOTO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Toast.makeText(Publish.this, "Post uploaded", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e("Publish", "Unexpected server error: " + obj.getString("code"));
                        Toast.makeText(Publish.this, "There was an error while uploading your post. Please try again later.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (JSONException ex) {
                    pDialog.dismiss();
                    Log.e("Publish", "Publish Request: " + ex.getMessage() + "\n" + response);
                    Toast.makeText(Publish.this, "There was an error while sharing your post. Please try again later in a while.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Log.e("Publish", "Unexpected error: " + error.getMessage());
                Toast.makeText(Publish.this, "Unable to share your post.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("content", getStringImage(bitmap));
                params.put("description", txtDescription.getText().toString());
                params.put("disableComments", toggleComments.isChecked() ? "1" : "0");
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
