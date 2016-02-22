package com.kaltura.playersdk.players;

import android.text.TextUtils;

import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by gilad.nadav on 23/02/2016.
 */
public  class KExoPlayerTracksUtil {

    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_METADATA = 3;

    public static String getLanguagesTracksJson(ExoplayerWrapper mExoPlayer, final int trackType) {
        if (mExoPlayer == null) {
            return null;
        }
        int trackCount = mExoPlayer.getTrackCount(trackType);

        if (trackCount == 0) {
            return null;
        }

        JSONObject langObj = new JSONObject();
        JSONArray langsArray = new JSONArray();

        try {
            JSONObject mixedObj = new JSONObject();
            mixedObj.put("index", 0);
            mixedObj.put("label", "Off");
            langsArray.put(mixedObj);

            for (int i = 0; i < trackCount; i++) {
                com.google.android.exoplayer.MediaFormat mediaFormat = mExoPlayer.getTrackFormat(trackType, i);
                mixedObj = new JSONObject();
                mixedObj.put("index", i+1);
                mixedObj.put("label", mediaFormat.trackId);
                langsArray.put(mixedObj);
            }
            langObj.put("languages", langsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return langObj.toString();
    }

    public static String getAudioFreqRateTracksJson(ExoplayerWrapper mExoPlayer, final int trackType) {
        if (mExoPlayer == null) {
            return null;
        }
        int trackCount = mExoPlayer.getTrackCount(trackType);

        if (trackCount == 0) {
            return null;
        }

        JSONObject langObj = new JSONObject();
        JSONArray langsArray = new JSONArray();

        try {
            JSONObject mixedObj = new JSONObject();
            mixedObj.put("id", 0);
            mixedObj.put("stream_label", "Auto");
            langsArray.put(mixedObj);

            for (int i = 0; i < trackCount; i++) {
                com.google.android.exoplayer.MediaFormat mediaFormat = mExoPlayer.getTrackFormat(trackType, i);
                mixedObj = new JSONObject();
                mixedObj.put("index", i);
                mixedObj.put("stream_label", buildAudioPropertyString(mediaFormat));
                langsArray.put(mixedObj);
            }
            langObj.put("languages", langsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return langObj.toString();
    }

    public static String getBitRateTracksJson(ExoplayerWrapper mExoPlayer, final int trackType, boolean includeAutoSource) {
        if (mExoPlayer == null) {
            return null;
        }
        int trackCount = mExoPlayer.getTrackCount(trackType);

        if (trackCount == 0) {
            return null;
        }

        JSONObject sourceObj = new JSONObject();
        JSONArray sourcesArray = new JSONArray();

        try {

            JSONObject mixedObj = new JSONObject();
            int index = 0;
            for (int i = 0; i < trackCount; i++) {
                com.google.android.exoplayer.MediaFormat mediaFormat = mExoPlayer.getTrackFormat(trackType, i);
                if(includeAutoSource == false && mediaFormat.bitrate == -1){
                    continue;
                }

                mixedObj = new JSONObject();
                mixedObj.put("assetid", index);
                mixedObj.put("bandwidth", buildBitrateString(mediaFormat));
                mixedObj.put("height", mediaFormat.height);
                mixedObj.put("mimeType", mediaFormat.mimeType);
                mixedObj.put("src", "undefined");
                mixedObj.put("type", mediaFormat.mimeType);
                sourcesArray.put(mixedObj);
                index++;
            }
            sourceObj.put("sources", sourcesArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sourceObj.toString();
    }

    public static String buildTrackName(com.google.android.exoplayer.MediaFormat format) {
        if (format.adaptive) {
            return "auto";
        }
        String trackName;
        if (MimeTypes.isVideo(format.mimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        } else if (MimeTypes.isAudio(format.mimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                            buildAudioPropertyString(format)), buildBitrateString(format)),
                    buildTrackIdString(format));
        } else { //text/vtt
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }

    public static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    public static String buildResolutionString(com.google.android.exoplayer.MediaFormat format) {
        return format.width == com.google.android.exoplayer.MediaFormat.NO_VALUE || format.height == com.google.android.exoplayer.MediaFormat.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    public static  String buildTrackIdString(com.google.android.exoplayer.MediaFormat format) {
        return format == null ? "" : " (" + format.trackId + ")";
    }

    public static String buildAudioPropertyString(com.google.android.exoplayer.MediaFormat format) {
        return format.channelCount == com.google.android.exoplayer.MediaFormat.NO_VALUE || format.sampleRate == com.google.android.exoplayer.MediaFormat.NO_VALUE
                ? "" : /*format.channelCount + "ch, " + */format.sampleRate + "Hz";
    }
    public static String buildLanguageString(com.google.android.exoplayer.MediaFormat format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    public static String buildBitrateString(com.google.android.exoplayer.MediaFormat format) {
        return format.bitrate == com.google.android.exoplayer.MediaFormat.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }
}
