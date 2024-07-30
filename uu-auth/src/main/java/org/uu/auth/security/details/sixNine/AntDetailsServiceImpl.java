package org.uu.auth.security.details.sixNine;

import lombok.RequiredArgsConstructor;
import org.uu.auth.comm.enums.PasswordEncoderTypeEnum;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.api.AntFeignClient;
import org.uu.common.pay.dto.AntInfoDTO;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Objects;

@Service("antDetailsService")
@RequiredArgsConstructor
public class AntDetailsServiceImpl implements UserDetailsService {
    private final AntFeignClient antFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RestResult<AntInfoDTO> responseVO = this.antFeignClient.getAntByAntName(username);
        AntDetails antDetails = null;
        if (RestResult.ok().getCode().equals(responseVO.getCode())) {
            AntInfoDTO antInfoDTO = responseVO.getData();
            if (Objects.nonNull(antInfoDTO)) {
                antDetails = AntDetails.builder()
                        .anyId(antInfoDTO.getId())
                        .username(antInfoDTO.getPhoneNumber())
                        .password(PasswordEncoderTypeEnum.BCRYPT.getPrefix() + antInfoDTO.getPassword())
                        .authorities(Collections.emptyList())
                        .registerInviteId(antInfoDTO.getRegisterInviteId())
                        .registerInviteCode(antInfoDTO.getRegisterInviteCode())
                        .build();
            }
        }

        if (Objects.isNull(antDetails)) {
            throw new UsernameNotFoundException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getMsg());
        } else if (!antDetails.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!antDetails.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!antDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return antDetails;
    }
}
