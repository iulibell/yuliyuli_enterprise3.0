package com.yuliyuli.entity.delivery;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class VideoDeliveryWithoutFile {
  @Parameter(name = "作者Id")
  private Long userId;

  @Parameter(name = "视频标题")
  private String title;

  @Parameter(name = "视频介绍")
  private String intro;

  @Parameter(name = "视频url")
  private String url;

  @Parameter(name = "视频封面url")
  private String coverUrl;

  @Parameter(name = "视频类型Id")
  private Integer typeId;

  @Parameter(name = "作者名称")
  private String authorName;

  @Parameter(name = "初始未删除")
  private int isDelete;

  @Parameter(name = "作者头像")
  private String authorAvatar;
}
