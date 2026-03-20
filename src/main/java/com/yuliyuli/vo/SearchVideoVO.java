package com.yuliyuli.vo;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SearchVideoVO {
  @Schema(description = "视频ID")
  private String id;
  @Schema(description = "视频标题")
  private String title;        // 标题
  @Schema(description = "作者ID")
  private Long userId;         // 作者ID
  @Schema(description = "作者名")
  private String authorName;   // 作者名
  @Schema(description = "视频地址")
  private String url;          // 视频地址
  @Schema(description = "封面URL")
  private String coverUrl;     // 封面
  @Schema(description = "视频类型ID")
  private Integer typeId;
  @Schema(description = "播放量")
  private Long playCount;      // 播放量
  @Schema(description = "点赞量")
  private Long likeCount;
  @Schema(description = "收藏量")
  private Long collectionCount;
  @Schema(description = "评论量")
  private Long commentCount;
  @Schema(description = "视频时长")
  private String duration;
  @Schema(description = "创建时间")
  private Date createTime;
  @Schema(description = "视频介绍")
  private String intro;        // 视频介绍
  @Schema(description = "作者头像")
  private String authorAvatar; // 作者头像
}
