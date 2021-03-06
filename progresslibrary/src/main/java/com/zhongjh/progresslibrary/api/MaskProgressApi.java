package com.zhongjh.progresslibrary.api;

import com.zhongjh.progresslibrary.entity.MultiMediaView;

import java.util.List;

/**
 * 九宫格多媒体展示的相关api
 * Created by zhongjh on 2019/3/21.
 */
public interface MaskProgressApi {

    /**
     * 设置authority
     * @param authority provider的authorities属性
     */
    void setAuthority(String authority);

    /**
     * 设置图片并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param imagePaths 图片数据源
     */
    void addImagesStartUpload(List<String> imagePaths);

    /**
     * 添加图片网址数据
     *
     * @param imagesUrls 图片网址
     */
    void addImageUrls(List<String> imagesUrls);

    /**
     * 设置视频地址并且启动上传(一般用于刚确认了哪些数据后)
     */
    void addVideoStartUpload(List<String> videoPath);

    /**
     * 设置视频地址直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     */
    void addVideoCover(List<String> videoPath);

    /**
     * 添加视频网址数据
     *
     * @param videoUrl 视频网址
     */
    void addVideoUrl(String videoUrl);

    /**
     * 设置音频数据并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param filePath 音频文件地址
     */
    void addAudioStartUpload(String filePath, int length);

    /**
     * 添加音频网址数据
     *
     * @param audioUrl 音频网址
     */
    void addAudioUrl(String audioUrl);

    /**
     * 设置音频文件直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     *
     * @param file 文件路径
     */
    void addAudioCover(String file);

    /**
     * @return 返回当前包含url的图片数据
     */
    List<MultiMediaView> getImages();

    /**
     * @return 返回当前包含url的视频数据
     */
    List<MultiMediaView> getVideos();

    /**
     * @return 返回当前包含url的音频数据
     */
    List<MultiMediaView> getAudios();

    /**
     * 语音点击
     */
    void onAudioClick();

    /**
     * 视频点击
     */
    void onVideoClick();

    /**
     * 删除单个图片
     * @param position 图片的索引，该索引列表不包含视频等
     */
    void onRemoveItemImage(int position);

    /**
     * 设置是否可操作(一般只用于展览作用)
     *
     * @param isOperation 是否操作
     */
    void setOperation(boolean isOperation);

    /**
     * 销毁所有相关正在执行的东西
     */
    void destroy();

}
