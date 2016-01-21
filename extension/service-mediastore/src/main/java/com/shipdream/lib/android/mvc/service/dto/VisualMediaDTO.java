/*
 * Copyright 2016 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvc.service.dto;

import java.util.Date;

public class VisualMediaDTO extends MediaDTO {
    private int width;
    private int height;
    private double latitude;
    private double longitude;
    private Date takenDate;

    public int getWidth(){
        return width;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public int getHeight(){
        return height;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public Date getTakenDate(){
        return takenDate;
    }

    public void setTakenDate(Date takenDate){
        this.takenDate = takenDate;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof VisualMediaDTO)) return false;
        if (!super.equals(o)) return false;

        VisualMediaDTO that = (VisualMediaDTO) o;

        if (height != that.height) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (width != that.width) return false;
        return !(takenDate != null ? !takenDate.equals(that.takenDate) : that.takenDate != null);

    }

    @Override
    public int hashCode(){
        int result = super.hashCode();
        long temp;
        result = 31 * result + width;
        result = 31 * result + height;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (takenDate != null ? takenDate.hashCode() : 0);
        return result;
    }
}
