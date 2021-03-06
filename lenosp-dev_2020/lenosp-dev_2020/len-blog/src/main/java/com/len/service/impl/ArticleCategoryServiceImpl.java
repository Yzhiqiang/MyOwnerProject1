package com.len.service.impl;

import com.len.base.BaseMapper;
import com.len.base.impl.BaseServiceImpl;
import com.len.entity.ArticleCategory;
import com.len.mapper.ArticleCategoryMapper;
import com.len.service.ArticleCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhuxiaomeng
 * @date 2018/10/11.
 * @email lenospmiller@gmail.com
 */
@Service
public class ArticleCategoryServiceImpl extends BaseServiceImpl<ArticleCategory, String>
        implements ArticleCategoryService {

    @Autowired
    private ArticleCategoryMapper articleCategoryMapper;

    @Override
    public void delByIds(List<String> ids) {
        articleCategoryMapper.delByIds(ids);
    }
}
