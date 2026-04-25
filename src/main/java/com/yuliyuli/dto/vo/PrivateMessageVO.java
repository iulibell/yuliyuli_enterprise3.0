package com.yuliyuli.dto.vo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivateMessageVO {
  private Long id;
  private Long fromUserId;
  private Long toUserId;
  private String content;
  private Integer isRead;
  private Date createTime;
}
