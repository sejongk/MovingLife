package com.example.tb990.shortpathalarm;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.AlarmClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class busAlarm extends AppCompatActivity {
    ODsayService odsayService;
    String SX ="127.363709";
    String SY ="36.372896";
    String EX = "127.449876";
    String EY = "36.341166";
    String tmp_EndX;
    String tmp_EndY;

    ArrayList<PathItem> dataList = new ArrayList<PathItem>();
    private static CustomAdapter adapter;
    ListView listView;

     LocationManager lm;

     boolean alarmFlag = false;
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double tmpX = Double.valueOf(tmp_EndX) - location.getLongitude();
            double tmpY = Double.valueOf(tmp_EndY) - location.getLatitude();
            double distance = Math.sqrt((tmpX*tmpX) + (tmpY*tmpY));
            if(distance <= 0.002){
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minutes = Calendar.getInstance().get(Calendar.MINUTE);
                int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                        .putExtra(AlarmClock.EXTRA_MESSAGE, "목표 정류장에 곧 도착합니다.") //알람 메세지
                        .putExtra(AlarmClock.EXTRA_HOUR, hour) // 알람 HOUR : 24시 기준
                        .putExtra(AlarmClock.EXTRA_MINUTES, minutes) // 알람 MINUTE
                        .putExtra(AlarmClock.EXTRA_DAYS,day) // 1주일중 무슨요일에 올릴것인지.(반복시 설정하는것 )
                        .putExtra(AlarmClock.EXTRA_SKIP_UI, true); //창 전환 안함 FALSE면 알람앱으로 넘어가고, TRUE면 앱안 넘어감
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }

        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_alarm);
        listView=(ListView)findViewById(R.id.listViewExample);

        // 싱글톤 생성, Key 값을 활용하여 객체 생성
        odsayService = ODsayService.init(getApplicationContext(), "2THoVtikyd6tIxR1mQvyKpdnBPWdJ9mcVzBiiRTvtrs");
        // 서버 연결 제한 시간(단위(초), default : 5초)
        odsayService.setReadTimeout(5000);
        // 데이터 획득 제한 시간(단위(초), default : 5초)
        odsayService.setConnectionTimeout(5000);
        //odsayService.requestSearchPubTransPath(SX,SY,EX,EY,"0","0","2", onResultCallbackListener);
        odsayService.requestSearchPubTransPath(SX,SY,EX,EY,"0","0","2", onResultCallbackListener);

    }

    void busAlarmStart(){
        alarmFlag = true;
        lm= (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                50, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                50, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
    }
    void busAlarmStop(){
        alarmFlag = false;
    lm.removeUpdates(mLocationListener);
    }

    OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        // 호출 성공 시 실행
        @Override
        public void onSuccess(ODsayData odsayData, API api) {
            try {
                JSONObject jsonObject = odsayData.getJson();
               // String stationName = odsayData.getJson().getJSONObject("result").getString("stationName");
                JSONArray subPath = jsonObject.getJSONObject("result").getJSONArray("path").getJSONObject(0).getJSONArray("subPath");
                for(int i=0 ; i<subPath.length();i++){
                    JSONObject subObj = subPath.getJSONObject(i);
                    int trafficType = subObj.getInt("trafficType");
                    int distance = subObj.getInt("distance");
                    int sectionTime = subObj.getInt("sectionTime");

                    if(trafficType == 2){
                        String busNo = subObj.getJSONArray("lane").getJSONObject(0).getString("busNo");
                        int stationCount = subObj.getInt("stationCount");
                        JSONArray stations = subObj.getJSONObject("passStopList").getJSONArray("stations");
                        String startName = subObj.getString("startName");
                        double startX = subObj.getDouble("startX");
                        double startY = subObj.getDouble("startY");
                        String endName = subObj.getString("endName");
                        double endX = subObj.getDouble("endX");
                        double endY = subObj.getDouble("endY");
                        dataList.add(new PathItem(2,distance,sectionTime,startName));
                        dataList.add(new PathItem(5,distance,sectionTime,endName,Double.toString(endX),Double.toString(endY)));
                        //버스 정류장 추가
                        for(int j=1;j<=stationCount;j++){
                            JSONObject station = stations.getJSONObject(j-1);
                        }
                    }
                    if(trafficType == 3){
                        if(i==0) {
                            JSONObject nextObj = subPath.getJSONObject(1).getJSONObject("passStopList").getJSONArray("stations").getJSONObject(0);
                            dataList.add(new PathItem(trafficType, distance, sectionTime, "도보로 이동", SX, SY,nextObj.getString("x"), nextObj.getString("y")));
                        }
                        else if(i == (subPath.length()-1)){
                            JSONObject prevObj = subPath.getJSONObject(1).getJSONObject("passStopList").getJSONArray("stations").getJSONObject(subPath.length()-2);
                            dataList.add(new PathItem(trafficType, distance, sectionTime, "도보로 이동", prevObj.getString("x"), prevObj.getString("y"),EX,EY));
                        }
                        else{
                            JSONObject prevObj = subPath.getJSONObject(1).getJSONObject("passStopList").getJSONArray("stations").getJSONObject(i-1);
                            JSONObject nextObj = subPath.getJSONObject(1).getJSONObject("passStopList").getJSONArray("stations").getJSONObject(i+1);
                            dataList.add(new PathItem(trafficType, distance, sectionTime, "도보로 이동", prevObj.getString("x"), prevObj.getString("y"),nextObj.getString("x"),nextObj.getString("y")));
                        }

                        }
                }
                }catch (JSONException e) {
                e.printStackTrace();
            }
            adapter= new CustomAdapter(dataList,getApplicationContext());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PathItem dataModel= dataList.get(position);
                if(dataModel.path_type == 3) { //도보라면 walkMap 에서 도보길안내를 한다.
                Intent intent = new Intent(busAlarm.this, walkMap.class);
                intent.putExtra("startX",dataModel.cordi_X);
                intent.putExtra("startY",dataModel.cordi_Y);
                intent.putExtra("nextX",dataModel.next_X);
                intent.putExtra("nextY",dataModel.next_Y);
                Log.e(dataModel.name,"test: "+dataModel.cordi_X);
                Log.e(dataModel.name,"test: "+dataModel.cordi_Y);
                Log.e(dataModel.name,"test: "+dataModel.next_X);
                Log.e(dataModel.name,"test: "+dataModel.next_Y);
                startActivity(intent);
                }
                if(dataModel.path_type == 5) {
                   if(!alarmFlag) {
                       tmp_EndX = dataModel.cordi_X;
                       tmp_EndY = dataModel.cordi_Y;
                       busAlarmStart();
                       Toast.makeText(getApplicationContext(), "알람이 시작되었습니다.", Toast.LENGTH_LONG).show();

                   }
                   else {
                       busAlarmStop();
                       Toast.makeText(getApplicationContext(), "알람이 종료되었습니다.", Toast.LENGTH_LONG).show();
                   }
                }
            }
        });
        }
        // 호출 실패 시 실행
        @Override
        public void onError(int i, String s, API api) {
            Log.d("error", "error");
        }
    };

    public class CustomAdapter extends ArrayAdapter<PathItem> implements View.OnClickListener{

        private ArrayList<PathItem> dataSet;
        Context mContext;

        // View lookup cache
        private class ViewHolder {
            TextView txtName;
            TextView txtType;
            TextView txtVersion;
            ImageView info;
        }

        public CustomAdapter(ArrayList<PathItem> data, Context context) {
            super(context, R.layout.row_item, data);
            this.dataSet = data;
            this.mContext=context;
        }

        @Override
        public void onClick(View v) {
            int position=(Integer) v.getTag();
            Object object= getItem(position);
            PathItem dataModel=(PathItem)object;
        }

        private int lastPosition = -1;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            PathItem dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag
            final View result;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.row_item, parent, false);
                viewHolder.txtName = (TextView) convertView.findViewById(R.id.itemname);
                viewHolder.txtType = (TextView) convertView.findViewById(R.id.itemtype);
                viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.distance);
         //       viewHolder.info = (ImageView) convertView.findViewById(R.id.sectime);
                result=convertView;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result=convertView;
            }

            lastPosition = position;
            viewHolder.txtName.setText(dataModel.name);
            viewHolder.txtType.setText(Integer.toString(dataModel.path_type));
            viewHolder.txtVersion.setText(Integer.toString(dataModel.path_distance));
//            viewHolder.info.setOnClickListener(this);
   //         viewHolder.info.setTag(position);
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
