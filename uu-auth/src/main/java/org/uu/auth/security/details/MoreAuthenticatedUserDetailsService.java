package org.uu.auth.security.details;


import lombok.NoArgsConstructor;
import org.uu.auth.comm.utils.CommonUtils;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("moreUserDetailsService")
@NoArgsConstructor
public class MoreAuthenticatedUserDetailsService implements UserDetailsService {


    private Map<String, UserDetailsService> userDetailsServiceMap;

    public MoreAuthenticatedUserDetailsService(Map<String, UserDetailsService> userDetailsServiceMap) {
        this.userDetailsServiceMap = userDetailsServiceMap;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        String clientId = CommonUtils.getOAuth2ClientId();
        AssertUtil.notEmpty(clientId, ResultCode.PARAM_IS_NOT_EMPTY);
        UserDetailsService userDetailsService = userDetailsServiceMap.get(clientId);
        return userDetailsService.loadUserByUsername(userName);
    }
}
