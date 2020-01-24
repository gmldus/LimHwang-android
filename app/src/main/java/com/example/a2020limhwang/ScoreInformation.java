package com.example.a2020limhwang;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ScoreInformation implements Serializable {
    float rate, points_abs, count_abs, points_lat, count_lat, score;
    int attendanceNum, absenceNum, latenessNum;

    public ScoreInformation(float rate, float points_abs, float count_abs, float points_lat, float count_lat) {
        this.rate = rate;
        this.points_abs = points_abs;
        this.count_abs = count_abs;
        this.points_lat = points_lat;
        this.count_lat = count_lat;
        attendanceNum = 0;
        absenceNum = 0;
        latenessNum = 0;
        score = 0;
    }
}
