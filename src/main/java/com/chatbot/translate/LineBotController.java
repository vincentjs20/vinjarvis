
package com.chatbot.translate;

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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

    static HashMap<String, String> hmap = new HashMap<String, String>();

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

    private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
        return DriverManager.getConnection(dbUrl);
    }

    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String aXLineSignature,
            @RequestBody String aPayload) throws IOException {
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

        FileInputStream serviceAccount = new FileInputStream("/src/main/resources/lbwchatbot-firebase-adminsdk.json");



        if (eventType.equals("join")){
            if (payload.events[0].source.type.equals("group")){
                replyToUser(payload.events[0].replyToken, "Hello Group");
            }
            if (payload.events[0].source.type.equals("room")){
                replyToUser(payload.events[0].replyToken, "Hello Room");
            }
        } else if (eventType.equals("message")){
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
                        replyToUser(payload.events[0].replyToken, kalauBossAda);
                    }

                    if(statusBos==false){
                        if(msgText.contains("Save")||msgText.contains("save")){
                            simpanPesan(msgText, payload);
                            replyToUser(payload.events[0].replyToken, "Ok");
                        }
                        else if(msgText.contains("Load")||msgText.contains("load")){
                            String hasil = keluarkanPesan(msgText, payload);
                            replyToUser(payload.events[0].replyToken, hasil);
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

    private void translate(String fromLang, String toLang, String text, String payload) throws IOException {
        // TODO: Should have used a 3rd party library to make a JSON string from an object
        String jsonPayload = new StringBuilder()
                .append("{")
                .append("\"fromLang\":\"")
                .append(fromLang)
                .append("\",")
                .append("\"toLang\":\"")
                .append(toLang)
                .append("\",")
                .append("\"text\":\"")
                .append(text)
                .append("\"")
                .append("}")
                .toString();

        //URL url = new URL(ENDPOINT);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setDoOutput(true);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("X-WM-CLIENT-ID", CLIENT_ID);
//        conn.setRequestProperty("X-WM-CLIENT-SECRET", CLIENT_SECRET);
//        conn.setRequestProperty("Content-Type", "application/json");
//
//        OutputStream os = conn.getOutputStream();
//        os.write(jsonPayload.getBytes());
//        os.flush();
//        os.close();
//
//        int statusCode = conn.getResponseCode();
//        System.out.println("Status Code: " + statusCode);
//        BufferedReader br = new BufferedReader(new InputStreamReader(
//                (statusCode == 200) ? conn.getInputStream() : conn.getErrorStream()
//        ));
//        String output;
//        while ((output = br.readLine()) != null) {
//            replyToUser(payload, output);
//            //System.out.println(output);
//        }
//        conn.disconnect();
    }

    private void simpanPesan(String perintah, Payload payload){
        String[] data = perintah.split(" ");
        String id = payload.events[0].source.userId;
        String key = data[1]+id;
        String value = data[2];

        Simpanan dataSimpanan = new Simpanan(id, key, value);
        insertData(dataSimpanan);
        hmap.put(key, value);
    }

    public void insertData(Simpanan simpanan){
        String sql="INSERT INTO simpanan(id_person,key,value)"
                + "VALUES(?,?,?)";

        try (
                Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(sql);) {
                statement.setString(1, simpanan.getId_person());
                statement.setString(2, simpanan.getKey());
            statement.setString(2, simpanan.getValue());

            statement.execute();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String keluarkanPesan(String perintah, Payload payload){
        String[] data = perintah.split(" ");
        String id = payload.events[0].source.userId;
        String val = hmap.get(data[1]+id);
        return val;

    }

    private void getMessageData(String message, String targetID) throws IOException{
        if (message!=null){
            pushMessage(targetID, message);
        }
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
