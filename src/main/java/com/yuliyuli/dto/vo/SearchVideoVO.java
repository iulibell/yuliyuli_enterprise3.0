package com.yuliyuli.dto.vo;

import java.util.Date;
import lombok.Data;

@Data
public class SearchVideoVO {
  private String id;
  private String title; // 标题
  private Long userId; // 作者ID
  private String authorName; // 作者名
  private String url; // 视频地址
  private String coverUrl; // 封面
  private Integer typeId;
  private Long playCount; // 播放量
  private Long likeCount;
  private Long collectionCount;
  private Long commentCount;
  private String duration;
  private Date createTime;
  private String intro; // 视频介绍
  private String authorAvatar; // 作者头像
}
