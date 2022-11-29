package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.service.*;
import docSharing.utils.Action;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AbstractController {

    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;

    public ResponseEntity<List<FileRes>> get(Long parentFolderId, Long userId) {
        //FIXME: check if parent folder exists
        try {
            Folder parentFolder = folderService.findById(parentFolderId);
            Set<Folder> folderSet = parentFolder.getFolders();
            Set<Document> documentSet = parentFolder.getDocuments();
            List<FileRes> files = new ArrayList<>();
            for (Folder folder :
                    folderSet) {
                files.add(new FileRes(folder.getName(), folder.getId(), Type.FOLDER));
            }
            for (Document document :
                    documentSet) {
                files.add(new FileRes(document.getName(), document.getId(), Type.DOCUMENT));
            }
            return ResponseEntity.ok().body(files);
        } catch (AccountNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> create(GeneralItem item) {
        // make sure we got all the data from the client
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), item.getName());
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body(convertFromItemToService(item).create(item).toString());
    }

    public ResponseEntity<Object> rename(GeneralItem item) {
        String name = item.getName();
        Long folderId = item.getId();

        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        return ResponseEntity.ok().body(String.valueOf(convertFromItemToService(item).rename(folderId, name)));
    }

    public ResponseEntity<String> delete(GeneralItem item) {
        Long id = item.getId();

        if (id == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is its id?");

        convertFromItemToService(item).delete(id);
        return ResponseEntity.ok().body("A document answering to the id:" + id + " has been successfully erased from the database!");
    }

    public ResponseEntity<Object> relocate(Long newParentId, GeneralItem item) {
        Folder parentFolder = null;
        try {
            if (newParentId != null) {
                parentFolder = folderService.findById(newParentId);
            }
            Long folderId = item.getId();
            if (folderId == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);

            return ResponseEntity.ok().body(convertFromItemToService(item).relocate(parentFolder, folderId));
        }catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     *
     * @param item
     * @return
     */
    private ServiceInterface convertFromItemToService(GeneralItem item) {
        if (item instanceof Document) return documentService;
        if (item instanceof Folder) return folderService;
        return null;
    }
}