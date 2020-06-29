package com.example.monotostereo;

import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick {

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView recyclerView;
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private File fileToPlay;
    private ImageView playBtn;
    private TextView playerHeader;
    private TextView playerFileName;
    private SeekBar playerSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;
    private ImageView playLastBtn;
    private ImageView playNextBtn;
    private AudioListAdapter.AudioViewHolder audioViewHolder;
    private Dictionary<String, Integer> filename;

    public AudioListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        recyclerView = view.findViewById(R.id.audio_list_view);
        playBtn = view.findViewById(R.id.player_play_btn);
        playerFileName = view.findViewById(R.id.player_file_name);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerSeekBar = view.findViewById(R.id.player_seek_bar);
        playLastBtn = view.findViewById(R.id.last_audio);
        playNextBtn = view.findViewById(R.id.next_audio);

        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();
        audioListAdapter = new AudioListAdapter(allFiles, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(audioListAdapter);
        filename = new Hashtable<String, Integer>();
        for (int i = 0; i < allFiles.length; i++) {
            filename.put(allFiles[i].getName(), i);
        }


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        playNextBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                int fileIndex = filename.get(fileToPlay.getName()) + 1;
                if (fileIndex > allFiles.length - 1) {
                    fileIndex = 0;
                }
                fileToPlay = allFiles[fileIndex];
                stopAudio();
                playAudio(fileToPlay);
            }
        });
        playLastBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                int fileIndex = filename.get(fileToPlay.getName()) - 1;
                if (fileIndex < 0) {
                    fileIndex = allFiles.length - 1;
                }
                fileToPlay = allFiles[fileIndex];
                stopAudio();
                playAudio(fileToPlay);
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    if (fileToPlay != null) {
                        resumeAudio();
                    }
                }
            }
        });
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    pauseAudio();
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = allFiles[position];
        if (isPlaying) {
            stopAudio();
        }
        playAudio(fileToPlay);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        isPlaying = true;
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopAudio() {
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        playerHeader.setText("Stopped");
        mediaPlayer.stop();
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playAudio(File fileToPlay) {
        Log.d("new", "onViewCreated: " + filename.get(fileToPlay.getName()));

        mediaPlayer = new MediaPlayer();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        try {
            mediaPlayer.setDataSource((fileToPlay.getAbsolutePath()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        playerFileName.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopAudio();
                playerHeader.setText("Finished");
            }
        });

        playerSeekBar.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    private void updateRunnable() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                playerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this, 500);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            stopAudio();
        }
    }
}