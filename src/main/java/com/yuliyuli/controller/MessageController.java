package com.yuliyuli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuliyuli.common.CurrentUserHolder;
import com.yuliyuli.common.Result;
import com.yuliyuli.dto.request.SendMessageRequest;
import com.yuliyuli.dto.vo.MessageConversationVO;
import com.yuliyuli.dto.vo.PrivateMessageVO;
import com.yuliyuli.dto.vo.ReplyNoticeVO;
import com.yuliyuli.entity.message.PrivateMessage;
import com.yuliyuli.entity.user.User;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.PrivateMessageMapper;
import com.yuliyuli.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/message")
@Tag(name = "消息模块")
public class MessageController {

  @Resource private PrivateMessageMapper privateMessageMapper;
  @Resource private UserMapper userMapper;
  @Resource private CommentMapper commentMapper;

  @GetMapping("/conversations")
  @Operation(summary = "获取会话列表")
  public Result<List<MessageConversationVO>> conversations() {
    User currentUser = CurrentUserHolder.getUser();
    if (currentUser == null || currentUser.getUserId() == null) {
      return Result.fail("请完成登录");
    }
    Long currentUserId = currentUser.getUserId();

    List<PrivateMessage> messages =
        privateMessageMapper.selectList(
            new LambdaQueryWrapper<PrivateMessage>()
                .and(
                    w ->
                        w.eq(PrivateMessage::getFromUserId, currentUserId)
                            .or()
                            .eq(PrivateMessage::getToUserId, currentUserId))
                .orderByDesc(PrivateMessage::getCreateTime));

    Map<Long, PrivateMessage> latestByPartner = new LinkedHashMap<>();
    for (PrivateMessage message : messages) {
      Long partnerId =
          currentUserId.equals(message.getFromUserId())
              ? message.getToUserId()
              : message.getFromUserId();
      latestByPartner.putIfAbsent(partnerId, message);
    }

    List<MessageConversationVO> result = new ArrayList<>();
    for (Map.Entry<Long, PrivateMessage> entry : latestByPartner.entrySet()) {
      Long partnerId = entry.getKey();
      PrivateMessage last = entry.getValue();
      User partner =
          userMapper.selectOne(
              new LambdaQueryWrapper<User>().eq(User::getUserId, partnerId).last("LIMIT 1"));
      if (partner == null) {
        continue;
      }
      int unread = privateMessageMapper.countUnread(partnerId, currentUserId);
      result.add(
          new MessageConversationVO(
              partnerId,
              partner.getUsername() != null ? partner.getUsername() : "用户",
              partner.getAvatar(),
              last.getContent(),
              last.getCreateTime(),
              unread));
    }
    return Result.success(result);
  }

  @GetMapping("/list")
  @Operation(summary = "获取与某用户的消息列表")
  public Result<List<PrivateMessageVO>> list(@RequestParam Long targetUserId) {
    User currentUser = CurrentUserHolder.getUser();
    if (currentUser == null || currentUser.getUserId() == null) {
      return Result.fail("请完成登录");
    }
    Long currentUserId = currentUser.getUserId();
    List<PrivateMessage> messages =
        privateMessageMapper.selectList(
            new LambdaQueryWrapper<PrivateMessage>()
                .and(
                    w ->
                        w.nested(
                                n ->
                                    n.eq(PrivateMessage::getFromUserId, currentUserId)
                                        .eq(PrivateMessage::getToUserId, targetUserId))
                            .or()
                            .nested(
                                n ->
                                    n.eq(PrivateMessage::getFromUserId, targetUserId)
                                        .eq(PrivateMessage::getToUserId, currentUserId)))
                .orderByAsc(PrivateMessage::getCreateTime));

    privateMessageMapper.update(
        null,
        new LambdaUpdateWrapper<PrivateMessage>()
            .eq(PrivateMessage::getFromUserId, targetUserId)
            .eq(PrivateMessage::getToUserId, currentUserId)
            .eq(PrivateMessage::getIsRead, 0)
            .set(PrivateMessage::getIsRead, 1));

    List<PrivateMessageVO> result = new ArrayList<>();
    for (PrivateMessage message : messages) {
      result.add(
          new PrivateMessageVO(
              message.getId(),
              message.getFromUserId(),
              message.getToUserId(),
              message.getContent(),
              message.getIsRead(),
              message.getCreateTime()));
    }
    return Result.success(result);
  }

  @PostMapping("/send")
  @Operation(summary = "发送消息")
  public Result<String> send(@Validated @RequestBody SendMessageRequest request) {
    User currentUser = CurrentUserHolder.getUser();
    if (currentUser == null || currentUser.getUserId() == null) {
      return Result.fail("请完成登录");
    }
    Long currentUserId = currentUser.getUserId();
    if (request.getToUserId().equals(currentUserId)) {
      return Result.fail("不能给自己发消息");
    }
    PrivateMessage message = new PrivateMessage();
    message.setFromUserId(currentUserId);
    message.setToUserId(request.getToUserId());
    message.setContent(request.getContent().trim());
    message.setIsRead(0);
    privateMessageMapper.insert(message);
    return Result.success("发送成功");
  }

  @GetMapping("/reply-me")
  @Operation(summary = "获取回复我的通知")
  public Result<List<ReplyNoticeVO>> replyMe(@RequestParam(defaultValue = "50") Integer limit) {
    User currentUser = CurrentUserHolder.getUser();
    if (currentUser == null || currentUser.getUserId() == null) {
      return Result.fail("请完成登录");
    }
    int safeLimit = Math.max(1, Math.min(limit, 200));
    return Result.success(commentMapper.listReplyNotices(currentUser.getUserId(), safeLimit));
  }
}
