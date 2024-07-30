package org.uu.manager.util;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.uu.common.core.page.PageReturn;

import java.util.List;


public class PageUtils {
        public static <T> PageReturn<T> flush(Page page, List<T> list ) {
            PageReturn<T> pageReturn = new PageReturn<>();
            pageReturn.setPageNo(page.getPages());
            pageReturn.setPageSize(page.getSize());
            pageReturn.setTotal(page.getTotal());
            pageReturn.setList(list);
            return pageReturn;
        }
    }

