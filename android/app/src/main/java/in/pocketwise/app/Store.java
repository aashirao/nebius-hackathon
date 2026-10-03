package in.pocketwise.app;
import android.content.Context;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
public class Store {
 private static JSONObject state;
 static synchronized JSONObject read(Context c) {
  if(state==null) {try{state=new JSONObject(c.getSharedPreferences("pocketwise",0).getString("state","{}"));}catch(Exception e){state=new JSONObject();}}
  try {if(!state.has("transactions"))state.put("transactions",new JSONArray());if(!state.has("goals"))state.put("goals",new JSONArray());if(!state.has("settings"))state.put("settings",new JSONObject());}catch(Exception ignored){}
  return state;
 }
 static synchronized void save(Context c) {c.getSharedPreferences("pocketwise",0).edit().putString("state",read(c).toString()).commit();}
 static JSONObject settings(Context c){return read(c).optJSONObject("settings");}
 static long balance(Context c){long n=settings(c).optLong("opening",0);JSONArray a=read(c).optJSONArray("transactions");for(int i=0;i<a.length();i++){JSONObject t=a.optJSONObject(i);n+=t.optBoolean("incoming")?t.optLong("paise"):-t.optLong("paise");}return n;}
 static long allocated(Context c){long n=0;JSONArray a=read(c).optJSONArray("goals");for(int i=0;i<a.length();i++)n+=a.optJSONObject(i).optLong("saved");return n;}
 static String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x));return v.toString();}catch(Exception e){throw new RuntimeException(e);}}
 static synchronized String ingest(Context c,String text,String source,long time) {
  TransactionParser.Parsed p=TransactionParser.parse(text);if(p==null)return "Unsupported or non-final transaction; not imported";
  JSONArray a=read(c).optJSONArray("transactions");
  String id=hash(p.reference().isEmpty()?source+text:p.reference()+p.incoming());
  for(int i=0;i<a.length();i++){JSONObject old=a.optJSONObject(i);if(old.optString("id").equals(id))return "Already imported";
   if(p.reference().isEmpty() && old.optString("merchant").equals(p.merchant()) && old.optLong("paise")==p.paise() && old.optBoolean("incoming")==p.incoming() && Math.abs(old.optLong("time")-time)<120000)return "Possible duplicate skipped; check statement";}
  String category=p.incoming()?"Other incoming":TransactionParser.category(p.merchant());
  JSONObject s=settings(c);String m=p.merchant().toLowerCase(Locale.ROOT);
  if(p.incoming())for(String type:new String[]{"guardian","pocket","stipend"}){String sender=s.optString(type+"Sender").trim().toLowerCase(Locale.ROOT);if(!sender.isEmpty() && m.equals(sender)){category=type.equals("guardian")?"Guardian support":type.equals("pocket")?"Pocket money":"Stipend";break;}}
  try{JSONObject t=new JSONObject().put("id",id).put("paise",p.paise()).put("incoming",p.incoming()).put("merchant",p.merchant()).put("time",time).put("category",category).put("evidence","Local rule / sender match");a.put(t);save(c);Ai.classify(c,t,null);return p.incoming()?"Incoming money recorded. Your goals are ready to fund.":"Expense recorded";}catch(Exception e){return "Import failed";}
 }
}
