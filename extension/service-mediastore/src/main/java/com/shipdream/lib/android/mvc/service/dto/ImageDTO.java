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
 * DTO to represent an image
 */
public class ImageDTO extends VisualMediaDTO{
    public enum Orientation{
        UNDEFINED,
        FLIP_HORIZONTAL,
        FLIP_VERTICAL,
        NORMAL,
        ROTATE_90,
        ROTATE_180,
        ROTATE_270,
        TRANSPOSE,
        TRANSVERSE
    }

    private Orientation orientation;

    public Orientation getOrientation(){
        return orientation;
    }

    public void setOrientation(Orientation orientation){
        this.orientation = orientation;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof ImageDTO)) return false;
        if (!super.equals(o)) return false;

        ImageDTO imageDTO = (ImageDTO) o;

        boolean sameOrientation = orientation == imageDTO.orientation;
        return sameOrientation;

    }

    @Override
    public int hashCode(){
        int result = super.hashCode();
        result = 31 * result + (orientation != null ? orientation.hashCode() : 0);
        return result;
    }
}
