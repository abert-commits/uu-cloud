package org.uu.auth.security.details.member;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.auth.comm.enums.PasswordEncoderTypeEnum;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.api.AppMemberFeignClient;
import org.uu.common.pay.api.MemberFeignClient;
import org.uu.common.pay.dto.MemberAuthDTO;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service("appMemberDetailsService")
@Slf4j
@RequiredArgsConstructor
public class AppMemberDetailsServiceImpl implements UserDetailsService {
    private final AppMemberFeignClient memberAppFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 后面从管理端获取用户信息
        RestResult<MemberAuthDTO> result = memberAppFeignClient.getAppMemberByUsername(username);

        MemberDetails memberDetails = null;

        if (RestResult.ok().getCode().equals(result.getCode())) {

            MemberAuthDTO member = result.getData();

            if (null != member) {
                memberDetails = MemberDetails.builder()
                        .userId(member.getUserId())
                        .username(member.getUsername())
                        .authorities(handleRoles(member.getRoles()))
                        .enabled(member.getStatus() == 1)
                        .memberType(member.getMemberType())
                        .firstLoginIp(member.getFirstLoginIp())
                        .password(PasswordEncoderTypeEnum.BCRYPT.getPrefix() + member.getPassword())
                        .build();
            }
        }
        if (Objects.isNull(memberDetails)) {
            throw new UsernameNotFoundException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getMsg());
        } else if (!memberDetails.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!memberDetails.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!memberDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return memberDetails;
    }

    private Collection<SimpleGrantedAuthority> handleRoles(List<String> roles) {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }


}
