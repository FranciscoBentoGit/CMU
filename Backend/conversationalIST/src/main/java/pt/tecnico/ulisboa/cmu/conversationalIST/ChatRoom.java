package pt.tecnico.ulisboa.cmu.conversationalIST;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Entity
@Table(name = "chatroom")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false, unique = true)
    private Long id;
    @Column(name = "name", unique = true)
    private String name;
    @Column(name = "type")
    private boolean isPublic;
    @Column(name = "geo")
    private boolean isGeo;
    @Column(name = "latlng")
    private String latlng;
    @Column(name = "size")
    private Integer size;
    @Column(name = "link")
    private String link;
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "chat_users")
    private List<String> users;
    @OneToMany(cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Message> messages;

    public ChatRoom() {
    }

    public ChatRoom(String name, boolean aPublic, boolean aGeo, String alatlon, Integer asize) {
        this.name = name;
        isPublic = aPublic;
        if (!isPublic)
            setLink(name + generateRandomLink()); //generate random char sequence
        System.out.println("------" + name + "------" + link);
        isGeo = aGeo;
        latlng = alatlon;
        size = asize;
    }
    public ChatRoom(String name, boolean aPublic, boolean aGeo) {
        this.name = name;
        isPublic = aPublic;
        if (!isPublic)
            setLink(name + generateRandomLink()); //generate random char sequence
        System.out.println("------" + name + "------" + link);
    }

    public ChatRoom(String name, List<String> users) {
        this.name = name;
        this.users = users;
    }

    public String generateRandomLink() {
        String alphabet = "0123456789";
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();
        int length = 7;

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic() { return isPublic; }

    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public String getLink() { return link; }

    public void setLink(String link) { this.link = link; }

    public List<String> getUsers() {
        return users;
    }

    public void addUser(User user) {
        this.users.add(user.getUsername());
    }
    public void removeUser(String username) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).equals(username))
                users.remove(i);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Message getLastMessage() {
        return messages.get(messages.size()-1);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public boolean isIsGeo() {
        return this.isGeo;
    }

    public boolean getIsGeo() {
        return this.isGeo;
    }

    public void setIsGeo(boolean isGeo) {
        this.isGeo = isGeo;
    }

    public String getLatlng() {
        return this.latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    
}
