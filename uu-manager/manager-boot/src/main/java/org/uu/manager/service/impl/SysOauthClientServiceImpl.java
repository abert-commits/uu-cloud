package org.uu.manager.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.manager.entity.SysOauthClient;
import org.uu.manager.mapper.SysOauthClientMapper;
import org.uu.manager.service.ISysOauthClientService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SysOauthClientServiceImpl extends ServiceImpl<SysOauthClientMapper, SysOauthClient> implements ISysOauthClientService {

}
