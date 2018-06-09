package com.softdev.instaphoto.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Adapter.ExploreGridAdapter;
import com.softdev.instaphoto.Adapter.FeedAdapter;
import com.softdev.instaphoto.Adapter.FeedItemAnimator;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;
import com.softdev.instaphoto.RevealBackgroundView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity implements RevealBackgroundView.OnStateChangeListener,
        ExploreGridAdapter.OnExploreItemClickListener, FeedAdapter.OnFeedItemClickListener {

    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    String strUsername, strName, strId;
    TextView txtPosts;
    TextView txtFollowers;
    TextView txtFollowing, txtUserFollowing;
    TextView txtName, txtToolbarTitle, txtBio;
    LinearLayout tabsView;
    RevealBackgroundView revealView;
    View vUserProfileRoot;
    RecyclerView recyclerView;
    CircleImageView photo;
    LinearLayout detailsView, statsView;
    ArrayList<Feed> feedArrayList;
    FeedAdapter feedAdapter;
    ExploreGridAdapter gridAdapter;
    ImageView btnGrid, btnList;
    public boolean isGridMode = true;
    int selectedPosition;
    Toolbar toolbar;
    LinearLayout viewFollowers, viewFollowing;
    User crUser;
    boolean isFollowing, isFollowed = false;
    Button btnFollow;
    ProgressDialog progressDialog;

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity, String username, String name) {
        Intent intent = new Intent(startingActivity, UserProfile.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra("username", username);
        intent.putExtra("name", name);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        viewFollowers = (LinearLayout) findViewById(R.id.viewFollowers);
        viewFollowing = (LinearLayout) findViewById(R.id.viewFollowing);
        photo = (CircleImageView) findViewById(R.id.photo);
        txtPosts = (TextView) findViewById(R.id.txtPosts);
        txtFollowers = (TextView) findViewById(R.id.txtFollowers);
        txtFollowing = (TextView) findViewById(R.id.txtFollowing);
        txtName = (TextView) findViewById(R.id.txtName);
        photo = (CircleImageView) findViewById(R.id.photo);
        detailsView = (LinearLayout) findViewById(R.id.detailsView);
        statsView = (LinearLayout) findViewById(R.id.statsView);
        btnGrid = (ImageView) findViewById(R.id.btnGrid);
        btnList = (ImageView) findViewById(R.id.btnList);
        tabsView = (LinearLayout) findViewById(R.id.tabsView);
        revealView = (RevealBackgroundView) findViewById(R.id.revealView);
        vUserProfileRoot = findViewById(R.id.vUserProfileRoot);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtBio = (TextView) findViewById(R.id.txtBio);
        feedArrayList = new ArrayList<>();
        txtUserFollowing = (TextView) findViewById(R.id.txtUserFollowing);
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        txtUserFollowing.setVisibility(View.GONE);
        btnFollow = (Button) findViewById(R.id.btnFollow);
        btnFollow.setEnabled(false);
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing) {
                    new AlertDialog.Builder(UserProfile.this)
                            .setTitle("Unfollow :user?".replace(":user", strName))
                            .setMessage("Do you really want to unfollow :user?".replace(":user", strName))
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StringRequest request = new StringRequest(Request.Method.PUT, Config.UNFOLLOW_USER.replace(":id", strUsername), new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject obj = new JSONObject(response);
                                                if (!obj.getBoolean("error")) {
                                                    isFollowing = false;
                                                    if (crUser != null) {
                                                        setupUser(crUser);
                                                    }
                                                }
                                            } catch (JSONException ex) {
                                                Log.e("UserProfile", "JSON Parse error: " + ex.getMessage() + "\nResponse: " + response);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("UserProfile", "Error" + error.getMessage());
                                            Toast.makeText(UserProfile.this, "Your request was not proceed.", Toast.LENGTH_SHORT).show();
                                        }
                                    }) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            return AppHandler.getInstance().getAuthorization();
                                        }
                                    };
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(UserProfile.this)
                            .setTitle("Follow :user?".replace(":user", strName))
                            .setMessage("Do you really want to follow :user?".replace(":user", strName))
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    StringRequest request = new StringRequest(Request.Method.PUT, Config.FOLLOW_USER.replace(":id", strUsername), new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject obj = new JSONObject(response);
                                                if (!obj.getBoolean("error")) {
                                                    Toast.makeText(UserProfile.this, "You're now following :user".replace(":user", strName), Toast.LENGTH_SHORT).show();
                                                    isFollowing = true;
                                                    if (crUser != null) {
                                                        setupUser(crUser);
                                                    }
                                                }
                                            } catch (JSONException ex) {
                                                Log.e("UserProfile", "JSON Parse error: " + ex.getMessage() + "\nResponse: " + response);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("UserProfile", "Error" + error.getMessage());
                                            Toast.makeText(UserProfile.this, "Your request was not proceed.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        });

        switchGridMode();
        strUsername = getIntent().getStringExtra("username");
        strName = getIntent().getStringExtra("name");
        getUserInfo(strUsername);
        txtName.setText(strName);
        txtToolbarTitle.setText(strUsername);

        setupRevealBackground(savedInstanceState);
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        revealView.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            revealView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    revealView.getViewTreeObserver().removeOnPreDrawListener(this);
                    revealView.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            revealView.setToFinishedFrame();
        }
    }

    private void animateUserProfileOptions() {
        tabsView.setTranslationY(-tabsView.getHeight());
        tabsView.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void setupUser(User u) {
        Picasso.with(this)
                .load(u.getIcon())
                .placeholder(R.drawable.ic_people)
                .error(R.drawable.ic_people)
                .into(photo);
        strId = u.getId();
        txtPosts.setText(u.getPosts());
        txtFollowers.setText(u.getFollowers());
        txtFollowing.setText(u.getFollowing());
        txtName.setText(u.getName());
        txtBio.setText(u.getBio());
        btnFollow.setText(isFollowing ? "Following" : "Follow");
        btnFollow.setEnabled(true);
        txtUserFollowing.setText(":user is already following you.".replace(":user", strName));
        txtUserFollowing.setVisibility(isFollowed ? View.VISIBLE : View.INVISIBLE);
        crUser = u;
    }

    public void getUserInfo(String username) {
        StringRequest request = new StringRequest(Request.Method.GET, Config.GET_USER.replace(":user", username), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        User u = new User();
                        u.setId(obj.getString("id"));
                        u.setName(obj.getString("name"));
                        u.setUsername(obj.getString("username"));
                        u.setIcon(obj.getString("icon"));
                        u.setCreation(obj.getString("created_At"));
                        u.setEmail(obj.getString("email"));
                        u.setFollowing(obj.getString("following"));
                        u.setFollowers(obj.getString("followers"));
                        u.setPosts(obj.getString("totalPosts"));
                        u.setBio(obj.getString("bio"));
                        if (!obj.getBoolean("isBlocked")) {
                            JSONObject relation = obj.getJSONObject("relation");
                            isFollowed = relation.getInt("isFollowed") == 1;
                            isFollowing = relation.getInt("isFollowing") == 1;
                            setupUser(u);
                            JSONArray feeds = obj.getJSONArray("feeds");
                            if (feeds.length() != 0) {
                                for (int i = 0; i < feeds.length(); i++) {
                                    JSONObject feed = feeds.getJSONObject(i);
                                    Feed f = new Feed();
                                    f.setUser_id(feed.getString("user_id"));
                                    f.setPostId(feed.getString("postId"));
                                    f.setUsername(feed.getString("username"));
                                    f.setName(feed.getString("name"));
                                    f.setIcon(feed.getString("icon"));
                                    f.setLiked(feed.getInt("isLiked") == 1);
                                    f.setDescription(feed.getString("description"));
                                    f.setCreation(feed.getString("creation"));
                                    f.setType(feed.getString("type"));
                                    f.setComments(feed.getString("comments"));
                                    f.setLikes(feed.getString("likes"));
                                    f.setContent(feed.getString("content"));
                                    f.setCommentsEnable(feed.getInt("isAllowComments") == 1);
                                    f.setPublicCommentsAllowed(feed.getInt("isPublicComments") == 1);
                                    f.setFollowing(feed.getInt("isFollowing") == 1);
                                    feedArrayList.add(f);
                                }
                                switchGridMode();
                            }
                        } else {
                            new AlertDialog.Builder(UserProfile.this)
                                    .setTitle("Sorry!")
                                    .setMessage("You are either not allowed to view this profile or the user account doesn't exist.")
                                    .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    }
                    else {
                        Toast.makeText(UserProfile.this, "Unable to proceed your request.", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("UserProfile", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(UserProfile.this, "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("UserProfile", "Unexpected error: " + error.getMessage());
                Toast.makeText(UserProfile.this, "Couldn't refresh", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            recyclerView.setVisibility(View.VISIBLE);
            tabsView.setVisibility(View.VISIBLE);
            vUserProfileRoot.setVisibility(View.VISIBLE);
            btnGrid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isGridMode)
                        switchGridMode();
                }
            });
            btnList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isGridMode)
                        switchListMode();
                }
            });
            viewFollowers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (crUser != null) {
                        Intent followers = new Intent(UserProfile.this, Followers.class);
                        followers.putExtra("id", crUser.getId());
                        startActivity(followers);
                    }
                }
            });
            viewFollowing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (crUser != null) {
                        Intent following = new Intent(UserProfile.this, Following.class);
                        following.putExtra("id", crUser.getId());
                        startActivity(following);
                    }
                }
            });
            animateUserProfileHeader();
            animateUserProfileOptions();
            switchGridMode();
        } else {
            tabsView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            vUserProfileRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void switchGridMode() {
        isGridMode = true;
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setPadding(2, 2, 2, 2);
        gridAdapter = new ExploreGridAdapter(this, feedArrayList);
        recyclerView.setAdapter(gridAdapter);
        gridAdapter.setOnExploreItemClickListener(this);
        animateItems(false);
    }

    private void switchListMode() {
        isGridMode = false;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setPadding(0, 0, 0, 0);
        feedAdapter = new FeedAdapter(this, feedArrayList);
        recyclerView.setAdapter(feedAdapter);
        recyclerView.setItemAnimator(new FeedItemAnimator());
        animateItems(false);
        feedAdapter.setOnFeedItemClickListener(this);
        recyclerView.smoothScrollToPosition(selectedPosition);
    }

    private void animateUserProfileHeader() {
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        photo.setTranslationY(-photo.getHeight());
        detailsView.setTranslationY(-detailsView.getHeight());
        statsView.setAlpha(0);

        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        photo.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        detailsView.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        statsView.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
    }

    private void animateItems(boolean animate) {
        if (isGridMode) {
            gridAdapter.updateItems(animate);
        } else {
            feedAdapter.updateItems(animate);
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        selectedPosition = position;
        switchListMode();
    }

    @Override
    public void onCommentsClick(View v, int position) {
        final Intent intent = new Intent(this, Comments.class);
        Feed f = feedArrayList.get(position);
        intent.putExtra("postId", f.getPostId());
        intent.putExtra("poster", f.getName());
        intent.putExtra("description", f.getDescription());
        intent.putExtra("username", f.getUsername());
        intent.putExtra("creation", f.getCreation());
        intent.putExtra("icon", f.getIcon());
        intent.putExtra("isDisabled", f.isCommentsEnable());
        intent.putExtra("isPublicAllowed", f.isPublicCommentsAllowed());
        intent.putExtra("isFollowing", f.isFollowing());
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(Config.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, int position) {

    }

    @Override
    public void onProfileClick(View v, int position) {
        // Nothing happens
    }

    @Override
    public void onLikeClick(View v, final int position, final int action) {
        StringRequest request = new StringRequest(Request.Method.PUT, Config.UPDATE_LIKE.replace(":id", feedArrayList.get(position).getPostId()), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        feedArrayList.get(position).setLiked(action == 1);
                    } else {
                        Log.e("Home", "Server response: " + obj.getString("code"));
                        feedAdapter.notifyItemChanged(position);
                        Toast.makeText(UserProfile.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Home", "Unexpected error: " + ex.getMessage());
                    feedAdapter.notifyItemChanged(position);
                    Toast.makeText(UserProfile.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Home", "Unexpected error: " + error.getMessage());
                feedAdapter.notifyItemChanged(position);
                Toast.makeText(UserProfile.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", String.valueOf(action));
                return params;
            }

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

    @Override
    public void onLikesClick(View v, int position) {
        final Intent intent = new Intent(this, Likes.class);
        Feed f = feedArrayList.get(position);
        intent.putExtra("postId", f.getPostId());
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_block) {
            new AlertDialog.Builder(this)
                    .setTitle("Block :user?".replace(":user", strName))
                    .setMessage("Do you really want to block :user?".replace(":user", strName))
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog.setIndeterminate(true);
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setMessage("Please wait for a moment...");
                            progressDialog.show();
                            StringRequest request = new StringRequest(Request.Method.PUT, Config.BLOCK_USER.replace(":id", strId), new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    progressDialog.dismiss();
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (!obj.getBoolean("error")) {
                                            Toast.makeText(UserProfile.this, ":user has been blocked.".replace(":user", strName), Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(UserProfile.this, "Your request was not proceed.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException ex) {
                                        Log.e("UserProfile", "Error: " + ex.getMessage() + "\nResponse: " + response);
                                        Toast.makeText(UserProfile.this, "Your request was not proceed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.dismiss();
                                    Log.e("UserProfile", "Unexpected error: " + error.getMessage());
                                    Toast.makeText(UserProfile.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
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
                    }).show();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, Settings.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onHashTagPressed(String hashTag) {
        Intent i = new Intent(this, Hashtags.class);
        i.putExtra("hashtag", hashTag);
        startActivity(i);
    }
}
