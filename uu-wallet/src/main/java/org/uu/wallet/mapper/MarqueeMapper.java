package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.uu.wallet.entity.Marquee;
import org.uu.wallet.entity.MatchPool;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface MarqueeMapper extends BaseMapper<Marquee> {
    
}
