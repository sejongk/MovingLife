package com.example.tb990.shortpathalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.text.DecimalFormat;

public class walkMap extends AppCompatActivity {
    private Context mContext = null;
    private TMapView tmapview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_map);
        Intent intent = getIntent();
        double startX = Double.valueOf(intent.getStringExtra("startX"));
        double startY = Double.valueOf(intent.getStringExtra("startY"));
        double endX = Double.valueOf(intent.getStringExtra("nextX"));
        double endY = Double.valueOf(intent.getStringExtra("nextY"));
        mContext = this;
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.TmapWalkMap);
        final TMapView tMapView = new TMapView(this);
        tmapview = tMapView;
        tMapView.setSKTMapApiKey("60540fe3-19c2-4b66-9a2e-442a7f53e860");
        linearLayoutTmap.addView( tMapView );
        tmapview.setLocationPoint(startX, startY);
        tmapview.setCenterPoint(startX, startY);
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        TMapData tmapdata = new TMapData();
        final TMapPoint tMapPointStart = new TMapPoint(startX, startY);
        final TMapPoint tMapPointEnd = new TMapPoint(endX, endY);
        /*
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.BLUE);
                tMapPolyLine.setLineWidth(2);
                tmapview.addTMapPolyLine("Line1", tMapPolyLine);
            }
        });
*/

        for(int i=0;i<2;i++){
            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            TMapPoint tMapPoint1;
            if(i==0) tMapPoint1= tMapPointStart; // SKT타워
            else tMapPoint1 = tMapPointEnd;
            markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
            markerItem1.setCanShowCallout(true); // 풍선뷰 사용 여부
            if(i==1) {
                markerItem1.setName("도착지"); // 마커의 타이틀 지정
                markerItem1.setCalloutTitle("도착지");
            }
            else{
                markerItem1.setName("출발지"); // 마커의 타이틀 지정
                markerItem1.setCalloutTitle("출발지");
            }
            tmapview.addMarkerItem(tMapPoint1.toString(), markerItem1); // 지도에 마커 추가
        }

    }
}
