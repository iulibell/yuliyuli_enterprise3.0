package com.yuliyuli.entity;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoDelivery {
  @Parameter(name = "视频文件")
  private MultipartFile videoFile;

  @Parameter(name = "视频封面文件")
  private MultipartFile coverFile;

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
}
