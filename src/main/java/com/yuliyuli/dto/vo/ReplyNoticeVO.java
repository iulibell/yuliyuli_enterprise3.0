package com.yuliyuli.dto.vo;

import java.util.Date;
import lombok.Data;

@Data
public class ReplyNoticeVO {
  private Long replyCommentId;
  private String videoId;
  private String replyContent;
  private String parentContent;
  private Date createTime;
  private String replyUsername;
  private String replyAvatar;
}
