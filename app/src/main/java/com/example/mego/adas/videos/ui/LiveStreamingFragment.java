/*
 * Copyright (c) 2017 Ahmed-Abdelmeged
 *
 * github: https://github.com/Ahmed-Abdelmeged
 * email: ahmed.abdelmeged.vm@gamil.com
 * Facebook: https://www.facebook.com/ven.rto
 * Twitter: https://twitter.com/A_K_Abd_Elmeged
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.mego.adas.videos.ui;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mego.adas.R;
import com.example.mego.adas.auth.AuthenticationUtilities;
import com.example.mego.adas.auth.User;
import com.example.mego.adas.utils.Constants;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveStreamingFragment extends Fragment {

    /**
     * UI Element
     */
    private TextView noLiveVideoTextView;
    private ImageView liveStreamingImageView;
    private FrameLayout liveVideoContainer;
    private ProgressBar loadingBar;

    /**
     * Firebase objects
     * to specific part of the database
     */
    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mLiveVideoIdDatabaseReference;

    private ValueEventListener mLiveVideoIdValueListener;

    /**
     * the current live video id
     */
    private String currentLiveVideoId = null;


    public LiveStreamingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_live_streaming, container, false);

        initializeScreen(rootView);


        //check the internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            //show a loading bar till the onDataChange triggered
            loadingBar.setVisibility(View.VISIBLE);
            //set up the firebase
            mFirebaseDatabase = FirebaseDatabase.getInstance();


            //get the current user uid
            User currentUser = AuthenticationUtilities.getCurrentUser(getContext());
            String uid = currentUser.getUserUid();

            //get the references for the child
            //the  child for the  live video id
            mLiveVideoIdDatabaseReference = mFirebaseDatabase.getReference().child(Constants.FIREBASE_USERS)
                    .child(uid).child(Constants.FIREBASE_USER_INFO).child(Constants.FIREBASE_LIVE_STREAMING_VIDEO_ID);

            //set the id for listener
            mLiveVideoIdValueListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //hide the loading bar
                    loadingBar.setVisibility(View.INVISIBLE);

                    if (dataSnapshot.exists()) {
                        //get the current video id
                        currentLiveVideoId = dataSnapshot.getValue(Constants.FIREBASE_LIVE_STREAMING_VIDEO_ID.getClass());
                        if (currentLiveVideoId.equals(Constants.LIVE_STREAMING_NO_LIVE_VIDEO)) {

                            //show the warning image and text
                            noLiveVideoTextView.setVisibility(View.VISIBLE);
                            liveStreamingImageView.setVisibility(View.VISIBLE);

                            //hide the fragment
                            liveVideoContainer.setVisibility(View.GONE);

                        } else {

                            //hide the warning image and text
                            noLiveVideoTextView.setVisibility(View.GONE);
                            liveStreamingImageView.setVisibility(View.GONE);

                            liveVideoContainer.setVisibility(View.VISIBLE);

                            //setup the player fragment
                            YouTubePlayerSupportFragment youTubePlayerFragment =
                                    YouTubePlayerSupportFragment.newInstance();

                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                            transaction.replace(R.id.watch_live_video_player, youTubePlayerFragment).commit();

                            youTubePlayerFragment.initialize(Constants.API_KEY, new YouTubePlayer.OnInitializedListener() {
                                @Override
                                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {

                                    //load and play the video with current id
                                    youTubePlayer.loadVideo(currentLiveVideoId);
                                    youTubePlayer.play();
                                }

                                @Override
                                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                                    Toast.makeText(getContext(), R.string.can_not_initialize_youtube_player, Toast.LENGTH_LONG).show();

                                }
                            });

                        }
                    } else {
                        mLiveVideoIdDatabaseReference.setValue(Constants.NO_LIVE_VIDEO);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            //attach the listener
            mLiveVideoIdDatabaseReference.addValueEventListener(mLiveVideoIdValueListener);
        } else {
            noLiveVideoTextView.setVisibility(View.VISIBLE);
            liveStreamingImageView.setVisibility(View.VISIBLE);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Line the UI Element with the UI
     */
    private void initializeScreen(View view) {
        noLiveVideoTextView = view.findViewById(R.id.no_live_streaming_video_textView);

        liveStreamingImageView = view.findViewById(R.id.live_streaming_image_view);

        liveVideoContainer = view.findViewById(R.id.watch_live_video_player);

        loadingBar = view.findViewById(R.id.loading_streaming_indicator_progress);
    }

}
