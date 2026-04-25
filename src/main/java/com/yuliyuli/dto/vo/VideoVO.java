package com.yuliyuli.dto.vo;

import java.util.Date;
import lombok.Data;

// 视频VO类
@Data
public class VideoVO {
  private Long userId;
  private String title;
  private String intro;
  private String url;
  private String cover;
  private int typeId;
  private int playCount;
  private int likeCount;
  private int collectionCount;
  private Date createTime;
  private String authorName;
  private int commentCount;
  private String authorAvatar;
}
