package com.example.weathersearch;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
//import com.facebook.samples.sessionlogin.LoginUsingActivityActivity;
//import com.facebook.samples.sessionlogin.R;
//import com.facebook.samples.sessionlogin.SessionLoginSampleActivity;


public class MainActivity extends Activity {

	public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	
	public String tempUnit = "f";
	public String CityRegEx = "^\\s*[a-zA-Z0-9'.]+(?:[\\s-][a-zA-Z0-9'.]+)*(,\\s?[a-zA-Z0-9'.]+(?:[\\s-][a-zA-Z0-9'.]+)*){1,2}$";
	public String ZipRegEx = "\\b\\s*\\d{5}\\b";
	public String emptyRegEx = "^[\\s]+$";
	public String locationType;
	public String location;
	public boolean correctInput;
	public AlertDialog.Builder altDialog;
	public JSONObject jsonString;
	
	public ArrayList<ArrayList<String>> feedDetails;
	public int feedIndex;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void SCWClick(View view) throws UnsupportedEncodingException{
				
		final Dialog alertDialog = new Dialog(this);
		alertDialog.setContentView(R.layout.fb_dialog_layout);
		alertDialog.setTitle("Post to Facebook");
		
		Button postButton = (Button) alertDialog.findViewById(R.id.postButton);
		postButton.setText("Post Current Weather");
		// if button is clicked, close the custom dialog
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					feedIndex = 0;
					postOnFB();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				alertDialog.cancel();
			}
		});
		
		Button cancelButton = (Button) alertDialog.findViewById(R.id.cancelButton);
		// if button is clicked, close the custom dialog
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		// show it
		alertDialog.show();
	}
	public void SWFClick(View view) throws UnsupportedEncodingException{
AlertDialog.Builder currentF = new AlertDialog.Builder(this);
		
		final Dialog alertDialog = new Dialog(this);
		alertDialog.setContentView(R.layout.fb_dialog_layout);
		alertDialog.setTitle("Post to Facebook");
		
		Button postButton = (Button) alertDialog.findViewById(R.id.postButton);
		postButton.setText("Post Weather Forecast");
		// if button is clicked, close the custom dialog
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					feedIndex = 1;
					postOnFB();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				alertDialog.cancel();
			}
		});
		
		Button cancelButton = (Button) alertDialog.findViewById(R.id.cancelButton);
		// if button is clicked, close the custom dialog
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		// show it
		alertDialog.show();
		
	}

	public void postOnFB() throws UnsupportedEncodingException{
		
		altDialog= new AlertDialog.Builder(this);
		// start Facebook Login
	    Session.openActiveSession(this, true, new Session.StatusCallback() {

	    	// callback when session changes state…checks for returning users or logout
	        @SuppressWarnings("deprecation")
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				// TODO Auto-generated method stub
	        	
	        	if (session.isOpened()) {
	        		Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
	        			
	        			// callback after Graph API response with user object
						@Override
						public void onCompleted(GraphUser user, Response response) {
							System.out.println("inside onCompleted");
							// TODO Auto-generated method stub
							if (user != null) {
								System.out.println("Inside if user != null");
								try {
									postWeatherInfo();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}							
						}});	        		
	        	}	        	
			}});
		
	}
	
	/** Called when the user clicks the Search button 
	 * @throws UnsupportedEncodingException */
	public void searchWeather(View view) throws UnsupportedEncodingException {
	    // Do something in response to button
		
		//boolean checked = ((RadioButton) view).isChecked();
		RadioGroup g = (RadioGroup) findViewById(R.id.tempRadio);
		switch(g.getCheckedRadioButtonId()) {
	        case R.id.radioF:	            
	                tempUnit = "f";
	            break;
	        case R.id.radioC:	            
	            	tempUnit = "c";
	            break;
	    }
		
		//Intent intent = new Intent(this, DisplayMessageActivity.class);
		correctInput = false;
		EditText editText = (EditText) findViewById(R.id.edit_message);
		String message = editText.getText().toString();
		String displayText = "";
		if(message== "" || message.matches(emptyRegEx)){
			displayText = "Please enter a Location";
		}
		else if(message.matches("^\\s*\\d+$")){
			if(message.matches(ZipRegEx)){
				locationType = "zip";
				location = message;
				displayText = location;
				correctInput = true;
				
			}
			else{
				displayText = "Please enter a valid 5 digit Zip Code as Location\nExample: 90007"; 
			}			
		}
		else if(message.matches(CityRegEx)){
			locationType = "city";
			location = message.replace("'", " ");		
			//displayText = location;
			correctInput = true;
			
		}
		else{
			
			displayText = "Please enter a valid City Location, must include state and/or country separated by comma. \nExample: Los Angeles, CA";
		}
		//intent.putExtra(EXTRA_MESSAGE, message);
		//startActivity(intent);
		
				
		
		if(correctInput == true){
			makeConnection();			
		}
		else{
			hideDynamicArea();
			Toast toast = Toast.makeText(getApplicationContext(),
					displayText,
                    Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 300);
			toast.show();
			
		}
					
	}
	
	public void makeConnection() throws UnsupportedEncodingException{		
			
		String query = "location="+URLEncoder.encode(location, "UTF-8")+"&locType="+locationType+"&tempUnit="+tempUnit;
		HttpReq hreq = new HttpReq();
        String[] params = new String[1];
        params[0] = query;        
        hreq.execute(params);
		
		
	}
	
	public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

		ImageView imageView = null;

		@Override
		protected Bitmap doInBackground(ImageView... imageViews) {
		    this.imageView = imageViews[0];
		    return download_Image((String)imageView.getTag());
		}

		@Override
		protected void onPostExecute(Bitmap result) {
		    imageView.setImageBitmap(result);
		}


		private Bitmap download_Image(String url) {
			Bitmap bm = null;
			Bitmap transparentbm = null;
						
		    try {
		    	HttpClient httpclient = new DefaultHttpClient();
	             
	             // make GET request to the given URL
	            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
	            // receive response as inputStream
	            InputStream inputStream = httpResponse.getEntity().getContent();
		        BufferedInputStream bis = new BufferedInputStream(inputStream);
		        bm = BitmapFactory.decodeStream(bis);
		        bis.close();
		        inputStream.close();
		    } catch (IOException e) {
		        Log.e("Hub","Error getting the image from server : " + e.getMessage().toString());
		    } 
		    transparentbm = eraseBG(bm, -1);
		    return transparentbm;
		}
	}
	
	@SuppressLint("NewApi")
	private static Bitmap eraseBG(Bitmap src, int color) {
	    int width = src.getWidth();
	    int height = src.getHeight();
	    Bitmap b = src.copy(Config.ARGB_8888, true);
	    b.setHasAlpha(true);

	    int[] pixels = new int[width * height];
	    src.getPixels(pixels, 0, width, 0, 0, width, height);

	    for (int i = 0; i < width * height; i++) {
	        if (pixels[i] == color) {
	            pixels[i] = 0;
	        }
	    }

	    b.setPixels(pixels, 0, width, 0, 0, width, height);

	    return b;
	}
	
	private class HttpReq extends AsyncTask<String, Void, String>{
		@Override
	    protected String doInBackground(String... params) {
	        String retval = null;
	        try
	           {
	             // create a url object
	        	 String query = params[0];
	             //URL url = new URL("http://cs-server.usc.edu:12277/examples/servlet/hw7Servlet?" + URLEncoder.encode(aURL, "UTF-8"));
	             String url = "http://cs-server.usc.edu:12277/examples/servlet/hw7Servlet?" + query;//URLEncoder.encode(query, "UTF-8");
	             System.out.println(url);

	             // create a urlconnection object
	             HttpClient httpclient = new DefaultHttpClient();
	             
	             // make GET request to the given URL
	             HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
	  
	             // receive response as inputStream
	             InputStream inputStream = httpResponse.getEntity().getContent();
	  
	             // convert inputstream to string
	             if(inputStream != null)
	            	 retval = inputStreamToString(inputStream);
	             else
	            	 retval = "No Data";
	           }
	           catch(Exception e)
	           {
	        	   Log.d("InputStream", e.getLocalizedMessage());
	           }
	           return retval;

	    }
		
		private String inputStreamToString(InputStream inputStream) throws IOException{
	        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
	        String line = "";
	        String retval = "";
	        while((line = bufferedReader.readLine()) != null)
	        	retval += line;
	 
	        inputStream.close();
	        return retval;
	 
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	// result is what you got from your connection
	    	String message = "";
	    	try {
				jsonString = new JSONObject(result);
				//message = jsonString.getJSONObject("weather").getString("feed");
				
				if("0".equals(jsonString.getJSONObject("weather").getString("feed"))){
					//Weather Information cannot be found!
					//make text views invisible
					hideDynamicArea();
									
					((TextView)findViewById(R.id.regionTV)).setText("Weather Information cannot be found!");
					findViewById(R.id.regionTV).setVisibility(View.VISIBLE);
				}
				else{
					TextView cityTV = (TextView) findViewById(R.id.cityTV);
					String cityName = jsonString.getJSONObject("weather").getJSONObject("location").getString("city");
					cityTV.setText(cityName);
					cityTV.setVisibility(View.VISIBLE);
					
					String region = jsonString.getJSONObject("weather").getJSONObject("location").getString("region");
					region += ", " + jsonString.getJSONObject("weather").getJSONObject("location").getString("country");
					TextView regionTV = (TextView) findViewById(R.id.regionTV);
					regionTV.setText(region);
					regionTV.setVisibility(View.VISIBLE);
					
					String imageURL = jsonString.getJSONObject("weather").getString("img");
					ImageView imageV = (ImageView) findViewById(R.id.imageV);				
					imageV.setTag(imageURL);
					DownloadImagesTask downloadImage = new DownloadImagesTask(); 
					downloadImage.execute(imageV);
					imageV.setVisibility(View.VISIBLE);
					
					String text = jsonString.getJSONObject("weather").getJSONObject("condition").getString("text");
					TextView textTV = (TextView) findViewById(R.id.textTV);
					textTV.setText(text);
					textTV.setVisibility(View.VISIBLE);
					
					char degree = '\u00B0';
					String currentTemp = jsonString.getJSONObject("weather").getJSONObject("condition").getString("temp");
					currentTemp += degree+jsonString.getJSONObject("weather").getJSONObject("units").getString("temperature");
					TextView tempTV = (TextView) findViewById(R.id.tempTV);
					tempTV.setText(currentTemp);
					tempTV.setVisibility(View.VISIBLE);
					
					//Feeding the values in the table
					ArrayList<ArrayList<String>> days = new ArrayList<ArrayList<String>>();
					
					String tempU = degree + jsonString.getJSONObject("weather").getJSONObject("units").getString("temperature");
					for(int i = 0 ; i < 5; i++){
						ArrayList<String> tempA = new ArrayList<String>();
						tempA.add(jsonString.getJSONObject("weather").getJSONArray("forecast").getJSONObject(i).getString("day"));
						tempA.add(jsonString.getJSONObject("weather").getJSONArray("forecast").getJSONObject(i).getString("text"));
						tempA.add(jsonString.getJSONObject("weather").getJSONArray("forecast").getJSONObject(i).getString("high"));
						tempA.add(jsonString.getJSONObject("weather").getJSONArray("forecast").getJSONObject(i).getString("low"));
						
						days.add(tempA);
					}
					
					String forecastString = "";
									
					//Row1
					TextView day1 = (TextView) findViewById(R.id.day1);
					day1.setText(days.get(0).get(0));
					TextView weather1 = (TextView) findViewById(R.id.weather1);
					weather1.setText(days.get(0).get(1));
					TextView high1 = (TextView) findViewById(R.id.high1);
					high1.setText(days.get(0).get(2)+tempU);
					TextView low1 = (TextView) findViewById(R.id.low1);
					low1.setText(days.get(0).get(3)+tempU);
					
					forecastString += days.get(0).get(0)+": "+days.get(0).get(1)+", " + days.get(0).get(2)+"/"+days.get(0).get(3)+tempU +"; ";
					
					//Row2
					TextView day2 = (TextView) findViewById(R.id.day2);
					day2.setText(days.get(1).get(0));
					TextView weather2 = (TextView) findViewById(R.id.weather2);
					weather2.setText(days.get(1).get(1));
					TextView high2 = (TextView) findViewById(R.id.high2);
					high2.setText(days.get(1).get(2)+tempU);
					TextView low2 = (TextView) findViewById(R.id.low2);
					low2.setText(days.get(1).get(3)+tempU);
					
					forecastString += days.get(1).get(0)+": "+days.get(1).get(1)+", " + days.get(1).get(2)+"/"+days.get(1).get(3)+tempU +"; ";
					
					//Row3
					TextView day3 = (TextView) findViewById(R.id.day3);
					day3.setText(days.get(2).get(0));
					TextView weather3 = (TextView) findViewById(R.id.weather3);
					weather3.setText(days.get(2).get(1));
					TextView high3 = (TextView) findViewById(R.id.high3);
					high3.setText(days.get(2).get(2)+tempU);
					TextView low3 = (TextView) findViewById(R.id.low3);
					low3.setText(days.get(2).get(3)+tempU);
					
					forecastString += days.get(2).get(0)+": "+days.get(2).get(1)+", " + days.get(2).get(2)+"/"+days.get(2).get(3)+tempU +"; ";
									
					//Row4
					TextView day4 = (TextView) findViewById(R.id.day4);
					day4.setText(days.get(3).get(0));
					TextView weather4 = (TextView) findViewById(R.id.weather4);
					weather4.setText(days.get(3).get(1));
					TextView high4 = (TextView) findViewById(R.id.high4);
					high4.setText(days.get(3).get(2)+tempU);
					TextView low4 = (TextView) findViewById(R.id.low4);
					low4.setText(days.get(3).get(3)+tempU);
	
					forecastString += days.get(3).get(0)+": "+days.get(3).get(1)+", " + days.get(3).get(2)+"/"+days.get(3).get(3)+tempU +"; ";
					
					//Row5
					TextView day5 = (TextView) findViewById(R.id.day5);
					day5.setText(days.get(4).get(0));
					TextView weather5 = (TextView) findViewById(R.id.weather5);
					weather5.setText(days.get(4).get(1));
					TextView high5 = (TextView) findViewById(R.id.high5);
					high5.setText(days.get(4).get(2)+tempU);
					TextView low5 = (TextView) findViewById(R.id.low5);
					low5.setText(days.get(4).get(3)+tempU);
					
					forecastString += days.get(4).get(0)+": "+days.get(4).get(1)+", " + days.get(4).get(2)+"/"+days.get(4).get(3)+tempU;
	
					
					findViewById(R.id.dataTable).setVisibility(View.VISIBLE);
					
					findViewById(R.id.shareCurrent).setVisibility(View.VISIBLE);
					findViewById(R.id.shareForecast).setVisibility(View.VISIBLE);
					
					feedDetails = new ArrayList<ArrayList<String>>();
					ArrayList<String> pcw = new ArrayList<String>();
					ArrayList<String> pwf = new ArrayList<String>();
					
					pcw.add(cityName +", "+ region);
					pwf.add(cityName +", "+ region);
					
					pcw.add("The current condition for "+cityName+" is "+text+".");
					pwf.add("Weather Forecast for "+cityName+".");
					
					pcw.add("Temperature is "+currentTemp);
					pwf.add(forecastString);
					
					pcw.add(jsonString.getJSONObject("weather").getString("feed"));
					pwf.add(jsonString.getJSONObject("weather").getString("feed"));
					
					pcw.add(jsonString.getJSONObject("weather").getString("img"));
					pwf.add("http://www-scf.usc.edu/~csci571/2013Fall/hw8/weather.jpg");
					
					pcw.add(jsonString.getJSONObject("weather").getString("link"));
					pwf.add(jsonString.getJSONObject("weather").getString("link"));
					
					feedDetails.add(pcw);
					feedDetails.add(pwf);
				}
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				message = "There was a problem retrieving the data";
				Log.d("InputStream", "There was a problem retrieving the data");
				
				hideDynamicArea();
				Toast toast = Toast.makeText(getApplicationContext(),
						message,
	                    Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 300);
				toast.show();
			}
	    	
	    }

	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		  super.onActivityResult(requestCode, resultCode, data);
		  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	private void postWeatherInfo() throws JSONException {
	    Bundle params = new Bundle();
	    params.putString("name", feedDetails.get(feedIndex).get(0));
	    params.putString("caption", feedDetails.get(feedIndex).get(1));
	    params.putString("description", feedDetails.get(feedIndex).get(2));
	    params.putString("link", feedDetails.get(feedIndex).get(3));
	    params.putString("picture", feedDetails.get(feedIndex).get(4));
	    //String tempstr = "{ \"Look at details: \" : { \"text\":\"here\" ,\"href\" :"+feedDetails.get(feedIndex).get(5)+" }}";
	    //params.putString("properties", tempstr);
	    JSONObject property = new JSONObject(); 
	    property.put("text", "here"); 
	    property.put("href", feedDetails.get(feedIndex).get(5)); 
	    JSONObject properties = new JSONObject(); 
	    properties.put("Look at details", property); 
	    params.putString("properties", properties.toString());


	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(getApplicationContext(),
	                            "Posted story, id: "+postId,
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(getApplicationContext().getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(getApplicationContext().getApplicationContext(), 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(getApplicationContext().getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
	public void hideDynamicArea(){
		findViewById(R.id.cityTV).setVisibility(View.INVISIBLE);
		findViewById(R.id.regionTV).setVisibility(View.INVISIBLE);
		findViewById(R.id.imageV).setVisibility(View.INVISIBLE);
		findViewById(R.id.textTV).setVisibility(View.INVISIBLE);
		findViewById(R.id.tempTV).setVisibility(View.INVISIBLE);
		findViewById(R.id.dataTable).setVisibility(View.INVISIBLE);
		findViewById(R.id.shareCurrent).setVisibility(View.INVISIBLE);
		findViewById(R.id.shareForecast).setVisibility(View.INVISIBLE);
	}
}
