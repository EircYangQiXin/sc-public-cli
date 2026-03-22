package com.sc.api.system.factory;

import com.sc.api.system.RemoteUserService;
import com.sc.api.system.dto.SysUserDTO;
import com.sc.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户远程调用降级处理
 */
@Slf4j
@Component
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService> {

    @Override
    public RemoteUserService create(Throwable cause) {
        log.error("系统用户服务调用失败: {}", cause.getMessage());
        return new RemoteUserService() {
            @Override
            public R<SysUserDTO> getUserInfo(String username) {
                return R.fail("获取用户信息失败: " + cause.getMessage());
            }

            @Override
            public R<Void> updateUserMfa(SysUserDTO dto) {
                return R.fail("更新用户MFA信息失败: " + cause.getMessage());
            }

            @Override
            public R<List<Long>> getUserIdsByRoleIds(List<Long> roleIds) {
                return R.fail("查询角色用户失败: " + cause.getMessage());
            }
        };
    }
}
