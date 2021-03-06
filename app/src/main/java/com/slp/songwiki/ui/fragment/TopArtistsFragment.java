package com.slp.songwiki.ui.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.slp.songwiki.BuildConfig;
import com.slp.songwiki.R;
import com.slp.songwiki.adapter.ArtistAdapter;
import com.slp.songwiki.model.Artist;
import com.slp.songwiki.ui.activity.ArtistActivity;
import com.slp.songwiki.ui.activity.SearchResultsActivity;
import com.slp.songwiki.utilities.ArtistUtils;
import com.slp.songwiki.utilities.NetworkUtils;
import com.slp.songwiki.utilities.PreferenceUtils;
import com.slp.songwiki.utilities.SongWikiConstants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Lakshmiprasad on 4/30/2017.
 */

public class TopArtistsFragment extends Fragment implements SongWikiFragmentable,SongWikiConstants, LoaderManager.LoaderCallbacks<List<Artist>>, ArtistAdapter.ListItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int TOP_ARTISTS = 321;
    private View rootView;
    private LoaderManager loaderManager;
    private List<Artist> topArtists;
    private Artist topArtist;
    @Bind(R.id.rv_artists)
    RecyclerView rvArtists;
    @Bind(R.id.artist_loader)
    ProgressBar artistLoader;
    @Bind(R.id.error)
    TextView error;
    private SearchView searchView;
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseRemoteConfig mFBConfig;
    public static final String ACTION_DATA_UPDATED = "com.slp.songwiki.ACTION_DATA_UPDATED";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView: ", String.valueOf(topArtists));
        super.onCreateView(inflater, container, savedInstanceState);
        if (null == rootView)
            rootView = inflater.inflate(R.layout.fragment_artist_top, container, false);
        ButterKnife.bind(this, rootView);
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            error.setVisibility(View.GONE);
            loaderManager = getActivity().getSupportLoaderManager();
            loaderManager.initLoader(TOP_ARTISTS, null, this);
            setupFB();
            setHasOptionsMenu(true);
        } else {
            error.setVisibility(View.VISIBLE);
        }
        PreferenceUtils.getPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        return rootView;
    }

    @Override
    public void onDestroy() {
        PreferenceUtils.getPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    private void setupFB() {
        mFBConfig = FirebaseRemoteConfig.getInstance();
        firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFBConfig.setConfigSettings(configSettings);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.artist_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_artist);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        //searchView.setQueryHint(getString(R.string.artist_title));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ((ArtistAdapter) rvArtists.getAdapter()).getFilter().filter(query);
                Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
                intent.putExtra(SEARCH_QUERY, query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArtistAdapter adapter = (ArtistAdapter) rvArtists.getAdapter();
                if (null != adapter)
                    adapter.getFilter().filter(newText);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<List<Artist>>(getActivity()) {
            List<Artist> artists;

            @Override
            protected void onStartLoading() {
                if (null != artists) {
                    deliverResult(artists);
                } else {
                    rvArtists.setVisibility(View.GONE);
                    artistLoader.setVisibility(View.VISIBLE);

                    forceLoad();
                }
            }

            @Override
            public List<Artist> loadInBackground() {
                try {
                    artists = ArtistUtils.getTopChartArtists(getActivity());
                    Log.i("loadInBackground: ", artists.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                return artists;
            }

            @Override
            public void deliverResult(List<Artist> data) {
                artists = data;
                super.deliverResult(data);
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> artists) {
        artistLoader.setVisibility(View.GONE);
        rvArtists.setVisibility(View.VISIBLE);
        initializeRecyclerView(artists);

    }

    @Override
    public void onLoaderReset(Loader<List<Artist>> loader) {
        topArtists = null;

    }

    private void initializeRecyclerView(List<Artist> artists) {
        if (null != artists && artists.size() > 0) {
            error.setVisibility(View.GONE);
            topArtists = artists;
            topArtist = artists.get(0);
            rvArtists.setAdapter(new ArtistAdapter(artists, this));
            int gridSize = getResources().getInteger(R.integer.artist_grid);
            rvArtists.setLayoutManager(new GridLayoutManager(getActivity(), gridSize));
            rvArtists.setHasFixedSize(true);
            setNavHeaderBackground();
        }


    }

    private void setNavHeaderBackground() {
        final NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        final View header = navigationView.getHeaderView(0);
        final ImageView navHeaderImage = (ImageView) header.findViewById(R.id.nav_header_image);
        navHeaderImage.setContentDescription(topArtist.getName());

        Picasso.with(getContext()).load(topArtist.getImageLink()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.i("onBitmapLoaded: ","setting image");
                navHeaderImage.setImageBitmap(bitmap);
                navHeaderImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent artistIntent = new Intent(getActivity(), ArtistActivity.class);
                        Pair[] pairs = new Pair[1];
                        pairs[0] = new Pair<>(navHeaderImage, topArtist.getName());
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
                        artistIntent.putExtra("artist", topArtist);
                        startActivity(artistIntent,options.toBundle());
                    }
                });

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.i("onBitmapLoaded: ","failed" );

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Override
    public void onArtistItemClick(int clickedItemIndex) {
        if (null != topArtists) {
            Intent artistIntent = new Intent(getActivity(), ArtistActivity.class);
            Artist clickedArtist = ((ArtistAdapter) rvArtists.getAdapter()).getItem(clickedItemIndex);
            ArtistAdapter.ArtistViewHolder viewHolder = (ArtistAdapter.ArtistViewHolder) rvArtists.findViewHolderForAdapterPosition(clickedItemIndex);
            Pair[] pairs = new Pair[1];
            pairs[0] = new Pair<>(viewHolder.getArtistImage(), viewHolder.getArtistName().getText());
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
            artistIntent.putExtra("artist", clickedArtist);
            Bundle params = new Bundle();
            params.putString("artist", clickedArtist.getName());
            if(null != firebaseAnalytics)
            firebaseAnalytics.logEvent("artist", params);
            startActivity(artistIntent, options.toBundle());
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("top_artists_limit") || key.equals("country")) {
            Log.i("onSharedPreference ", "reloading");
            if(null==loaderManager){
                getActivity().getSupportLoaderManager().initLoader(TOP_ARTISTS, null, this);
            }else{
                loaderManager.restartLoader(TOP_ARTISTS, null, this);
            }
            Intent dataUpdated = new Intent(ACTION_DATA_UPDATED).setPackage(getActivity().getPackageName());
            getContext().sendBroadcast(dataUpdated);
        }
    }
}
