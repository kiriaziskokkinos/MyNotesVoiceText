package com.kokkinosk.mynotesvoicetext;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;

/**
 * Created by Akshay Raj on 7/17/2016.
 * Snow Corporation Inc.
 * www.bmnepali.org
 */
public class VolleyController {
    public static final String TAG = VolleyController.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private Context context;
    private static VolleyController mInstance;
    private WeakReference activityReference;
    private WeakReference activityContext;


    public VolleyController(Activity activity){
        if (activity!= null){
            activityReference = new WeakReference<>(activity);
            activityContext = new WeakReference<>(activity.getApplicationContext());
        }
        mInstance = this;
    }



    public static synchronized VolleyController getInstance() {
        if (mInstance == null) {
            Log.e("TEST","mInstance is null!");
        }

        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue((Context)activityContext.get());
        }
        if (mRequestQueue == null) {
            Log.e("TEST","RQ is null!");
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}