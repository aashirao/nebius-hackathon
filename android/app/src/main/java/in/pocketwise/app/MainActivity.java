package in.pocketwise.app;
import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
public class MainActivity extends Activity {
 LinearLayout root; final int ink=Color.rgb(21,31,49), blue=Color.rgb(33,77,233);
 @Override public void onCreate(Bundle b){super.onCreate(b);home();}
 @Override public void onResume(){super.onResume();home();}
 LinearLayout page(String title){ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(28,48,28,48);root.setBackgroundColor(Color.rgb(245,247,253));scroll.addView(root);setContentView(scroll);text(title,30,true);return root;}
 void text(String value,int size,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(ink);t.setPadding(0,12,0,12);if(bold)t.setTypeface(null,Typeface.BOLD);root.addView(t);}
 void button(String value,Runnable action){Button b=new Button(this);b.setText(value);b.setTextColor(blue);root.addView(b);b.setOnClickListener(v->action.run());}
 EditText input(LinearLayout parent,String label,String value){TextView t=new TextView(this);t.setText(label);t.setTextSize(16);parent.addView(t);EditText e=new EditText(this);e.setText(value);e.setTextSize(16);e.setSingleLine(true);parent.addView(e);return e;}
 String money(long p){return NumberFormat.getCurrencyInstance(new Locale("en","IN")).format(p/100.0);}
 long amount(String v){long n=new BigDecimal(v.trim()).movePointRight(2).longValueExact();if(n<0 || n>10000000000L)throw new IllegalArgumentException();return n;}
 void toast(String v){Toast.makeText(this,v,Toast.LENGTH_LONG).show();}
 void home(){page("Pocketwise");text("Money for student life",16,false);long balance=Store.balance(this),saved=Store.allocated(this);text(money(TransactionParser.available(balance,saved)),38,true);text("Available after your savings goals",16,false);text("Tracked balance  "+money(balance)+"\nSet aside  "+money(saved),18,false);if(balance<saved)text("Your tracked balance is below your goals. Release some savings or reconcile missing transactions.",16,true);text("Based on captured transactions; check against your bank balance.",14,false);
  button("Income & capture settings",this::settings);button("Savings goals",this::goals);button("Import a transaction",this::importDialog);button("Refresh",this::home);
  text("Recent transactions",23,true);JSONArray a=Store.read(this).optJSONArray("transactions");if(a.length()==0)text("No transactions yet. Set your opening balance, then enable capture or import a sample.",16,false);
  for(int i=a.length()-1;i>=Math.max(0,a.length()-40);i--){JSONObject t=a.optJSONObject(i);button((t.optBoolean("incoming")?"+":"−")+money(t.optLong("paise"))+"  "+t.optString("merchant")+"\n"+t.optString("category"),()->details(t));}
 }
 void details(JSONObject t){new AlertDialog.Builder(this).setTitle(t.optString("merchant")).setMessage(t.optString("category")+"\n"+t.optString("evidence")).setPositiveButton("Correct category",(d,w)->{
  String[] cats=t.optBoolean("incoming")?new String[]{"Guardian support","Pocket money","Stipend","Refund","Other incoming"}:new String[]{"Food & groceries","Travel","Friend repayment","Shopping","Education","Other","Needs review"};
  new AlertDialog.Builder(this).setTitle("Choose category").setItems(cats,(dd,which)->{try{t.put("category",cats[which]).put("corrected",true).put("evidence","Corrected by you");Store.save(this);home();}catch(Exception ignored){}}).show();
 }).setNeutralButton("Retry AI",(d,w)->{Ai.classify(this,t,()->runOnUiThread(this::home));toast("AI retry requested if consent and connection are configured");}).setNegativeButton("Close",null).show();}
 void importDialog(){LinearLayout fields=new LinearLayout(this);fields.setOrientation(1);fields.setPadding(24,12,24,12);EditText entry=input(fields,"Paste a completed transaction notification","");entry.setSingleLine(false);entry.setMinLines(3);
  new AlertDialog.Builder(this).setTitle("Transaction import").setView(fields).setPositiveButton("Import",(d,w)->{toast(Store.ingest(this,entry.getText().toString(),"manual",System.currentTimeMillis()));home();}).setNeutralButton("Sample ₹20 expense",(d,w)->{toast(Store.ingest(this,"INR 20 paid to Campus Supermarket via UPI ref DEMO000020","demo",System.currentTimeMillis()));home();}).setNegativeButton("Cancel",null).show();
 }
 void goals(){page("Your savings goals");text("Virtual envelopes. Money stays in your bank account. Add any amount whenever you have money available.",16,false);button("Create a goal",()->{
  LinearLayout box=new LinearLayout(this);box.setOrientation(1);EditText name=input(box,"Goal name","");EditText target=input(box,"Target in rupees","");new AlertDialog.Builder(this).setTitle("New goal").setView(box).setPositiveButton("Create",(d,w)->{try{long v=amount(target.getText().toString());String n=name.getText().toString().trim();if(v<=0||n.isEmpty())throw new Exception();Store.read(this).optJSONArray("goals").put(new JSONObject().put("name",n).put("target",v).put("saved",0));Store.save(this);goals();}catch(Exception e){toast("Enter a name and a positive target");}}).setNegativeButton("Cancel",null).show();});
  JSONArray a=Store.read(this).optJSONArray("goals");for(int i=0;i<a.length();i++){JSONObject g=a.optJSONObject(i);text(g.optString("name"),22,true);text(money(g.optLong("saved"))+" of "+money(g.optLong("target")),18,false);ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(100);bar.setProgress((int)Math.min(100,g.optLong("saved")*100/Math.max(1,g.optLong("target"))));root.addView(bar);button("Add or release savings",()->allocation(g));}button("Back",this::home);
 }
 void allocation(JSONObject g){EditText e=new EditText(this);e.setHint("Amount in rupees");new AlertDialog.Builder(this).setTitle(g.optString("name")).setView(e).setPositiveButton("Set aside",(d,w)->{try{long n=amount(e.getText().toString());synchronized(Store.class){if(!TransactionParser.canAllocate(n,Store.balance(this),Store.allocated(this)))throw new Exception();g.put("saved",g.optLong("saved")+n);Store.save(this);}goals();}catch(Exception x){toast("Enter a positive amount within your available balance");}}).setNeutralButton("Release",(d,w)->{try{long n=amount(e.getText().toString());synchronized(Store.class){if(n<=0||n>g.optLong("saved"))throw new Exception();g.put("saved",g.optLong("saved")-n);Store.save(this);}goals();}catch(Exception x){toast("Amount exceeds this goal’s savings");}}).setNegativeButton("Cancel",null).show();}
 void settings(){page("Income & settings");JSONObject s=Store.settings(this);text("All income sources are optional. A schedule never adds money until a transaction arrives. You can use several sources together.",16,false);
  EditText opening=input(root,"Opening balance before the first imported payment (₹)",String.valueOf(s.optLong("opening")/100.0));
  text("Pocket money",22,true);EditText pocketSender=input(root,"Exact sender name / UPI (blank to disable)",s.optString("pocketSender"));EditText pocketAmount=input(root,"Expected amount ₹ (optional)",s.optString("pocketAmount"));
  text("Guardian support · whenever needed",22,true);EditText guardian=input(root,"Exact guardian name / UPI (blank to disable)",s.optString("guardianSender"));
  text("Stipend",22,true);EditText stipend=input(root,"Exact stipend sender (blank to disable)",s.optString("stipendSender"));EditText stipendAmount=input(root,"Expected amount ₹ (optional)",s.optString("stipendAmount"));EditText months=input(root,"Every how many months?",s.optString("months","1"));
  text("Automatic capture",22,true);text("Only selected apps are processed. Notification formats vary; missing notifications and unsupported formats require import. OTPs and non-final payments are ignored.",16,false);
  EditText packages=input(root,"Allowed Android package names, comma separated",s.optString("packages"));CheckBox capture=new CheckBox(this);capture.setText("Enable local transaction capture");capture.setChecked(s.optBoolean("capture"));root.addView(capture);
  button("Open Android notification permission",()->startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
  text("Optional AI categorization",22,true);text("With your consent, a merchant label is sent to your backend and Nebius. UPI handles and long numbers are redacted. No account balance, OTP or full notification is sent. Merchant names may still identify people.",16,false);
  EditText backend=input(root,"HTTPS backend URL",s.optString("backend"));EditText token=input(root,"Backend access token (not the Nebius key)",s.optString("token"));token.setInputType(129);CheckBox consent=new CheckBox(this);consent.setText("Allow merchant labels to be processed by Nebius");consent.setChecked(s.optBoolean("aiConsent"));root.addView(consent);
  button("Save settings",()->{try{long v=amount(opening.getText().toString());int interval=Integer.parseInt(months.getText().toString());if(interval<1||interval>120)throw new Exception();for(EditText amt:new EditText[]{pocketAmount,stipendAmount})if(!amt.getText().toString().trim().isEmpty())amount(amt.getText().toString());String url=backend.getText().toString().trim();if(!url.isEmpty()&&!url.startsWith("https://"))throw new Exception();s.put("opening",v).put("pocketSender",pocketSender.getText().toString().trim()).put("pocketAmount",pocketAmount.getText().toString()).put("guardianSender",guardian.getText().toString().trim()).put("stipendSender",stipend.getText().toString().trim()).put("stipendAmount",stipendAmount.getText().toString()).put("months",String.valueOf(interval)).put("packages",packages.getText().toString()).put("capture",capture.isChecked()).put("backend",url).put("token",token.getText().toString()).put("aiConsent",consent.isChecked());Store.save(this);home();}catch(Exception e){toast("Check amounts, month interval and HTTPS URL");}});button("Back",this::home);
 }
}
