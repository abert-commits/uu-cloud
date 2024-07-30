package org.uu.wallet.async;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uu.wallet.entity.RegisterRecord;
import org.uu.wallet.service.RegisterRecordService;
import org.uu.wallet.util.IpUtil;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 注册记录相关异步任务
 */
@Service
@RequiredArgsConstructor
public class RegisterRecordAsync {
    private final RegisterRecordService registerRecordService;

    /**
     * 添加注册记录异步任务
     * @param antId 蚂蚁ID
     * @param inviteCode 注册邀请码
     */
    @Async
    public void insertRegisterRecord(Long antId, String inviteCode, HttpServletRequest request) {
        this.registerRecordService.save(
                RegisterRecord.builder()
                        .antId(antId)
                        .inviteCode(inviteCode)
                        .registerIp(IpUtil.getRealIP(request))
                        .registerTime(LocalDateTime.now())
                        .build()
        );
    }
}