package in.pocketwise.app;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.*;
public final class TransactionParser {
 public record Parsed(long paise, boolean incoming, String merchant, String reference) {}
 public static Parsed parse(String text) {
  if(text==null || text.length()>4000) return null;
  String s=text.toLowerCase(Locale.ROOT);
  if(s.matches("(?s).*\\b(otp|verification|failed|declined|pending|requested|request|reversed|reversal|refund)\\b.*")) return null;
  boolean incoming=s.matches("(?s).*\\b(credited|received)\\b.*");
  boolean outgoing=s.matches("(?s).*\\b(debited|paid|sent)\\b.*");
  if(incoming==outgoing) return null;
  Matcher amount=Pattern.compile("(?:₹|INR\\s*|Rs\\.?\\s*)([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",Pattern.CASE_INSENSITIVE).matcher(text);
  if(!amount.find()) return null;
  long paise;
  try { paise=new BigDecimal(amount.group(1).replace(",", "")).movePointRight(2).longValueExact(); } catch(Exception e){return null;}
  if(paise<=0 || paise>10000000000L) return null;
  Matcher merchant=Pattern.compile("\\b"+(incoming?"from":"to")+"\\s+(.+?)(?=\\s+(?:on|via|ref|UPI ref|UTR|at)\\b|$)",Pattern.CASE_INSENSITIVE).matcher(text);
  String name=merchant.find()?merchant.group(1).trim():"Unknown";
  Matcher ref=Pattern.compile("\\b(?:UTR|(?:UPI\\s*)?ref(?:erence)?(?:\\s*no)?)[\\s:#.-]*([A-Za-z0-9]{6,})",Pattern.CASE_INSENSITIVE).matcher(text);
  return new Parsed(paise,incoming,name,ref.find()?ref.group(1):"");
 }
 public static String category(String merchant) {
  String s=merchant.toLowerCase(Locale.ROOT);
  if(s.matches(".*\\b(supermarket|grocery|groceries|swiggy|zomato)\\b.*")) return "Food & groceries";
  if(s.matches(".*\\b(uber|ola|rapido|metro)\\b.*")) return "Travel";
  return "Needs review";
 }
 public static long available(long balance,long allocated){return Math.max(0,balance-allocated);}
 public static boolean canAllocate(long amount,long balance,long allocated){return amount>0 && amount<=available(balance,allocated);}
}
