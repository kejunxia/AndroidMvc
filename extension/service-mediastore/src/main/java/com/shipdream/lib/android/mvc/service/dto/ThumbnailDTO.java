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

/**
 * The wrapper of Android media thumbnail.
 */
public class ThumbnailDTO {
    /**
     * Refer http://developer.android.com/reference/android/provider/MediaStore.Images.Thumbnails.html#FULL_SCREEN_KIND
     */
    public enum Kind{
        MICRO,
        MINI,
        FULL_SCREEN
    }
    /**
     * Id of the original file for the thumbnail
     */
    private long originalFileId;
    private String uri;
    private int width;
    private int height;
    private Kind kind;

    public long getOriginalFileId(){
        return originalFileId;
    }

    public void setOriginalFileId(long originalFileId){
        this.originalFileId = originalFileId;
    }

    public String getUri(){
        return uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

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

    public Kind getKind(){
        return kind;
    }

    public void setKind(Kind kind){
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof ThumbnailDTO)) return false;

        ThumbnailDTO that = (ThumbnailDTO) o;

        if (height != that.height) return false;
        if (originalFileId != that.originalFileId) return false;
        if (width != that.width) return false;
        if (kind != that.kind) return false;
        return !(uri != null ? !uri.equals(that.uri) : that.uri != null);

    }

    @Override
    public int hashCode(){
        int result = (int) (originalFileId ^ (originalFileId >>> 32));
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
    }
}
