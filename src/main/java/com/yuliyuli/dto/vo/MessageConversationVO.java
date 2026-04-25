package com.yuliyuli.dto.vo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageConversationVO {
  private Long userId;
  private String username;
  private String avatar;
  private String lastContent;
  private Date lastTime;
  private Integer unreadCount;
}
