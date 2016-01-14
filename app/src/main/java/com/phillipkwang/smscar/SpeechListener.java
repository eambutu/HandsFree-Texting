package com.phillipkwang.smscar;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.util.Log;

/**
 * Created by Phillip on 1/12/2016.
 */
public class SpeechListener implements RecognitionListener {
    private static final String TAG = "SpeechListener";

    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");
    }

    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    public void onError(int error) {
        Log.d(TAG, "onEndError");
    }

    public void onEvent(int eventType, Bundle params) {
        Log.d(TAG, "onEvent");
    }

    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults");
    }

    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
    }

    public void onResults(Bundle results) {
        Log.d(TAG, "onResults");
    }

    public void onRmsChanged(float rmsdB) {
        Log.d(TAG, "onRmsChanged");
    }
}
