package com.len.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.len.base.impl.BaseServiceImpl;
import com.len.entity.SysMenu;
import com.len.entity.SysRoleMenu;
import com.len.exception.ServiceException;
import com.len.mapper.SysMenuMapper;
import com.len.mapper.SysRoleMenuMapper;
import com.len.service.MenuService;
import com.len.service.RoleMenuService;
import com.len.util.MsHelper;
import com.len.util.TreeUtil;
import com.len.validator.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaomeng
 * @date 2017/12/12.
 * @email lenospmiller@gmail.com
 */
@Service
public class MenuServiceImpl extends BaseServiceImpl<SysMenu, String> implements MenuService {

    @Autowired
    private SysMenuMapper menuDao;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private RoleMenuService roleMenuService;


    @Override
    public List<SysMenu> getMenuNotSuper() {
        return menuDao.getMenuNotSuper();
    }


    @Override
    public List<SysMenu> getMenuChildren(String id) {
        return menuDao.getMenuChildren(id);
    }

    public SysMenu child(SysMenu sysMenu, List<SysMenu> sysMenus, Integer num) {
        List<SysMenu> childSysMenu = sysMenus.stream().filter(s ->
                s.getPId().equals(sysMenu.getId())).collect(Collectors.toList());
        sysMenus.removeAll(childSysMenu);
        for (SysMenu menu : childSysMenu) {
            ++num;
            child(menu, sysMenus, num);
            sysMenu.addChild(menu);
        }
        return sysMenu;
    }

    @Override
    public JSONArray getMenuJsonList() {
        List<SysMenu> sysMenus = list();
        List<SysMenu> supers = sysMenus.stream().filter(sysMenu ->
                StringUtils.isEmpty(sysMenu.getPId()))
                .collect(Collectors.toList());
        sysMenus.removeAll(supers);
        supers.sort(Comparator.comparingInt(SysMenu::getOrderNum));
        JSONArray jsonArr = new JSONArray();
        for (SysMenu sysMenu : supers) {
            SysMenu child = child(sysMenu, sysMenus, 0);
            jsonArr.add(child);
        }
        return jsonArr;
    }

    @Override
    public JSONArray getMenuJsonByUser(List<SysMenu> menuList) {
        JSONArray jsonArr = new JSONArray();
        menuList.sort((o1, o2) -> {
            if (o1.getOrderNum() == null || o2.getOrderNum() == null) {
                return -1;
            }
            if (o1.getOrderNum() > o2.getOrderNum()) {
                return 1;
            }
            if (o1.getOrderNum().equals(o2.getOrderNum())) {
                return 0;
            }
            return -1;
        });
        int pNum = 1000;
        for (SysMenu menu : menuList) {
            if (StringUtils.isEmpty(menu.getPId())) {
                SysMenu sysMenu = getChilds(menu, pNum, 0, menuList);
                jsonArr.add(sysMenu);
                pNum += 1000;
            }
        }
        return jsonArr;
    }

    @Override
    public boolean del(String id) {
        ValidatorUtils.notEmpty(id, "failed.get.data");
        SysRoleMenu sysRoleMenu = new SysRoleMenu();
        sysRoleMenu.setMenuId(id);
        QueryWrapper<SysRoleMenu> sysRoleMenuQueryWrapper = new QueryWrapper<>(sysRoleMenu);
        int count = roleMenuService.count(sysRoleMenuQueryWrapper);
        //??????????????????????????????
        if (count > 0) {
            throw new ServiceException(MsHelper.getMsg("menu.bind.role"));
        }
        //?????????????????? ????????????
        SysMenu sysMenu = new SysMenu();
        sysMenu.setPId(id);
        QueryWrapper<SysMenu> sysRoleMenuQueryWrapperTwo = new QueryWrapper<>(sysMenu);
        if (menuDao.selectCount(sysRoleMenuQueryWrapperTwo) > 0) {
            throw new ServiceException(MsHelper.getMsg("menu.exists.children"));
        }
        return removeById(id);
    }

    public SysMenu getChilds(SysMenu menu, int pNum, int num, List<SysMenu> menuList) {
        for (SysMenu menus : menuList) {
            if (menu.getId().equals(menus.getPId()) && menus.getMenuType() == 0) {
                ++num;
                SysMenu m = getChilds(menus, pNum, num, menuList);
                m.setNum(pNum + num);
                menu.addChild(m);
            }
        }
        return menu;

    }

    @Override
    public List<SysMenu> getMenuChildrenAll(String id) {
        return menuDao.getMenuChildrenAll(id);
    }


    @Override
    public JSONArray getTreeUtil(String roleId) {
        TreeUtil treeUtil;
        List<SysMenu> sysMenus = list();
        List<SysMenu> supers = sysMenus.stream().filter(sysMenu ->
                StringUtils.isEmpty(sysMenu.getPId()))
                .collect(Collectors.toList());
        sysMenus.removeAll(supers);
        supers.sort(Comparator.comparingInt(SysMenu::getOrderNum));
        JSONArray jsonArr = new JSONArray();
        for (SysMenu sysMenu : supers) {
            treeUtil = getChildByTree(sysMenu, sysMenus, 0, null, roleId);
            jsonArr.add(treeUtil);
        }
        return jsonArr;

    }

    @Override
    public List<SysMenu> getUserMenu(String id) {
        return menuDao.getUserMenu(id);
    }

    private TreeUtil getChildByTree(SysMenu sysMenu, List<SysMenu> sysMenus, int layer, String pId, String roleId) {
        layer++;
        List<SysMenu> childSysMenu = sysMenus.stream().filter(s ->
                s.getPId().equals(sysMenu.getId())).collect(Collectors.toList());
        sysMenus.removeAll(childSysMenu);
        TreeUtil treeUtil = new TreeUtil();
        treeUtil.setId(sysMenu.getId());
        treeUtil.setName(sysMenu.getName());
        treeUtil.setLayer(layer);
        treeUtil.setPId(pId);
        /*??????????????????*/
        if (!StringUtils.isEmpty(roleId)) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(sysMenu.getId());
            sysRoleMenu.setRoleId(roleId);
            int count = roleMenuMapper.selectCountByCondition(sysRoleMenu);
            if (count > 0) {
                treeUtil.setChecked(true);
            }
        }
        for (SysMenu menu : childSysMenu) {
            TreeUtil m = getChildByTree(menu, sysMenus, layer, menu.getId(), roleId);
            treeUtil.getChildren().add(m);
        }
        return treeUtil;
    }
}
