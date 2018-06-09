package com.softdev.instaphoto.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.softdev.instaphoto.Adapter.PageFragmentAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.DataStorage;
import com.softdev.instaphoto.Fragment.Explore;
import com.softdev.instaphoto.Fragment.Followers;
import com.softdev.instaphoto.Fragment.Gallery;
import com.softdev.instaphoto.Fragment.Home;
import com.softdev.instaphoto.Fragment.Profile;
import com.softdev.instaphoto.R;
import com.softdev.instaphoto.Utils;

import static com.softdev.instaphoto.Configuration.Config.ANIM_DURATION_TOOLBAR;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, EditText.OnFocusChangeListener {

    TabLayout tabLayout;
    ViewPager viewPager;
    PageFragmentAdapter adapter;
    Home fragmentHome;
    Explore fragmentExplore;
    Followers fragmentFollowers;
    Gallery fragmentGallery;
    Profile fragmentProfile;
    Toolbar toolbar;
    LinearLayout tabsLayout;
    ImageView logo;
    ImageView imgExplore;
    EditText txtExplore;
    boolean pendingAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding Views
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        logo = (ImageView) findViewById(R.id.logo);
        imgExplore = (ImageView) findViewById(R.id.imgExplore);
        txtExplore = (EditText) findViewById(R.id.txtExplore);
        tabsLayout = (LinearLayout) findViewById(R.id.tabsLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        imgExplore.setVisibility(View.GONE);
        txtExplore.setVisibility(View.GONE);
        txtExplore.setOnFocusChangeListener(this);
        txtExplore.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    fragmentExplore.updateSearch(txtExplore.getText().toString());
                    return true;
                }
                return false;
            }
        });
        setupTabs();
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);
        setupTabIcons();

        if (savedInstanceState == null) {
            pendingAnimation = true;
        }
    }

    private void setupTabs() {
        adapter = new PageFragmentAdapter(getSupportFragmentManager());
        if (fragmentHome == null) {
            fragmentHome = new Home();
        }
        if (fragmentExplore == null) {
            fragmentExplore = new Explore();
            fragmentExplore.setOnExploreViewChanged(new Explore.OnExploreViewChanged() {
                @Override
                public void ViewChanged(int v) {
                    if (viewPager.getCurrentItem() == 1) {
                        setupToolbar(v);
                    }
                }

                @Override
                public void onTabsChanged(int t) {
                    if (t == 1) {
                        txtExplore.setText("");
                        txtExplore.setHint("Search Peoples");
                        txtExplore.setText(fragmentExplore.getLastSearch());
                    } else if (t == 2) {
                        txtExplore.setText("");
                        txtExplore.setHint("Search Hashtag");
                        txtExplore.setText(fragmentExplore.getLastSearch());
                    }
                }
            });
        }
        if (fragmentGallery == null) {
            fragmentGallery = new Gallery();
        }
        if (fragmentFollowers == null) {
            fragmentFollowers = new Followers();
        }
        if (fragmentProfile == null) {
            fragmentProfile = new Profile();
        }
        adapter.addFragment(fragmentHome, null);
        adapter.addFragment(fragmentExplore, null);
        adapter.addFragment(fragmentGallery, null);
        adapter.addFragment(fragmentFollowers, null);
        adapter.addFragment(fragmentProfile, null);
        viewPager.setAdapter(adapter);
    }

    private void setupToolbar(int v) {
        if (v == 1) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
            toolbar.setTitle("Explore");
            imgExplore.setVisibility(View.GONE);
            txtExplore.setVisibility(View.GONE);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentExplore.goBack();
                }
            });
        } else {
            toolbar.setNavigationIcon(null);
            imgExplore.setVisibility(View.VISIBLE);
            txtExplore.setVisibility(View.VISIBLE);
            txtExplore.clearFocus();
            txtExplore.setText("");
            txtExplore.setHint("Search");
            toolbar.setNavigationOnClickListener(null);
        }
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.tab_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_explore);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_tab_gallery);
        tabLayout.getTabAt(3).setIcon(R.drawable.tab_friend);
        tabLayout.getTabAt(4).setIcon(R.drawable.tab_profile);
    }

    private void changeState(int s) {
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("");
        logo.setVisibility(View.VISIBLE);
        imgExplore.setVisibility(View.GONE);
        txtExplore.setVisibility(View.GONE);
        toolbar.setNavigationOnClickListener(null);
        if (s == 1) {
            toolbar.setTitle("");
        } else if (s == 2) {
            fragmentExplore.clearSearch();
            fragmentExplore.selectedTab = 0;
            logo.setVisibility(View.GONE);
            txtExplore.setText("");
            imgExplore.setVisibility(View.VISIBLE);
            txtExplore.setVisibility(View.VISIBLE);
            txtExplore.clearFocus();
            if (!fragmentExplore.isGridMode) {
                setupToolbar(1);
            }
        } else if (s == 3) {

        } else if (s == 4) {
            logo.setVisibility(View.GONE);
            toolbar.setTitle(AppHandler.getInstance().getDataManager().getString("name", ""));
        } else if (s == 5) {
            logo.setVisibility(View.GONE);
            toolbar.setTitle(AppHandler.getInstance().getDataManager().getString("name", ""));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (pendingAnimation) {
            pendingAnimation = false;
            setupAnimation();
        }
        return true;
    }

    private void setupAnimation() {
        int actionbarSize = Utils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);
        tabsLayout.setTranslationX(-tabsLayout.getX());
        toolbar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);
        tabsLayout.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400)
                .start();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        viewPager.setCurrentItem(position);
        changeState(position + 1);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == txtExplore) {
            if (hasFocus) {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       onBackPressed();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(txtExplore.getWindowToken(), 0);
                    }
                });
                fragmentExplore.setupSearch();
                imgExplore.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtExplore.getWindowToken(), 0);
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            if (fragmentExplore != null) {
                if (fragmentExplore.isSearchMode) {
                    fragmentExplore.goBack();
                    setupToolbar(0);
                    fragmentExplore.clearSearch();
                    return;
                }
            }
        }
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(0);
        }
    }
}
