package com.yuliyuli.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

@Data
@TableName("private_message")
public class PrivateMessage {
  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("from_user_id")
  private Long fromUserId;

  @TableField("to_user_id")
  private Long toUserId;

  @TableField("content")
  private String content;

  @TableField("is_read")
  private Integer isRead;

  @TableField("create_time")
  private Date createTime;
}
