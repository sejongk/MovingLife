package com.example.tb990.shortpathalarm;

import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class busArrival extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MainViewAdapter adapter;
    ArrayList<busInfo> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrival);

        getBuslist getBus = new getBuslist("8002940");
        getBus.execute();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        adapter = new MainViewAdapter(this,list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    public class MainViewAdapter extends RecyclerView.Adapter<MainViewAdapter.Holder> {
        private Context context;
        private List<busInfo> list;
        public MainViewAdapter(Context context,List<busInfo> list) {
            this.context = context;
            this.list = list;
        }

        // ViewHolder 생성
        // row layout을 화면에 뿌려주고 holder에 연결
        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bus_row, parent, false);
            Holder holder = new Holder(view);
            return holder;
        }


        @Override
        public void onBindViewHolder(Holder holder, int position) {
            // 각 위치에 문자열 세팅
            final int itemposition = position;
            holder.busNumText.setText(list.get(itemposition).busNum);
            holder.busNumText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String busNum = list.get(itemposition).busNum;
                    String extime_min = list.get(itemposition).extime_min;
                    String status_pos = list.get(itemposition).status_pos;
/*
                    Intent intent = new Intent(this,PersonInfo.class);
                    intent.putExtra("busNum",busNum);
                    intent.putExtra("extime_min",extime_min);
                    intent.putExtra("status_pos",status_pos);
                    startActivity(intent);
*/
                }
            });

            // Log.e("StudyApp", "onBindViewHolder" + itemposition);
        }

        // 몇개의 데이터를 리스트로 뿌려줘야하는지 반드시 정의해줘야한다
        @Override
        public int getItemCount() {
            return list.size(); // RecyclerView의 size return
        }

        // ViewHolder는 하나의 View를 보존하는 역할을 한다
        public class Holder extends RecyclerView.ViewHolder{
            public TextView extimeText;
            public TextView busNumText;
            public TextView posText;
            public Holder(View view){
                super(view);
                busNumText = (TextView) view.findViewById(R.id.busNumText);
            }
        }


    }

    private  class getBuslist extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String stopId;

        JSONObject data = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        getBuslist(String stopId){
            this.stopId =stopId;
        }
        @Override
        protected String doInBackground(Void... params) {
            try {
                String $url_json = "http://openapitraffic.daejeon.go.kr/api/rest/arrive/getArrInfoByStopID?serviceKey=" +
                        "Y7P4OnJUKz%2BdXlsbpmPWg41oVpkLNOMQgUi2T5Dml8l0J57zY8RWUqEvcgnOLYa%2FrfGtqiWdraowCUmrQH1uSw%3D%3D&BusStopID="+stopId;
                URL url = new URL($url_json);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFactory.newPullParser();
                parser.setInput(inputStream, "UTF-8");

                boolean target = false;
                String startTag = "";
                //차량번호, 도착예정시간, 메세지 유형, 노선번호, 잔여정류장 수
                //EXTIME_MIN, MSP_TP, ROUTE_NO, STATUS_POS
                int eventType = parser.getEventType();
                JSONObject jsonObject = new JSONObject();
                String tmp_tag = "";
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tmp_tag = parser.getName();
                        startTag = parser.getName();
                        if (startTag.equals("EXTIME_MIN")) {
                            jsonObject = new JSONObject();
                            target = true;
                        }
                        if (startTag.equals("MSG_TP") || startTag.equals("ROUTE_NO") || startTag.equals("STATUS_POS")) {
                            target = true;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        String text = parser.getText();
                        if (target) {
                            jsonObject.put(startTag, text);
                            target = false;
                            if (tmp_tag.equals("STATUS_POS")) {
                                jsonArray.put(jsonObject);
                            }
                        }
                    }
                    eventType = parser.next();
                }
                Log.e("!!!end", jsonArray.toString());
                data.put("!!!data", jsonArray);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            ArrayList<busInfo> tmp_arr = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++) {
                try {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    String extime_min = jObj.getString("EXTIME_MIN");
                    String busNum = jObj.getString("ROUTE_NO");
                    String status_pos = jObj.getString("STATUS_POS");
                    busInfo tmp_item = new busInfo(extime_min, busNum, status_pos);
                    tmp_arr.add(tmp_item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            list = tmp_arr;
            recyclerView.setHasFixedSize(true);
            adapter = new MainViewAdapter(busArrival.this,list);
            recyclerView.setLayoutManager(new LinearLayoutManager(busArrival.this));
            recyclerView.setAdapter(adapter);
        }
    }
}
