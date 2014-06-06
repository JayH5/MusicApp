package za.jamie.soundstage.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.SearchFragment;

/**
 * Created by jamie on 2014/01/12.
 */
public class SearchActivity extends MusicActivity implements
        SearchView.OnQueryTextListener {

    private static final String STATE_FILTER = "state_filter";

    private SearchFragment mSearchFragment;
    private String mFilterString;

    private SearchView mSearchView;

    private void showSoftInputUnchecked() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            Method showSoftInputUnchecked = null;
            try {
                showSoftInputUnchecked = imm.getClass()
                        .getMethod("showSoftInputUnchecked", int.class, ResultReceiver.class);
            } catch (NoSuchMethodException e) {
                Log.e("Search", "NoSuchMethodException", e);
            }

            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, 0, null);
                    Log.d("Search", "showSoftInputUnchecked called successfully");
                } catch (IllegalAccessException e) {
                    Log.e("Search", "IllegalAccessException", e);
                } catch (InvocationTargetException e) {
                    Log.e("Search", "InvocationTargetException", e);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFilterString = savedInstanceState.getString(STATE_FILTER);
            if (mSearchView != null) {
                mSearchView.setQuery(mFilterString, false);
            }
        }

        mSearchFragment = SearchFragment.newInstance(mFilterString);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSearchFragment)
                .commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER, mFilterString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_activity, menu);

    	SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);

        if (mFilterString != null) {
            mSearchView.setQuery(mFilterString, false);
        }

        mSearchView.requestFocus();
        mSearchView.post(new Runnable() {
            @Override
            public void run() {
                showSoftInputUnchecked();
            }
        });

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String query = intent.getStringExtra(SearchManager.QUERY);
        onQueryTextChange(query);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        if (mSearchView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        mFilterString = query;
        mSearchFragment.setFilterString(mFilterString);
        return true;
    }

}
