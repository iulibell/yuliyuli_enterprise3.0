package com.yuliyuli.dto.command;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowCommand implements Serializable {
  private Long followUserId;
  private Long fanUserId;
  private String operation;
}
