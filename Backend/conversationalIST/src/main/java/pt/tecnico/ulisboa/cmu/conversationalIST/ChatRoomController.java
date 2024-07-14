package pt.tecnico.ulisboa.cmu.conversationalIST;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.boot.jaxb.mapping.spi.LifecycleCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatRoomController {
    private static ChatRoomRepository chatRoomRepository = null;
    private static UserRepository userRepository = null;
    private MessageRepository messageRepository;

    public ChatRoomController(ChatRoomRepository chatRoomRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/chatroom_alive")
    String chatroom_alive() {
        return "chatroom_alive";
    }

    @GetMapping("/chatroom")
    List<ChatRoom> listAll() { return chatRoomRepository.findAll(); }

    @GetMapping("/chatroom/public")
    List<ObjectNode> listAllPublic() {

        List<ObjectNode> wantedChatRooms = new ArrayList<ObjectNode>();
        List<ChatRoom> allChats = chatRoomRepository.findAll();

        for (int i = 0; i < allChats.size(); i++) {
            if (allChats.get(i).isPublic()) {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode json = objectMapper.createObjectNode();
                json.put("name", allChats.get(i).getName());
                json.put("link", allChats.get(i).getLink());
                json.put("users", "[]");
                json.put("messages", "[]");
                json.put("public", allChats.get(i).isPublic());

                wantedChatRooms.add(json);
            }
        }

        return wantedChatRooms;
    }



    @GetMapping("/chatroom/users/{chat_room}")
    List<String> listUsersPrivateChatRoom(@PathVariable String chat_room) {
        ChatRoom chat = chatRoomRepository.findChatRoomByName(chat_room);
        return chat.getUsers();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
      }
      
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      /*::  This function converts decimal degrees to radians             :*/
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
      }
      
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      /*::  This function converts radians to decimal degrees             :*/
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
      }

    @GetMapping("/chatroom/{username}/location/{location}")
    List<ObjectNode> getChatRoomByParticipant(@PathVariable String username, @PathVariable String location) throws JSONException {
        List<ObjectNode> wantedChatRooms = new ArrayList<ObjectNode>();
        List<ChatRoom> allChats = chatRoomRepository.findAll();

        for (int i = 0; i < allChats.size(); i++) {
            List<String> allUsers = allChats.get(i).getUsers();
            for (int j = 0; j < allUsers.size(); j++) {
                if (allUsers.get(j).equals(username)) {
                    if(allChats.get(i).isIsGeo() && (distance(Double.parseDouble(allChats.get(i).getLatlng().split(",")[0]), Double.parseDouble(allChats.get(i).getLatlng().split(",")[1]), Double.parseDouble(location.split(",")[0]), Double.parseDouble(location.split(",")[1])) > allChats.get(i).getSize())){
                        System.out.println("chat out of bounds: " + distance(Double.parseDouble(allChats.get(i).getLatlng().split(",")[0]), Double.parseDouble(allChats.get(i).getLatlng().split(",")[1]), Double.parseDouble(location.split(",")[0]), Double.parseDouble(location.split(",")[0])));
                        continue;
                    }
                    //[{"name":"f","link":null,"users":["joo"],"messages":[],"public":true}]
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode json = objectMapper.createObjectNode();
                    json.put("name", allChats.get(i).getName());
                    json.put("link", allChats.get(i).getLink());
                    json.put("users", "[]");
                    if (allChats.get(i).getMessages().size() > 0) {
                        String message = "[";
                        message += "{\"sender\":{\"username\":" + allUsers.get(j) + ",\"passwd\":" + "not_interesting" + "},";
                        message += "\"message\":" + "\"" + allChats.get(i).getLastMessage().getMessage() + "\"" + ",";
                        message += "\"time\":" + "\"" + allChats.get(i).getLastMessage().getTime()  + "\"" + "}]";
                        json.put("messages", message);
                    }
                    else
                        json.put("messages", "[]");
                    json.put("public", allChats.get(i).isPublic());

                    wantedChatRooms.add(json);
                }
            }
        }

        return wantedChatRooms;
    }

    @GetMapping("/chatroom/create/{chatroom_name}/{isPublic}/{isGeo}/{latlng}/{size}")
    String createChatroom(@PathVariable String chatroom_name, @PathVariable boolean isPublic, @PathVariable boolean isGeo,  @PathVariable String latlng,  @PathVariable Integer size) {
        if (chatRoomRepository.findChatRoomByName(chatroom_name) != null)
            return "Error: chatroom already exist.";
        ChatRoom chat = new ChatRoom(chatroom_name, isPublic, isGeo, latlng, size);
        chatRoomRepository.save(chat);
        return "Chatroom created: " + chatroom_name;
    }

    @GetMapping("/chatroom/create/{chatroom_name}/{isPublic}/{isGeo}")
    String createChatroom(@PathVariable String chatroom_name, @PathVariable boolean isPublic, @PathVariable boolean isGeo) {
        if (chatRoomRepository.findChatRoomByName(chatroom_name) != null)
            return "Error: chatroom already exist.";
        ChatRoom chat = new ChatRoom(chatroom_name, isPublic, isGeo);
        chatRoomRepository.save(chat);
        return "Chatroom created: " + chatroom_name;
    }

    //addUsersToChatRoom
    @GetMapping("/chatroom/{chatroom_name}/add_user")
    String addUserToChatroom(@PathVariable String chatroom_name, @RequestParam(value = "user") String username) {
        System.out.println("ADDDD");
        System.out.println(chatroom_name);
        System.out.println(username);
        ChatRoom chat = chatRoomRepository.findChatRoomByName(chatroom_name);
        if (chat == null)
            return "Error: chatroom does not exist.";

        User user = userRepository.findUserByName(username);
        if (user == null) {
            return "Error: user does not exist.";
        } else {
            boolean shouldAdd = true;
            for (String u: chat.getUsers()) {
                if (u.equals(username))
                    shouldAdd = false;
            }
            if (shouldAdd) {
                chat.addUser(user);
                chatRoomRepository.save(chat);
                return "Added user " + username + " to chatroom: " + chatroom_name;
            }
        }

        return "Error: user already in chatroom.";
    }

    @GetMapping("/chatroom/{chatroom_name}/remove_user")
    String removeUserFromChatroom(@PathVariable String chatroom_name, @RequestParam(value = "user") String username) {
        System.out.println("remove pls");
        ChatRoom chat = chatRoomRepository.findChatRoomByName(chatroom_name);
        if (chat == null)
            return "Error: chatroom does not exist.";

        User user = userRepository.findUserByName(username);
        if (user == null) {
            return "Error: user does not exist.";
        } else {
            boolean shouldRemove = false;
            for (String u: chat.getUsers()) {
                if (u.equals(username))
                    shouldRemove = true;
            }
            if (shouldRemove) {
                chat.removeUser(username);
                chatRoomRepository.save(chat);
                return "Removed user " + username + " from chatroom: " + chatroom_name;
            }
        }

        return "Error: user not in chatroom.";
    }

    @GetMapping("/chatroom/{chatroom_name}/join")
    String joinChatroom(@PathVariable String chatroom_name, @RequestParam(value = "user") String username, @RequestParam(value = "link") String link) {
        ChatRoom chat = chatRoomRepository.findChatRoomByName(chatroom_name);
        if (chat == null)
            return "Error: chatroom does not exist.";

        User user = userRepository.findUserByName(username);
        if (user == null) {
            return "Error: user does not exist.";
        }
        for (int i = 0; i < chat.getUsers().size(); i++) {
            //if (chat.getUsers().get(i).getUsername().equals(username))
            if (chat.getUsers().get(i).equals(username))
                return "Error: user already joined the chatroom.";
        }

        if ((!chat.isPublic() && chat.getLink().equals(link)) || chat.isPublic()) {
            chat.addUser(user);
            chatRoomRepository.save(chat);
            return "You successfully joined the chatroom: " + chatroom_name;
        } else
            return "Error: you have no access to this private chat.";
    }

    @GetMapping("/chatroom/{name}/link")
    String getChatroomLink(@PathVariable String name) {
        ChatRoom chat = chatRoomRepository.findChatRoomByName(name);
        return chat.getLink();
    }

    @GetMapping("/chatroom/{name}/isPublic")
    Boolean isChatRoomPublic(@PathVariable String name) {
        ChatRoom chat = chatRoomRepository.findChatRoomByName(name);
        return chat.isPublic();
    }

    @GetMapping("/chatroom/{name}/leave/{username}")
    String leaveChatroom(@PathVariable String name, @PathVariable String username) {
        ChatRoom chat = chatRoomRepository.findChatRoomByName(name);
        chat.removeUser(username);
        chatRoomRepository.save(chat);
        return "User removed";
    }

    //sendMessage
    @GetMapping("/chatroom/{chatroom_name}/{user}")
    static String sendMessage(@PathVariable("chatroom_name") String chatroom_name, @PathVariable("user") String username, @RequestParam(value = "message") String message, @RequestParam(value = "image") String image) {
        ChatRoom chat = chatRoomRepository.findChatRoomByName(chatroom_name);
        if (chat == null)
            return "Error: chatroom does not exist.";

        User user = userRepository.findUserByName(username);
        if (user == null)
            return "Error: user does not exist.";
        if (!chat.getUsers().contains(user.getUsername()))
            return "Error: user is not allowed to participate on this chatroom.";

        String time = new SimpleDateFormat("HH:mm").format(new Timestamp(System.currentTimeMillis()));
        Message message_object = new Message(user, message, time, image);
        chat.addMessage(message_object);

        chatRoomRepository.save(chat);
        return "Message " + message + " posted to chatroom: " + chatroom_name;
    }

    @GetMapping("/chatroom/{chatroom_name}/messages")
    static List<Message> getChat(@PathVariable String chatroom_name) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByName(chatroom_name);
        return chatRoom.getMessages();
    }
}
