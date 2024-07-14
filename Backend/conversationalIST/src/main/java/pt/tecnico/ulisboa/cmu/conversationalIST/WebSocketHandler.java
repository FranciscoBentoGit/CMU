package pt.tecnico.ulisboa.cmu.conversationalIST;

import org.apache.tomcat.util.digester.SystemPropertySource;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebSocketHandler extends AbstractWebSocketHandler {

    private static List<WebSocketSession> clientSessions = new ArrayList<>();

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        session.setTextMessageSizeLimit(999999);
        session.setBinaryMessageSizeLimit(999999);

        String[] components = message.getPayload().split("/",7);
       
        if (!clientSessions.contains(session))
            clientSessions.add(session);

        if (components.length == 2)
            webSocketLoadAllMessages(ChatRoomController.getChat(components[1]), session);

        if (components.length == 7) {
            ChatRoomController.sendMessage(components[1], components[2], components[4], components[6]);
            webSocketShowMessage(ChatRoomController.getChat(components[1]));
        }
    }

    public void webSocketLoadAllMessages(List<Message> messages, WebSocketSession session) throws IOException {
        for (int i = 0; i < messages.size(); i++) {
            //retrieve message by message and send it to each active client
            String message = "[";
            message += "{\"sender\":{\"username\":" + messages.get(i).getSender().getUsername() + ",\"passwd\":" + "not_interesting" + "},";
            message += "\"message\":" + "\"" + messages.get(i).getMessage().toString() + "\"" + ",\"image\":" + "\"" + messages.get(i).getImage() + "\"" + ",";
            message += "\"time\":" + "\"" + messages.get(i).getTime() + "\"" + "}]";

            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                session.close();
            }
        }
    }

    public void webSocketShowMessage(List<Message> messages) throws IOException {
        int position = messages.size()-1;
        if (position == -1)
            return;
        String message = "[";

        /*for (int i = 0; i < messages.size(); i++) {
        message += "{\"sender\":{\"username\":" + messages.get(i).getSender().getUsername() + ",\"passwd\":" + "not_interesting" + "},";
        message += "\"message\":" + "\"" + messages.get(i).getMessage() + "\"" + ",";
        message += "\"time\":" + "\"" + messages.get(i).getTime() + "\"" + "},";
        if (!(i == messages.size()-1))
            message += ",";
        }
        message += "]";*/
        //retrieve message by message
        message += "{\"sender\":{\"username\":" + messages.get(position).getSender().getUsername() + ",\"passwd\":" + "not_interesting" + "},";
        message += "\"message\":" + "\"" + messages.get(position).getMessage() + "\"" + ",\"image\":" + "\"" + messages.get(position).getImage() + "\"" + ",";
        message += "\"time\":" + "\"" + messages.get(position).getTime() + "\"" + "}]";

        for (WebSocketSession ses: clientSessions) {
            try {
                ses.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                ses.close();
            }
        }
    }
}
