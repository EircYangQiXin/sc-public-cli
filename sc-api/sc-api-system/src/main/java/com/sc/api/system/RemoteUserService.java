package com.sc.api.system;

import com.sc.api.system.dto.SysUserDTO;
import com.sc.api.system.factory.RemoteUserFallbackFactory;
import com.sc.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户远程调用接口
 */
@FeignClient(value = "sc-system", fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserService {

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/system/user/info/{username}")
    R<SysUserDTO> getUserInfo(@PathVariable("username") String username);

    /**
     * 更新用户 MFA 信息
     */
    @PutMapping("/system/user/mfa")
    R<Void> updateUserMfa(@RequestBody SysUserDTO dto);

    /**
     * 根据角色ID列表查询用户ID列表（内部调用）
     */
    @PostMapping("/system/user/internal/user-ids-by-roles")
    R<List<Long>> getUserIdsByRoleIds(@RequestBody List<Long> roleIds);
}
