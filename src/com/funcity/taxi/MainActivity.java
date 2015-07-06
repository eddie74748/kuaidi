package com.funcity.taxi;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import com.funcity.taxi.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONTokener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MainActivity extends Activity{

    private KuaidiNearByCarReq kuaidiNearByCarReq;
    static TextView textView, textView2, textView3, textView4;
	Button btn1;

	RandomAccessFile cityPointFile;
	
	int MSG_CITY = 0,
		MSG_POINT = 1,
		MSG_HTTP = 2,
		MSG_JSON = 3;
	static Handler mHandler = new Handler(){	      
	    @Override
	    public void handleMessage(Message msg) {
	    	//鏉╂瑩鍣风亸鍗炲讲娴犮儰绮爉sg閸欐牕鍤担鐘辩矤缁捐法鈻兼稉顓濈炊閸掔櫊I缁捐法鈻奸惃鍕鐟楀尅绱�
	    	String strMessage = (String)msg.obj;
	    	String txt = strMessage;
	    	
	    	switch(msg.what)
	    	{
	    	case 0:
	    		textView.setText("CITY STATUS: " + txt);
	    		break;
	    	case 1:
	    		textView2.setText("POINT: " + txt);
	    		break;
	    	case 2:
	    		textView3.setText("HTTP: " + txt);
	    		break;
	    	case 3:
	    		textView4.setText("JSON: "+ txt);
	    		break;
    		default:
    			break;
	    	}
	    	
	    }
	};
	
	public class CityPoint{
		public String city;
		public double left;
		public double top;
		public double right;
		public double bottom;
		public double xd;
		public double yd;
	}
	
	int year = -1;
	int month = -1;
	int date = -1;  
	int hour = -1;
	int minute = -1;
	
	Queue<CityPoint> queueCityPoint = new LinkedList<CityPoint>();
	CityPoint currentCityPoint;
	int bRunning = 0;
	private Logger gLogger;
	//private Logger gWriter;
    
    public int updateTime()
    {
    	int ret = 0;
    	Calendar c = Calendar.getInstance();
    	
    	//Time t = new Time("GMT+8");
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		if(date != c.get(Calendar.DAY_OF_MONTH))
		{
			ret = 1;
		}
		date = c.get(Calendar.DAY_OF_MONTH);
		hour = c.get(Calendar.HOUR_OF_DAY);
		/*if(minute != c.get(Calendar.MINUTE))
		{
			ret = 1;
		}*/
		minute = c.get(Calendar.MINUTE);
		
		return ret;
    }
    
    //final LogConfigurator writerConfigurator = new LogConfigurator();
	final LogConfigurator logConfigurator = new LogConfigurator();
	public void configLog()
	{	
		String strDate = "" + year + "_" + month + "_" + date;  
		
		
		//writerConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator + "kuaidi.data");
		logConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator + "kuaidi_" + strDate + ".csv");
		// Set the root log level
		//writerConfigurator.setRootLevel(Level.DEBUG);
		logConfigurator.setRootLevel(Level.DEBUG);
		///writerConfigurator.setMaxFileSize(1024 * 1024 * 500);
		logConfigurator.setMaxFileSize(1024 * 1024 * 1024);
		// Set log level of a specific logger
		//writerConfigurator.setLevel("org.apache", Level.ERROR);
		logConfigurator.setLevel("org.apache", Level.ERROR);
		logConfigurator.setImmediateFlush(true);
		//writerConfigurator.configure();
		logConfigurator.configure();

		//gLogger = Logger.getLogger(this.getClass());
		//gWriter = 
		gLogger = Logger.getLogger("kuaidi");
	}
	
	void writeLine(String line)
	{
		if(updateTime() == 1)
		{
			configLog();
		}
		
		gLogger.debug(line);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        kuaidiNearByCarReq = new KuaidiNearByCarReq("zhuanche");
        
        textView = (TextView) findViewById(R.id.text);
		textView2 = (TextView) findViewById(R.id.text2);
		textView3 = (TextView) findViewById(R.id.text3);
		textView4 = (TextView) findViewById(R.id.text4);
		btn1 = (Button)findViewById(R.id.button1);
		
		updateTime();
		configLog();
		gLogger.debug("start kuaidi spider");
		
		btn1.setText("start");
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Thread t;
				if(bRunning == 0)
				{
					bRunning = 1;
					btn1.setText("start");
					t = new Thread(new HTTPThread());
					t.start();
				}else
				{
					bRunning = 0;
					btn1.setText("end");
				}
				
			}
		});
    }
    
    public void GetAllCityPoints(){
		try
		{
			cityPointFile = new RandomAccessFile(Environment.getExternalStorageDirectory().getPath() + "/citypoints.txt", "rw"); 
			while(cityPointFile.read() != -1){
				CityPoint cityPoint = new CityPoint();
				
			 	String strCityPoint = new String(cityPointFile.readLine().getBytes("ISO-8859-1"),"GBK");
			 	String[] vals = strCityPoint.split("\t");
			 	if(vals.length <= 2)
			 	{
			 		continue;
			 	}
			 	
			 	Log.i("city", strCityPoint);
			 	
			 	cityPoint.city = vals[1];
			 	String[] lefttop = vals[2].split(",");
			 	cityPoint.left = Double.parseDouble(lefttop[0]);
			 	cityPoint.top = Double.parseDouble(lefttop[1]);
			 	String[] rightbottom = vals[3].split(",");
			 	cityPoint.right = Double.parseDouble(rightbottom[0]);
			 	cityPoint.bottom = Double.parseDouble(rightbottom[1]);
			 	cityPoint.xd = Double.parseDouble(vals[6]);
			 	cityPoint.yd = Double.parseDouble(vals[7]);
			 	
			 	queueCityPoint.offer(cityPoint);
		 	}
			cityPointFile.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}

    public void UpdateUI(int type, String info)
	{
    	Message msg = new Message();
	    msg.what =  type;
	    msg.obj = info;
	    mHandler.sendMessage(msg);
	}
    
    public class HTTPThread implements Runnable {

		@Override
		public void run() {
			while(bRunning == 1)
			{
				one_loop();
			}
		}
		
		public void one_loop()
		{
			//String request1 = "http://dfcar.kuaidadi.com/dfcar/request/json?cmd=76802&idx=0&type=P&ver=3.8.1&os=android&uuid=00000000-0c9b-6274-ffee-ffff980a634d&token&lat=31.276783701512826&lng=120.75763521519225&city=%E8%8B%8F%E5%B7%9E%E5%B8%82&osver=5.0.2&ts="+Long.toString(System.currentTimeMillis())+"&cmd=76802&sign=52dee52813633c5e81f5e4c0a25b48b80862f405a3e492035a08";
			//String body1 = "{\"lat\":31.280565999809888,\"lng\":120.768530011837,\"num\":\"0\"}";
			//String result1 = HttpPostData(request1, body1);
			
			GetAllCityPoints();
			
			for(currentCityPoint = queueCityPoint.poll(); null != currentCityPoint; currentCityPoint = queueCityPoint.poll())
			{
				String city = currentCityPoint.city;
				UpdateUI(MSG_CITY, city);
				
				double d = 2.828427124,
				lngstep = (currentCityPoint.bottom - currentCityPoint.top)/(currentCityPoint.xd/d),
				latstep = (currentCityPoint.left - currentCityPoint.right)/(currentCityPoint.yd/d),
				stlng = currentCityPoint.top,
				endlng = currentCityPoint.bottom,
				stlat = currentCityPoint.right,
				endlat = currentCityPoint.left;
				
				for(double i = stlng; i < endlng; i+=lngstep)
				{
					for(double j = stlat; j < endlat; j+=latstep)
					{
						UpdateUI(MSG_POINT, "\r\nlng: " + i + " \r\nlat: " + j);
						kuaidiNearByCarReq.setCity(city);
						kuaidiNearByCarReq.setLat(""+j);
						kuaidiNearByCarReq.setLng(""+i);
						String request = kuaidiNearByCarReq.makeRequest();
						request = "http://c2.kuaidadi.com/taxi/a/js.do?sign=6825c788c6d66b3e1a86d16baa1ab1c5085ce63701a613afcc08&ver=3.8.1&os=android&lat=31.276718169934956&lng=120.7579297025087&osver=5.0.2&ts=" + Long.toString(System.currentTimeMillis()) + "&cmd=50306";
						String body = kuaidiNearByCarReq.makeBody();
						body = "{\"lat\":"+31.276718169934956+",\"lng\":"+120.7579297025087+",\"num\":\"0\"}";		
						String result = HttpPostData(request, body);
						if(result.length() < 10)
						{
							UpdateUI(MSG_JSON, "http request failue! Continue...");
							continue;
						}
						try{
							JSONTokener jsonParser = new JSONTokener(result);
							JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
							int statusCode = jsonObj.getInt("code");
							if(statusCode != 0)
							{
								UpdateUI(MSG_JSON, "response failue! Continue...");
								continue;
							}
							JSONObject resultObj = jsonObj.getJSONObject("result");
							int count = resultObj.getInt("count");
							JSONArray carsObj = resultObj.getJSONArray("cars");
							if(count < carsObj.length())
							{
								count = carsObj.length();
							}
							for(int c = 0; c < count; c++)
							{
								UpdateUI(MSG_JSON, "response car " + c);
								JSONObject carObj = carsObj.getJSONObject(c);
								double carLat = carObj.getDouble("lat");
								double carLng = carObj.getDouble("lng");
								int carDriverId = carObj.getInt("driver_id");
								int carDriverType = carObj.getInt("driver_type");
								int carType = carObj.getInt("car_type");
								
								String line = "," + 
											city + "," +
											i + "," +
											j + "," +
											carLat + "," + 
											carLng + "," + 
											carDriverId + "," +
											carDriverType + "," +
											carType;

								//gLogger.debug(line);
								writeLine(line);
							}
						}catch(JSONException e)
						{
							e.printStackTrace();
							continue;
						}catch (Exception e) {
							//strResult = e.getMessage().toString();
							e.printStackTrace();
							continue;
						}
					}
				}
				
				
				if(bRunning == 0)
				{
					return;
				}
			}
			UpdateUI(MSG_CITY, "finish!");
			gLogger.debug("finish");
		}
	}
    
    public String HttpPostData(String request, String body) {
		String strResult = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			String uri = request;
			HttpPost httppost = new HttpPost(uri);

			httppost.setEntity(new StringEntity(body));
			
			HttpResponse response;
			response = httpclient.execute(httppost);
		
			int code = response.getStatusLine().getStatusCode();
			Log.i("status code", Integer.toString(code));
			if (code == 200) {
				HttpEntity entity = response.getEntity();
				int length=(int) entity.getContentLength();
				strResult = EntityUtils.toString(entity);
				UpdateUI(MSG_HTTP, "Response: "+strResult);
				
				return strResult;
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
			//strResult = e.getMessage().toString();
			e.printStackTrace();
		} catch (Exception e) {
			//strResult = e.getMessage().toString();
			e.printStackTrace();
		}
		Log.i("body", strResult);
		return strResult;
	}
}
