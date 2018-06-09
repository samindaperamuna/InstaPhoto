package com.softdev.instaphoto.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Adapter.CommentsAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Comment;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.ItemDivider;
import com.softdev.instaphoto.R;
import com.softdev.instaphoto.Utils;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.softdev.instaphoto.Configuration.Config.ARG_DRAWING_START_LOCATION;

public class Comments extends AppCompatActivity implements CommentsAdapter.OnItemClickListener {

    RecyclerView recyclerView;
    EditText txtComment;
    ImageButton btnSend;
    LinearLayout mainLayout, commentLayout;
    Toolbar toolbar;
    CommentsAdapter cAdapter;
    ArrayList<Comment> commentsList;
    String postId;
    boolean isDisabled;
    boolean isPublicAllowed, isFollowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Binding Views
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        commentsList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtComment = (EditText) findViewById(R.id.txtComment);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        mainLayout = (LinearLayout) findViewById(R.id.layoutMain);
        commentLayout = (LinearLayout) findViewById(R.id.commentLayout);

        // Getting parameters
        Bundle param = getIntent().getExtras();
        if (param != null) {
            postId = param.getString("postId");
            isDisabled = param.getBoolean("isDisabled");
            String poster = param.getString("poster");
            String description = param.getString("description");
            String username = param.getString("username");
            String creation = param.getString("creation");
            String strIcon = param.getString("icon");
            if (param.containsKey("isFollowing")) {
                isFollowing = param.getBoolean("isFollowing");
                isPublicAllowed = param.getBoolean("isPublicAllowed");
            }
            Comment c = new Comment();
            c.setPostid(postId);
            c.setUsername(username);
            c.setName(poster);
            c.setIcon(strIcon);
            c.setContent("<b>" + c.getUsername() + "</b> " + description);
            c.setCreation(creation);
            commentsList.add(c);
        }

        if (isDisabled) {
            txtComment.setEnabled(false);
            txtComment.setHint("Comments are disabled by the owner of this post.");
            btnSend.setVisibility(View.INVISIBLE);
        } else if (!isPublicAllowed && !isFollowing) {
            txtComment.setEnabled(false);
            txtComment.setHint("Only the followers of this user can comment on this post.");
            btnSend.setVisibility(View.INVISIBLE);
        }

        txtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtComment.length() > 0) {
                    btnSend.setEnabled(true);
                } else {
                    btnSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Setting up comments and adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        cAdapter = new CommentsAdapter(this, commentsList);
        cAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(cAdapter);
        recyclerView.addItemDecoration(new ItemDivider(this));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    cAdapter.setAnimationsLocked(true);
                }
            }
        });

        // Event Listeners
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String UTCTime = sdf.format(new Date());
                Comment c = new Comment();
                c.setPostid(postId);
                c.setUsername(AppHandler.getInstance().getDataManager().getString("username", ""));
                c.setName(AppHandler.getInstance().getDataManager().getString("name", ""));
                c.setIcon(AppHandler.getInstance().getDataManager().getString("icon", ""));
                c.setContent(txtComment.getText().toString());
                c.setContent("<b>" + c.getUsername() + "</b> " + c.getContent());
                c.setCreation(UTCTime);
                commentsList.add(c);
                AddComment(txtComment.getText().toString(), c);
                cAdapter.addItem();
                txtComment.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtComment.getWindowToken(), 0);
            }
        });
        cAdapter.updateItems();
        LoadComments();
        animateContent();
    }

    private void AddComment(final String comment, final Comment c) {
        StringRequest request = new StringRequest(Request.Method.POST, Config.ADD_COMMENT.replace(":id", postId), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        commentsList.get(commentsList.indexOf(c)).setCommentId(obj.getString("id"));
                        recyclerView.smoothScrollToPosition(commentsList.size());
                        cAdapter.notifyItemChanged(commentsList.indexOf(c));
                    } else {
                        Log.e("Comments", "Server response: " + obj.getString("code"));
                        commentsList.remove(c);
                        Toast.makeText(Comments.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Comments", "Unexpected error: " + ex.getMessage());
                    commentsList.remove(c);
                    Toast.makeText(Comments.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Comments", "Unexpected error: " + error.getMessage());
                commentsList.remove(c);
                Toast.makeText(Comments.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("comment", comment);
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

    private void animateContent() {
        commentLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(200).start();
    }

    @Override
    public void onBackPressed() {
        ViewCompat.setElevation(toolbar, 0);
        mainLayout.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Comments.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    @Override
    public void onProfileClick(View v, int position) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        UserProfile.startUserProfileFromLocation(startingLocation, this, commentsList.get(position).getUsername(), commentsList.get(position).getName());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    final String commentId = commentsList.get(position).getCommentId();
                    commentsList.remove(position);
                    cAdapter.notifyItemRemoved(position);
                    StringRequest request = new StringRequest(Request.Method.PUT, Config.DELETE_COMMENT.replace(":postId", postId), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.getBoolean("error")) {
                                    Toast.makeText(Comments.this, "Comment deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("Comments", "Server response: " + obj.getString("code"));
                                    //cAdapter.notifyItemInserted(position);
                                    Toast.makeText(Comments.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (JSONException ex) {
                                Log.e("Comments", "Unexpected error: " + ex.getMessage());
                                //cAdapter.notifyItemInserted(position);
                                Toast.makeText(Comments.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Comments", "Unexpected error: " + error.getMessage());
                            //cAdapter.notifyItemInserted(position);
                            Toast.makeText(Comments.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            return AppHandler.getInstance().getAuthorization();
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("id", commentId);
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
                return true;
            }
        });
        popupMenu.inflate(R.menu.menu_comments);
        popupMenu.show();
    }

    private void LoadComments() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_COMMENTS + "0?postId="+postId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONArray comments = obj.getJSONArray("comments");
                        if (comments.length() != 0) {
                            for (int i = 0; i < comments.length(); i++) {
                                JSONObject comment = comments.getJSONObject(i);
                                Comment c = new Comment();
                                c.setCommentId(comment.getString("id"));
                                c.setPostid(comment.getString("post_id"));
                                c.setUsername(comment.getString("username"));
                                c.setIcon(comment.getString("icon"));
                                c.setName(comment.getString("name"));
                                c.setContent("<b>" + c.getUsername() + "</b> " + comment.getString("content"));
                                c.setCreation(comment.getString("creation"));
                                commentsList.add(c);
                                Log.d("Comments", c.getContent());
                            }
                            cAdapter.updateItems();
                            recyclerView.smoothScrollToPosition(commentsList.size());
                        }
                    } else {
                        Toast.makeText(Comments.this, "Couldn't load", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Comments", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(Comments.this, "Couldn't load", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Comments", "Unexpected error: " + error.getMessage());
                Toast.makeText(Comments.this, "Couldn't load", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }
}
