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

package com.shipdream.lib.android.mvc.service.internal;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.shipdream.lib.android.mvc.service.MediaStoreService;
import com.shipdream.lib.android.mvc.service.dto.ImageDTO;
import com.shipdream.lib.android.mvc.service.dto.ThumbnailDTO;
import com.shipdream.lib.android.mvc.service.dto.VideoDTO;
import com.shipdream.lib.android.mvc.service.dto.VisualMediaDTO;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Media store service to use real Android media store API
 */
public class MediaStoreServiceImpl implements MediaStoreService {
    private static final String TAG = MediaStoreServiceImpl.class.getName();
    public static final String MediaStore_Media_WIDTH = "width";
    public static final String MediaStore_Media_HEIGHT = "height";
    protected Context context;

    public MediaStoreServiceImpl(Context context) {
        this.context = context;
    }

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    public String getBucketId(String path) {
        return String.valueOf(path.toLowerCase(Locale.US).hashCode());
    }

    public String[] getDicmBuckets() {
        String[] dcimBuckets;
        final String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath();
        java.io.File dcim = new java.io.File(root);
        if (dcim.exists()) {
            String[] subFolders = dcim.list(new FilenameFilter() {
                @Override
                public boolean accept(java.io.File dir, String name) {
                    return dir.isDirectory() && !name.startsWith(".");
                }
            });

            if (subFolders.length == 0) {
                dcimBuckets = new String[]{getBucketId(root)};
            } else {
                dcimBuckets = new String[subFolders.length];
                for (int i = 0; i < subFolders.length; i++) {
                    dcimBuckets[i] = getBucketId(root + "/" + subFolders[i]);
                }
            }
        } else {
            dcimBuckets = new String[0];
        }
        return dcimBuckets;
    }

    @Override
    public VisualMediaDTO getLatestVisualMedia() {
        List<ImageDTO> images = extractImagesFromCursor(
                getAllBucketImagesCursor(null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"), 0, 1);
        List<VideoDTO> videos = extractVideosFromCursor(
                getAllBucketVideosCursor(null,
                        MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC"), 0, 1);
        ImageDTO lastImage = null;
        if (images.size() > 0) {
            lastImage = images.get(0);
        }
        VideoDTO lastVideo = null;
        if (videos.size() > 0) {
            lastVideo = videos.get(0);
        }

        if (lastImage == null && lastVideo == null) {
            return null;
        } else {
            if (lastImage != null && lastVideo != null) {
                if (lastImage.getTakenDate().getTime() > lastVideo.getTakenDate().getTime()) {
                    return lastImage;
                } else {
                    return lastVideo;
                }
            } else if (lastImage != null) {
                return lastImage;
            } else {
                return lastVideo;
            }
        }
    }

    public Map<String, List<ImageDTO>> getDcimImages() {
        Map<String, List<ImageDTO>> result = new LinkedHashMap<>();
        String[] imageBuckets = getDicmBuckets();
        for (int i = 0; i < imageBuckets.length; i++) {
            String bucket = imageBuckets[i];
            result.put(bucket, getImages(bucket, 0, 0));
        }
        return result;
    }

    public int getDcimImageCount() {
        int count = 0;
        String[] imageBuckets = getDicmBuckets();
        for (int i = 0; i < imageBuckets.length; i++) {
            Cursor cursor = getAllBucketImagesCursor(imageBuckets[i], null);
            if(cursor != null) {
                count += cursor.getCount();
                cursor.close();
            }
        }
        return count;
    }

    private String[] imageProjection = null;
    protected String[] getImageProjection() {
        if(imageProjection == null) {
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DESCRIPTION,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE};

            if (android.os.Build.VERSION.SDK_INT >= 16) {
                //Added two columns for SDK > 16
                int len = projection.length + 2;
                String[] projection16 = new String[projection.length + 2];
                for (int i = 0; i < projection.length; i++) {
                    projection16[i] = projection[i];
                }
                projection16[len - 2] = MediaStore_Media_WIDTH;
                projection16[len - 1] = MediaStore_Media_HEIGHT;

                projection = projection16;

                imageProjection = projection;
            }
        }
        return imageProjection;
    }

    private Cursor getAllBucketImagesCursor(String bucketId, String orderBy) {
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        String[] selectionArgs = {bucketId};
        if (bucketId == null) {
            selection = null;
            selectionArgs = null;
        }

        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                getImageProjection(), selection, selectionArgs, orderBy);

        if(cursor == null) {
            Log.w(TAG, "Failed to get cursor of all images of bucket " + bucketId);
        }
        return cursor;
    }

    @Override
    public List<ImageDTO> getImages(String bucketId, int offset, int limit) {
        return extractImagesFromCursor(getAllBucketImagesCursor(bucketId, null), offset, limit);
    }

    private int imageIdCol = -1;
    private int imageTitleCol;
    private int imageDisplayNameCol;
    private int imageDescriptionCol;
    private int imageBucketIdCol;
    private int imageBucketDisplayNameCol;
    private int imageDataCol;
    private int imageMimeCol;
    private int imageSizeCol;
    private int imageOrientationCol;
    private int imageDateAddedCol;
    private int imageDateTakenCol;
    private int imageDateModifyCol;
    private int latitudeCol;
    private int longitudeCol;
    private int widthCol = -1;
    private int heightCol = -1;

    /**
     * Extract an imageDTO from given cursor position from its current position.
     * @param cursor
     * @return
     */
    protected ImageDTO extractOneImageFromCurrentCursor(Cursor cursor) {
        int api = android.os.Build.VERSION.SDK_INT;
        if(imageIdCol == -1) {
            imageIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            imageTitleCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
            imageDisplayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            imageDescriptionCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DESCRIPTION);
            imageBucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            imageBucketDisplayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            imageDataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imageMimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            imageSizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            imageOrientationCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);
            imageDateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            imageDateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            imageDateModifyCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
            latitudeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE);
            longitudeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE);
            if (api >= 16) {
                widthCol = cursor.getColumnIndexOrThrow(MediaStore_Media_WIDTH);
                heightCol = cursor.getColumnIndexOrThrow(MediaStore_Media_HEIGHT);
            }
        }

        ImageDTO image = new ImageDTO();
        image.setId(cursor.getLong(imageIdCol));
        image.setTitle(cursor.getString(imageTitleCol));
        image.setDisplayName(cursor.getString(imageDisplayNameCol));
        image.setDescription(cursor.getString(imageDescriptionCol));
        image.setBucketId(cursor.getString(imageBucketIdCol));
        image.setBucketDisplayName(cursor.getString(imageBucketDisplayNameCol));
        image.setUri(cursor.getString(imageDataCol));
        image.setMimeType(cursor.getString(imageMimeCol));
        image.setSize(cursor.getLong(imageSizeCol));
        image.setOrientation(translateOrientation(cursor.getInt(imageOrientationCol)));
        image.setAddedDate(new Date(cursor.getLong(imageDateAddedCol)));
        image.setTakenDate(new Date(cursor.getLong(imageDateTakenCol)));
        image.setModifyDate(new Date(cursor.getLong(imageDateModifyCol)));
        image.setLatitude(cursor.getDouble(latitudeCol));
        image.setLongitude(cursor.getDouble(longitudeCol));
        if (api >= 16) {
            image.setWidth(cursor.getInt(widthCol));
            image.setHeight(cursor.getInt(heightCol));
        }

        return image;
    }

    /**
     * Extract a list of imageDTO from current cursor with the given offset and limit.
     * @param cursor
     * @param offset
     * @param limit
     * @return
     */
    protected List<ImageDTO> extractImagesFromCursor(Cursor cursor, int offset, int limit) {
        List<ImageDTO> images = new ArrayList<>();
        int count = 0;
        int begin = offset > 0 ? offset : 0;
        if (cursor.moveToPosition(begin)) {
            do {
                ImageDTO image = extractOneImageFromCurrentCursor(cursor);
                images.add(image);
                count++;
                if (limit > 0 && count > limit) {
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return images;
    }

    public String getImageUriById(String imageId) {
        final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        final String selection = MediaStore.Images.Media._ID + " = ?";
        final String[] selectionArgs = {imageId};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null);
        if (cursor != null) {
            final int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String imageUrl = null;
            if (cursor.moveToFirst()) {
                do {
                    imageUrl = cursor.getString(dataCol);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return imageUrl;
        } else {
            return null;
        }
    }

    @Override
    public ThumbnailDTO getImageThumbnailById(long imageId, ThumbnailDTO.Kind kind) {
        final String[] projection = {
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.WIDTH,
                MediaStore.Images.Thumbnails.HEIGHT,
        };
        final String selection = MediaStore.Images.Thumbnails.IMAGE_ID + " = ? AND "
                + MediaStore.Images.Thumbnails.KIND + " = ?";
        String kindInString = null;
        switch (kind) {
            case MICRO:
                kindInString = Integer.toString(MediaStore.Images.Thumbnails.MICRO_KIND);
                break;
            case MINI:
                kindInString = Integer.toString(MediaStore.Images.Thumbnails.MINI_KIND);
                break;
            case FULL_SCREEN:
                kindInString = Integer.toString(MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        }
        final String[] selectionArgs = {String.valueOf(imageId), kindInString};

        ThumbnailDTO thumbnail = null;
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                thumbnail = new ThumbnailDTO();
                final int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
                final int widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.WIDTH);
                final int heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.HEIGHT);
                if (cursor.moveToFirst()) {
                    thumbnail.setOriginalFileId(imageId);
                    thumbnail.setUri(cursor.getString(dataCol));
                    thumbnail.setWidth(cursor.getInt(widthCol));
                    thumbnail.setHeight(cursor.getInt(heightCol));
                    thumbnail.setKind(kind);
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return thumbnail;
    }

    public Map<String, List<VideoDTO>> getDcimVideos() {
        Map<String, List<VideoDTO>> result = new LinkedHashMap<>();
        String[] videoBuckets = getDicmBuckets();
        for (int i = 0; i < videoBuckets.length; i++) {
            String bucket = videoBuckets[i];
            result.put(bucket, getVideos(bucket, 0, 0));
        }
        return result;
    }

    @Override
    public int getDcimVideosCount() {
        int count = 0;
        String[] videoBuckets = getDicmBuckets();
        for (int i = 0; i < videoBuckets.length; i++) {
            Cursor cursor = getAllBucketVideosCursor(videoBuckets[i], null);
            if(cursor != null) {
                count += cursor.getCount();
                cursor.close();
            }
        }
        return count;
    }

    private String [] videoProjection;
    protected String[] getVideoProjection() {
        if(videoProjection == null) {
            String[] projection = {MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DESCRIPTION,
                    MediaStore.Video.Media.BUCKET_ID,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.MIME_TYPE,
                    MediaStore.Video.Media.RESOLUTION,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media.LATITUDE,
                    MediaStore.Video.Media.LONGITUDE,
                    MediaStore.Video.Media.ALBUM,
                    MediaStore.Video.Media.ARTIST};
            videoProjection = projection;
        }
        return videoProjection;
    }

    private Cursor getAllBucketVideosCursor(String bucketId, String orderBy) {
        String selection = MediaStore.Video.Media.BUCKET_ID + " = ?";
        String[] selectionArgs = {bucketId};
        if (bucketId == null) {
            selection = null;
            selectionArgs = null;
        }
        final Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                getVideoProjection(), selection, selectionArgs, orderBy);
        if(cursor == null) {
            Log.w(TAG, "Failed to get cursor of all video of bucket " + bucketId);
        }
        return cursor;
    }


    @Override
    public List<VideoDTO> getVideos(String bucketId, int offset, int limit) {
        return extractVideosFromCursor(getAllBucketVideosCursor(bucketId, null), offset, limit);
    }

    private int videoIdCol = -1;
    private int videoTitleCol;
    private int videoDisplayNameCol;
    private int videoDescriptionCol;
    private int videoBucketIdCol;
    private int videoBucketDisplayNameCol;
    private int videoDataCol;
    private int videoMimeCol;
    private int videoResolutionCol;
    private int videoSizeCol;
    private int videoDateAddedCol;
    private int videoDateTakenCol;
    private int videoDateModifyCol;
    private int videoLatitudeCol;
    private int videoLongitudeCol;
    private int videoAlbumCol;
    private int videoArtistCol;

    /**
     * Extract one videoDTO from the given cursor from its current position
     * @param cursor
     * @return
     */
    protected VideoDTO extractOneVideoFromCursor(Cursor cursor) {
        if(videoIdCol == -1) {
            videoIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            videoTitleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            videoDisplayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            videoDescriptionCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DESCRIPTION);
            videoBucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
            videoBucketDisplayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            videoDataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            videoMimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            videoResolutionCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION);
            videoSizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            videoDateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            videoDateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN);
            videoDateModifyCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            videoLatitudeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.LATITUDE);
            videoLongitudeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.LONGITUDE);
            videoAlbumCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM);
            videoArtistCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST);
        }

        VideoDTO video = new VideoDTO();
        video.setId(cursor.getLong(videoIdCol));
        video.setTitle(cursor.getString(videoTitleCol));
        video.setDisplayName(cursor.getString(videoDisplayNameCol));
        video.setDescription(cursor.getString(videoDescriptionCol));
        video.setBucketId(cursor.getString(videoBucketIdCol));
        video.setBucketDisplayName(cursor.getString(videoBucketDisplayNameCol));
        video.setUri(cursor.getString(videoDataCol));
        video.setMimeType(cursor.getString(videoMimeCol));
        video.setSize(cursor.getLong(videoSizeCol));
        video.setAddedDate(new Date(cursor.getLong(videoDateAddedCol)));
        video.setTakenDate(new Date(cursor.getLong(videoDateTakenCol)));
        video.setModifyDate(new Date(cursor.getLong(videoDateModifyCol)));
        video.setLatitude(cursor.getDouble(videoLatitudeCol));
        video.setLongitude(cursor.getDouble(videoLongitudeCol));
        video.setAlbum(cursor.getString(videoAlbumCol));
        video.setArtist(cursor.getString(videoArtistCol));
        String resolution = cursor.getString(videoResolutionCol);
        if (resolution != null) {
            try {
                String[] res = resolution.split("x");
                int width = Integer.parseInt(res[0]);
                int height = Integer.parseInt(res[1]);
                video.setWidth(width);
                video.setHeight(height);
            } catch (Exception e) {
                Log.w(TAG, String.format("Failed to parse resolution of video(id=%d, title=%s, displayName=%s)",
                        video.getId(), video.getTitle(), video.getDisplayName()), e);
            }

        }
        return video;
    }

    /**
     * Extract a list of videoDTO from current cursor with the given offset and limit.
     * @param cursor
     * @param offset
     * @param limit
     * @return
     */
    protected List<VideoDTO> extractVideosFromCursor(Cursor cursor, int offset, int limit) {
        List<VideoDTO> videos = new ArrayList<>();
        int count = 0;
        int begin = offset > 0 ? offset : 0;
        if (cursor.moveToPosition(begin)) {
            do {
                VideoDTO video = extractOneVideoFromCursor(cursor);
                videos.add(video);
                count++;
                if (limit > 0 && count > limit) {
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return videos;
    }

    public String getVideoUriById(String videoId) {
        final String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
        final String selection = MediaStore.Video.Media._ID + " = ?";
        final String[] selectionArgs = {videoId};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        final int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        String videoUrl = null;
        if (cursor.moveToFirst()) {
            do {
                videoUrl = cursor.getString(dataCol);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return videoUrl;
    }

    @Override
    public ThumbnailDTO getVideoThumbnailById(long videoId, ThumbnailDTO.Kind kind) {
//        ThumbnailUtils.createVideoThumbnail()
        final String[] projection = {
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.WIDTH,
                MediaStore.Video.Thumbnails.HEIGHT,
        };
        final String selection = MediaStore.Video.Thumbnails.VIDEO_ID + " = ? AND "
                + MediaStore.Video.Thumbnails.KIND + " = ?";
        String kindInString = null;
        switch (kind) {
            case MICRO:
                kindInString = Integer.toString(MediaStore.Images.Thumbnails.MICRO_KIND);
                break;
            case MINI:
                kindInString = Integer.toString(MediaStore.Images.Thumbnails.MINI_KIND);
                break;
            case FULL_SCREEN:
                kindInString = Integer.toString(MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        }
        final String[] selectionArgs = {String.valueOf(videoId), kindInString};

        ThumbnailDTO thumbnail = null;
        final Cursor cursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                thumbnail = new ThumbnailDTO();
                final int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);
                final int widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.WIDTH);
                final int heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.HEIGHT);
                if (cursor.moveToFirst()) {
                    thumbnail.setOriginalFileId(videoId);
                    thumbnail.setUri(cursor.getString(dataCol));
                    thumbnail.setWidth(cursor.getInt(widthCol));
                    thumbnail.setHeight(cursor.getInt(heightCol));
                    thumbnail.setKind(kind);
                }
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return thumbnail;
    }

    @Override
    public void deleteFiles(long[] deletingFileIds) {
        for (int i = 0; i < deletingFileIds.length; i++) {
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, deletingFileIds[i]);
            context.getContentResolver().delete(uri, null, null);
        }
    }

    private ImageDTO.Orientation translateOrientation(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return ImageDTO.Orientation.FLIP_HORIZONTAL;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return ImageDTO.Orientation.FLIP_VERTICAL;
            case ExifInterface.ORIENTATION_NORMAL:
                return ImageDTO.Orientation.NORMAL;
            case ExifInterface.ORIENTATION_ROTATE_90:
                return ImageDTO.Orientation.ROTATE_90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return ImageDTO.Orientation.ROTATE_180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return ImageDTO.Orientation.ROTATE_270;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                return ImageDTO.Orientation.TRANSPOSE;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                return ImageDTO.Orientation.TRANSVERSE;
            default:
                return ImageDTO.Orientation.UNDEFINED;
        }
    }
}
