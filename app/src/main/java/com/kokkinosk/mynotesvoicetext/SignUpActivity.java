package com.kokkinosk.mynotesvoicetext;

import android.app.Activity;
import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.kokkinosk.mynotesvoicetext.RecordingManager.cloudRecordings;
import static com.kokkinosk.mynotesvoicetext.RecordingManager.recordingArrayList;

public class SignUpActivity extends AppCompatActivity {
    private WeakReference<Activity> activityReference;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        activityReference = new WeakReference<>((Activity)this);
        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = ((TextView) findViewById(R.id.register_username)).getText().toString();
                final String password = ((TextView) findViewById(R.id.register_password)).getText().toString();
                boolean fieldsValid = true;
                final String passwordVerification = ((TextView) findViewById(R.id.register_verify_password)).getText().toString();
                Log.d("REGISTER",password + " " + passwordVerification);

                if (username.length() < 4) {
                    ((TextView) findViewById(R.id.register_username)).setError("Username must be at least 4 characters long.");
                    fieldsValid = false;
                }

                if (password.length() < 8) {
                    ((TextView) findViewById(R.id.register_password)).setError("Password must be at least 8 characters long.");
                    fieldsValid = false;
                }

                if (!password.equals(passwordVerification)) {
                    ((TextView) findViewById(R.id.register_verify_password)).setError("Passwords do not match.");
                    fieldsValid = false;
                }
                if (fieldsValid) {
                    new VolleyController(activityReference.get());
                    final String register_url = Website.getUrl() + "php/add_user.php";

                    Map<String, String> params2 = new HashMap<>();
                    params2.put("username",username);
                    params2.put("password", password);

                    StringRequest request = new StringRequest(Request.Method.POST, register_url, new Response.Listener<String>() {
                        @Override
                        public synchronized void onResponse(String response) {
                            Log.d("REGISTER_RESULT", response);
                            if (response.equals("1")) {
                                Toast.makeText(activityReference.get(), "Account created succesfully!", Toast.LENGTH_LONG).show();
                                activityReference.get().onBackPressed();
                            } else if (response.equals("ERREXISTS")) {
                                ((TextView) findViewById(R.id.register_username)).setError("Username already exists.");
                            } else {
                                Toast.makeText(activityReference.get(), "Account could not be created. Try again later.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(activityReference.get(), "An error has occurred. Check your network connection and try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                    request.setRetryPolicy(new DefaultRetryPolicy(
                            5000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    request.setParams(params2);

                    VolleyController.getInstance().getRequestQueue().add(request);
                }






            }
        });


    }
}
