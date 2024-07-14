package pt.tecnico.ulisboa.cmu.conversationalIST;

import javax.persistence.*;

@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name= "id", updatable = false, nullable = false, unique = true)
    private Long id;
    @OneToOne
    private User sender;
    @Column(name = "content")
    private String message;
    @Column(name = "image", length = 5000)
    private String image;
    @Column(name = "time")
    private String time;

    public Message() {
    }

    public Message(User sender, String message, String time, String image) {
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.image = image;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
