package info.smemo.nowordschat.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;

import info.smemo.nbaseaction.adapter.NFragmentPagerAdapter;
import info.smemo.nbaseaction.base.NBaseCompatActivity;
import info.smemo.nbaseaction.util.StringUtil;
import info.smemo.nowordschat.R;
import info.smemo.nowordschat.action.UserInfoAction;
import info.smemo.nowordschat.appaction.controller.ImController;
import info.smemo.nowordschat.contract.MainContract;
import info.smemo.nowordschat.databinding.NavHeaderMainBinding;
import info.smemo.nowordschat.fragment.BookFragment;
import info.smemo.nowordschat.fragment.FindFragment;
import info.smemo.nowordschat.fragment.IndexFragment;
import info.smemo.nowordschat.presenter.BookPresenter;
import info.smemo.nowordschat.presenter.IndexPresenter;
import info.smemo.nowordschat.presenter.MainPresenter;

public class MainActivity extends NBaseCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener, TabLayout.OnTabSelectedListener, MainContract.View {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private IndexFragment indexFragment;
    private BookFragment bookFragment;
    private FindFragment findFragment;

    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImController.init(this);
        mPresenter = new MainPresenter(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        NavHeaderMainBinding navHeaderMainBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.nav_header_main, navigationView, false);
        navHeaderMainBinding.setUserBean(UserInfoAction.getUserInfo());
        navHeaderMainBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("user",UserInfoAction.getUserInfo());
                startActivity(intent);
            }
        });
        navigationView.addHeaderView(navHeaderMainBinding.getRoot());

        initFragmentAdapter();
        mPresenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 初始化fragment
     */
    private void initFragmentAdapter() {
        ArrayList<Fragment> list = new ArrayList<>();
        indexFragment = new IndexFragment();
        new IndexPresenter(indexFragment);

        bookFragment = new BookFragment();
        new BookPresenter(bookFragment);

        findFragment = new FindFragment();
        list.add(indexFragment);
        list.add(bookFragment);
        list.add(findFragment);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        NFragmentPagerAdapter adapter = new NFragmentPagerAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("消息"));
        tabLayout.addTab(tabLayout.newTab().setText("联系人"));
        tabLayout.addTab(tabLayout.newTab().setText("发现"));

        viewPager.addOnPageChangeListener(this);
        tabLayout.addOnTabSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("搜索好友");
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchActivity(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_add) {
            startActivity(new Intent(this, AddFriendActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_user:
                startActivity(new Intent(this, MyUserActivity.class));
                break;
            case R.id.nav_photo:
                break;
            case R.id.nav_collect:
                break;
            case R.id.nav_emoji:
                break;
            case R.id.nav_wallet:
                break;
            case R.id.nav_setting:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_about:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void findMenuClick(View view) {
        if (null != findFragment)
            findFragment.findMenuClick(view);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (tabLayout != null && tabLayout.getTabAt(position) != null)
            tabLayout.getTabAt(position).select();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        if (viewPager.getCurrentItem() != position) {
            viewPager.setCurrentItem(position, true);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {

    }

    @Override
    public void reLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void startSearchActivity(String query) {
        if (!StringUtil.isEmpty(query)) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("query", query);
            startActivity(intent);
        }
    }
}
