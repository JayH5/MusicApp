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

    private SearchFragment mSearchFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String query = getIntent().getStringExtra(SearchManager.QUERY);

        ActionBar ab = getActionBar();
        ab.setSubtitle("\"" + query + "\"");
        ab.setDisplayHomeAsUpEnabled(true);

        mSearchFragment = SearchFragment.newInstance(query);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSearchFragment)
                .commit();
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
        getSearchView().setOnQueryTextListener(mSearchFragment);
        return result;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String query = intent.getStringExtra(SearchManager.QUERY);
        mSearchFragment.onQueryTextChange(query);
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
        getActionBar().setSubtitle("\"" + mSearchFragment.getFilterString() + "\"");
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        return false;
    }

}
