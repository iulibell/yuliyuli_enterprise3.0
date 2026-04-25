package com.yuliyuli.dto.vo;

import com.yuliyuli.entity.video.Comment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailPageVO {
  private List<HotRecommendVideoVO> hotVideoVOList;
  private List<Comment> commentList;
  private boolean isFollow;
}
