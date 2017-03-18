package local.medstream.patappointment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class otp extends AppCompatActivity {

    //Declare Variables
    TextView dadid;
    TextView phadid;
    Button cresend, cnext;
    private Context context;
    private String adid;
    EditText etcotpno;
    ProgressBar bar;
    //Declare End

    //Declare Connection Variables
    Connection connection;
    String un, pass, db, ip;
    //Connection Variables Declared

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        //Declaring Server Details
        ip = "192.168.1.10";
        db = "PatientApp";
        un = "sa";
        pass = "sql@2012";
        //end declare

        //unique id
        String z = "";
        phadid = (TextView) findViewById(R.id.tvpuid);
        adid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        phadid.setText(adid);
        bar=(ProgressBar)findViewById(R.id.pbar);
        //get values
        cnext = (Button) findViewById(R.id.bnext);
        cresend = (Button) findViewById(R.id.btnresend);
        etcotpno = (EditText) findViewById(R.id.etotp);
        dadid = (TextView) findViewById(R.id.tvdotp);
        //End Gathering Variables


        //query for otp on uniqueid

        try {
            connection=connectionclass(un,pass,db,ip);
            if(connection==null)
            {
                z="Check your internet connection no internet connection found!";
            }
            else {
                String getotp = "Select PatOtp from patdetails where UniqueID = '" + adid.toString() + "' and OtpVerification = 'N'";
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(getotp);
                ArrayList<String> dotp = new ArrayList<String>();
                while (rs.next()) {
                    String otp = rs.getString("PatOtp");
                    dotp.add(otp);
                    dadid.setText(otp);
                }
                    /*String[] array = dotp.toArray(new String[0]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(otp.this, android.R.layout.simple_list_item_1, dotp);
                    dadid.setAdapter(adapter);*/
                //}
            }

        } catch (SQLException ex) {
            z=ex.getMessage();
        }
        //button function
        cnext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Updateotp updateotp = new Updateotp();
                updateotp.execute("");
            }
        });
        cresend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resendotp resendotp = new Resendotp();
                resendotp.execute("");
            }
        });
    }
    public class Updateotp extends AsyncTask<String, String, String> {
        String z="";
        String m="";
        String cotp=etcotpno.getText().toString();
        String dotp = dadid.getText().toString();
        String puid = adid;
        Boolean isSuccess=false;
        Toast eotp = Toast.makeText(otp.this,"Please Insert Otp number to proceed further",Toast.LENGTH_LONG);
        Toast wotp=Toast.makeText(otp.this,"Wrong OTP entered please enter correct otp number",Toast.LENGTH_LONG);
        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(String r){
            bar.setVisibility(View.GONE);
            Toast.makeText(otp.this,r,Toast.LENGTH_LONG).show();
            if(isSuccess)
            {

            }
        }
        @Override
        protected  String  doInBackground(String...params){
            if(cotp.equals("")){
                eotp.show();
                return m;
            }
            else {
                try {
                    //connection=connectionclass(un,pass,db,ip);
                    if (connection == null) {
                        z = "No internet connection found please check your connection!";
                    } else {
                        if (dotp.equals(cotp)) {
                            String verify = "update PatDetails set OtpVerification='Y' where UniqueID='" + puid.toString() + "' and OtpVerification = 'N' and PatOtp = '" + dotp.toString() + "'";
                            PreparedStatement stmt = connection.prepareStatement(verify);
                            stmt.executeUpdate();
                        } else {
                            wotp.show();
                        }
                        isSuccess=true;
                    }
                }
                catch (Exception ex)
                {
                    isSuccess=false;
                    z=ex.getMessage();
                }
            }
            return (z);
        }
    }
    public class Resendotp extends AsyncTask<String,String,String>
    {
        String w="";
        String y="";
        String phuid =adid;
        String notp="";
        String cno ="";
        Boolean isSuccess=false;
        ArrayList gotp = new ArrayList();
        @Override
        protected void onPreExecute(){bar.setVisibility(View.VISIBLE);}
        @Override
        protected void onPostExecute(String r){
            Toast.makeText(otp.this,r,Toast.LENGTH_SHORT).show();
            if(isSuccess){
                Intent i = new Intent(otp.this,otp.class);
                startActivity(i);
                finish();
            }
        }
        @Override
        protected String doInBackground(String...params){
            try {
                if(connection==null){
                    w="No internet connection found please check your connection!";
                }
                else {
                    String uotp="declare @patotp as int set @patotp= (SELECT CAST((RAND() * (89999) + 10000) as int))update PatDetails set PatOtp = @patotp where OtpVerification='N' and UniqueID='"+phuid.toString()+"'";
                    PreparedStatement stmt = connection.prepareStatement(uotp);
                    stmt.executeUpdate();
                    //Send new otp as msg
                    String getotp="select patotp,Patcno from PatDetails where UniqueID='"+phuid.toString()+"'";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(getotp);
                    while (resultSet.next()){
                        notp=resultSet.getString("patotp");
                        cno=resultSet.getString("Patcno");
                    }
                }
                String user = "&username=" + URLEncoder.encode("parthbheda.pb@gmail.com","UTF-8");
                String hash = "&hash=" + URLEncoder.encode("0a2a1420b38bc4b75bdfeba4403f0656295a9b78", "UTF-8");
                //String message = "&message=" + URLEncoder.encode("This is your one-time password" +dotp+".\\r\\n\\r\\n Thank you,", "UTF-8");
                String message = "&message="+URLEncoder.encode("This is your one-time password "+notp+". Thank you, From Medstream Technologies.","UTF-8");
                //This is your one-time password %%|OTP^{"inputtype" : "text", "maxlength" : "5"}%%. Thank you, From Medstream Technologies.
                String sender = "&sender=" + URLEncoder.encode("MDSTHD", "UTF-8");
                //String sender = "&sender=" + URLEncoder.encode("TXTLCL", "UTF-8");
                String numbers = "&numbers=" + URLEncoder.encode(cno, "UTF-8");

                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://api.textlocal.in/send/?").openConnection();
                String data = user+hash+numbers+message+sender;
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Length",Integer.toString(data.length()));
                urlConnection.getOutputStream().write(data.getBytes("UTF-8"));
                final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                final StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line=reader.readLine()) !=null){
                    stringBuffer.append(line);
                }
                reader.close();
                isSuccess=true;
                return stringBuffer.toString();

            }
            catch (Exception ex){
                isSuccess=false;
                w=ex.getMessage();
            }
            return (w);
        }
    }

    @SuppressLint("NewApi")
    public Connection connectionclass(String user,String password, String database,String server)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection=null;
        String ConnectionURL=null;
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL="jdbc:jtds:sqlserver://"+ip+";"+"databaseName="+db+";user="+un+";password="+pass+";";
            connection= DriverManager.getConnection(ConnectionURL);
        }
        catch (SQLException se)
        {
            Log.e("error here 1:",se.getMessage());
        }
        catch (ClassNotFoundException ce)
        {
            Log.e("error here 2:",ce.getMessage());
        }
        catch (Exception e)
        {
            Log.e("error here 3:",e.getMessage());
        }
        return connection;
    }
}
