package in.pocketwise.app;
import android.app.Notification;
import android.service.notification.*;
import java.util.Arrays;
public class TransactionListener extends NotificationListenerService {
 @Override public void onNotificationPosted(StatusBarNotification sbn){
  String packages=Store.settings(this).optString("packages","");
  if(!Store.settings(this).optBoolean("capture",false) || !Arrays.asList(packages.replace(" ","").split(",")).contains(sbn.getPackageName()))return;
  Notification n=sbn.getNotification();if(n.extras==null)return;
  CharSequence title=n.extras.getCharSequence(Notification.EXTRA_TITLE,"");
  CharSequence text=n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
  if(text==null)text=n.extras.getCharSequence(Notification.EXTRA_TEXT,"");
  Store.ingest(this,title+" "+text,sbn.getPackageName(),sbn.getPostTime());
 }
}
