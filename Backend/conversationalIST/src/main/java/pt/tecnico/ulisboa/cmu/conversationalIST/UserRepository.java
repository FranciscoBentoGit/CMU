package pt.tecnico.ulisboa.cmu.conversationalIST;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

interface UserRepository extends JpaRepository<User, String> {
    @Query(value = "SELECT * FROM app_user u WHERE u.username=?1", nativeQuery = true)
    User findUserByName(String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM app_user u WHERE u.username=?1", nativeQuery = true)
    Integer deleteUserByName(String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE User SET passwd= :passwd WHERE username= :name")
    Integer modifyPassword(String name, String passwd);
}
