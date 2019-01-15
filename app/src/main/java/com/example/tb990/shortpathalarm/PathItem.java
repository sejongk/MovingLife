package com.example.tb990.shortpathalarm;

import java.nio.file.Path;

public class PathItem {
    int path_type=0;  //3 도보 2 버스 start 5 버스 end 4 버스 정류장
    int path_distance=0;
    int path_sectionTime=0;
    String name="default";

    String startName;
    String endName;
    int stationCount;

    String cordi_X;
    String cordi_Y;

    String next_X;
    String next_Y;

    PathItem(int type, int distance, int sectionTime){
        this.path_distance = distance;
        this.path_sectionTime = sectionTime;
        this.path_type = type;
    }
    PathItem(int type, int distance, int sectionTime,String name){
        this.path_distance = distance;
        this.path_sectionTime = sectionTime;
        this.path_type = type;
        this.name = name;
    }
    PathItem(int type, int distance, int sectionTime,String name,String cordi_X, String cordi_Y){
        this.path_distance = distance;
        this.path_sectionTime = sectionTime;
        this.path_type = type;
        this.name = name;
        this.cordi_X = cordi_X;
        this.cordi_Y = cordi_Y;
    }
    PathItem(int type, int distance, int sectionTime,String name,String cordi_X, String cordi_Y,String next_X, String next_Y){
        this.path_distance = distance;
        this.path_sectionTime = sectionTime;
        this.path_type = type;
        this.name = name;
        this.cordi_X = cordi_X;
        this.cordi_Y = cordi_Y;
        this.next_X = next_X;
        this.next_Y = next_Y;
    }
    //                        dataList.add(new PathItem(2,distance,sectionTime,busNo,startName,endName,stationCount));
    PathItem(int type, int distance, int sectionTime, String busNo, String startName, String endName, int stationCount,String endX, String endY){
        this.path_type = type;
        this.path_distance = distance;
        this.path_sectionTime = sectionTime;
        this.name = busNo;
        this.startName = startName;
        this.endName = endName;
        this.stationCount = stationCount;
        this.cordi_X = endX;
        this.cordi_Y = endY;
    }
}
