package in.pocketwise.app;
import android.content.Context;
import org.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
public class Ai {
 static final java.util.concurrent.ExecutorService queue=Executors.newSingleThreadExecutor();
 static void classify(Context context,JSONObject t,Runnable done){
  Context c=context.getApplicationContext();
  if(t.optBoolean("incoming") || !Store.settings(c).optBoolean("aiConsent",false)){if(done!=null)done.run();return;}
  final String id=t.optString("id");
  queue.execute(()->{
   HttpsURLConnection conn=null;
   try{
    JSONObject settings=Store.settings(c);
    String base=settings.optString("backend");if(!base.startsWith("https://"))throw new Exception();
    conn=(HttpsURLConnection)new URL(base.replaceAll("/+$","")+"/classify").openConnection();
    conn.setRequestMethod("POST");conn.setConnectTimeout(10000);conn.setReadTimeout(45000);conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type","application/json");conn.setRequestProperty("Authorization","Bearer "+settings.optString("token"));
    String merchant=t.optString("merchant").replaceAll("[\\w.+-]+@[\\w.-]+","[UPI]").replaceAll("\\d{4,}","[number]");
    byte[] body=new JSONObject().put("merchant",merchant).put("note","").toString().getBytes(StandardCharsets.UTF_8);
    conn.getOutputStream().write(body);
    if(conn.getResponseCode()!=200)throw new Exception();
    java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream();
    try(java.io.InputStream stream=conn.getInputStream()){byte[] chunk=new byte[2048];int count;while((count=stream.read(chunk))!=-1){output.write(chunk,0,count);if(output.size()>16384)throw new Exception();}}
    String response=output.toString("UTF-8");
    JSONObject r=new JSONObject(response);
    synchronized(Store.class){
     JSONArray all=Store.read(c).optJSONArray("transactions");
     for(int i=0;i<all.length();i++){JSONObject current=all.optJSONObject(i);
      if(current.optString("id").equals(id) && !current.optBoolean("corrected")){
       current.put("category",r.getString("category")).put("evidence",r.optString("reason")+" · "+r.optString("provider")+" / "+r.optString("model"));
      }
     }Store.save(c);
    }
   }catch(Exception e){/* Preserve local categorization when offline. */}
   finally{if(conn!=null)conn.disconnect();if(done!=null)done.run();}
  });
 }
}
