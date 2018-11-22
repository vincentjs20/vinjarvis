
package com.chatbot.cheat;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashMap;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{
    @Autowired
    @Qualifier("com.linecorp.channel_secret")
    String lChannelSecret;

    @Autowired
    @Qualifier("com.linecorp.channel_access_token")
    String lChannelAccessToken;
    private static Connection getConnection() throws URISyntaxException, SQLException {

        Connection connection=null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://ec2-54-83-27-162.compute-1.amazonaws.com:5432/dg7laquo4cnhn", "hkcgmyojwiiysw", "aaf41a665067f4f09a3286ed54e40d7453dcc2b8d120acee1a61257b7ee7fadc");
            System.out.println("Java JDBC PostgreSQL Example");
            // When this class first attempts to establish a connection, it automatically loads any JDBC 4.0 drivers found within
            // the class path. Note that your application must manually load any JDBC drivers prior to version 4.0.
//			Class.forName("org.postgresql.Driver");

            System.out.println("Connected to PostgreSQL database!");
            return connection;
        } /*catch (ClassNotFoundException e) {
			System.out.println("PostgreSQL JDBC driver not found.");
			e.printStackTrace();
		}*/ catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        String dbUrl = "postgres://hkcgmyojwiiysw:aaf41a665067f4f09a3286ed54e40d7453dcc2b8d120acee1a61257b7ee7fadc@ec2-54-83-27-162.compute-1.amazonaws.com:5432/dg7laquo4cnhn";

        String username ="hkcgmyojwiiysw";

        String password="aaf41a665067f4f09a3286ed54e40d7453dcc2b8d120acee1a61257b7ee7fadc";

        return connection;
    }


    private static final String CLIENT_ID = "FREE_TRIAL_ACCOUNT";
    private static final String CLIENT_SECRET = "PUBLIC_SECRET";
    //private static final String ENDPOINT = "http://api.whatsmate.net/v1/translation/translate";
    private boolean statusBos = false;
    private String kalauBossAda = "Bola.net - Mega Bintang Juventus, Cristiano Ronaldo memberikan sebuah pernyataan mengejutkan baru-baru ini. Ronaldo mengakui bahwa ia ingin bermain kembali bersama Wayne Rooney suatu saat nanti.\n" +
            "\n" +
            "Ronaldo sendiri pertama kali mengenal Rooney di Manchester United. Ia menjadi tandem mantan kapten Timnas inggris itu di lini serang United.\n" +
            "\n" +
            "Kombinasi kedua pemain ini terbukti cukup tokcer. Mereka meraih banyak trofi mayor selama lima tahun bekerja bersama, termasuk trofi Liga Champions di tahun 2008 silam.\n" +
            "\n" +
            "Ronaldo sendiri mengakui bahwa Rooney adalah rekan setim yang fantastis baginya dan ia berharap bisa bermain lagi dengannya suatu saat nanti. \"Bagi saya, hal terbaik dari Rooney adalah kekuatan dan juga mentalitasnya,\" ujar Ronaldo kepada Goalhanger.";

    @RequestMapping(value="/callback", method=RequestMethod.POST)

    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String aXLineSignature,
            @RequestBody String aPayload) throws IOException, URISyntaxException, SQLException {
        final String text=String.format("The Signature is: %s",
                (aXLineSignature!=null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
        System.out.println(text);
        final boolean valid=new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
        System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));
        if(aPayload!=null && aPayload.length() > 0)
        {
            System.out.println("Payload: " + aPayload);
        }
        Gson gson = new Gson();
        Payload payload = gson.fromJson(aPayload, Payload.class);

        String msgText = " ";
        String idTarget = " ";
        String eventType = payload.events[0].type;

         if (eventType.equals("message")){
            if (payload.events[0].source.type.equals("group")){
                idTarget = payload.events[0].source.groupId;
            } else if (payload.events[0].source.type.equals("room")){
                idTarget = payload.events[0].source.roomId;
            } else if (payload.events[0].source.type.equals("user")){
                idTarget = payload.events[0].source.userId;
            }

            if (!payload.events[0].message.type.equals("text")){
                replyToUser(payload.events[0].replyToken, "Unknown message");
            } else {
                msgText = payload.events[0].message.text;
                //msgText = msgText.toLowerCase();

                if (!msgText.contains("bot leave")){
                    if(msgText.equalsIgnoreCase("no boss")){
                        statusBos = false;
                        replyToUser(payload.events[0].replyToken, "OK");
                    }
                    else if(msgText.equalsIgnoreCase("Boss")){
                        statusBos = true;
                        replyToUser(payload.events[0].replyToken, kalauBossAda);;
                    }

                    if(statusBos==false){
                        if(msgText.contains("Save")||msgText.contains("save")){
                            simpanPesan(msgText, payload);
                            replyToUser(payload.events[0].replyToken, "Ok");
                        }
                        else if(msgText.contains("Load")||msgText.contains("load")){
                            String hasil = keluarkanPesan(msgText, payload);
                            if(hasil!= null){
                                replyToUser(payload.events[0].replyToken, hasil);
                            }
                            else{
                                replyToUser(payload.events[0].replyToken, "Value tidak ditemukan");
                            }

                        }
                    }

                    //String fromLang = "id";
                    //String toLang = "su";
                    //String tex = "Let's have some fun!";

                    //translate(fromLang, toLang, msgText, payload.events[0].replyToken);

                    //replyToUser(payload.events[0].replyToken, msgText);
                    /*try {
                        getMessageData(msgText, idTarget);
                    } catch (IOException e) {
                        System.out.println("Exception is raised ");
                        e.printStackTrace();
                    }
                    */
                }

                else {
                    if (payload.events[0].source.type.equals("group")){
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")){
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            }
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private void simpanPesan(String perintah, Payload payload) throws URISyntaxException, SQLException {
        String[] data = perintah.split(" ");
        String id = payload.events[0].source.userId;
        String key = data[1];
        String value = data[2];

        insertData(id,key,value);
    }

    public void insertData(String id, String key, String value) throws URISyntaxException, SQLException {
        String temp = getData(id, key);
        PreparedStatement st;
        if(temp!=null){
            st = getConnection().prepareStatement("UPDATE simpanan SET value = ? WHERE id_person = ? AND key = ?;");
            st.setString(1, value);
            st.setString(2, id);
            st.setString(3, key);
        }
        else{
            st = getConnection().prepareStatement("INSERT INTO simpanan (id_person,key,value)" + "\n" + " VALUES(?,?,?);");
            st.setString(1, id);
            st.setString(2, key);
            st.setString(3, value);
        }


        st.executeUpdate();
        st.close();
    }

    public String getData(String id, String value) throws URISyntaxException, SQLException{
        PreparedStatement st = getConnection().prepareStatement("select value from simpanan where id_person = ? AND key = ?;");
        st.setString(1, id);
        st.setString(2, value);
        ResultSet rs= st.executeQuery();
        rs.next();
        String hasil = (String)rs.getObject(1);
        //System.out.println(rs.getObject(1));
        st.close();
        return hasil;
    }

    private String keluarkanPesan(String perintah, Payload payload) throws URISyntaxException, SQLException {
        String[] data = perintah.split(" ");
        String id = payload.events[0].source.userId;
        String key=data[1];
        //String val = hmap.get(data[1]+id);
        String val = getData(id,key);
        return val;

    }

    private void replyToUser(String rToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(rToken, textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println("Reply Message: " + response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void pushMessage(String sourceId, String txt){
        TextMessage textMessage = new TextMessage(txt);
        PushMessage pushMessage = new PushMessage(sourceId,textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .pushMessage(pushMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void leaveGR(String id, String type){
        try {
            if (type.equals("group")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                        .create(lChannelAccessToken)
                        .build()
                        .leaveGroup(id)
                        .execute();
                System.out.println(response.code() + " " + response.message());
            } else if (type.equals("room")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                        .create(lChannelAccessToken)
                        .build()
                        .leaveRoom(id)
                        .execute();
                System.out.println(response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }
}
