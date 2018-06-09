package com.softdev.instaphoto.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.softdev.instaphoto.Activity.Hashtags;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SearchHashtags extends Fragment {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    public String lastSearch;
    TextView txtStatus;

    public SearchHashtags() {
        // Required empty public constructor
    }

    public void updateSearch(String query) {
        if (!query.equals(""))
            checkHashtag(query);

        lastSearch = query;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_hashtags, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        txtStatus = (TextView) v.findViewById(R.id.txtStatus);
        txtStatus.setText("Use search bar to search for hashtags.");
        progressBar.setVisibility(View.GONE);
        return v;
    }

    private void checkHashtag(final String hashtag) {
        txtStatus.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.GET, Config.CHECK_HASHTAG.replace(":hashtag", hashtag), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        if (obj.getBoolean("isAvailable")) {
                            Intent i = new Intent(getContext(), Hashtags.class);
                            i.putExtra("hashtag", hashtag);
                            startActivity(i);
                            txtStatus.setText("Use search bar to search for hashtags.");
                            txtStatus.setVisibility(View.VISIBLE);
                        } else {
                            txtStatus.setText("No results found for #:hashtag".replace(":hashtag", hashtag));
                            txtStatus.setVisibility(View.VISIBLE);
                        }
                    } else {
                        txtStatus.setText("No results found for #:hashtag".replace(":hashtag", hashtag));
                        txtStatus.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException ex) {
                    txtStatus.setText("No results found for #:hashtag".replace(":hashtag", hashtag));
                    txtStatus.setVisibility(View.VISIBLE);
                    Log.e("SearchHashtags", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't connect to server", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtStatus.setText("No results found for #:hashtag".replace(":hashtag", hashtag));
                txtStatus.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.e("SearchHashtags", "Unexpected error: " + error.getMessage());
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
