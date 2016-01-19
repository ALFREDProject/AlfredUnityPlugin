package de.tu_darmstadt.kom.alfredplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;

class Receiver extends BroadcastReceiver {
    public static final String PAUSE_GAME = "eu.alfred.api.PauseGame";
    public static final String RESUME_GAME = "eu.alfred.api.ResumeGame";
    public static final String STOP_GAME = "eu.alfred.api.StopGame";

    public static final int PAUSE = 0;
    public static final int RESUME = 1;
    public static final int STOP = 2;

    public final ArrayList<Integer> actions = new ArrayList<Integer>();

    public void onReceive(Context context, Intent intent) {
        String actionString = intent.getAction();
        int action;
        if (actionString.equals(PAUSE_GAME)) {
            action = PAUSE;
        }
        else if (actionString.equals(RESUME_GAME)) {
            action = RESUME;
        }
        else if (actionString.equals(STOP_GAME)) {
            action = STOP;
        }
        else {
            return;
        }
        synchronized (actions) {
            actions.add(action);
        }
    }
}

public class Alfred {
    private static Receiver receiver;

    public static void init() {
        try {
            Class clazz = Class.forName("com.unity3d.player.UnityPlayer");
            Context context = (Context)clazz.getField("currentActivity").get(null);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Receiver.PAUSE_GAME);
            filter.addAction(Receiver.RESUME_GAME);
            filter.addAction(Receiver.STOP_GAME);
            context.registerReceiver(receiver = new Receiver(), filter);
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
        synchronized (receiver.actions) {
            if (receiver.actions.size() > 0) {
                nextAction = receiver.actions.get(0);
                receiver.actions.remove(0);
            }
        }
        return nextAction;
    }
}
