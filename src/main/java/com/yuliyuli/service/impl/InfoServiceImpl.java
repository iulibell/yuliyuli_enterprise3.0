package com.yuliyuli.service.impl;

import com.yuliyuli.common.CurrentUserHolder;
import com.yuliyuli.common.ServiceResult;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.dto.command.FollowCommand;
import com.yuliyuli.dto.command.VideoDeleteCommand;
import com.yuliyuli.dto.query.VideoWrapper;
import com.yuliyuli.dto.vo.UserProfileVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.entity.user.User;
import com.yuliyuli.mapper.FollowMapper;
import com.yuliyuli.mapper.UserMapper;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.service.InfoService;
import com.yuliyuli.util.VideoConvertUtil;

import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InfoServiceImpl implements InfoService {

  @Resource private RabbitTemplate rabbitTemplate;

  @Resource private VideoWrapper videoWrapper;

  @Resource private VideoMapper videoMapper;

  @Resource private UserMapper userMapper;

  @Resource private FollowMapper followMapper;

  /**
   * 删除视频
   *
   * @param videoIUrl 视频ID
   * @param coverUrl 封面URL
   * @return 删除结果
   */
  @Override
  public ServiceResult videoDelete(String videoIUrl, Long userId) {
    if (!checkIsLogin()) {
      log.error("用户未登录，无法删除视频");
      return ServiceResult.fail("请完成登录");
    }
    try {
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.DELETE_EXCHANGE_NAME,
          RabbitMqConfig.DELETE_ROUTING_KEY,
          new VideoDeleteCommand(videoIUrl, userId));
      log.info("删除视频传至mq成功,视频ID:{}", videoIUrl);
      return ServiceResult.success("删除视频成功");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("删除视频传至mq失败,视频ID:{}", videoIUrl, e);
      return ServiceResult.fail("删除视频失败,请稍后重试");
    }
  }

  /**
   * 获取作者页面信息,传视频的信息，包括制作的视频
   *
   * @param userId 作者ID
   * @return 作者页面所有视频
   */
  @Override
  public List<VideoVO> getAuthorPageVideo(Long userId) {
    try {
      return VideoConvertUtil.convertVideoListToVideoVOList(
          videoMapper.selectList(videoWrapper.getAuthorPageVideo(userId)));
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("获取作者页面视频失败,作者ID:{}", userId, e);
      throw new GlobalExceptionHandler.BusinessException("获取作者页面视频失败,作者ID:" + userId);
    }
  }

  /**
   * 获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息，包括用户名、头像、关注数、粉丝数等
   */
  @Override
  public UserProfileVO getUserInfo(Long userId) {
    try {
      User user = userMapper.selectById(userId);
      if (user == null) {
        log.error("用户不存在,用户ID:{}", userId);
        throw new GlobalExceptionHandler.BusinessException("该用户不存在!");
      }
      return buildUserProfile(user, false);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("获取用户信息失败,用户ID:{}", userId, e);
      throw new GlobalExceptionHandler.BusinessException("获取用户信息失败,用户ID:" + userId);
    }
  }

  /**
   * 根据作者名字获取用户信息
   *
   * @param authorName 作者名字
   * @return 用户信息，包括用户ID、用户名、头像、关注数、粉丝数等
   */
  @Override
  public UserProfileVO getUserInfoByAuthorName(String authorName) {
    try {
      User user =
          userMapper.selectOne(
              new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                  .eq(User::getUsername, authorName)
                  .or()
                  .eq(User::getNickname, authorName));
      if (user == null) {
        log.error("用户不存在,作者名字:{}", authorName);
        throw new GlobalExceptionHandler.BusinessException("该作者不存在!");
      }
      return buildUserProfile(user, true);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("获取用户信息失败,作者名字:{}", authorName, e);
      throw new GlobalExceptionHandler.BusinessException("获取用户信息失败,作者名字:" + authorName);
    }
  }

  public ServiceResult userFollow(Long followUserId, Long fanUserId) {
    if (!checkIsLogin()) {
      log.error("用户未登录，无法关注用户");
      return ServiceResult.fail("请完成登录");
    }
    try {
      // 检查是否已经关注
      if (followMapper.getFollow(followUserId, fanUserId) != null) {
        log.info("用户已经关注了该用户,关注用户ID:{},粉丝用户ID:{}", followUserId, fanUserId);
        return ServiceResult.fail("已经关注该用户");
      }
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.FOLLOW_EXCHANGE_NAME,
          RabbitMqConfig.FOLLOW_ROUTING_KEY,
          new FollowCommand(followUserId, fanUserId, "follow"));
      log.info("关注用户传至mq成功,关注用户ID:{}", followUserId);
      return ServiceResult.success("关注成功");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("关注用户传至mq失败,关注用户ID:{}", followUserId, e);
      return ServiceResult.fail("关注失败,请稍后重试");
    }
  }

  public ServiceResult userUnfollow(Long followUserId, Long fanUserId) {
    if (!checkIsLogin()) {
      log.error("用户未登录，无法取消关注用户");
      return ServiceResult.fail("请完成登录");
    }
    try {
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.FOLLOW_EXCHANGE_NAME,
          RabbitMqConfig.FOLLOW_ROUTING_KEY,
          new FollowCommand(followUserId, fanUserId, "unfollow"));
      log.info("取消关注用户传至mq成功,关注用户ID:{}", followUserId);
      return ServiceResult.success("取消关注成功");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("取消关注用户传至mq失败,关注用户ID:{}", followUserId, e);
      return ServiceResult.fail("取消关注失败,请稍后重试");
    }
  }

  public boolean checkIsLogin() {
    if (CurrentUserHolder.getUser() == null) {
      return false;
    }
    return true;
  }

  private UserProfileVO buildUserProfile(User user, boolean includeUserId) {
    return new UserProfileVO(
        includeUserId ? user.getId() : null,
        user.getUsername(),
        user.getNickname(),
        user.getAvatar(),
        user.getFollowCount() == null ? 0L : user.getFollowCount(),
        user.getFansCount() == null ? 0L : user.getFansCount());
  }
}
