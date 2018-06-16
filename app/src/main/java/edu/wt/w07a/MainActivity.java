package edu.wt.w07a;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText editQueryString;
    ArrayList<ItemQuestion> questions;
    ListView questionsListView;
    Context context = this;
    TextView questionsFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editQueryString = findViewById(R.id.editQueryString);
        questions = new ArrayList<>();
        questionsListView = findViewById(R.id.questionsListView);
        questionsListView.setAdapter(new ItemQuestionAdapter());
        questionsFound = findViewById(R.id.questionsFound);
    }

    public void setCatalogContent(View button) {
        new loadDataTask().execute();
    }


    private class loadDataTask extends AsyncTask<Void, Void, String> {
        private String TAG = "HTTP";

        private static final String requestUrlTemplate = "https://api.stackexchange.com/2.2/search?order=desc&sort=activity&intitle=###&site=stackoverflow";
        private String requestUrl;
        StringBuilder sb;

        @Override
        protected void onPreExecute() {
            String queryString = "";
            try {
                queryString = URLEncoder.encode(editQueryString.getText().toString(),"UTF-8");
            } catch(UnsupportedEncodingException e) {
                Log.e(TAG, e.toString());
            }
            requestUrl = requestUrlTemplate.replace("###",queryString);
            sb = new StringBuilder();
        }

        @Override
        protected String doInBackground(Void... params) {
            String data = "";

            try {
                URL url = new URL(requestUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                data = readDataFromStream(in);

                urlConnection.disconnect();
            } catch (UnknownHostException exception) {
                exception.printStackTrace();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return data;
        }


        @Override
        protected void onPostExecute(String s) {
            try{
                questions.clear();
                JSONObject json = new JSONObject(s);
                JSONArray items = json.getJSONArray("items");
                for(int i=0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    ItemQuestion question = null;
                    question = new ItemQuestion(URLDecoder.decode(item.getString("title"),"UTF-8"),
                        item.getString("link"),
                        item.getLong("question_id"));
                    questions.add(question);
                }
            }
            catch(JSONException|UnsupportedEncodingException e){
                Log.e(TAG, e.toString());
            }
            ((ItemQuestionAdapter)questionsListView.getAdapter()).notifyDataSetChanged();
            questionsListView.setSelectionAfterHeaderView();
            questionsFound.setText("We have found " + questions.size() + " questions");
        }

        private String readDataFromStream(InputStream is) {
            String line;
            BufferedReader reader = null;
            StringBuilder data = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
            return data.toString();
        }
    }

    private class ItemQuestionAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;

        public ItemQuestionAdapter() {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return questions.size();
        }

        @Override
        public Object getItem(int position) {
            return questions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {
            if(rowView == null) {
                rowView = layoutInflater.inflate(R.layout.item_question,null);
            }
            TextView questionText = rowView.findViewById(R.id.question);
            TextView questionLink = rowView.findViewById(R.id.link);
            TextView questionId = rowView.findViewById(R.id.question_id);
            ItemQuestion iq = questions.get(position);
            questionText.setText(iq.getTitle());
            questionLink.setText(iq.getLink());
            questionId.setText(Long.toString(iq.getId()));

            return rowView;
        }
    }
}
