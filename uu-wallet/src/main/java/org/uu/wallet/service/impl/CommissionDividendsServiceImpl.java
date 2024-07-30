package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.uu.wallet.entity.CommissionDividends;
import org.uu.wallet.mapper.CommissionDividendsMapper;
import org.uu.wallet.service.CommissionDividendsService;

@Service
public class CommissionDividendsServiceImpl extends ServiceImpl<CommissionDividendsMapper, CommissionDividends> implements CommissionDividendsService {
}
