package com.presto.p2048.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.presto.p2048.ContextHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FeedbackSenderTask extends AsyncTask<Void, Void, Boolean> {
    String MyPrefs = "FeedbackPref";
    String LAST_SENT_COUNTER = "lastSentCounter";
    String LAST_MAIL_COUNTER = "lastMAILCounter";
    String MAIL_BODY_PREFIX = "mailBodyPrefix";
    private String mailhost = "smtp.gmail.com";
    private String userName = "test123232223232@gmail.com";
    private String pwd = "";
    private final String MAIL_SUBJECT_FEEDBACK = "2048 feedback";
    private String mailBody = "";
    private boolean cleanUpTask = false;


    public FeedbackSenderTask(String mailBody) {
        this.mailBody = mailBody;
        this.cleanUpTask = false;
    }

    public FeedbackSenderTask() {
        this.cleanUpTask = true;
        this.mailBody = "";
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.i("FeedbackSenderTask", " doInBackground cleanUpTask = " + cleanUpTask);
        try {
            SharedPreferences sharedpreferences = ContextHolder.getApplicationContext().getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
            long lastSentCounter = sharedpreferences.getLong(LAST_SENT_COUNTER, 0);
            long lastMailCounter = sharedpreferences.getLong(LAST_MAIL_COUNTER, 0);
            Log.i("FeedbackSenderTask", " doInBackground lastSentCounter = " + lastSentCounter + ", lastMailCounter = " + lastMailCounter);
            if (!cleanUpTask) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(MAIL_BODY_PREFIX + lastMailCounter, this.mailBody);
                lastMailCounter++;
                editor.putLong(LAST_MAIL_COUNTER, lastMailCounter);
                editor.commit();
            }

            for (long i = lastSentCounter; i < lastMailCounter; i++) {
                String body = sharedpreferences.getString(MAIL_BODY_PREFIX + i, " could not get mail body for lastSentCounter " + i);
                sendMail(MAIL_SUBJECT_FEEDBACK + " " + (new Date()).getTime(), body);
                lastSentCounter = i + 1;
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putLong(LAST_SENT_COUNTER, lastSentCounter);
                editor.commit();
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    private void sendMail(String subject, String body) throws Exception {
        if (!isNetworkAvailable())
            throw new Exception("No network connection");
        GMailSender sender = new GMailSender(userName, pwd);
        sender.sendMail(subject,
                body,
                userName,
                userName);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ContextHolder.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try {
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}

final class JSSEProvider extends Provider {
    public JSSEProvider() {
        super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
        AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
            public Void run() {
                put("SSLContext.TLS",
                        "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                put("Alg.Alias.SSLContext.TLSv1", "TLS");
                put("KeyManagerFactory.X509",
                        "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                put("TrustManagerFactory.X509",
                        "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                return null;
            }
        });
    }
}
