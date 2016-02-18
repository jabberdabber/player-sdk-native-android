package com.kaltura.kalturaplayertoolkit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.kaltura.playersdk.KPPlayerConfig;
import com.kaltura.playersdk.PlayerViewController;
import com.kaltura.playersdk.casting.KCastRouterManagerListener;
import com.kaltura.playersdk.casting.KRouterInfo;
import com.kaltura.playersdk.events.KPEventListener;
import com.kaltura.playersdk.events.KPlayerState;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by itayi on 2/12/15.
 */
public class FullscreenFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = FullscreenFragment.class.getSimpleName();
    private View mFragmentView = null;
    private CastDetectedDevicesDialog mDevicesDialog;
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    private PlayerViewController mPlayerView;
    private static final int FULL_SCREEN_FLAG = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FullscreenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FullscreenFragment newInstance(String param1, String param2) {
        FullscreenFragment fragment = new FullscreenFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FullscreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mFragmentView == null) {
            mFragmentView = inflater.inflate(R.layout.fragment_fullscreen, container, false);
        }
        
        mPlayerView = (PlayerViewController) mFragmentView.findViewById(R.id.player);
        mPlayerView.loadPlayerIntoActivity(getActivity());
        mPlayerView.getKCastRouterManager().setCastRouterManagerListener(new KCastRouterManagerListener() {
            @Override
            public void castButtonClicked() {
                getDevicesDialog().show();
            }

            @Override
            public void castDeviceConnectionState(boolean isConnected) {
                getDevicesDialog().deviceConnectionStateDidChange(isConnected);
            }

            @Override
            public void didDetectCastDevices(boolean didDetect) {

            }

            @Override
            public void addedCastDevice(KRouterInfo info) {
                getDevicesDialog().addCastDevice(info);
            }

            @Override
            public void removedCastDevice(KRouterInfo info) {
                getDevicesDialog().removeCastDevice(info);
            }
        });
        mPlayerView.getKCastRouterManager().enableKalturaCastButton(true);
        mPlayerView.addEventListener(new KPEventListener() {
            @Override
            public void onKPlayerStateChanged(PlayerViewController playerViewController, KPlayerState state) {
                switch (state) {
                    case READY:
                        setFullScreen();
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        mPlayerView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                Log.d(TAG, "onSystemVisibility change");
                                if (visibility == FULL_SCREEN_FLAG) {
                                    Point size = getRealScreenSize();
                                    mPlayerView.setPlayerViewDimensions(size.x, size.y);
                                } else {
                                    Point size = getScreenWithoutNavigationSize();//getActivity().getWindowManager().getDefaultDisplay().getSize(size)
                                    mPlayerView.setPlayerViewDimensions(size.x, size.y);
                                }
                            }
                        });
                        break;
                    case PAUSED:
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                }
            }

            @Override
            public void onKPlayerPlayheadUpdate(PlayerViewController playerViewController, float currentTime) {

            }

            @Override
            public void onKPlayerFullScreenToggeled(PlayerViewController playerViewController, boolean isFullscrenn) {
                setFullScreen();
            }
        });

//        mPlayerView.setCustomSourceURLProvider(new PlayerViewController.SourceURLProvider() {
//            @Override
//            public String getURL(String entryId, String currentURL) {
//                return "http://cdnapi.kaltura.com/p/243342/sp/24334200/playManifest/entryId/0_uka1msg4/flavorIds/1_vqhfu6uy,1_80sohj7p,1_ry9w1l0b/format/applehttp/protocol/http/a.m3u8";
//                //return "http://cfvod.kaltura.com/scf/fhls/p/243342/sp/24334200/serveFlavor/entryId/0_uka1msg4/v/1/pv/1/flavorId/1_vqhfu6uy/name/a.mp4/index.m3u8?Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cDovL2Nmdm9kLmthbHR1cmEuY29tL3NjZi9maGxzL3AvMjQzMzQyL3NwLzI0MzM0MjAwL3NlcnZlRmxhdm9yL2VudHJ5SWQvMF91a2ExbXNnNC92LzEvcHYvMS9mbGF2b3JJZC8xXyoiLCJDb25kaXRpb24iOnsiRGF0ZUxlc3NUaGFuIjp7IkFXUzpFcG9jaFRpbWUiOjE0NTU1MjU1ODF9fX1dfQ__&Signature=SeEvTkFoIEMCuBCqElROXSEofeGAEeiOvM93-BBFg1NE3r7zcHcTV~Ap1O-yIFUE37RFefQ6RLKnJR~j-Gcl6sCICup6tKAZzSXBvsRmxgJxBdbQ1KLVIImd~NhKFTytxpQxejYOrLelDJX86X7XoNT4gg0avQbMY1vSXLhTZfga47g47NZYAdH8tO-Gn-TN0EcMMjFGXKSlg1DyZAmQt0LmY1UPiUBtC4t7OAQET~iQy6hpQBEFT4lscHC5Ha6qPWK1S0O84McTGk6vMVPZfEd-MiqnxugSqhTWNig5LXHbfj--MgeyUetFTdb05lao5wiuh8UAUz1TG42fsbWx6A__&Key-Pair-Id=APKAJT6QIWSKVYK3V34A";
//            }
//        });

        showPlayerView();
        Bundle bundle = getArguments();
        KPPlayerConfig config = null;
        if (bundle != null && (config = (KPPlayerConfig)bundle.getSerializable("config")) != null){
            mPlayerView.initWithConfiguration(config);
        }

        return mFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private CastDetectedDevicesDialog getDevicesDialog() {
        if (mDevicesDialog == null) {
            mDevicesDialog = new CastDetectedDevicesDialog(getActivity(), new CastDetectedDevicesDialog.CastDetectedDevicesDialogListener() {
                @Override
                public void disconnect() {
                    mPlayerView.getKCastRouterManager().disconnect();
                }

                @Override
                public void routeSelected(String castDeviceId) {
                    mPlayerView.getKCastRouterManager().connectDevice(castDeviceId);
                }
            });
        }
        return mDevicesDialog;
    }


    private void setFullScreen (){
        View decorView = getActivity().getWindow().getDecorView(); //navigation view
        int uiOptions = FULL_SCREEN_FLAG;
        decorView.setSystemUiVisibility(uiOptions);
//        Point size = getRealScreenSize();
//        mPlayerView.setPlayerViewDimensions(size.x, size.y);
    }

    private Point getScreenWithoutNavigationSize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics realMetrics = new DisplayMetrics();
        display.getMetrics(realMetrics);
        int width = realMetrics.widthPixels;
        int height = realMetrics.heightPixels;
        return new Point(width, height);
    }

    @SuppressLint("NewApi") private Point getRealScreenSize(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int realWidth = 0;
        int realHeight = 0;

        if (Build.VERSION.SDK_INT >= 17){
            //new pleasant way to get real metrics
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            realWidth = realMetrics.widthPixels;
            realHeight = realMetrics.heightPixels;

        } else {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                realWidth = display.getWidth();
                realHeight = display.getHeight();
                Log.e("Display Info", "Couldn't use reflection to get the real display metrics.");
            }

        }
        return new Point(realWidth,realHeight);
    }

    private void showPlayerView() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mPlayerView.setVisibility(RelativeLayout.VISIBLE);
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        mPlayerView.setPlayerViewDimensions( size.x, size.y, 0, 0 );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if ( mPlayerView.getVisibility() == RelativeLayout.VISIBLE ) {
            Timer swapTimer = new Timer();
            swapTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Point size = new Point();
                            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                            mPlayerView.setPlayerViewDimensions(size.x, size.y, 0, 0);
                            View decorView = getActivity().getWindow().getDecorView();
                            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                    });
                }
            }, 100 );

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if ( mPlayerView != null ) {
            mPlayerView.releaseAndSavePosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mPlayerView != null ) {
            mPlayerView.resumePlayer();
        }
    }

}
