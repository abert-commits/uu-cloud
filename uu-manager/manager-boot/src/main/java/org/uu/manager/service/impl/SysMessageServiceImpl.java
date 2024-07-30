package org.uu.manager.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.entity.SysMessage;
import org.uu.manager.mapper.SysMessageMapper;
import org.uu.manager.mapper.SysUserMapper;
import org.uu.manager.req.SysMessageIdReq;
import org.uu.manager.req.SysMessageReq;
import org.uu.manager.req.SysMessageSendReq;
import org.uu.manager.service.ISysMessageService;
import org.uu.manager.util.PageUtils;
import org.uu.manager.websocket.SendForbidUserMsg;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2024-05-06
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

    @Resource
    SysUserMapper sysUserMapper;
    @Resource
    SendForbidUserMsg sendForbidUserMsg;

    @Override
    public PageReturn<SysMessage> listPage(SysMessageReq req) {
        String userId = String.valueOf(UserContext.getCurrentUserId());
        Page<SysMessage> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<SysMessage> lambdaQuery = lambdaQuery();
        lambdaQuery.eq(SysMessage::getMessageTo, userId);
        lambdaQuery.eq(SysMessage::getDeleted, 0);
        lambdaQuery.orderByDesc(SysMessage::getId);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<SysMessage> records = page.getRecords();
        return PageUtils.flush(page, records);
    }

    @Override
    public RestResult deleted(SysMessageIdReq req) {
        SysMessage sysMessage = new SysMessage();
        BeanUtils.copyProperties(req, sysMessage);
        SysMessage baseSysMessage = baseMapper.selectById(sysMessage.getId());
        baseSysMessage.setDeleted(1);
        baseMapper.updateById(baseSysMessage);
        sendForbidUserMsg.send("message deleted", "1", baseSysMessage.getMessageTo());
        return RestResult.ok("删除成功");
    }

    @Override
    public RestResult read(SysMessageIdReq req) {
        SysMessage sysMessage = new SysMessage();
        BeanUtils.copyProperties(req, sysMessage);
        SysMessage baseSysMessage = baseMapper.selectById(sysMessage.getId());
        baseSysMessage.setMessageStatus(1);
        baseSysMessage.setMessageReadTime(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(baseSysMessage);
        sendForbidUserMsg.send("message read", "1", baseSysMessage.getMessageTo());
        return RestResult.ok("更改状态成功");
    }

    @Override
    public RestResult sendMessage(SysMessageSendReq sysMessage) {
        String messageTo = sysMessage.getMessageTo();
        if (messageTo.equals("*")) {
            List<Long> toUserList = sysUserMapper.getEffectiveUserIdList();
            return sendMessage(sysMessage.getMessageFrom(), toUserList, sysMessage.getMessageType(), sysMessage.getMessageContent());
        } else {
            return sendMessage(sysMessage.getMessageFrom(), sysMessage.getMessageTo(), sysMessage.getMessageType(), sysMessage.getMessageContent());
        }
    }

    @Override
    public Integer unReadMessageCount(String userId) {
        LambdaQueryChainWrapper<SysMessage> lambdaQuery = lambdaQuery();
        lambdaQuery.eq(SysMessage::getMessageTo, userId);
        lambdaQuery.eq(SysMessage::getDeleted, 0);
        lambdaQuery.eq(SysMessage::getMessageStatus, 0);
        return baseMapper.selectCount(lambdaQuery.getWrapper());
    }

    public RestResult sendMessage(String from, String to, Integer type, String content) {

        SysMessage sysMessage = new SysMessage();
        sysMessage.setMessageFrom(from);
        sysMessage.setMessageTo(to);
        sysMessage.setMessageType(type);
        sysMessage.setMessageContent(content);
        int insert = baseMapper.insert(sysMessage);
        if (insert > 0) {
            sendForbidUserMsg.send("new message", "1", to);
            return RestResult.ok();
        }

        return RestResult.failed();
    }

    public RestResult sendMessage(String from, List<Long> to, Integer type, String content) {
        List<SysMessage> saveData = new ArrayList<>();
        for (Long toUser : to) {
            SysMessage sysMessage = new SysMessage();
            sysMessage.setMessageFrom(from);
            sysMessage.setMessageTo(String.valueOf(toUser));
            sysMessage.setMessageType(type);
            sysMessage.setMessageContent(content);
            saveData.add(sysMessage);
            sendForbidUserMsg.send("new message", "1", String.valueOf(toUser));
        }
        boolean b = this.saveBatch(saveData);
        if (b) {
            return RestResult.ok();
        }
        return RestResult.failed();
    }
}
