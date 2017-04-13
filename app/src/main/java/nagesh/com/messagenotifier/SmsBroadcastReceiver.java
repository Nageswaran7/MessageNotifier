package nagesh.com.messagenotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    SessionManager session;
    public static final String SMS_BUNDLE = "pdus";
    String smsBody="";
    String address="";
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        session=new SessionManager(context);
        HashMap<String, String> userDetails=session.getUserDetails();
            if (intentExtras != null) {
                Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
                String smsMessageStr = "";
                for (int i = 0; i < sms.length; ++i) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                    smsBody = smsMessage.getMessageBody().toString();
                    address = smsMessage.getOriginatingAddress();

                    //smsMessageStr += "SMS Received is - - -  From: " + address + "\n";
                    //  smsMessageStr += smsBody + "\n \n \n - An App by Nagesh";
                    smsMessageStr = getMailBody(userDetails.get(SessionManager.KEY_NAME), address, smsBody,context);
                }

                Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

                Log.d("Message", smsMessageStr);
                if(!userDetails.get(SessionManager.KEY_EMAIL).trim().equals("")) {
                    Log.d("Message","Inside the Mail");
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                            Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    try {
               /* new SendMailAsynTask(context.getApplicationContext()).execute();*/
                        GMailSender sender = new GMailSender("mnageswaran7@gmail.com", "Nagesh@123");
                        sender.sendMail("New SMS Received From - " + address+" ("+Util.getContactName(context,address)+")", smsMessageStr, "mnageswaran7@gmail.com", userDetails.get(SessionManager.KEY_EMAIL).trim());

                    } catch (Exception ex) {
                        Log.e("Messages", ex + "");
                        ex.printStackTrace();
                        Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                Log.d("Message", "Message Email End");
                //this will update the UI with message
                SmsActivity inst = SmsActivity.instance();
                inst.updateList(smsMessageStr);
            }
    }
    public String getMailBody(String name,String from,String smsBody,Context context)
    {
        String mailBody="";
        String salName=name.equals("")?",":" "+name+",";
        mailBody+="Hi"+salName+"\n";
        mailBody+="Sender : "+from+" ("+Util.getContactName(context,address)+")\nMessage : "+ smsBody+"\n \n \n - An App by Nagesh";;
        return mailBody;
    }
}