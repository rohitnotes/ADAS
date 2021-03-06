/*
 * Copyright (c) 2017 Ahmed-Abdelmeged
 *
 * github: https://github.com/Ahmed-Abdelmeged
 * email: ahmed.abdelmeged.vm@gamil.com
 * Facebook: https://www.facebook.com/ven.rto
 * Twitter: https://twitter.com/A_K_Abd_Elmeged
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mego.adas.accidents.db.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Define the Data structure for accident  information
 */
@Entity(tableName = "accidents")
public class Accident {

    @PrimaryKey
    private String accidentId;

    private String date;
    private String time;

    @ColumnInfo(name = "accident_title")
    private String accidentTitle;
    @ColumnInfo(name = "accident_longitude")
    private double accidentLongitude;
    @ColumnInfo(name = "accident_latitude")
    private double accidentLatitude;

    /**
     * Required public constructor
     */
    @Ignore
    public Accident() {
    }

    /**
     * Use the constructor to create new Accident
     *
     * @param date
     * @param time
     * @param accidentTitle
     * @param accidentLongitude
     * @param accidentLatitude
     * @param accidentId
     */
    public Accident(String date, String time, String accidentTitle, double accidentLongitude,
                    double accidentLatitude, String accidentId) {
        this.date = date;
        this.time = time;
        this.accidentTitle = accidentTitle;
        this.accidentLongitude = accidentLongitude;
        this.accidentLatitude = accidentLatitude;
        this.accidentId = accidentId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getAccidentTitle() {
        return accidentTitle;
    }

    public double getAccidentLongitude() {
        return accidentLongitude;
    }

    public double getAccidentLatitude() {
        return accidentLatitude;
    }

    public String getAccidentId() {
        return accidentId;
    }

    public void setAccidentId(String accidentId) {
        this.accidentId = accidentId;
    }
}
