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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.provider.Settings.Secure;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import static java.util.Calendar.getInstance;

/*import static android.icu.text.DateFormat.getDateTimeInstance;
import static android.icu.text.DateFormat.getInstance;*/

public class Register extends AppCompatActivity {
    //Declare Layout buttons, edittext
    Button next;
    EditText etname, etcno, etemail;
    ProgressBar bar;
    TextView adid;
    Spinner otpview;
    TelephonyManager telephonyManager;
    private Context context;
    private String androidID;
    //Declare End

    //Declare Connection Variables
    Connection connection;
    String un, pass, db, ip;
    //Connection variablesw Declare end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Imei practice
        adid = (TextView) findViewById(R.id.tdvadid);
        androidID = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        adid.setText(androidID);
        //get values
        next = (Button) findViewById(R.id.bnext);
        etname = (EditText) findViewById(R.id.etdname);
        etcno = (EditText) findViewById(R.id.etdcno);
        etemail = (EditText) findViewById(R.id.etdemail);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        //End Getting Variables

        //Declaring Server Details
        ip = "192.168.1.10";
        db = "PatientApp";
        un = "sa";
        pass = "sql@2012";
        //end declare

        //Set button function
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Adddtl adddtl = new Adddtl();
                adddtl.execute("");

            }
        });
        //End button function
    }

    public class Adddtl extends AsyncTask<String, String, String> {
        ArrayList otpdata = new ArrayList();
        String dotp="";
        String z = "";
        String m = "";
        String  name= etname.getText().toString();
        String cno = etcno.getText().toString();
        String email = etemail.getText().toString();
        String uid = androidID;
        Boolean isSuccess = false;
        Toast ntoast = Toast.makeText(Register.this, "Please Enter Your Full Name To Proceed Further", Toast.LENGTH_LONG);
        Toast ctoast = Toast.makeText(Register.this, "Please Enter Your Valid Mobile Number To Proceed Further", Toast.LENGTH_LONG);
        Toast etoast = Toast.makeText(Register.this, "Please Enter Your Email ID To Proceed Further", Toast.LENGTH_LONG);
        @Override
        protected void onPreExecute()
        {
            bar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(String r)
        {
            bar.setVisibility(View.GONE);
            Toast.makeText(Register.this,r,Toast.LENGTH_LONG).show();
            if(isSuccess)
            {

                startActivity(new Intent(Register.this,otp.class));
            }
        }
        @Override
        protected String doInBackground(String... params) {
            if (etname.equals("")) {
                ntoast.show();
                return m;
            } else if (etcno.equals("")) {
                ctoast.show();
                return m;
            } else if (etemail.equals("")) {
                etoast.show();
                return m;
            } else {
                try {
                    connection = connectionclass(un, pass, db, ip);//connect database
                    if (connection == null) {
                        z = "Check Your Internet Connection No Internet Found!";
                    } else {
                        String date = new java.text.SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH).format(getInstance().getTime());
                        Calendar calendar = Calendar.getInstance();
                        //SimpleDateFormat d = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                        java.sql.Date d = new java.sql.Date(calendar.getTime().getTime());
                        String chkdata;
                        String insdata;
                        //String insotp="SELECT CAST((RAND() * (899999) + 100000) as int)";
                        //insdata = "insert into Patdetails (Date,PatName,Patcno,Patemail,UniqueID)" + "values(?,?,?,?,?)";
                        insdata = "declare @patotp as int set @patotp= (SELECT CAST((RAND() * (89999) + 10000) as int)) Insert into PatDetails (Date,PatName,Patcno,Patemail,UniqueID,PatOtp)"+ "values (?,?,?,?,?,@patotp)";
                        PreparedStatement statement = connection.prepareStatement(insdata);
                        statement.setDate(1, d);
                        statement.setString(2, name);
                        statement.setString(3, cno);
                        statement.setString(4, email);
                        statement.setString(5,androidID);
                        //PreparedStatement otpStatement = connection.prepareStatement(insotp);
                        statement.executeUpdate();
                        //sendotp.class.getClass();
                        String getotp = "select top 1 PatOtp from PatDetails where Patcno = '" + cno.toString() + "' and OtpVerification = 'N'";
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery(getotp);
                        while (rs.next()) {
                            dotp = rs.getString("PatOtp");
                            otpdata.add(dotp);
                        }

                        String[] array = (String[]) otpdata.toArray(new String[0]);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(Register.this,android.R.layout.simple_list_item_1,otpdata);
                        //otpview.setAdapter(arrayAdapter);
                        //}
                        //Construct Data
                        String user = "username=" + URLEncoder.encode("parthbheda.pb@gmail.com", "UTF-8");
                        String hash = "&hash=" + URLEncoder.encode("0a2a1420b38bc4b75bdfeba4403f0656295a9b78", "UTF-8");
                        //String message = "&message=" + URLEncoder.encode("This is your one-time password" +dotp+".\\r\\n\\r\\n Thank you,", "UTF-8");
                        String message = "&message="+URLEncoder.encode("This is your one-time password "+dotp+". Thank you, From Medstream Technologies.","UTF-8");
                        //This is your one-time password %%|OTP^{"inputtype" : "text", "maxlength" : "5"}%%. Thank you, From Medstream Technologies.
                        String sender = "&sender=" + URLEncoder.encode("MDSTHD", "UTF-8");
                        //String sender = "&sender=" + URLEncoder.encode("TXTLCL", "UTF-8");
                        String numbers = "&numbers=" + URLEncoder.encode(cno, "UTF-8");

                        //Send date
                        /*String data = "http://api.textlocal.in/send/?" + user + hash + numbers + message + sender;
                        URL url = new URL(data);
                        URLConnection conn = url.openConnection();
                        conn.setDoOutput(true);*/
                        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://api.textlocal.in/send/?").openConnection();
                        String data = user+hash+numbers+message+sender;
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Length",Integer.toString(data.length()));
                        urlConnection.getOutputStream().write(data.getBytes("UTF-8"));
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        final StringBuffer stringBuffer = new StringBuffer();
                        String line;
                        while((line=reader.readLine()) !=null){
                            stringBuffer.append(line);
                        }
                        reader.close();
                        isSuccess=true;
                        return stringBuffer.toString();
                        /*//Get Response
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        String sResult = "";
                        while ((line = rd.readLine()) != null) {
                            //Process line
                            sResult = sResult + line + " ";
                        }
                        rd.close();*/


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
