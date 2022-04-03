package com.xgs.healthupdate.service;

import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.xgs.healthupdate.common.HealthUpdate;
import com.xgs.healthupdate.dao.UserDao;
import com.xgs.healthupdate.pojo.User;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//提供每日自动上报方法,该方法定时执行
@Component
public class AutoUpdateJob  {

  @Autowired
  HealthUpdate healthUpdate;
  @Autowired
  UserDao userDao;

  //该方法能够查询所有用户并进行上报操作

  @Scheduled(cron = "0 0 8 * * ?")
  public void execute() {
    List<User> users = userDao.selectAllUser();//获取所有用户
    //查询status，为true允许调用上报
    for (User user : users) {
      boolean status = user.isStatus();
      if(status){
        try {
          healthUpdate.longin(user.getUsername(),user.getPassword());
          healthUpdate.healthUpdate();
          healthUpdate.clear();
        } catch (HttpProcessException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
