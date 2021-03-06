package com.slp.songwiki.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.slp.songwiki.R;
import com.slp.songwiki.data.playlist.PlaylistContract;
import com.slp.songwiki.model.Artist;
import com.slp.songwiki.model.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Lakshmiprasad on 4/30/2017.
 */

public class TrackUtils implements SongWikiConstants {

    public static List<Track> getTopChartTracks(Context context) throws IOException, JSONException {
        List<Track> topTracks = new ArrayList<>();
        String response = NetworkUtils.getResponseFromHttpUrl(LastFmUtils.getTopTracksUrl(context));
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray trackArray = jsonResponse.getJSONObject(TRACKS).getJSONArray(TRACK);

        return getTracks(trackArray);
    }

    @Deprecated
    public static List<Track> getTopChartTracks() throws IOException, JSONException {
        List<Track> topTracks = new ArrayList<>();
        String response = NetworkUtils.getResponseFromHttpUrl(LastFmUtils.getTopTracksUrl());
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray trackArray = jsonResponse.getJSONObject(TRACKS).getJSONArray(TRACK);

        return getTracks(trackArray);
    }

    private static List<Track> getTracks(JSONArray jsonArray) throws JSONException {
        List<Track> tracks = new ArrayList<>();
        String trackTitle;
        String artist;
        long listeners = 0;
        String imageLink;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject trackInfo = (JSONObject) jsonArray.get(i);
            trackTitle = trackInfo.getString(NAME);
            artist = trackInfo.getJSONObject(ARTIST).getString(NAME);
            if (trackInfo.has(LISTENERS))
                listeners = Long.valueOf(trackInfo.getString(LISTENERS));

            imageLink = LastFmUtils.getImage(trackInfo.getJSONArray(IMAGE));
            tracks.add(new Track(trackTitle, artist, listeners, imageLink));
        }
        return tracks;
    }

    public static void addTrackInfo(Track track) throws IOException, JSONException {
        String response = NetworkUtils.getResponseFromHttpUrl(LastFmUtils.getTrackInfoUrl(track.getArtist(), track.getTitle()));
        JSONObject jsonObject = new JSONObject(response);
        JSONObject trackInfo = jsonObject.getJSONObject(TRACK);
        track.setTrackLink(trackInfo.getString(URL));
        if (trackInfo.has(ALBUM)) {

            JSONObject albumJson = trackInfo.getJSONObject(ALBUM);
            String album = albumJson.getString(TITLE);
            track.setAlbum(album);
        }

        if (trackInfo.has(WIKI)) {
            JSONObject trackWiki = trackInfo.getJSONObject(WIKI);
            track.setSummary(trackWiki.getString(SUMMARY));
            track.setContent(trackWiki.getString(CONTENT));
        }

        if (trackInfo.has(TOP_TAGS)) {
            track.setTags(getTopTags(trackInfo.getJSONObject(TOP_TAGS).getJSONArray(TAG)));
        }
    }

    private static List<String> getTopTags(JSONArray tagArray) throws JSONException {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagArray.length(); i++) {
            tags.add(tagArray.getJSONObject(i).getString(NAME));
        }
        return tags;
    }

    public static List<Track> getTrackResult(String track) throws IOException, JSONException {
        Log.i("getTrackResult: ", LastFmUtils.getSearchTrackUrl(track).toString());
        String resultsJson = NetworkUtils.getResponseFromHttpUrl(LastFmUtils.getSearchTrackUrl(track));
        JSONObject json = new JSONObject(resultsJson);
        JSONObject resultJson = json.getJSONObject(RESULTS);
        JSONArray trackArray = resultJson.getJSONObject(TRACK_MATCHES).getJSONArray(TRACK);
        return getTracksFromMatches(trackArray);
    }

    private static List<Track> getTracksFromMatches(JSONArray trackArray) throws JSONException {
        List<Track> tracks = new ArrayList<>();
        String trackTitle = null;
        String artist = null;
        long listeners = 0;
        String imageLink = null;
        for (int i = 0; i < trackArray.length(); i++) {
            JSONObject trackInfo = (JSONObject) trackArray.get(i);
            trackTitle = trackInfo.getString(NAME);
            artist = trackInfo.getString(ARTIST);
            listeners = Long.valueOf(trackInfo.getString(LISTENERS));
            imageLink = LastFmUtils.getImage(trackInfo.getJSONArray(IMAGE));
            tracks.add(new Track(trackTitle, artist, listeners, imageLink));
        }
        return tracks;
    }

    public static List<Track> getSimilarTracks(Track track) throws IOException, JSONException {
        String response = NetworkUtils.getResponseFromHttpUrl(LastFmUtils.getSimilarTracksUrl(track));
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray similarTracks = jsonResponse.getJSONObject(SIMILAR_TRACKS).getJSONArray(TRACK);
        return getTracks(similarTracks);
    }

    public static List<Track> getTopTracks(String artist) throws IOException, JSONException {
        String response = NetworkUtils.getResponseFromHttpUrl(LastFmUtils.getArtistTopTracksUrl(artist));
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray topTracks = jsonResponse.getJSONObject(TOP_TRACKS).getJSONArray(TRACK);
        return getTracks(topTracks);
    }

    public static ContentValues getTrackContent(Track track) {
        ContentValues values = new ContentValues();
        values.put(PlaylistContract.PlaylistEntry.TRACK, track.getTitle());
        values.put(PlaylistContract.PlaylistEntry.ARTIST, track.getArtist());
        values.put(PlaylistContract.PlaylistEntry.IMAGE_LINK, track.getImageLink());
        values.put(PlaylistContract.PlaylistEntry.VIDEO_ID, track.getVideoId());
        return values;

    }

    public static List<Track> getTracksFromPlaylist(Context context) {
        List<Track> tracks = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(PlaylistContract.PlaylistEntry.CONTENT_URI, null, null, null, PlaylistContract.PlaylistEntry.DATE);
        int title = cursor.getColumnIndex(PlaylistContract.PlaylistEntry.TRACK);
        int artist = cursor.getColumnIndex(PlaylistContract.PlaylistEntry.ARTIST);
        int imageLink = cursor.getColumnIndex(PlaylistContract.PlaylistEntry.IMAGE_LINK);
        int videoId = cursor.getColumnIndex(PlaylistContract.PlaylistEntry.VIDEO_ID);
        if (cursor.moveToFirst()) {
            do {
                tracks.add(new Track(cursor.getString(title), cursor.getString(artist), cursor.getString(imageLink), cursor.getString(videoId)));
            } while (cursor.moveToNext());
        }
        return tracks;
    }

    public static List<String> getVideoIdsFromPlaylist(Context context) {
        List<String> videoIds = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(PlaylistContract.PlaylistEntry.CONTENT_URI, new String[]{PlaylistContract.PlaylistEntry.VIDEO_ID}, null, null, PlaylistContract.PlaylistEntry.DATE);
        int videoIdIndex = cursor.getColumnIndex(PlaylistContract.PlaylistEntry.VIDEO_ID);
        if (cursor.moveToFirst()) {
            do {
                videoIds.add(cursor.getString(videoIdIndex));
            } while (cursor.moveToNext());
        }
        return videoIds;
    }

    public static void setTopTracks(Artist artist) throws IOException, JSONException {
        if(null != artist){
            artist.setTopTracks(getTopTracks(artist.getName()));
        }
    }
}
