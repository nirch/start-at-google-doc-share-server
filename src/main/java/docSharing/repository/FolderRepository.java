package docSharing.repository;

import docSharing.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Folder f " + "SET f.name = ?1 WHERE f.id = ?2")
    int updateName(String name, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Folder f " + "SET f.parentFolderId = ?1 WHERE f.id = ?2")
    int updateParentFolderId(Long parentFolderId, Long id);
}