package com.softdev.instaphoto.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.softdev.instaphoto.Adapter.ExploreGridAdapter;
import com.softdev.instaphoto.Adapter.FeedAdapter;
import com.softdev.instaphoto.Adapter.FeedItemAnimator;
import com.softdev.instaphoto.Adapter.PageFragmentAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Explore extends Fragment implements ExploreGridAdapter.OnExploreItemClickListener, FeedAdapter.OnFeedItemClickListener,
TabLayout.OnTabSelectedListener {

    RecyclerView recyclerView;
    ArrayList<Feed> exploreList;
    ProgressBar progressBar;
    FeedAdapter feedAdapter;
    ExploreGridAdapter gridAdapter;
    TabLayout tabLayout;
    ViewPager viewPager;
    public boolean isGridMode = true;
    int selectedPosition;
    private OnExploreViewChanged onExploreViewChanged;
    SearchHashtags fragmentHashtags;
    SearchPeoples fragmentPeoples;
    PageFragmentAdapter adapter;
    public boolean isSearchMode = false;
    public int selectedTab;
    public Explore() {
        // Required empty public constructor
    }

    public void goBack() {
        clearSearch();
        if (!isGridMode) {
            switchGridMode();
        }
    }

    public String getLastSearch() {
        if (isSearchMode) {
            if (viewPager.getCurrentItem() == 0) {
                return fragmentPeoples.lastSearch;
            } else {
                return fragmentHashtags.lastSearch;
            }
        }
        return "";
    }

    public interface OnExploreViewChanged {
        void ViewChanged(int v);
        void onTabsChanged(int t);
    }

    public void setOnExploreViewChanged(OnExploreViewChanged onExploreViewChanged) {
        this.onExploreViewChanged = onExploreViewChanged;
    }

    public void updateSearch(String query) {
        if (viewPager.getCurrentItem() == 0) {
            fragmentPeoples.updateSearch(query);
        } else {
            fragmentHashtags.updateSearch(query);
        }
    }

    public void setupSearch() {
        viewPager.setCurrentItem(0);
        isSearchMode = true;
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    public void clearSearch() {
        isSearchMode = false;
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        switchGridMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        exploreList = new ArrayList<>();
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        tabLayout = (TabLayout) v.findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        setupTabs();
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);
        switchGridMode();
        loadPopularFeed();
        return v;
    }

    private void setupTabs() {
        adapter = new PageFragmentAdapter(getChildFragmentManager());
        if (fragmentPeoples == null) {
            fragmentPeoples = new SearchPeoples();
        }
        if (fragmentHashtags == null) {
            fragmentHashtags = new SearchHashtags();
        }
        adapter.addFragment(fragmentPeoples, "Peoples");
        adapter.addFragment(fragmentHashtags, "Hashtags");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        selectedTab = position;
        onExploreViewChanged.onTabsChanged(position + 1);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void switchGridMode() {
        isGridMode = true;
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setPadding(2, 2, 2, 2);
        gridAdapter = new ExploreGridAdapter(getActivity(), exploreList);
        recyclerView.setAdapter(gridAdapter);
        gridAdapter.setOnExploreItemClickListener(this);
        onExploreViewChanged.ViewChanged(0);
    }

    private void switchListMode() {
        isGridMode = false;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setPadding(0, 0, 0, 0);
        feedAdapter = new FeedAdapter(getActivity(), exploreList);
        recyclerView.setAdapter(feedAdapter);
        recyclerView.setItemAnimator(new FeedItemAnimator());
        animateItems(false);
        feedAdapter.setOnFeedItemClickListener(this);
        recyclerView.smoothScrollToPosition(selectedPosition);
        onExploreViewChanged.ViewChanged(1);
    }

    private void loadPopularFeed() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_POPULAR_FEED.replace(":from", "0"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
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
                                exploreList.add(f);
                            }
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            animateItems(gridAdapter.getItemCount()<=0);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Explore", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Explore", "Unexpected error: " + error.getMessage());
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
        final Intent intent = new Intent(getContext(), Comments.class);
        Feed f = exploreList.get(position);
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
    public void onMoreClick(View v, int position) {

    }

    @Override
    public void onProfileClick(View v, int position) {

    }

    @Override
    public void onLikeClick(View v, final int position, final int action) {
        StringRequest request = new StringRequest(Request.Method.PUT, Config.UPDATE_LIKE.replace(":id", exploreList.get(position).getPostId()), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        exploreList.get(position).setLiked(action == 1);
                    } else {
                        Log.e("Explore", "Server response: " + obj.getString("code"));
                        feedAdapter.notifyItemChanged(position);
                        Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Explore", "Unexpected error: " + ex.getMessage());
                    feedAdapter.notifyItemChanged(position);
                    Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Explore", "Unexpected error: " + error.getMessage());
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
    public void onLikesClick(View v, int position) {
        final Intent intent = new Intent(getContext(), Likes.class);
        Feed f = exploreList.get(position);
        intent.putExtra("postId", f.getPostId());
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onHashTagPressed(String hashTag) {
        Intent i = new Intent(getActivity(), Hashtags.class);
        i.putExtra("hashtag", hashTag);
        startActivity(i);
    }
}
