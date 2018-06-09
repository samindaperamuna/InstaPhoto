package com.softdev.instaphoto.Activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Adapter.CommentsAdapter;
import com.softdev.instaphoto.Adapter.NotificationsAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Notification;
import com.softdev.instaphoto.ItemDivider;
import com.softdev.instaphoto.R;
import com.softdev.instaphoto.RevealBackgroundView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class Notifications extends AppCompatActivity implements RevealBackgroundView.OnStateChangeListener, NotificationsAdapter.OnNotificationItemClickListener {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    Toolbar toolbar;
    RevealBackgroundView revealView;
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    TextView emptyNotification;
    ArrayList<Notification> notificationArrayList = new ArrayList<>();
    NotificationsAdapter notificationsAdapter;

    public static void startNotificationsFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, Notifications.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notificationsAdapter = new NotificationsAdapter(this, notificationArrayList);
        notificationsAdapter.setOnNotificationItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(notificationsAdapter);
        recyclerView.addItemDecoration(new ItemDivider(this));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    notificationsAdapter.setAnimationsLocked(true);
                }
            }
        });
        revealView = (RevealBackgroundView) findViewById(R.id.revealView);
        emptyNotification = (TextView) findViewById(R.id.emptyNotification);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent, R.color.colorAccentLight);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotifications();
            }
        });
        loadNotifications();
        loadNotificationLocally();
        setupRevealBackground(savedInstanceState);
    }

    private void loadNotifications() {
        swipeRefreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_NOTIFICATIONS.replace(":from", "0"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONArray notifications = obj.getJSONArray("notifications");
                        if (notifications.length() != 0) {
                            for (int i = 0; i < notifications.length(); i++) {
                                JSONObject notification = notifications.getJSONObject(i);
                                Notification n = new Notification();
                                n.id = notification.getString("id");
                                n.user = notification.getString("user");
                                n.postId = notification.getString("postId");
                                n.userId = notification.getString("userId");
                                n.username = notification.getString("username");
                                n.name = notification.getString("name");
                                n.icon = notification.getString("icon");
                                n.action = notification.getString("action");
                                n.creation = notification.getString("creation");
                                AppHandler.getInstance().getDBHandler().addNotification(n);
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        loadNotificationLocally();
                    } else {
                        Toast.makeText(Notifications.this, "Unable to load notifications.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ex) {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e("Notifications", "Unable to to parse response, response: " + response);
                    Toast.makeText(Notifications.this, "Unable to load notifications.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("Notifications", "Error: " + error.getMessage());
                Toast.makeText(Notifications.this, "Unable to load notifications.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }

    private void loadNotificationLocally() {
        notificationArrayList.clear();
        notificationArrayList.addAll(AppHandler.getInstance().getDBHandler().getNotifications());
        if (notificationArrayList.size() > 0) {
            emptyNotification.setVisibility(View.GONE);
        } else {
            emptyNotification.setVisibility(View.VISIBLE);
        }
        notificationsAdapter.updateItems();
    }

    @Override
    public void onItemClick(View v, int position, int action) {
        if (action == 1) {

        } else if (action == 2) {

        } else if (action == 3) {
            int[] startingLocation = new int[2];
            v.getLocationOnScreen(startingLocation);
            startingLocation[0] += v.getWidth() / 2;
            UserProfile.startUserProfileFromLocation(startingLocation, this, notificationArrayList.get(position).username, notificationArrayList.get(position).name);
            overridePendingTransition(0, 0);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notification) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyNotification.setVisibility(View.GONE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
