package com.xgs.healthupdate.pojo;


import java.io.Serializable;

public class User implements Serializable {

  String username;
  String password;
  String invitation;
  boolean status;




  public User(String username, String password, String invitation,boolean status) {
    this.username = username;
    this.password = password;
    this.invitation = invitation;
    this.status=status;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public User() {
    this.status=true;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getInvitation() {
    return invitation;
  }

  public void setInvitation(String invitation) {
    this.invitation = invitation;
  }
}
