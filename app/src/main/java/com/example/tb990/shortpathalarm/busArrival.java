package com.example.tb990.shortpathalarm;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

public class busArrival extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrival);
        getBuslist getBus = new getBuslist("8002940");
        getBus.execute();
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

        }
    }
}
