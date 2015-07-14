package com.funcity.taxi;

import android.widget.Toast;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Anonymous on 15-5-16.
 */
public class KuaidiNearByCarReq {
	private String car = "taxi";
    private String prefix = "http://dfcar.kuaidadi.com/dfcar/request/json?";//dfcar.kuaidadi.com
    private String cmd = "cmd=76802&";
    private String idx = "idx=0&";
    private String type = "type=P&";
    private String ver = "ver=3.8.1&";
    private String os = "os=android&";
    private String uuid = "uuid=00000000-0c9b-6274-ffee-ffff980a634d&";
    private String token = "token=&";
    private String lat = "31.276783701512826";
    private String lng = "120.75763521519225";
    private String city = "%E8%8B%8F%E5%B7%9E%E5%B8%82&";//urlencode utf-8
    private String osver="5.0.2&";//閺堫剚婧�ndroid閻楀牊婀�    private String ts;//閺冨爼妫块幋锟�   private String sign;//閻€劋绮懛顏勭箒閻ㄥ墕o鎼存捁顓哥粻锟�   private String request;
    private String request;
    private String sign;
    private JNILib jniLib;//閹稿鍙庢禒鏍畱婢圭増妲戦弬鐟扮础鐎规矮绠熸稉锟介嚋jnilib

    public KuaidiNearByCarReq (String c){
        super();
        car = c;
        if(car.equalsIgnoreCase("zhuanche"))
        {
        	prefix = "http://c2.kuaidadi.com/taxi/a/js.do?";
        	cmd = "cmd=50306&";
        }
    //    jniLib = new JNILib();
    }

    public void setCity(String city) {
        try {
        	String strCity = URLDecoder.decode("%E8%8B%8F%E5%B7%9E%E5%B8%82", "UTF-8");
            this.city = URLEncoder.encode(city, "UTF-8") + "&";
        }catch (Exception e) {
            Toast.makeText(null, e.toString() + "Error Encode City", Toast.LENGTH_LONG).show();
        }
    }

    public void setOsver(String osver) {
        this.osver = osver + "&";
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLat() {
        return lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLng() {
        return lng;
    }

    public String makeRequest() {
        this.request = prefix + cmd + idx + type + ver + os + uuid + token + "lat=" + lat + "&lng=" + lng + "&city=" + city + "osver=" + osver + "ts=" +Long.toString(System.currentTimeMillis()) + "&" + cmd + "sign=" + makeSign();
        return this.request;
    }
    
    public String makeBody()
    {
    	if(car.equalsIgnoreCase("zhuanche"))
        {
        	return "{\"cmd\":50306,\"request\":{\"lng\":" + lng + ",\"lat\":" + lat + "}}";
        }else
        {
        	return "{\"lat\":"+lat+",\"lng\":"+lng+",\"num\":\"0\"}";
        }
    }

    public String makeSign() {
        sign = "cmd:76802{\"lat\":" + lat + ",\"lng\":" + lng + ",\"num\":0}";
        
        //sign = "cmd:50306{\"lat\":" + 31.276718169934956 + ",\"lng\":" + 120.7579297025087 + ",\"num\":0}";
        if(car.equalsIgnoreCase("zhuanche"))
        {
        	sign = "{\"cmd\":50306,\"request\":{\"lng\":" + lng + ",\"lat\":" + lat + "}}";
        }
        //return sign;
        String res = JNILib.getSign(sign);
        return res;
    }
}
