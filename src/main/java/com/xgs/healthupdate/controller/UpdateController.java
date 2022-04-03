package com.xgs.healthupdate.controller;

import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.xgs.healthupdate.common.HealthUpdate;
import com.xgs.healthupdate.dao.UserDao;
import com.xgs.healthupdate.pojo.Result;
import com.xgs.healthupdate.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthUpdate")
public class UpdateController {

  @Autowired
  HealthUpdate healthUpdate;
  @Autowired
  UserDao userDao;

  @RequestMapping(value = "/oneday", method = RequestMethod.POST)
  public Result oneday(@RequestBody User user) {
    Result result = new Result();
    String name = "";
    try {
      name = healthUpdate.longin(user.getUsername(), user.getPassword());
    } catch (HttpProcessException e) {
      //出现异常，说明登陆失败
      e.printStackTrace();
    }
    if ("fail".equals(name)) {
      result.setFlag(false);
      result.setMessage("账号密码错误");
    } else {
      result.setFlag(true);
      result.setMessage("上报成功," + name + "同学");
      //执行健康上报
      try {
        healthUpdate.healthUpdate();
        healthUpdate.clear();//清除cookie
      } catch (HttpProcessException e) {
        result.setFlag(false);
        result.setMessage("上报失败，未知错误");
      }
    }
    return result;
  }

  //该方法进行
  @RequestMapping(value = "/everyday", method = RequestMethod.POST)
  public Result everyday(@RequestBody User user) {
    Result result = new Result();
    if ("".equals(user.getInvitation()) || user.getInvitation() == null) {
      result.setFlag(false);
      result.setMessage("邀请码不合法");
      return result;
    }
    String loginResult = "";
    try {
      loginResult = healthUpdate.longin(user.getUsername(), user.getPassword());
      healthUpdate.clear();

    } catch (HttpProcessException e) {
      result.setFlag(false);
      result.setMessage("账号密码错误");
      return result;
    }

    //登陆失败，直接返回即可
    if ("fail".equals(loginResult)) {
      result.setFlag(false);
      result.setMessage("账号密码错误");
      return result;
    } else {
      //登陆成功,先检查数据库有没有保存该账号，保存了就返回错误信息
      User userResult = userDao.selectById(user.getUsername());
      if (userResult != null) {
        if (userResult.isStatus()) {
          result.setFlag(false);
          result.setMessage("该账号已保存，请勿重复提交");
        } else {
          result.setFlag(true);
          result.setMessage("重新启动每日健康上报成功");
          userResult.setStatus(true);
          userDao.updateUser(userResult);
        }
      }//说明已经存在账号在数据库中了
      else {
        //检查邀请码合法性，判断是否存在该邀请码以及是否被人占用
        User u = userDao.selectByInvitation(user.getInvitation());
        //查不到该邀请码
        if (u == null) {
          result.setFlag(false);
          result.setMessage("邀请码不合法");
        } else if ("".equals(u.getUsername()) || u.getUsername() == null) {
          result.setFlag(true);
          result.setMessage(loginResult + "同学，每日健康上报设置成功,今日已自动上报");
          user.setStatus(true);
          userDao.updateUser(user);
          try {
            healthUpdate.longin(user.getUsername(),user.getPassword());
            healthUpdate.healthUpdate();
            healthUpdate.clear();
          } catch (HttpProcessException e) {
            result.setFlag(false);
            result.setMessage("上报失败，未知错误");
          }
        } else {
          result.setFlag(false);
          result.setMessage("该邀请码已被使用");
        }
      }
    }
    return result;
  }

  //取消每日健康上报
  @RequestMapping(value = "/canceleveryday", method = RequestMethod.POST)
  public Result canceleveryday(@RequestBody User user) {
    //先检查数据库有没有，
    Result result = new Result();
    if ("".equals(user.getUsername()) || user.getUsername() == null) {
      result.setFlag(false);
      result.setMessage("数据不合法");
      return result;
    }
    User resultUser = userDao.selectByUser(user);
    if (resultUser == null) {
      result.setFlag(false);
      result.setMessage("数据库中不存在该用户");
    } else {
      resultUser.setStatus(false);
      userDao.updateUser(resultUser);
      result.setFlag(true);
      result.setMessage("停止每日上报成功");
    }
    return result;
  }

}
