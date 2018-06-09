package com.softdev.instaphoto.Fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Activity.UserProfile;
import com.softdev.instaphoto.Adapter.SearchPeoplesAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.ItemDivider;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class SearchPeoples extends Fragment {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    ArrayList<User> peoples;
    TextView txtStatus;
    SearchPeoplesAdapter pAdapter;
    public String lastSearch;

    public SearchPeoples() {
    }

    public void updateSearch(String query) {
        if (!query.equals(""))
            SearchOnline(query);

        lastSearch = query;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_peoples, container, false);

        // Binding views
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        txtStatus = (TextView) v.findViewById(R.id.txtStatus);
        txtStatus.setText("Type name, username or email address of the person you want to search.");
        peoples = new ArrayList<>();
        pAdapter = new SearchPeoplesAdapter(getActivity(), peoples);
        pAdapter.setOnProfileItemClickListener(new SearchPeoplesAdapter.OnProfileItemClickListener() {
            @Override
            public void onProfileClick(View v, int position) {
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                startingLocation[0] += v.getWidth() / 2;
                UserProfile.startUserProfileFromLocation(startingLocation, getActivity(), peoples.get(position).getUsername(), peoples.get(position).getName());
                getActivity().overridePendingTransition(0, 0);
            }
        });

        // Setting up peoples and adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(pAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getActivity()));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    pAdapter.setAnimationsLocked(true);
                }
            }
        });
        return v;
    }

    public void SearchOnline(String query) {
        txtStatus.setText("No results found.");
        recyclerView.setVisibility(View.INVISIBLE);
        txtStatus.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.GET, Config.SEARCH_PEOPLE.replace(":toFind", query), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                peoples.clear();
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONArray usersArray = obj.getJSONArray("users");
                        if (usersArray.length() != 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            for (int i = 0; i < usersArray.length(); i++) {
                                JSONObject user = usersArray.getJSONObject(i);
                                User u = new User();
                                u.setId(user.getString("id"));
                                u.setUsername(user.getString("username"));
                                u.setName(user.getString("name"));
                                u.setEmail(user.getString("email"));
                                u.setIcon(user.getString("icon"));
                                u.setCreation(user.getString("creation"));
                                peoples.add(u);
                            }
                            pAdapter.updateItems();
                            txtStatus.setVisibility(View.GONE);
                        } else {
                            txtStatus.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException ex) {
                    txtStatus.setVisibility(View.VISIBLE);
                    Log.e("SearchPeoples", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't connect to server", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtStatus.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.e("SearchPeoples", "Unexpected error: " + error.getMessage());
                Toast.makeText(getActivity(), "Couldn't connect to server", Toast.LENGTH_SHORT).show();
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
