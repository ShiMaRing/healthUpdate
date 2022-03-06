package com.xgs.healthupdate.common;

import org.apache.ibatis.annotations.ResultMap;
import org.springframework.stereotype.Controller;

@Controller
public class Index {

  @ResultMap("/")
  public String getIndex() {
    return "LoginPage";
  }
}
