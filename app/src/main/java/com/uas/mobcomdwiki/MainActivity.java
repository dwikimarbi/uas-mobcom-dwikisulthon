package com.uas.mobcomdwiki;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Domain;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Validator.ValidationListener {

    @NotEmpty @Domain
    EditText editText;
    Button button;
    Spinner spinner;
    TextView textView;

    ProgressDialog mProgressDialog;
    String htmlSpinner;
    String htmlText;
    String url;
    String title;

    Validator validator;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        spinner = findViewById(R.id.spinner);
        textView = findViewById(R.id.textViewOutput);

        validator = new Validator(this);
        validator.setValidationListener(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    validator.validate();
                } catch (Exception e) {
                    // This will catch any exception, because they are all descended from Exception
                    String message = "Error " + e.getMessage();
                    Log.e(TAG, message);
                }

            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        htmlSpinner = spinner.getSelectedItem().toString();
        htmlText = editText.getText().toString().trim();

        url = htmlSpinner+htmlText;
        new Title().execute();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Title extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Get Page Source");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                title = response.toString().trim();
                                title = title.replace("\n", "");
                                Log.d(TAG, title);
                                textView.setText(title);
                                mProgressDialog.dismiss();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        title ="Error!";
                        textView.setText(title);
                        mProgressDialog.dismiss();
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set title into TextView
            textView.setText(title);
            mProgressDialog.dismiss();
        }
    }

}