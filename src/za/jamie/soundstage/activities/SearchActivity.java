package za.jamie.soundstage.activities;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import za.jamie.soundstage.fragments.SearchFragment;

/**
 * Created by jamie on 2014/01/12.
 */
public class SearchActivity extends MusicActivity implements
        SearchView.OnQueryTextListener {

    private static final String STATE_FILTER = "state_filter";

    private SearchFragment mSearchFragment;
    private String mFilterString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFilterString = savedInstanceState.getString(STATE_FILTER);
        } else {
            mFilterString = getIntent().getStringExtra(SearchManager.QUERY);
        }

        mSearchFragment = SearchFragment.newInstance(mFilterString);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSearchFragment)
                .commit();

        ActionBar ab = getActionBar();
        ab.setSubtitle("\"" + mFilterString + "\"");
        ab.setDisplayHomeAsUpEnabled(true);
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
        boolean result = super.onCreateOptionsMenu(menu);
        getSearchView().setOnQueryTextListener(this);
        return result;
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

        SearchView searchView = getSearchView();
        if (searchView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
            searchView.clearFocus();
        }
        getActionBar().setSubtitle("\"" + mFilterString + "\"");
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
