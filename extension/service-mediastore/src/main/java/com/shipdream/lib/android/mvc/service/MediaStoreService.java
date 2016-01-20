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

package com.shipdream.lib.android.mvc.service;

import com.shipdream.lib.android.mvc.service.dto.ImageDTO;
import com.shipdream.lib.android.mvc.service.dto.ThumbnailDTO;
import com.shipdream.lib.android.mvc.service.dto.VideoDTO;
import com.shipdream.lib.android.mvc.service.dto.VisualMediaDTO;

import java.util.List;
import java.util.Map;

public interface MediaStoreService {
    /**
     * @return All DICM buckets of the device
     */
    String[] getDicmBuckets();

    /**
     * Find the latest image or video.
     * @return the latest image or video or null if nothing found.
     */
    VisualMediaDTO getLatestVisualMedia();

    /**
     * Get all dcim images
     * @return Key: bucketId, Value: found image list
     */
    Map<String, List<ImageDTO>> getDcimImages();

    /**
     * @return The count of all dcim images
     */
    int getDcimImageCount();

    /**
     * Get all images of given bucket
     * @param bucketId The bucket id
     * @param offset The offset of range start
     * @param limit The limit of range size
     * @return The images
     */
    List<ImageDTO> getImages(String bucketId, int offset, int limit);

    /**
     * Get the uri of image of given image ID
     * @param imageId The image id
     * @return The uri of the image
     */
    String getImageUriById(String imageId);

    ThumbnailDTO getImageThumbnailById(long imageId, ThumbnailDTO.Kind kind);

    /**
     * Get all dcim videos
     * @return Key: bucketId, Value: found video list
     */
    Map<String, List<VideoDTO>> getDcimVideos();

    /**
     * @return The count of all DCIM videos
     */
    int getDcimVideosCount();

    /**
     *Get all videos of given bucket
     * @param bucketId The bucket id
     * @param offset The offset of range start
     * @param limit The limit of range size
     * @return The videos
     */
    List<VideoDTO> getVideos(String bucketId, int offset, int limit);

    /**
     * Get the uri of video of given video ID
     * @param videoId The video id
     * @return The uri of the video
     */
    String getVideoUriById(String videoId);

    /**
     * The video thumbnail by video ID
     * @param videoId The id of the video
     * @param kind The kind of the thumbnail
     * @return The thumbnail object
     */
    ThumbnailDTO getVideoThumbnailById(long videoId, ThumbnailDTO.Kind kind);

    /**
     * Delete files
     * @param deletingFileIds IDs of the files to delete
     */
    void deleteFiles(long[] deletingFileIds);
}
