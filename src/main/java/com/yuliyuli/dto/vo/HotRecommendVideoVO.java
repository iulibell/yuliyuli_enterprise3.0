package com.yuliyuli.dto.vo;

import java.util.Date;
import lombok.Data;

@Data
public class HotRecommendVideoVO {
  private String id;
  private String title;
  private String url;
  private String coverUrl;
  private String intro;
  private int typeId;
  private Long likeCount;
  private Long collectionCount;
  private Date createTime;
  private Long playCount;
  private Long commentCount;
  private String authorName;
  private String authorAvatar;
  private Long userId;
}
