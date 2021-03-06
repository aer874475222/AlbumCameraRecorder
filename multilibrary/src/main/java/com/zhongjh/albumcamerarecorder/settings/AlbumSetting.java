package com.zhongjh.albumcamerarecorder.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import gaode.zhongjh.com.common.enums.MimeType;
import com.zhongjh.albumcamerarecorder.album.filter.Filter;
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener;
import com.zhongjh.albumcamerarecorder.settings.api.AlbumSettingApi;

import java.util.ArrayList;
import java.util.Set;

/**
 * 相册设置
 * Created by zhongjh on 2018/12/27.
 */
public class AlbumSetting implements AlbumSettingApi {

    private final AlbumSpec mAlbumSpec;

    /**
     *
     * @param mediaTypeExclusive 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    public AlbumSetting(boolean mediaTypeExclusive) {
        mAlbumSpec = AlbumSpec.getCleanInstance();

        mAlbumSpec.mediaTypeExclusive = mediaTypeExclusive;
    }

    @Override
    public AlbumSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes) {
        mAlbumSpec.mimeTypeSet = mimeTypes;
        return this;
    }

    @Override
    public AlbumSetting showSingleMediaType(boolean showSingleMediaType) {
        mAlbumSpec.showSingleMediaType = showSingleMediaType;
        return this;
    }

    @Override
    public AlbumSetting countable(boolean countable) {
        mAlbumSpec.countable = countable;
        return this;
    }

    @Override
    public AlbumSetting addFilter(@NonNull Filter filter) {
        if (mAlbumSpec.filters == null) {
            mAlbumSpec.filters = new ArrayList<>();
        }
        mAlbumSpec.filters.add(filter);
        return this;
    }

    @Override
    public AlbumSetting originalEnable(boolean enable) {
        mAlbumSpec.originalable = enable;
        return this;
    }

    @Override
    public AlbumSetting maxOriginalSize(int size) {
        mAlbumSpec.originalMaxSize = size;
        return this;
    }

    @Override
    public AlbumSetting spanCount(int spanCount) {
        if (spanCount < 1) throw new IllegalArgumentException("spanCount cannot be less than 1");
        mAlbumSpec.spanCount = spanCount;
        return this;
    }

    @Override
    public AlbumSetting gridExpectedSize(int size) {
        mAlbumSpec.gridExpectedSize = size;
        return this;
    }

    @Override
    public AlbumSetting thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f)
            throw new IllegalArgumentException("缩略图比例必须介于(0.0, 1.0]之间");
        mAlbumSpec.thumbnailScale = scale;
        return this;
    }

    @NonNull  @Override
    public AlbumSetting setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mAlbumSpec.onSelectedListener = listener;
        return this;
    }

    @Override
    public AlbumSetting setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mAlbumSpec.onCheckedListener = listener;
        return this;
    }

}
