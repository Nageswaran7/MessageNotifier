package nagesh.com.messagenotifier;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final int RC_SIGN_IN = 007;
    SessionManager session;
    Button login;
    TextView emailTv,login_form_skip;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton googleSignIn_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        session=new SessionManager(getApplicationContext());
        login=(Button)this.findViewById(R.id.email_sign_in_button);
        emailTv=(TextView)this.findViewById(R.id.email);
        googleSignIn_btn=(SignInButton)this.findViewById(R.id.googleSignIn_btn);
        login_form_skip=(TextView)this.findViewById(R.id.login_form_skip);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=emailTv.getText().toString().trim();
                String name="";
                Log.d("Message",email+"----"+name);
                session.createLoginSession(name,email);
                Intent i = new Intent(getApplicationContext(), SmsActivity.class);
                startActivity(i);
                finish();
            }
        });
        login_form_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogInSkipDialog();
            }
        });
        googleSignIn_btn.setOnClickListener(this);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.googleSignIn_btn:
                signIn();
                break;
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Message", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e("Message", "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            String email = acct.getEmail();

            Log.e("Message", "Name: " + personName + ", email: " + email
                    );
            session.createLoginSession(personName,email);
            Intent i = new Intent(getApplicationContext(), SmsActivity.class);
            startActivity(i);
            finish();

        } else {
            // Signed out, show unauthenticated UI.
            Log.e("Message", "Sign In Refused" );
            showLogInSkipDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public void showLogInSkipDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("LogIn Is required to get SMS Notification on E-mail. Do you want to continue?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), SmsActivity.class);
                i.putExtra("skip",true);
                startActivity(i);
                finish();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed No button. Write Logic Here
                Toast.makeText(getApplicationContext(), "Enter E-mail Id or use Social Login", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }
}
