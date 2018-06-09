package com.softdev.instaphoto.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Activity.Comments;
import com.softdev.instaphoto.Activity.Hashtags;
import com.softdev.instaphoto.Activity.Likes;
import com.softdev.instaphoto.Activity.Login;
import com.softdev.instaphoto.Activity.Settings;
import com.softdev.instaphoto.Adapter.ExploreGridAdapter;
import com.softdev.instaphoto.Adapter.FeedAdapter;
import com.softdev.instaphoto.Adapter.FeedItemAnimator;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment implements ExploreGridAdapter.OnExploreItemClickListener, FeedAdapter.OnFeedItemClickListener {

    TextView txtPosts;
    TextView txtFollowers;
    TextView txtFollowing;
    TextView txtName;
    TextView txtBio;
    CircleImageView photo;
    RecyclerView recyclerView;
    ImageView btnGrid, btnList, btnTags;
    ArrayList<Feed> feedArrayList;
    public boolean isGridMode = true;
    int selectedPosition;
    FeedAdapter feedAdapter;
    ExploreGridAdapter gridAdapter;

    public Profile() {
    }

    public void goBack() {
        if (!isGridMode) {
            switchGridMode();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);
        txtPosts = (TextView) v.findViewById(R.id.txtPosts);
        txtFollowers = (TextView) v.findViewById(R.id.txtFollowers);
        txtFollowing = (TextView) v.findViewById(R.id.txtFollowing);
        txtName = (TextView) v.findViewById(R.id.txtName);
        txtBio = (TextView) v.findViewById(R.id.txtBio);
        photo = (CircleImageView) v.findViewById(R.id.photo);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        btnGrid = (ImageView) v.findViewById(R.id.btnGrid);
        btnList = (ImageView) v.findViewById(R.id.btnList);
        setupListeners();
        setupView();
        loadFeed();
        return v;
    }

    private void setupView() {
        feedArrayList = new ArrayList<>();
        txtPosts.setText(AppHandler.getInstance().getDataManager().getString("totalPosts", "0"));
        txtFollowers.setText(AppHandler.getInstance().getDataManager().getString("followers", "0"));
        txtFollowing.setText(AppHandler.getInstance().getDataManager().getString("following", "0"));
        txtName.setText(AppHandler.getInstance().getDataManager().getString("name", "Unknown"));
        txtBio.setText(AppHandler.getInstance().getDataManager().getString("bio", "Unknown"));
        Picasso.with(getContext())
                .load(AppHandler.getInstance().getDataManager().getString("icon", ""))
                .error(R.drawable.ic_people)
                .into(photo);
        switchGridMode();
    }

    private void setupListeners() {
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
    }

    private void loadFeed() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_MY_FEED, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONObject userObj = obj.getJSONObject("user");
                        JSONArray feeds = obj.getJSONArray("feeds");
                        User u = new User();
                        u.setId(userObj.getString("id"));
                        u.setUsername(userObj.getString("username"));
                        u.setName(userObj.getString("name"));
                        u.setEmail(userObj.getString("email"));
                        u.setCreation(userObj.getString("created_At"));
                        u.setIcon(userObj.getString("icon"));
                        u.setBio(userObj.getString("bio"));
                        u.setPosts(userObj.getString("totalPosts"));
                        u.setFollowers(userObj.getString("followers"));
                        u.setFollowing(userObj.getString("following"));
                        AppHandler.getInstance().updateProfile(u);
                        setupView();
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
                                feedArrayList.add(f);
                            }
                            animateItems(true);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ex) {
                    Log.e("Profile", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Profile", "Unexpected error: " + error.getMessage());
                Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }

    private void switchGridMode() {
        isGridMode = true;
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setPadding(2, 2, 2, 2);
        gridAdapter = new ExploreGridAdapter(getActivity(), feedArrayList);
        recyclerView.setAdapter(gridAdapter);
        gridAdapter.setOnExploreItemClickListener(this);
    }

    private void switchListMode() {
        isGridMode = false;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setPadding(0, 0, 0, 0);
        feedAdapter = new FeedAdapter(getActivity(), feedArrayList);
        recyclerView.setItemAnimator(new FeedItemAnimator());
        feedAdapter.setOnFeedItemClickListener(this);
        recyclerView.setAdapter(feedAdapter);
        animateItems(false);
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
        recyclerView.smoothScrollToPosition(selectedPosition);
    }

    @Override
    public void onLikesClick(View v, int position) {
        final Intent intent = new Intent(getContext(), Likes.class);
        Feed f = feedArrayList.get(position);
        intent.putExtra("postId", f.getPostId());
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onCommentsClick(View v, int position) {
        final Intent intent = new Intent(getContext(), Comments.class);
        Feed f = feedArrayList.get(position);
        intent.putExtra("postId", f.getPostId());
        intent.putExtra("poster", f.getName());
        intent.putExtra("description", f.getDescription());
        intent.putExtra("username", f.getUsername());
        intent.putExtra("creation", f.getCreation());
        intent.putExtra("icon", f.getIcon());
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(Config.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, final int position) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    new AlertDialog.Builder(getActivity())
                        .setTitle("Delete post")
                        .setMessage("Are you sure that you want to delete this post?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String postId = feedArrayList.get(position).getPostId();
                                feedArrayList.remove(position);
                                StringRequest request = new StringRequest(Request.Method.PUT, Config.DELETE_POST.replace(":postId", postId), new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject obj = new JSONObject(response);
                                            if (!obj.getBoolean("error")) {
                                                Toast.makeText(getActivity(), "Your post has been deleted.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e("Profile", "Server response: " + obj.getString("code"));
                                                Toast.makeText(getActivity(), "Unable to delete your post.", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException ex) {
                                            Log.e("Profile", "Error: " + ex.getMessage() + "\nResponse: " + response);
                                            Toast.makeText(getActivity(), "Unable to delete your post.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Profile", "Error: " + error.getMessage());
                                        Toast.makeText(getActivity(), "Unable to delete your post.", Toast.LENGTH_SHORT).show();
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
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                return true;
            }
        });
        popupMenu.inflate(R.menu.menu_my_post);
        popupMenu.show();
    }

    @Override
    public void onProfileClick(View v, int position) {
        // Nothing
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
                        Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Home", "Unexpected error: " + ex.getMessage());
                    feedAdapter.notifyItemChanged(position);
                    Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Home", "Unexpected error: " + error.getMessage());
                feedAdapter.notifyItemChanged(position);
                Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
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
    public void onHashTagPressed(String hashTag) {
        Intent i = new Intent(getActivity(), Hashtags.class);
        i.putExtra("hashtag", hashTag);
        startActivity(i);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), Settings.class));
            return true;
        } else if (id == R.id.action_logout) {
            // Logout
            new AlertDialog.Builder(getActivity())
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            AppHandler.getInstance().getDBHandler().resetDatabase();
                            AppHandler.getInstance().getDataManager().clear();
                            startActivity(new Intent(getActivity(), Login.class));
                            getActivity().finish();
                        }
                    }, 2000);
                    progressDialog.dismiss();
                }
            }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
