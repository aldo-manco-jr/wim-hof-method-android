package org.aldomanco.wimhofmethod.ui.home;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButtonToggleGroup;

import org.aldomanco.wimhofmethod.R;
import org.aldomanco.wimhofmethod.databinding.FragmentHomeBinding;

import static android.content.Context.BIND_AUTO_CREATE;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private int numberOfRounds;

    private Intent startFirstRound;
    private Intent startSecondRound;
    private Intent startThirdRound;

    private TextView textView;
    private ConstraintLayout layout;
    private Button buttonStopRound;
    private Button buttonPauseRound;

    private boolean stopPressed;
    private boolean isPaused;

    boolean firstServiceBounded;
    boolean secondServiceBounded;
    boolean thirdServiceBounded;

    RoundPlayerService firstService;
    RoundPlayerService secondService;
    RoundPlayerService thirdService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        numberOfRounds = 0;
        stopPressed = false;
        isPaused = false;

        firstServiceBounded = false;
        secondServiceBounded = false;
        thirdServiceBounded = false;

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textHome;
        layout = binding.layout;
        buttonStopRound = binding.buttonStopRound;
        buttonPauseRound = binding.buttonPauseRound;

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        textView.setOnClickListener(this);
        layout.setOnClickListener(this);
        buttonStopRound.setOnClickListener(this);
        buttonPauseRound.setOnClickListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.layout:
            case R.id.text_home:

                numberOfRounds++;
                homeViewModel.setTextView(numberOfRounds, textView);

                int remaining = numberOfRounds % 3;
                stopPressed = false;

                switch (remaining) {

                    case 1:

                        if (secondServiceBounded) {
                            getActivity().unbindService(secondConnection);
                            secondServiceBounded = false;
                        }
                        if (thirdServiceBounded) {
                            getActivity().unbindService(thirdConnection);
                            thirdServiceBounded = false;
                        }

                        startFirstRound = new Intent(getActivity(), RoundPlayerService.class);
                        startFirstRound.putExtra("remaining", 0);
                        getActivity().bindService(startFirstRound, firstConnection, BIND_AUTO_CREATE);

                        break;

                    case 2:

                        if (firstServiceBounded) {
                            Toast.makeText(getActivity(), "ww", Toast.LENGTH_LONG).show();
                            getActivity().unbindService(firstConnection);
                            firstServiceBounded = false;
                        }
                        if (thirdServiceBounded) {
                            getActivity().unbindService(thirdConnection);
                            thirdServiceBounded = false;
                        }

                        startSecondRound = new Intent(getActivity(), RoundPlayerService.class);
                        startSecondRound.putExtra("remaining", 1);
                        getActivity().bindService(startSecondRound, secondConnection, BIND_AUTO_CREATE);

                        break;

                    case 0:

                        if (firstService != null && firstService.isPlaying()) {
                            getActivity().stopService(startFirstRound);
                            getActivity().unbindService(firstConnection);
                            firstServiceBounded = false;
                        }
                        if (secondService != null && secondService.isPlaying()) {
                            getActivity().stopService(startSecondRound);
                            getActivity().unbindService(secondConnection);
                            secondServiceBounded = false;
                        }

                        startThirdRound = new Intent(getActivity(), RoundPlayerService.class);
                        startThirdRound.putExtra("remaining", 2);
                        getActivity().bindService(startThirdRound, thirdConnection, BIND_AUTO_CREATE);

                        break;
                }

                break;

            case R.id.buttonStopRound:

                if (!stopPressed) {

                    if (firstServiceBounded) {
                        getActivity().unbindService(firstConnection);
                        firstServiceBounded = false;

                    } else if (secondServiceBounded) {
                        getActivity().unbindService(secondConnection);
                        secondServiceBounded = false;

                    } else if (thirdServiceBounded) {
                        getActivity().unbindService(thirdConnection);
                        thirdServiceBounded = false;
                    }

                    homeViewModel.stopTextView(numberOfRounds, textView);
                    numberOfRounds--;

                    stopPressed = true;
                }

                break;

            case R.id.buttonPauseRound:

                if (firstServiceBounded) {

                    if (firstService.isPlaying()){
                        buttonPauseRound.setText("RESUME ROUND");
                        firstService.pauseMediaPlayer();
                    }else {
                        buttonPauseRound.setText("PAUSE ROUND");
                        firstService.resumeMediaPlayer();
                    }

                } else if (secondServiceBounded) {

                    if (secondService.isPlaying()){
                        buttonPauseRound.setText("RESUME ROUND");
                        secondService.pauseMediaPlayer();
                    }else {
                        buttonPauseRound.setText("PAUSE ROUND");
                        secondService.resumeMediaPlayer();
                    }

                } else if (thirdServiceBounded) {

                    if (thirdService.isPlaying()){
                        buttonPauseRound.setText("RESUME ROUND");
                        thirdService.pauseMediaPlayer();
                    }else {
                        buttonPauseRound.setText("PAUSE ROUND");
                        thirdService.resumeMediaPlayer();
                    }

                }

                break;
        }
    }

    ServiceConnection firstConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            firstServiceBounded = false;
            firstService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            firstServiceBounded = true;
            RoundPlayerService.LocalBinder firstLocalBinder = (RoundPlayerService.LocalBinder) service;
            firstService = firstLocalBinder.getServerInstance();
        }
    };

    ServiceConnection secondConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            secondServiceBounded = false;
            secondService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            secondServiceBounded = true;
            RoundPlayerService.LocalBinder secondLocalBinder = (RoundPlayerService.LocalBinder) service;
            secondService = secondLocalBinder.getServerInstance();
        }
    };

    ServiceConnection thirdConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            thirdServiceBounded = false;
            thirdService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            thirdServiceBounded = true;
            RoundPlayerService.LocalBinder thirdLocalBinder = (RoundPlayerService.LocalBinder) service;
            thirdService = thirdLocalBinder.getServerInstance();
        }
    };

    @Override
    public void onStop() {
        super.onStop();

        if (firstServiceBounded) {
            getActivity().unbindService(firstConnection);
            firstServiceBounded = false;

        } else if (secondServiceBounded) {
            getActivity().unbindService(secondConnection);
            secondServiceBounded = false;

        } else if (thirdServiceBounded) {
            getActivity().unbindService(thirdConnection);
            thirdServiceBounded = false;
        }
    }

    ;
}