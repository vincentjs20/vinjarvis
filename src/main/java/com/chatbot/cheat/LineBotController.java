
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        String urlPostgres = "postgres://hkcgmyojwiiysw:aaf41a665067f4f09a3286ed54e40d7453dcc2b8d120acee1a61257b7ee7fadc@ec2-54-83-27-162.compute-1.amazonaws.com:5432/dg7laquo4cnhn";
        URI dbUri = new URI(System.getenv(urlPostgres));
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        return DriverManager.getConnection(dbUrl, username, password);
    }

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

    private void simpanPesan(String perintah, Payload payload){
        String[] data = perintah.split(" ");
        String id = payload.events[0].source.userId;
        String key = data[1]+id;
        String value = data[2];
        hmap.put(key, value);
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