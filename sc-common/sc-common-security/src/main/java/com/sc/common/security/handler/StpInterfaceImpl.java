package com.sc.common.security.handler;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.sc.common.core.domain.model.LoginUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色认证接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        LoginUser loginUser = StpUtil.getSession().getModel("loginUser", LoginUser.class);
        if (loginUser != null && loginUser.getPermissions() != null) {
            return new ArrayList<>(loginUser.getPermissions());
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        LoginUser loginUser = StpUtil.getSession().getModel("loginUser", LoginUser.class);
        if (loginUser != null && loginUser.getRoles() != null) {
            return new ArrayList<>(loginUser.getRoles());
        }
        return new ArrayList<>();
    }
}
