package com.example.tb990.shortpathalarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class setVelocity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{
    DecimalFormat fmt = new DecimalFormat("0.#");

    String locationProvider = LocationManager.NETWORK_PROVIDER;
    LocationManager  lm;

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            tmapview.setLocationPoint( location.getLongitude(),location.getLatitude());
            tmapview.setCenterPoint(location.getLongitude(), location.getLatitude());
            tmapview.setTrackingMode(true);
            tmapview.setSightVisible(true);

        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };




    final static String filename = "savedVelo.txt";
    double toLong=0;
    double toLati=0;
    double fromLong=0;
    double fromLati=0;
    double time;
    boolean setLoc = true; //true면 출발지, false면 도착지

    static double velocity;
    private boolean m_bTrackingMode = true;
    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;

    double cur_lati=0;
    double cur_long=0;

    boolean check = true;



    @Override
    public void onLocationChange(Location location){
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
            cur_lati = location.getLatitude(); cur_long = location.getLongitude();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_velocity);
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);
        tmapview = tMapView;
        tMapView.setSKTMapApiKey("60540fe3-19c2-4b66-9a2e-442a7f53e860");
        linearLayoutTmap.addView( tMapView );

        lm= (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = lm.getLastKnownLocation(locationProvider);
        //gps setting
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();
            tmapview.setLocationPoint(lng,lat);
            tmapview.setCenterPoint( lng,lat);
            tmapview.setTrackingMode(true);
            tmapview.setSightVisible(true);

        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                5, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                5, // 통지사이의 최소 변경거리 (m)
                mLocationListener);


        Intent intent2 = getIntent();
        check = intent2.getBooleanExtra("check",true);
        //속력 파일 로딩
        try {
            double loadVelo = Double.valueOf(load().toString());
            if (loadVelo > 0 && check) {
                velocity = loadVelo;
                Intent intent = new Intent(setVelocity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "평소 다니는 거리와 이동시간을 설정해주세요.", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){ }




        /* 현위치 아이콘표시 */
        tmapview.setIconVisibility(true);
        /* 줌레벨 */
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        /*  화면중심을 단말의 현재위치로 이동 */
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);


        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, final TMapPoint tMapPoint) {
                if(setLoc){ //출발지
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(setVelocity.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                    oDialog.setMessage("출발지로 설정하시겠습니까?")
                            .setTitle("출발지 설정")
                            .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    fromLati = tMapPoint.getLatitude(); fromLong = tMapPoint.getLongitude();
                                    String test ="lon=" + fromLong + "\nlat=" +fromLati+"로  출발지가 설정되었습니다.";
                                    setLoc = false;
                                    Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
                                    TMapMarkerItem markerItem1 = new TMapMarkerItem();
                                    TMapPoint tMapPoint1 = new TMapPoint(fromLati,fromLong); // SKT타워
                                    markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                                    markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
                                    markerItem1.setCanShowCallout(true); // 풍선뷰 사용 여부
                                    markerItem1.setName("출발지"); // 마커의 타이틀 지정
                                    markerItem1.setCalloutTitle("출발지");
                                    tmapview.addMarkerItem("startMark", markerItem1); // 지도에 마커 추가

                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }
                else{ //도착지
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(setVelocity.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                    oDialog.setMessage("도착지로 설정하시겠습니까?")
                            .setTitle("도착지 설정")
                            .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    toLati = tMapPoint.getLatitude(); toLong = tMapPoint.getLongitude();
                                    String test ="lon=" + toLong + "\nlat=" +toLati+"로  도착지가 설정되었습니다.";
                                    Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
                                    TMapMarkerItem markerItem1 = new TMapMarkerItem();
                                    TMapPoint tMapPoint1 = new TMapPoint(toLati,toLong); // SKT타워
                                    markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                                    markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
                                    markerItem1.setCanShowCallout(true); // 풍선뷰 사용 여부
                                    markerItem1.setName("도착지"); // 마커의 타이틀 지정
                                    markerItem1.setCalloutTitle("도착지");
                                    tmapview.addMarkerItem("endMark", markerItem1); // 지도에 마커 추가
                                    setLoc = true;
                                    setTime();
                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }
            }
        });


    }

    public void setTime() {
        final EditText edittext = new EditText(this);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이동 시간");
        builder.setMessage("도보로 몇 분정도 걸리시나요?");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            time = Double.valueOf(edittext.getText().toString());
                        }catch (NumberFormatException e){
                            Toast.makeText(getApplicationContext(), "올바른 값이 입력되지 않았습니다.", Toast.LENGTH_LONG).show();
                            toLong =0;
                            toLati=0;
                            fromLati=0;
                            fromLong=0;
                            setLoc = true;
                            tmapview.removeAllMarkerItem();
                        }
                        if (toLong > 0 && toLati > 0 && fromLati > 0 && fromLong > 0) {
                            TMapPoint tMapPointStart = new TMapPoint(toLati, toLong);
                            TMapPoint tMapPointEnd = new TMapPoint(fromLati, fromLong);
                            TMapData tmapdata = new TMapData();
                            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
                                @Override
                                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                    double distance = tMapPolyLine.getDistance();
                                    velocity = distance / time;
                                    String veloText = Double.toString(velocity);
                                    save(veloText);
                                    Intent intent = new Intent(setVelocity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "출발지나 도착지가 똑바로 설정되지 않았습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }
    public void save(String text){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(text.getBytes());
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null){
                try {
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public StringBuilder load(){
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();

        try{
            fis = openFileInput(filename);
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

}
