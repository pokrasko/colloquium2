package ru.ifmo.md.colloquium2;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by pokrasko on 11.11.14.
 */
public class VoteResultReceiver extends ResultReceiver {
    Context context;
    public static final int OK = 0;
    public static final int PERSON_EXISTS_ERROR = 1;
    public static final int EXCEPTION_ERROR = 2;

    public VoteResultReceiver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    protected void onReceiveResult(int code, Bundle bundle) {
        String message = null;
        switch (code) {
        }
    }
}
