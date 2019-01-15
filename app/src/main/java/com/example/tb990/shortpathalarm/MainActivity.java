package com.example.tb990.shortpathalarm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.AlarmClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    ArrayList<MapPoint> busStops = new ArrayList<>();
    private Context mContext = null;
    private boolean m_bTrackingMode = true;

    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    TMapData tmapdata = new TMapData();
    double cur_lati=0;
    double cur_long=0;
    boolean setting = true; // true 이면 도착지 설정, false 이면 출발지 설정
    int viewNumber = 0;
    @Override
    public void onLocationChange(Location location){
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
            cur_lati = location.getLatitude(); cur_long = location.getLongitude();
            /*  화면중심을 단말의 현재위치로 이동 */
            tmapview.setTrackingMode(true);
            tmapview.setSightVisible(true);
        }
    }
    double dep_long=0;
    double dep_lati=0;
    double dest_long;
    double dest_lati;
    double velocity;
    double distance;

    //alarm
    int mHour=0;
    int mMinute=0;

    //side slide
    private DrawerLayout mDrawerLayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        //EditText test = (EditText)findViewById(R.id.editText);
        //test.setVisibility(View.INVISIBLE);

        try {
            double loadVelo = Double.valueOf(load().toString());
            if (loadVelo > 0) {
                Toast.makeText(getApplicationContext(), "저장된 속력은" + loadVelo + "m/분 입니다.", Toast.LENGTH_LONG).show();
                velocity = loadVelo;

            }
        }catch (Exception e){ }

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        final TMapView tMapView = new TMapView(this);
        tmapview = tMapView;
        tMapView.setSKTMapApiKey("60540fe3-19c2-4b66-9a2e-442a7f53e860");
        linearLayoutTmap.addView( tMapView );

        /* 현재 보는 방향 */
    //   tmapview.setCompassMode(true);
        /* 현위치 아이콘표시 */
        tmapview.setIconVisibility(true);
        /* 줌레벨 */
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        /* gps setting */
        tmapgps = new TMapGpsManager(MainActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
        //실내일 때 유용합니다.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        tmapgps.OpenGps();
        /*  화면중심을 단말의 현재위치로 이동 */
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);
        
        //     Bitmap icon = Bitmap.decodeResource(getResources(),R.drawable.locicon);
    //    tMapView.setIcon();
        //void setTMapPathIcon(Bitmap start, Bitmap end)
        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                viewNumber += 1;
                Log.e("OnPressEvent"+viewNumber,"ON!!!!");
                return false;
            }
            @Override
            public boolean onPressUpEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                if(arrayList.size() >0) {
                    viewNumber+=-1;
                    TMapMarkerItem marker = (TMapMarkerItem) arrayList.get(0);
                    String markerid = marker.getID();
                    if (markerid.substring(0, 3).equals("bus")) {
                        final String id = markerid.substring(3);
                        final TMapPoint markerPoint = marker.getTMapPoint();
                        TMapPoint bus_start;
                        if (dep_lati > 0) bus_start = new TMapPoint(dep_lati, dep_long);
                        else bus_start = new TMapPoint(cur_lati, cur_long);
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, bus_start, markerPoint, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                Intent intent = new Intent(MainActivity.this, busArrival.class);
                                intent.putExtra("id", id);
                                intent.putExtra("time", (tMapPolyLine.getDistance() / velocity));
                                startActivity(intent);
                            }
                        });
                    } if (markerid.substring(0, 3).equals("POI")) {
                        Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.custom_dialog);
                        dialog.setTitle("장소 상세정보");
                        TextView tv = (TextView) dialog.findViewById(R.id.text);
                        String content = marker.getCalloutSubTitle();
                        tv.setText(content);
                        dialog.show();
                    }
                }else{
                    viewNumber += 1;
                    changeView();
                    Log.e("OnPressUpEvent"+viewNumber,"ON!!!!");
                }

                return true;
            }
        });

        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, final TMapPoint tMapPoint) {
                viewNumber += -2;
                Log.e("PressLongEvent"+viewNumber,"ON!!!!");
                if(setting) {
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                    oDialog.setMessage("도착지로 설정하시겠습니까?")
                            .setTitle("도착지 설정")
                            .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("Dialog", "취소");
                                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dest_lati = tMapPoint.getLatitude();
                                    dest_long = tMapPoint.getLongitude();
                                    Toast.makeText(getApplicationContext(), "도착지가 lon=" + tMapPoint.getLongitude() + "\nlat=" + tMapPoint.getLatitude() + "로 설정되었습니다.", Toast.LENGTH_LONG).show();
                                    tmapview.removeAllMarkerItem();
                                    TMapPoint tMapPointStart;
                                    if(dep_lati>0)  tMapPointStart = new TMapPoint(dep_lati, dep_long);
                                    else tMapPointStart = new TMapPoint(cur_lati, cur_long);
                                    TMapPoint tMapPointEnd = new TMapPoint(dest_lati, dest_long); // N서울타워(목적지)
                                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
                                        @Override
                                        public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                            tMapPolyLine.setLineColor(Color.BLUE);
                                            tMapPolyLine.setLineWidth(2);
                                            tMapView.addTMapPolyLine("Line1", tMapPolyLine);
                                            distance = tMapPolyLine.getDistance();
                                            Log.d("도착지 설정거리:", new DecimalFormat("000.######").format(tMapPolyLine.getDistance()));
                                            showMarkerPoint(tMapPoint);
                                        }
                                    });
                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }
                else{
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                    oDialog.setMessage("출발지 설정하시겠습니까?")
                            .setTitle("출발지 설정")
                            .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Log.i("Dialog", "취소");
                                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dep_lati = tMapPoint.getLatitude(); dep_long = tMapPoint.getLongitude();
                                    Toast.makeText(getApplicationContext(), "출발지가 lon=" + tMapPoint.getLongitude() + "\nlat=" + tMapPoint.getLatitude()+"로 설정되었습니다.", Toast.LENGTH_LONG).show();
                                    tmapview.removeAllMarkerItem();
                                    TMapPoint tMapPointStart = new TMapPoint(dep_lati, dep_long);
                                    TMapPoint tMapPointEnd = new TMapPoint(dest_lati, dest_long);

                                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
                                        @Override
                                        public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                            tMapPolyLine.setLineColor(Color.BLUE);
                                            tMapPolyLine.setLineWidth(2);
                                            tMapView.addTMapPolyLine("Line1", tMapPolyLine);
                                            distance = tMapPolyLine.getDistance();
                                            Log.d("출발지 설정거리:", new DecimalFormat("000.######").format(tMapPolyLine.getDistance()));
                                            showMarkerPoint(tMapPoint);

                                        }
                                    });
                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }
            }

        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayout remainLayout = (LinearLayout)findViewById(R.id.remainPart);

        remainLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeView();
                Log.e("OnClickRemainEvent"+viewNumber,"ON!!!!");
            }
        });
        remainLayout.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                changeView();
                return true;
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.setAlarm: // 알람설정
                        Log.e("distance",Double.toString(distance));
                        Log.e("velocity",Double.toString(velocity));
                        double exp_time = distance / velocity;
                        Log.e("time",Double.toString(exp_time));

                        final int exp_hour = (int)exp_time / 60;
                        final int exp_minutes = (int) exp_time % 60;
                        TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mHour = hourOfDay - exp_hour ;
                                mMinute = minute - exp_minutes;
                                if(mMinute < 0){
                                    mHour--;
                                    mMinute = 60+mMinute;
                                }
                                createAlarm("약속시간 지키기",mHour,mMinute);
                            }
                        };
                        new TimePickerDialog(MainActivity.this, mTimeSetListener, mHour, mMinute, false).show();
                        break;

                    case R.id.search: // 검색
                        final EditText edittext = new EditText(MainActivity.this);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("장소 검색");
                        builder.setView(edittext);
                        builder.setPositiveButton("검색",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String strObj = edittext.getText().toString(); //검색어
                                        tmapview.removeAllMarkerItem();
                                        tmapdata.findAllPOI(strObj, new TMapData.FindAllPOIListenerCallback() {
                                            @Override
                                            public void onFindAllPOI(ArrayList poiItem) {
                                                for(int i = 0; i < poiItem.size(); i++) {
                                                    TMapPOIItem item = (TMapPOIItem) poiItem.get(i);
                                                    String subtitle = item.name+"\n"+item.upperAddrName+" "+item.middleAddrName+" "+item.lowerAddrName+"\n"+item.lowerBizName+"\n";
                                                            if(item.desc != null) subtitle += item.desc+"\n";
                                                            if(item.telNo != null) subtitle += "전화번호는 "+item.telNo+"입니다.";
                                                    markPOIPoint(item.getPOIPoint(), item.getPOIName(),subtitle);
                                                }
                                            }
                                        });
                                    }
                                });
                        builder.setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.show();
                        break;

                    case R.id.nearBusStation: // 주변버스정류장
                        JSONObject sObj = new JSONObject();
                        try {
                            sObj.accumulate("latitude", cur_lati);
                            sObj.accumulate("longitude", cur_long);
                            getStops gs = new getStops(sObj);
                            gs.execute();
                        }catch (JSONException e){e.printStackTrace();}
                        break;

                    case R.id.busTest : //버스테스트
                        Intent intent = new Intent(MainActivity.this, busArrival.class);
                        startActivity(intent);
                        break;

                    case R.id.setStart:
                        setting=false;
                        Toast.makeText(MainActivity.this,"출발지 설정 모드입니다.",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.setEnd:
                        setting=true;
                        Toast.makeText(MainActivity.this,"도착지 설정 모드입니다.",Toast.LENGTH_LONG).show();
                        break;

                }
                return true;
            }
        });
    }
    public void showMarkerPoint(TMapPoint point) {// 마커 찍는거 빨간색 포인트.
        TMapMarkerItem markerItem1 = new TMapMarkerItem();
        double exp_time = distance / velocity;
        TMapPoint tMapPoint1 = point; // SKT타워
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
        markerItem1.setCanShowCallout(true); // 풍선뷰 사용 여부
        if(setting) {
            markerItem1.setName("도착지"); // 마커의 타이틀 지정
            markerItem1.setCalloutTitle("도착지");
        }
        else{
            markerItem1.setName("출발지"); // 마커의 타이틀 지정
            markerItem1.setCalloutTitle("출발지");
        }
        markerItem1.setCalloutSubTitle("소요시간은 "+ exp_time);
        tmapview.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가
        tmapview.setCenterPoint( point.getLongitude(), point.getLatitude() );
    }

    public void markPOIPoint(TMapPoint point,String name,String subtitle) {// 마커 찍는거 빨간색 포인트.
        final TMapMarkerItem markerItem1 = new TMapMarkerItem();
        TMapPoint tMapPoint1 = point; // SKT타워
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
        markerItem1.setName(name); // 마커의 타이틀 지정
        markerItem1.setCanShowCallout(true); // 풍선뷰 사용 여부
        markerItem1.setCalloutTitle(name);
        markerItem1.setCalloutSubTitle(subtitle);
        tmapview.addMarkerItem("POI" + point.toString(), markerItem1); // 지도에 마커 추가
    }

    public void createAlarm(String message, int hour, int minutes) {
        ArrayList<Integer> days = new ArrayList<Integer>();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        days.addAll(Arrays.asList(day));
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message) //알람 메세지
                .putExtra(AlarmClock.EXTRA_HOUR, hour) // 알람 HOUR : 24시 기준
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes) // 알람 MINUTE
                .putExtra(AlarmClock.EXTRA_DAYS,days) // 1주일중 무슨요일에 올릴것인지.(반복시 설정하는것 )
                .putExtra(AlarmClock.EXTRA_SKIP_UI, false); //창 전환 안함 FALSE면 알람앱으로 넘어가고, TRUE면 앱안 넘어감
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    public StringBuilder load(){
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();

        try{
            fis = openFileInput(setVelocity.filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String text;

            while((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb;
    }
    private  class getStops extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        JSONObject obj;

        getStops(JSONObject obj) {
            this.obj = obj;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String $url_json = "http://143.248.140.106:3280/getStops";
                URL url = new URL($url_json);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                urlConnection.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                urlConnection.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                urlConnection.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                urlConnection.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                urlConnection.connect();
                //서버로 보내기위해서 스트림 만듬
                OutputStream outStream = urlConnection.getOutputStream();
                //버퍼를 생성하고 넣음
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(obj.toString());
                writer.flush();
                writer.close();//버퍼를 받아줌

                InputStream stream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임
        }


        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            try{
                tmapview.removeAllMarkerItem();
                ArrayList<MapPoint> tmp_bus = new ArrayList<>();
                JSONArray arr = new JSONArray(strJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    MapPoint point = new MapPoint(obj.getString("name"), Double.valueOf(obj.getString("latitude")), Double.valueOf(obj.getString("longitude")));
                    tmp_bus.add(point);
                    TMapMarkerItem markerItem1 = new TMapMarkerItem();
                    TMapPoint tMapPoint1 = new TMapPoint(point.getLatitude(), point.getLongitude()); // SKT타워
                    markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
                    markerItem1.setName(obj.getString("name")); // 마커의// 타이틀 지정
                    markerItem1.setCalloutTitle(obj.getString("name"));
                    markerItem1.setCalloutSubTitle("버스 정류장");
                    markerItem1.setCanShowCallout(true); // 풍선뷰 사용 여부
                    tmapview.addMarkerItem("bus" + obj.getString("id"), markerItem1); // 지도에 마커 추가
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void changeView() {
        //LinearLayout view1 = (LinearLayout) findViewById(R.id.linearLayoutTmap) ;
        DrawerLayout view2 = (DrawerLayout) findViewById(R.id.drawer_layout) ;
        switch (viewNumber) {
            case 2 :
                view2.setVisibility(View.VISIBLE) ;
                viewNumber=0;
                break ;
            case 0:
                view2.setVisibility(View.INVISIBLE) ;
                break ;
        }
    }
}
