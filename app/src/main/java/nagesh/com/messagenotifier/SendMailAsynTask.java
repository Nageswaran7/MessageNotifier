package nagesh.com.messagenotifier;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by nmuthusubramaniya on 08-Apr-17.
 */

    public class SendMailAsynTask extends AsyncTask<Void, Void, Void> {

    private Context context;

    public SendMailAsynTask(Context context){
        this.context=context;
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
    }
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... mApi) {
            try {
                GMailSender sender = new GMailSender("mnageswaran7@gmail.com", "Nagesh@123");
                sender.sendMail("Mail Subject..", " Mail Body..", "mnageswaran7@gmail.com", "mnageswaran@outlook.com");


            }

            catch (Exception ex) {
                Log.e("Messages","Error"+ex);
                    ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.cancel();
            Toast.makeText(context, "Email send", Toast.LENGTH_SHORT).show();
        }
    }


