package com.yuliyuli.dto.vo;

import com.yuliyuli.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
  private String token;
  private User user;
}
