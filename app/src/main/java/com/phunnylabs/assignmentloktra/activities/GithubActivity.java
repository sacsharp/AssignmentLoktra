package com.phunnylabs.assignmentloktra.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.phunnylabs.assignmentloktra.R;
import com.phunnylabs.assignmentloktra.adapter.GithubCommitsAdapter;
import com.phunnylabs.assignmentloktra.models.Commit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GithubActivity extends AppCompatActivity {

    private static final String TAG = "GithubActivityLogs";
    private ListView mListViewCommits;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_github);


        mListViewCommits = (ListView) findViewById(R.id.listViewCommits);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting Commits...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Thread thread = new Thread(this::listCommits);
        thread.start();
    }

    void listCommits() {
        ArrayList<Commit> commits = new ArrayList<>();
        HttpURLConnection urlConnection;
        URL url;
        InputStream inputStream;

        try {
            url = new URL("https://api.github.com/repos/rails/rails/commits?sha=master");
            urlConnection = (HttpURLConnection) url.openConnection();

            //set request type
            urlConnection.setRequestMethod("GET");


            urlConnection.setDoInput(true);
            urlConnection.connect();
            //check for HTTP response
            int httpStatus = urlConnection.getResponseCode();

            //if HTTP response is 200 i.e. HTTP_OK read inputstream else read errorstream
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getErrorStream();
                //print GitHub api hearder data
                Map<String, List<String>> map = urlConnection.getHeaderFields();
                System.out.println("Printing Response Header...\n");
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    System.out.println(entry.getKey()
                            + " : " + entry.getValue());
                }
            } else {
                inputStream = urlConnection.getInputStream();
            }

            //read inputstream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp, response = "";
            while ((temp = bufferedReader.readLine()) != null) {
                response += temp;
            }

            //GitHub api has limit to access over http.
            //Api rate limit is 10req/min for unauthenticated user and 30req/min is for authenticated user
            if (response.contains("API rate limit exceeded")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "API Rate Limit Exceeded. Please wait. ", Toast.LENGTH_SHORT).show();
                });

            } else {
                //convert data string into JSONObject
                Log.d(TAG, "listCommits: " + response);
                JSONArray jsonArrayCommits = new JSONArray(response);

                if (jsonArrayCommits.length() > 0) {

                    for (int i = 0; i < jsonArrayCommits.length(); i++) {
                        JSONObject jsonObjectCompleteCommit = jsonArrayCommits.getJSONObject(i);

                        JSONObject jsonCommitInCompleteCommit = jsonObjectCompleteCommit.optJSONObject("commit");
                        JSONObject jsonAuthorInCommit = jsonCommitInCompleteCommit.optJSONObject("author");

                        JSONObject jsonAuthorInCompleteCommit = jsonObjectCompleteCommit.optJSONObject("author");

                        Commit commit = new Commit();

                        commit.setAuthorName(jsonAuthorInCommit.optString("name"));
                        commit.setAuthorURL(jsonAuthorInCompleteCommit.optString("html_url"));
                        commit.setAvatarURL(jsonAuthorInCompleteCommit.optString("avatar_url"));

                        commit.setCommitID(jsonObjectCompleteCommit.optString("sha"));
                        commit.setCommitURL(jsonObjectCompleteCommit.optString("html_url"));
                        commit.setCommitMessage(jsonCommitInCompleteCommit.optString("message"));

                        commits.add(commit);
                    }

                    runOnUiThread(() -> {
                        mProgressDialog.dismiss();
                        setAdapterForList(commits);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Can't get the data from Github.", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            urlConnection.disconnect();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                finish();
                Toast.makeText(GithubActivity.this, "Error in Connecting. Please check Internet.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setAdapterForList(ArrayList<Commit> commits) {
        GithubCommitsAdapter githubCommitsAdapter = new GithubCommitsAdapter(this, commits);
        mListViewCommits.setAdapter(githubCommitsAdapter);
    }
}
