package com.phillipkwang.smscar;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Phillip on 1/11/2016.
 */
public class MainService extends Service implements AudioManager.OnAudioFocusChangeListener{
    private static final String TAG = "MainService";
    private final int MY_DATA_CHECK_CODE = 0;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private long SMS_delay = 2000;
    private int MAX_MESSAGE_LENGTH = 350;
    private int originalVolume;

    private boolean myTTSReady = true; //false?
    private boolean clearedTTS = false;
    private static boolean inProcess = false;
    private static String phonenumber = "";

    private static Application myApplication;
    private TextToSpeech myTTS;
    private AudioManager am;
    private TelephonyManager tm;
    private SpeechRecognizer sr;
    private SessionTracker st;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onAudioFocusChange(int input) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        myApplication = this.getApplication();
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        myTTS = new TextToSpeech(myApplication, listenerStarted);
        myTTS.setSpeechRate((float)1);
        myTTS.setPitch((float)1.4);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new SpeechListener());

        st = new SessionTracker("", 1);

        myApplication.registerReceiver(SMScatcher, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    private final BroadcastReceiver SMScatcher = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                while(inProcess){
                    //wait
                }
                inProcess = true;
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdusObj = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdusObj.length];
                    String strMessage = "";
                    String message = "";
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        strMessage += "SMS From: " + messages[i].getOriginatingAddress();
//                      message += "From: " + messages[i].getOriginatingAddress();
                        strMessage += " : ";
                        strMessage += messages[i].getMessageBody();
                        message += messages[i].getMessageBody();
                        strMessage += "\n";
                        phonenumber = messages[i].getOriginatingAddress();
                        Log.v(TAG, messages[i].getOriginatingAddress() + " " + messages[i].getMessageBody());
                    }
                    message = message.trim();

                    String contactName = getContactDisplayNameByNumber(phonenumber);
                    TextReader("Message from " + contactName + "... " + message + "... Say your response", -1, null);

                    new CountDownTimer(5000, 1000) {
                        @Override
                        public void onFinish() {
                            waitForTTS();
                            startVoiceRecognition();
                        }
                        @Override
                        public void onTick(long arg0) {}
                    }.start();
                }

            }
        }

    };

    public void waitForTTS() {
        boolean speakingDone;
        do{
            speakingDone = myTTS.isSpeaking();
        } while (speakingDone);
        return;
    }

    public void TextReader(String rawinput, final int caseNumber, String[] params) {
        if (myTTSReady) {
            final HashMap myHash = new HashMap<String, String>();
            myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SMSCar");

            if(rawinput == null){
                //Toast.makeText(application, "No input", Toast.LENGTH_LONG).show();
                return;
            }
            String input = rawinput.replaceAll("http.*? ", ", URL, ");;

            // trim off very long strings
            if (input.length() > MAX_MESSAGE_LENGTH){
                input = input.substring(0, MAX_MESSAGE_LENGTH);
                input += " , , , message truncated";
            }

            //assume incallstream for now
            if (am.isBluetoothScoAvailableOffCall()) {
                am.startBluetoothSco();
                am.setBluetoothScoOn(true);
            }

            am.requestAudioFocus(new MainService(), AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
            myHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_VOICE_CALL));

            originalVolume = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);

            final String str = input;
            final int inputNumber = caseNumber;
            if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                new CountDownTimer(SMS_delay, SMS_delay / 2) {
                    @Override
                    public void onFinish() {
                        try {
                            if(inputNumber == 0){
                                //"didn't catch that, try again"
                            }
                            else if(inputNumber == 1){
                                //"Message from..."
                            }
                            else if(inputNumber == 2){
                                //"Say your reply"
                            }
                            else if(inputNumber == 3){
                                //-speechinput-, "would you like to..."
                            }
                            else {
                                myTTS.speak(str, TextToSpeech.QUEUE_ADD, myHash);
                            }
                        } catch (Exception e) {
                            //Toast.makeText(MainActivity.this, R.string.TTSNotReady,Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onTick(long arg0) {

                    }
                }.start();
            }
        }
    }

    public void startVoiceRecognition(){
        Intent speechintent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        speechintent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        speechintent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
        sr.startListening(speechintent);
    }

    public TextToSpeech.OnInitListener listenerStarted = new TextToSpeech.OnInitListener() {
        // TTS engine now running so start the message receivers

        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                myTTSReady = true;
                myTTS.setOnUtteranceProgressListener(ul);
            }
        }
    };

    public android.speech.tts.UtteranceProgressListener ul = new UtteranceProgressListener() {

        @Override
        public void onDone(String uttId) {

            int result = AudioManager.AUDIOFOCUS_REQUEST_FAILED;

            if (!clearedTTS) {
                // clearTts();
                Intent c = new Intent();
                c.setAction("MainService.CLEAR");
                myApplication.sendBroadcast(c);
            }

            am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, originalVolume, 0);
            am.abandonAudioFocus(MainService.this);

            am.stopBluetoothSco();
            am.setBluetoothScoOn(false);
            am.setMode(AudioManager.MODE_NORMAL);
        }

        @Override
        public void onError(String utteranceId) { // TODO

        }

        @Override
        public void onStart(String utteranceId) { // TODO

        }

    };

    class SpeechListener implements RecognitionListener {
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
            am.startBluetoothSco();
            am.setBluetoothScoOn(true);
        }

        public void onResults(Bundle results) {
            String str = "";
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++){
                Log.d(TAG, "result " + data.get(i));
            }
            str = data.get(0).toString();
            sendSMS(str);

            am.stopBluetoothSco();
            am.setBluetoothScoOn(false);
            am.setMode(AudioManager.MODE_NORMAL);
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }
    }

    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "unknown number";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    public void sendSMS(String message){
        Log.d(TAG, "Sending SMS to phonenumber: " + phonenumber);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phonenumber, null, message, null, null);

        TextReader("Sent Succesfully", -1, null);
        inProcess = false;
    }
}
