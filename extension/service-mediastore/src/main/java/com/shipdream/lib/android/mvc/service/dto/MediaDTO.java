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

public class MediaDTO {
    private long id;
    private String title;
    private String displayName;
    private String description;
    private String bucketId;
    private String bucketDisplayName;
    private String uri;
    private String mimeType;
    private long size;
    private Date addedDate;
    private Date modifyDate;

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDisplayName(){
        return displayName;
    }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getBucketId(){
        return bucketId;
    }

    public void setBucketId(String bucketId){
        this.bucketId = bucketId;
    }

    public String getBucketDisplayName(){
        return bucketDisplayName;
    }

    public void setBucketDisplayName(String bucketDisplayName){
        this.bucketDisplayName = bucketDisplayName;
    }

    public String getUri(){
        return uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

    public String getMimeType(){
        return mimeType;
    }

    public void setMimeType(String mimeType){
        this.mimeType = mimeType;
    }

    public long getSize(){
        return size;
    }

    public void setSize(long size){
        this.size = size;
    }

    public Date getAddedDate(){
        return addedDate;
    }

    public void setAddedDate(Date addedDate){
        this.addedDate = addedDate;
    }

    public Date getModifyDate(){
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate){
        this.modifyDate = modifyDate;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof MediaDTO)) return false;

        MediaDTO mediaDTO = (MediaDTO) o;

        if (id != mediaDTO.id) return false;
        if (size != mediaDTO.size) return false;
        if (addedDate != null ? !addedDate.equals(mediaDTO.addedDate) : mediaDTO.addedDate != null)
            return false;
        if (bucketDisplayName != null ? !bucketDisplayName.equals(mediaDTO.bucketDisplayName) : mediaDTO.bucketDisplayName != null)
            return false;
        if (bucketId != null ? !bucketId.equals(mediaDTO.bucketId) : mediaDTO.bucketId != null)
            return false;
        if (description != null ? !description.equals(mediaDTO.description) : mediaDTO.description != null)
            return false;
        if (displayName != null ? !displayName.equals(mediaDTO.displayName) : mediaDTO.displayName != null)
            return false;
        if (mimeType != null ? !mimeType.equals(mediaDTO.mimeType) : mediaDTO.mimeType != null)
            return false;
        if (modifyDate != null ? !modifyDate.equals(mediaDTO.modifyDate) : mediaDTO.modifyDate != null)
            return false;
        if (title != null ? !title.equals(mediaDTO.title) : mediaDTO.title != null) return false;
        return !(uri != null ? !uri.equals(mediaDTO.uri) : mediaDTO.uri != null);

    }

    @Override
    public int hashCode(){
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (bucketId != null ? bucketId.hashCode() : 0);
        result = 31 * result + (bucketDisplayName != null ? bucketDisplayName.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (addedDate != null ? addedDate.hashCode() : 0);
        result = 31 * result + (modifyDate != null ? modifyDate.hashCode() : 0);
        return result;
    }
}
