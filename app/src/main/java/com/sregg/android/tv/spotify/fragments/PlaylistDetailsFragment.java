package com.sregg.android.tv.spotify.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;

import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.PlaylistActivity;
import com.sregg.android.tv.spotify.presenters.PlaylistDetailsPresenter;
import com.sregg.android.tv.spotify.presenters.PlaylistTrackRowPresenter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.TrackSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PlaylistDetailsFragment extends TracksDetailsFragment {

    private static final String TAG = PlaylistDetailsFragment.class.getSimpleName();

    private static final long ACTION_PLAY_PLAYLIST = 1;

    private SpotifyService mSpotifyService;
    private SpotifyTvApplication mApp;

    private String mPlaylistId;
    private String mUserId;
    private Playlist mPlaylist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();

        mPlaylistId = intent.getStringExtra(PlaylistActivity.ARG_PLAYLIST_ID);
        mUserId = intent.getStringExtra(PlaylistActivity.ARG_USER_ID);

        mApp = SpotifyTvApplication.getInstance();
        mSpotifyService = mApp.getSpotifyService();

        setupFragment();
        loadPlaylist();
    }

    @Override
    protected Presenter getDetailsPresenter() {
        return new PlaylistDetailsPresenter();
    }

    @Override
    protected Presenter getTrackRowPresenter() {
        return new PlaylistTrackRowPresenter();
    }

    private void setupFragment() {
        setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_PLAYLIST) {
                    mApp.getSpotifyPlayerController().play(mPlaylist);
                }
            }
        });
    }

    private void loadPlaylist() {
        // load artist from API to get their image
        mSpotifyService.getPlaylist(mUserId, mPlaylistId, new Callback<Playlist>() {
            @Override
            public void success(final Playlist playlist, Response response) {
                mPlaylist = playlist;
                setupDetails(playlist);

                List<TrackSimple> tracks = new ArrayList<TrackSimple>();
                for (PlaylistTrack playlistTrack : playlist.tracks.items) {
                    tracks.add(playlistTrack.track);
                }
                setupTracksRows(tracks);

                if (playlist.images.size() > 0) {
                    String imageUrl = playlist.images.get(0).url;
                    loadBackgroundImage(imageUrl);
                    loadDetailsRowImage(imageUrl);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupDetails(Playlist playlist) {
        DetailsOverviewRow detailsRow = new DetailsOverviewRow(playlist);

        detailsRow.addAction(new Action(
                ACTION_PLAY_PLAYLIST,
                getResources().getString(R.string.lb_playback_controls_play),
                null,
                getActivity().getDrawable(R.drawable.lb_ic_play)
        ));

        setDetailsRow(detailsRow);
    }
}
