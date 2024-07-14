package pt.tecnico.ulisboa.cmu.conversationalIST;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    //List<ChatRoom> findAll(Specification<String> spec);
    //@Query("select * from chatroom c where c.name=?1")
    @Query(value = "SELECT * FROM chatroom c WHERE c.name=?1", nativeQuery = true)
    ChatRoom findChatRoomByName(String name);

    @Query(value = "SELECT c.messages FROM chatroom c WHERE c.name=?1", nativeQuery = true)
    List<Message> findAllMessages(String name);

}
