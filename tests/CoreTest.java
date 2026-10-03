import in.pocketwise.app.TransactionParser;
public class CoreTest {
 static void check(boolean ok){if(!ok)throw new AssertionError();}
 public static void main(String[] args){
  var p=TransactionParser.parse("INR 20 paid to Campus Supermarket via UPI ref 123456789012");check(p.paise()==2000 && !p.incoming());check(p.merchant().equals("Campus Supermarket"));check(p.reference().equals("123456789012"));
  check(TransactionParser.category(p.merchant()).equals("Food & groceries"));
  check(TransactionParser.category("Ravi Kumar").equals("Needs review"));
  var incoming=TransactionParser.parse("Rs. 5,000.50 received from Mom via UPI ref 234567890123");check(incoming.incoming() && incoming.paise()==500050 && incoming.merchant().equals("Mom"));
  check(TransactionParser.parse("INR 500 payment pending to Uber")==null);
  check(TransactionParser.parse("OTP 123456 for INR 500 paid to Uber")==null);
  check(TransactionParser.parse("INR 500 refund received from Uber")==null);
  check(TransactionParser.parse("INR 500 debited and credited")==null);
  check(TransactionParser.parse("INR 0 paid to Uber")==null);
  check(TransactionParser.canAllocate(10000,50000,40000));
  check(!TransactionParser.canAllocate(10001,50000,40000));
  check(!TransactionParser.canAllocate(-1,50000,0));
  check(TransactionParser.available(30000,40000)==0);
  System.out.println("13 core assertions passed");
 }
}
