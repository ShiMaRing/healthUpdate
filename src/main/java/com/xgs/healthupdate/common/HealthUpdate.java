package com.xgs.healthupdate.common;
import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.HttpHeader;
import com.arronlong.httpclientutil.common.HttpMethods;
import com.arronlong.httpclientutil.common.HttpResult;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class HealthUpdate {

  String userId;
  HttpConfig config;
  HttpClientContext context = new HttpClientContext();
  CookieStore cookieStore = new BasicCookieStore();

  //获取用户名
  public String getName() {

    String url = "https://e-report.neu.edu.cn/notes/create";
    String s = null;
    try {
      s = HttpClientUtil.get(config.url(url));
    } catch (HttpProcessException e) {
      e.printStackTrace();
    }
    Document document = Jsoup.parse(s);
    //这一步用来拿到token
    Elements elementsByAttributeValue = document.getElementsByAttributeValue("name", "_token");
    String _token = elementsByAttributeValue.attr("value");

    //这一步用来拿到姓名
    Element navbarDropdown = document.getElementById("navbarDropdown");
    String text = navbarDropdown.text();
    String[] split = text.split("：");
    String name = split[1];//获取姓名
    return name;
  }

  //该方法用来进行登陆操作,返回用户名
  public String longin(String username, String password) throws HttpProcessException {
    this.userId = username;
    boolean flag = false;

    String url = "https://pass.neu.edu.cn/tpass/login?service=https://portal.neu.edu.cn/tp_up/";
    String mainPage = "https://portal.neu.edu.cn/tp_up/view?m=up";

    //设置上下文用来保存cookie

    context.setCookieStore(cookieStore);
    //设置保存cookie，请求登陆页面获取lt值,设置请求头
    config = HttpConfig.custom().url(url).context(context);
    Header[] headers = HttpHeader.custom().userAgent("User-Agent: Mozilla/5.0").build();
    config.headers(headers);

    //使用jsoup解析获取lt的值
    String html = HttpClientUtil.get(config);
    Document document = Jsoup.parse(html);
    Element ltElement = document.getElementById("lt");
    Elements elements = document.getElementsByAttributeValue("name", "execution");
    String exeValue = elements.attr("value");
    String lt = ltElement.attr("value");

    Map<String, Object> map = new HashMap<>();
    map.put("rsa", username + password + lt);
    map.put("ul", username.length());
    map.put("pl", password.length());
    map.put("execution", exeValue);
    map.put("_eventId", "submit");

    //获取lt之后模拟post请求发送表单
    //返回的是302界面，接下来解析302地址获取下一跳地址
    HttpResult httpResult = HttpClientUtil.sendAndGetResp(config.map(map).method(HttpMethods.POST));
    int status = httpResult.getStatusCode();

    if (status == 302) {
      flag = true;
    }

    String location = "";
    for (Header respHeader : httpResult.getRespHeaders()) {
      if (respHeader.getName().equals("Location")) {
        location = respHeader.getValue();
        break;
      }
    }
    //拿到新的location，再来一跳,此时会set一个cookie，然后再跳一次
    HttpResult tp = HttpClientUtil.sendAndGetResp(config.url(location).method(HttpMethods.GET));
    HttpClientUtil.get(config.url(mainPage));

    if (flag) {
      return getName();
    } else {
      return "fail";
    }

  }


  public void healthUpdate() throws HttpProcessException {

    String url = "https://e-report.neu.edu.cn/notes/create";
    String s = HttpClientUtil.get(config.url(url));
    Document document = Jsoup.parse(s);
    //这一步用来拿到token
    Elements elementsByAttributeValue = document.getElementsByAttributeValue("name", "_token");
    String _token = elementsByAttributeValue.attr("value");

    //这一步用来拿到姓名
    Element navbarDropdown = document.getElementById("navbarDropdown");
    String text = navbarDropdown.text();
    String[] split = text.split("：");
    String name = split[1];//获取姓名

    //构建post表单
    Map<String, Object> formMap = new HashMap<>();
    formMap.put("_token", _token);
    formMap.put("jibenxinxi_shifoubenrenshangbao", 1);
    formMap.put("profile[xuegonghao]", userId);
    formMap.put("profile[xingming]", name);
    formMap.put("profile[suoshubanji]", "");
    formMap.put("jiankangxinxi_muqianshentizhuangkuang", "正常");
    formMap.put("xingchengxinxi_weizhishifouyoubianhua", 0);
    formMap.put("cross_city", "无");
    formMap.put("qitashixiang_qitaxuyaoshuomingdeshixiang", "");
    //提交post表单
    String dest = "https://e-report.neu.edu.cn/api/notes";
    HttpResult httpResult = HttpClientUtil.sendAndGetResp(
        config.url(dest).map(formMap).method(HttpMethods.POST));
    everyDay();

    cookieStore.clear();//完成上报之后会清空cookie

  }

  //早中晚上报
  //首先进行登陆拿到cookie

  public void everyDay() throws HttpProcessException {
    String url = "https://e-report.neu.edu.cn/inspection/items";//获取url
    String s = HttpClientUtil.get(config.url(url));//获取响应
    Document document = Jsoup.parse(s);
    Elements elements = document.getElementsByAttributeValue("name", "csrf-token");
    Element element = elements.get(0);

    String token = element.attr("content");//拿到token

    System.out.println(token);


    Map<String, Object> map = new HashMap<>();
    /*_token: BXHD3gW0KlecI1OuNgWTFkCPywMRU5YbWDopulgg
       temperature: 36.5
       suspicious_respiratory_symptoms: 0
       symptom_descriptions: */
    map.put("_token", token);
    map.put("temperature", 36.5);
    map.put("suspicious_respiratory_symptoms", 0);
    map.put("symptom_descriptions", "");
    config.map(map);
    for (int i = 1; i <=3; i++) {
      String ul = "https://e-report.neu.edu.cn/inspection/items/" + i + "/records";//每一个url
      HttpClientUtil.post(config.url(ul));
    }

  }


}
