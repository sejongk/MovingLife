package com.example.tb990.shortpathalarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
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

        //속력 파일 로딩
        try {
            double loadVelo = Double.valueOf(load().toString());
            if (loadVelo > 0) {
                velocity = loadVelo;
                Intent intent = new Intent(setVelocity.this, MainActivity.class);
                startActivity(intent);
            }
        }catch (Exception e){ }

        Button getVelo = (Button)findViewById(R.id.getVelo);
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);
        tmapview = tMapView;
        tMapView.setSKTMapApiKey("60540fe3-19c2-4b66-9a2e-442a7f53e860");
        linearLayoutTmap.addView( tMapView );

        /* 현재 보는 방향 */
        tmapview.setCompassMode(true);
        /* 현위치 아이콘표시 */
        tmapview.setIconVisibility(true);
        /* 줌레벨 */
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        /* gps setting */
        tmapgps = new TMapGpsManager(setVelocity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
        //실내일 때 유용합니다.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        tmapgps.OpenGps();
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
                                    Log.i("Dialog", "취소");
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
                                    Log.i("Dialog", "취소");
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
                                    setLoc = true;

                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }
            }
        });

        getVelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime();
                }
        });
    }

    public void setTime() {
        final EditText edittext = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("평균 이동 시간");
        builder.setMessage("도보로 몇 분정도 걸리시나요?");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       time = Double.valueOf(edittext.getText().toString());

                        if (toLong > 0 && toLati > 0 && fromLati > 0 && fromLong > 0) {
                            TMapPoint tMapPointStart = new TMapPoint(toLati, toLong);
                            TMapPoint tMapPointEnd = new TMapPoint(fromLati, fromLong);
                            TMapData tmapdata = new TMapData();
                            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
                                @Override
                                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                    double distance = tMapPolyLine.getDistance();
                                    Log.e("거리:", Double.toString(distance) );
                                    Log.e("시간:", Double.toString(time) );
                                    velocity = distance / time;
                                    Log.e("속력:", Double.toString(velocity) );
                                    String veloText = Double.toString(velocity);
                                    save(veloText);
                                    Toast.makeText(getApplicationContext(), "속력이" + fmt.format(veloText) + "m/분으로 설정되었습니다.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(setVelocity.this, MainActivity.class);
                                    startActivity(intent);

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
/*
        TMapPoint tpoint = tmapview.getLocationPoint();
        double Latitude = tpoint.getLatitude();
        double Longitude = tpoint.getLongitude();

        TMapPoint tMapPointStart = new TMapPoint(36.372106, 127.360373);
        TMapPoint tMapPointEnd = new TMapPoint(36.372121, 127.358649);
        TMapData tmapdata = new TMapData();
        tmapdata.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document document) {
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                    for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                            Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim() );
                        }
                    }
                }
            }
        });
*/
                  /*



            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine tMapPolyLine) {
                        Log.d("거리:", new DecimalFormat("000.######").format(tMapPolyLine.getDistance()));
                    }
                }
        );
            */