package com.len.config;

import com.len.menu.LoginType;
import com.len.util.CustomUsernamePasswordToken;
import com.len.util.JwtToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author zhuxiaomeng
 * @date 2018/8/18.
 * @email lenospmiller@gmail.com
 * 多模块认证
 */
public class MyModularRealmAuthenticator extends ModularRealmAuthenticator {


    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        assertRealmsConfigured();
        LoginType type;
        AuthenticationToken token;
        if (authenticationToken instanceof JwtToken) {
            JwtToken token1 = (JwtToken) authenticationToken;
            token = token1;
            type = token1.getType();
        } else {
            CustomUsernamePasswordToken token1 = (CustomUsernamePasswordToken) authenticationToken;
            token = token1;
            type = token1.getType();
        }
        if (type==null) {
            throw new RuntimeException("登录认证授权类型不能为空");
        }
        Collection<Realm> realms = getRealms();
        Collection<Realm> realmsList = new ArrayList<>();
        for (Realm realm : realms) {
            if (realm.getName().contains(type.toString())) {
                realmsList.add(realm);
            }
        }
        return realmsList.size() == 1 ? doSingleRealmAuthentication(realmsList.iterator().next(), token)
                : doMultiRealmAuthentication(realmsList, token);
    }


}
