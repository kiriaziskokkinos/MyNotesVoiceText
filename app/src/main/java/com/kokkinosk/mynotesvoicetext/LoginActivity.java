package com.kokkinosk.mynotesvoicetext;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findViewById(R.id.login_offline_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),LandingActivity.class);
                startActivity(intent);
            }
        });
        new User();

        findViewById(R.id.login_online_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.login_progressBar).setVisibility(View.VISIBLE);
                final String url = Website.getUrl() + "php/check_valid_user.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                findViewById(R.id.login_progressBar).setVisibility(View.INVISIBLE);
                                if (response.equals("1")) {
                                    new User(((TextView) findViewById(R.id.username)).getText().toString(),((TextView) findViewById(R.id.password)).getText().toString());
                                    Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
                                    startActivity(intent);
                                } else if (response.equals("-1")) {
                                    Toast.makeText(getApplicationContext(), "Account not found. Check your credentials and try again.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "The server did not respond correctly. Please try again later.", Toast.LENGTH_LONG).show();

                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Something went wrong!");
                        findViewById(R.id.login_progressBar).setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("username", ((TextView) findViewById(R.id.username)).getText().toString());
                        params.put("password", ((TextView) findViewById(R.id.password)).getText().toString());

                        return params;
                    }
                };

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        5000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
            }
        });

        findViewById(R.id.login_sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (User.isLoggedIn()) new User();
    }
}
