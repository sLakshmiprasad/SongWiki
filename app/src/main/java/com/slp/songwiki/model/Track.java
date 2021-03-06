package com.slp.songwiki.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lakshmiprasad on 4/30/2017.
 */
public class Track implements Parcelable {
    private String title;
    private String artist;
    private String album;
    private long duration;
    private long listeners;
    private long playcount;
    private String content;
    private String imageLink;
    private String summary;
    private List<String> tags;
    private String trackLink;
    private List<Track> similarTracks;
    private String videoId;

    public Track(String title, String artist, long listeners, String imageLink) {
        this.title = title;
        this.artist = artist;
        this.listeners = listeners;
        this.imageLink = imageLink;
    }

    public Track(String title, String artist, long listeners, String imageLink, String trackLink) {
        this.title = title;
        this.artist = artist;
        this.listeners = listeners;
        this.imageLink = imageLink;
        this.trackLink = trackLink;
    }

    public Track(String title, String artist, String imageLink, String videoId) {
        this.title = title;
        this.artist = artist;
        this.imageLink = imageLink;
        this.videoId = videoId;
    }

    public Track() {

    }

    public String getTrackLink() {
        return trackLink;
    }

    public void setTrackLink(String trackLink) {
        this.trackLink = trackLink;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getPlaycount() {
        return playcount;
    }

    public void setPlaycount(long playcount) {
        this.playcount = playcount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getListeners() {
        return listeners;
    }

    public void setListeners(long listeners) {
        this.listeners = listeners;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Track> getSimilarTracks() {
        return similarTracks;
    }

    public void setSimilarTracks(List<Track> similarTracks) {
        this.similarTracks = similarTracks;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    protected Track(Parcel in) {
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        duration = in.readLong();
        listeners = in.readLong();
        playcount = in.readLong();
        content = in.readString();
        imageLink = in.readString();
        summary = in.readString();
        if (in.readByte() == 0x01) {
            tags = new ArrayList<String>();
            in.readList(tags, String.class.getClassLoader());
        } else {
            tags = null;
        }
        trackLink = in.readString();
        if (in.readByte() == 0x01) {
            similarTracks = new ArrayList<Track>();
            in.readList(similarTracks, Track.class.getClassLoader());
        } else {
            similarTracks = null;
        }
        videoId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(duration);
        dest.writeLong(listeners);
        dest.writeLong(playcount);
        dest.writeString(content);
        dest.writeString(imageLink);
        dest.writeString(summary);
        if (tags == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tags);
        }
        dest.writeString(trackLink);
        if (similarTracks == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(similarTracks);
        }
        dest.writeString(videoId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
}