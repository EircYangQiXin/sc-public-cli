package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.model.LoginUser;
import com.sc.system.domain.vo.OnlineUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线用户管理 + 强制下线 + 手动解锁
 */
@Slf4j
@Api(tags = "在线用户管理")
@RestController
@RequestMapping("/system/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private static final String LOCK_KEY_PREFIX = "account:lock:";
    private static final String ATTEMPTS_KEY_PREFIX = "account:attempts:";

    private final StringRedisTemplate redisTemplate;

    @ApiOperation("查询在线用户列表")
    @SaCheckPermission("system:online:list")
    @GetMapping("/list")
    public R<List<OnlineUserVO>> list(
            @ApiParam(value = "用户名（模糊搜索）") @RequestParam(required = false) String username) {
        List<OnlineUserVO> onlineUsers = new ArrayList<OnlineUserVO>();

        // 通过 Sa-Token 搜索所有活跃会话
        List<String> sessionIds = StpUtil.searchSessionId("", 0, -1, false);

        for (String sessionId : sessionIds) {
            try {
                SaSession session = StpUtil.getSessionBySessionId(sessionId);
                if (session == null) {
                    continue;
                }

                Object loginUserObj = session.get("loginUser");
                if (!(loginUserObj instanceof LoginUser)) {
                    continue;
                }

                LoginUser loginUser = (LoginUser) loginUserObj;

                // 用户名过滤
                if (username != null && !username.isEmpty()
                        && !loginUser.getUsername().contains(username)) {
                    continue;
                }

                // 获取该用户的所有 Token（脱敏后返回）
                List<String> tokens = StpUtil.getTokenValueListByLoginId(loginUser.getUserId());
                for (String token : tokens) {
                    OnlineUserVO vo = new OnlineUserVO();
                    vo.setTokenId(maskToken(token));
                    vo.setUserId(loginUser.getUserId());
                    vo.setUsername(loginUser.getUsername());
                    vo.setNickName(loginUser.getNickName());
                    onlineUsers.add(vo);
                }
            } catch (Exception e) {
                log.debug("解析在线用户会话异常: sessionId={}", sessionId, e);
            }
        }

        return R.ok(onlineUsers);
    }

    @ApiOperation("强制下线指定用户的所有会话")
    @SaCheckPermission("system:online:forceLogout")
    @DeleteMapping("/user/{userId}")
    public R<Void> forceLogoutUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        StpUtil.logout(userId);
        log.info("强制下线用户所有会话: userId={}", userId);
        return R.ok("已将该用户所有会话踢下线", null);
    }

    @ApiOperation("手动解锁被锁定的账号")
    @SaCheckPermission("system:online:unlock")
    @PutMapping("/unlock/{username}")
    public R<Void> unlock(
            @ApiParam(value = "被锁定的用户名", required = true) @PathVariable String username) {
        redisTemplate.delete(LOCK_KEY_PREFIX + username);
        redisTemplate.delete(ATTEMPTS_KEY_PREFIX + username);
        log.info("管理员手动解锁账号: {}", username);
        return R.ok("账号已解锁", null);
    }

    /**
     * Token 脱敏: 只显示前 8 位 + 掩码
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "********";
        }
        return token.substring(0, 8) + "****" + token.substring(token.length() - 4);
    }
}
