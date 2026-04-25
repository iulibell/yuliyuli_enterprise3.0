package com.yuliyuli.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentLikeResultVO {
  private boolean liked;
  private int likeCount;
}
