package com.xgs.healthupdate.dao;

import com.xgs.healthupdate.pojo.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface UserDao {
  //updateUser
  public int updateUser(User user);

  public User selectById(String username);

  public User selectByInvitation(String invitation);

  public User selectByUser(User user);

  public List<User> selectAllUser();
}
