package docSharing.repository;

import docSharing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    //        @Transactional
    @Modifying
    @Query("UPDATE User u " + "SET u.isActivated = ?1 WHERE u.id = ?2")
    Boolean updateIsActivated(Boolean bool, Long id);
}
