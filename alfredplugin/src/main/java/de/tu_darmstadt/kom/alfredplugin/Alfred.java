package de.tu_darmstadt.kom.alfredplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String actionString = intent.getAction();
        int action;
        if (actionString.equals(Alfred.PAUSE_GAME)) {
            action = Alfred.PAUSE;
        }
        else if (actionString.equals(Alfred.RESUME_GAME)) {
            action = Alfred.RESUME;
        }
        else if (actionString.equals(Alfred.STOP_GAME)) {
            action = Alfred.STOP;
        }
        else {
            return;
        }
        synchronized (Alfred.actions) {
            Alfred.actions.add(action);
        }
    }
}

class Listener implements RecognitionListener {
    private Context context;
    private SpeechRecognizer recognizer;
    private CountDownTimer timer;

    public Listener(Context context) {
        this.context = context;
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(this);
        listen();
    }

    private void listen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);
        recognizer.startListening(intent);
    }

    public void onBeginningOfSpeech() {
        Log.i("alfred", "Beginning of Speech");
    }

    public void onBufferReceived(byte[] buffer) {
        Log.i("alfred", "Buffer received");
    }

    public void onEndOfSpeech() {
        Log.i("alfred", "End of Speech");
    }

    public void onError(int error) {
        Log.i("alfred", "Error " + error);

        if (timer != null){
            timer.cancel();
        }

        if (timer == null) {
            timer = new CountDownTimer(2000, 500) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    Log.d("Speech", "Timer.onFinish: Timer Finished, Restart recognizer");
                    recognizer.cancel();
                    listen();
                }
            };
        }
        timer.start();
    }

    public void onEvent(int eventType, Bundle params) {
        Log.i("alfred", "Event");
    }

    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) text += result + "\n";
        Log.i("alfred", "Partial Results");
        Log.i("alfred", text);
        searchWords(matches);
    }

    public void onReadyForSpeech(Bundle params) {
        Log.i("alfred", "Ready For Speech");
    }

    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) text += result + "\n";
        Log.i("alfred", "Results");
        Log.i("alfred", text);
        searchWords(matches);
        listen();
    }

    public void onRmsChanged(float rmsdB) {
        //Log.i("alfred", "Rms Changed");
    }

    private void searchWords(ArrayList<String> words) {
        for (String result : words) {
            if (result.toLowerCase().equals("pause")) {
                synchronized (Alfred.actions) {
                    Alfred.actions.add(Alfred.PAUSE);
                    return;
                }
            }
            else if (result.toLowerCase().equals("resume") || result.toLowerCase().equals("weiter")) {
                synchronized (Alfred.actions) {
                    Alfred.actions.add(Alfred.RESUME);
                    return;
                }
            }
            else if (result.toLowerCase().equals("stop")) {
                synchronized (Alfred.actions) {
                    Alfred.actions.add(Alfred.STOP);
                    return;
                }
            }
        }
    }
}

public class Alfred {
    public static final String PAUSE_GAME = "eu.alfred.api.PauseGame";
    public static final String RESUME_GAME = "eu.alfred.api.ResumeGame";
    public static final String STOP_GAME = "eu.alfred.api.StopGame";

    public static final int PAUSE = 0;
    public static final int RESUME = 1;
    public static final int STOP = 2;

    public static final ArrayList<Integer> actions = new ArrayList<Integer>();

    private static Receiver receiver;
    private static Listener listener;

    public static void init() {
        try {
            Class clazz = Class.forName("com.unity3d.player.UnityPlayer");
            final Context context = (Context)clazz.getField("currentActivity").get(null);

            IntentFilter filter = new IntentFilter();
            filter.addAction(PAUSE_GAME);
            filter.addAction(RESUME_GAME);
            filter.addAction(STOP_GAME);
            context.registerReceiver(receiver = new Receiver(), filter);

            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    listener = new Listener(context);
                }
            };
            mainHandler.post(myRunnable);
        }
        catch (ClassNotFoundException ex) {

        }
        catch (NoSuchFieldException ex) {

        }
        catch (IllegalAccessException ex) {

        }
    }

    public static int getNextAction() {
        int nextAction = -1;
        synchronized (actions) {
            if (actions.size() > 0) {
                nextAction = actions.get(0);
                actions.remove(0);
            }
        }
        return nextAction;
    }
}
