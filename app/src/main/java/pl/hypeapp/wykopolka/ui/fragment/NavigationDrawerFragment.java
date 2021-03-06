package pl.hypeapp.wykopolka.ui.fragment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.grandcentrix.thirtyinch.TiFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.hypeapp.wykopolka.App;
import pl.hypeapp.wykopolka.R;
import pl.hypeapp.wykopolka.adapter.NavigationDrawerRecyclerAdapter;
import pl.hypeapp.wykopolka.model.NavigationItem;
import pl.hypeapp.wykopolka.model.UserStats;
import pl.hypeapp.wykopolka.presenter.NavigationDrawerPresenter;
import pl.hypeapp.wykopolka.ui.activity.AllBooksActivity;
import pl.hypeapp.wykopolka.ui.activity.BookPanelActivity;
import pl.hypeapp.wykopolka.ui.activity.DashboardActivity;
import pl.hypeapp.wykopolka.ui.activity.FaqActivity;
import pl.hypeapp.wykopolka.ui.activity.ProfileSettingsActivity;
import pl.hypeapp.wykopolka.ui.activity.RandomBookActivity;
import pl.hypeapp.wykopolka.ui.activity.SearchBookActivity;
import pl.hypeapp.wykopolka.ui.activity.StatsAndTopUsersActivity;
import pl.hypeapp.wykopolka.view.NavigationDrawerView;

public class NavigationDrawerFragment extends TiFragment<NavigationDrawerPresenter, NavigationDrawerView>
        implements NavigationDrawerView {
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    private static String[] navigationDrawerTitles;
    private static TypedArray navigationDrawerIcons;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private View mContainer;
    private boolean mDrawerOpened = false;
    private NavigationDrawerRecyclerAdapter mRecyclerAdapter;
    private String mAvatarUrl;
    private String mUserLogin;
    private NavigationDrawerPresenter mNavigationDrawerPresenter;
    @BindView(R.id.drawer_list) RecyclerView mRecyclerView;
    FloatingActionButton mFab;

    public NavigationDrawerFragment() {
    }

    @NonNull
    @Override
    public NavigationDrawerPresenter providePresenter() {
        String accountKey = App.readFromPreferences(getActivity(), "user_account_key", null);
        mNavigationDrawerPresenter = new NavigationDrawerPresenter(accountKey);
        return mNavigationDrawerPresenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = App.readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, false);
        mAvatarUrl = App.readFromPreferences(getActivity(), "user_avatar", "");
        mUserLogin = App.readFromPreferences(getActivity(), "user_login", "unknown");
        mFromSavedInstanceState = savedInstanceState != null;
        navigationDrawerIcons = getResources().obtainTypedArray(R.array.navigation_drawer_icons);
        navigationDrawerTitles = getResources().getStringArray(R.array.navigation_drawer_items);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.bind(this, view);
        mRecyclerAdapter = new NavigationDrawerRecyclerAdapter(getActivity(), getNavigationItems(), getUserData(), new ClickItemHandler());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        return view;
    }

    public static List<NavigationItem> getNavigationItems() {
        List<NavigationItem> data = new ArrayList<>();
        TypedArray icons = navigationDrawerIcons;
        String[] titles = navigationDrawerTitles;

        for (int i = 0; i < titles.length && i < icons.length(); i++) {
            NavigationItem current = new NavigationItem();
            current.title = titles[i];
            current.iconId = icons.getResourceId(i, 0);
            data.add(current);
        }
        return data;
    }

    public Map<String, String> getUserData() {
        Map<String, String> userData = new HashMap<>();
        userData.put("user_login", mUserLogin);
        userData.put("user_avatar", mAvatarUrl);
        return userData;
    }

    public void create(DrawerLayout drawerLayout, final Toolbar toolbar, int fragmentId) {
        mContainer = getActivity().findViewById(fragmentId);
        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add_book);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    App.saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer);
                }
                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toggleTranslateFAB(mFab, slideOffset);
            }

            private void toggleTranslateFAB(FloatingActionButton fab, float slideOffset) {
                if (fab != null) {
                    fab.setTranslationX(slideOffset * 200);
                }
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
                if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
                    mDrawerLayout.openDrawer(mContainer);
                }
            }
        });
    }

    @Override
    public void setUserStats(UserStats userStats) {
        mRecyclerAdapter.setUserStats(userStats);
    }

    @Override
    public void saveUserStatsToPreferences(UserStats userStats) {
        App.saveToPreferences(getActivity(), "user_stats_on_wish_list", userStats.getAddedOnWishList());
        App.saveToPreferences(getActivity(), "user_stats_added", userStats.getAddedBooks());
        App.saveToPreferences(getActivity(), "user_stats_read", userStats.getReadBooks());
    }

    @Override
    public void setUserStatsFromPreferences() {
        UserStats userStats = new UserStats();
        userStats.setAddedOnWishList(App.readIntegerFromPreferences(getActivity(), "user_stats_on_wish_list", 0));
        userStats.setAddedBooks(App.readIntegerFromPreferences(getActivity(), "user_stats_added", 0));
        userStats.setReadBooks(App.readIntegerFromPreferences(getActivity(), "user_stats_read", 0));
        mRecyclerAdapter.setUserStats(userStats);
    }

    public class ClickItemHandler {

        public void handleIntent(final int position) {
            mDrawerLayout.closeDrawers();
            mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {

                }

                @Override
                public void onDrawerOpened(View drawerView) {

                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    startIntentActivity(position);
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });

        }

        private void startIntentActivity(int position) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            switch (position) {
                case 0:
                    intent.setClass(getActivity(), DashboardActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 1:
                    intent.setClass(getActivity(), SearchBookActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 2:
                    intent.setClass(getActivity(), BookPanelActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 3:
                    intent.setClass(getActivity(), AllBooksActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 4:
                    intent.setClass(getActivity(), RandomBookActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 5:
                    intent.setClass(getActivity(), FaqActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 6:
                    intent.setClass(getActivity(), StatsAndTopUsersActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case 7:
                    intent.setClass(getActivity(), ProfileSettingsActivity.class);
                    getActivity().startActivity(intent);
                    break;
                default:
                    intent.setClass(getActivity(), DashboardActivity.class);
                    getActivity().startActivity(intent);
                    break;
            }
        }
    }
}
