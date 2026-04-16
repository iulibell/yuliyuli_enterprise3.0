package com.yuliyuli.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {
  private Long userId;
  private String username;
  private String nickname;
  private String avatar;
  private Long followCount;
  private Long fansCount;
}
