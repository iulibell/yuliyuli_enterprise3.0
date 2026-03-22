package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.video.Video;
import com.yuliyuli.entity.video.VideoLike;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

/**
 * 视频Mapper接口
 *
 * @author Dima
 * @date 2026-03-17
 */
public interface VideoMapper extends BaseMapper<Video> {

  /**
   * 更新视频作者头像
   *
   * @param userId 用户ID
   * @param avatar 头像URL
   * @return 影响行数
   */
  @Update("UPDATE video SET author_avatar = #{avatar} WHERE user_id = #{userId}")
  int updateVideoAuthorAvatar(Long userId, String avatar);

  /**
   * 插入视频
   *
   * @param video 视频信息
   * @return 影响行数
   */
  @Insert(
      "INSERT INTO video (`user_id`,`title`,`intro`,`url`,`cover`,`type_id`,`author_name`,`is_delete`,`author_avatar`)"
          + " VALUES (#{userId},#{title},#{intro},#{url},#{cover},#{typeId},#{authorName},#{isDelete},#{authorAvatar})")
  int insertVideo(
      Long userId,
      String title,
      String intro,
      String url,
      String cover,
      int typeId,
      String authorName,
      int isDelete,
      String authorAvatar);

  /**
   * 插入视频点赞
   *
   * @param videoLike 视频点赞信息
   * @return 影响行数
   */
  @Insert("INSERT INTO video_like (`video_id`,`user_id`)" + " VALUES (#{videoId},#{userId})")
  int insertVideoLike(VideoLike videoLike);

  /**
   * 更新视频点赞数
   *
   * @param videoId 视频ID
   * @param likeCount 点赞数
   * @return 影响行数
   */
  @Update("UPDATE video SET like_count = like_count + #{addCount} WHERE url = #{url}")
  int addVideoLikeCount(long addCount, String url);

  /**
   * 更新视频收藏数
   *
   * @param videoId 视频ID
   * @param collectionCount 收藏数
   * @return 影响行数
   */
  @Update("UPDATE video SET collection_count = #{collectionCount} WHERE url = #{url}")
  int updateVideoCollectCount(int collectionCount, String url);

  /**
   * 更新视频评论数
   *
   * @param videoId 视频ID
   * @param commentCount 评论数
   * @return 影响行数
   */
  @Update("UPDATE video SET comment_count = comment_count + 1 WHERE url = #{videoId}")
  int addVideoCommentCount(String videoId);

  /**
   * 更新视频播放数
   *
   * @param videoId 视频ID
   * @param playCount 播放数
   * @return 影响行数
   */
  @Update("UPDATE video SET play_count = play_count + #{addCount} WHERE url = #{url}")
  int addVideoPlayCount(long addCount, String url);

  /**
   * 删除视频
   *
   * @param url 视频URL
   * @param userId 用户ID
   * @return 影响行数
   */
  @Update("UPDATE video SET is_delete = 1 WHERE url = #{url} and user_id = #{userId}")
  int deleteVideo(String url, Long userId);

  /**
   * 删除视频点赞
   *
   * @param videoId 视频ID
   * @param userId 用户ID
   * @return 影响行数
   */
  @Delete("DELETE FROM video_like WHERE video_id = #{videoId} AND user_id = #{userId}")
  int deleteVideoLike(String videoId, Long userId);

  /**
   * 删除视频删除状态为1的视频 用于定时删除
   *
   * @return 影响行数
   */
  @Delete("DELETE FROM video WHERE is_delete = 1")
  int deleteVideoIsDelete();
}
