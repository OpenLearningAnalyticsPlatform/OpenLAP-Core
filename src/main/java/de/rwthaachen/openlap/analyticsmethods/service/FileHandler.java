package de.rwthaachen.openlap.analyticsmethods.service;

import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File handler for simple File and directory creation, saving and deletion
 */
public class FileHandler {

    private final Logger log;

    public FileHandler(Logger log) {
        this.log = log;
    }

    /**
     * TODO
     * @param fileToSave
     * @param savingFolder
     * @param fileName
     * @throws IOException
     * @throws SecurityException
     */
    public void saveFile(MultipartFile fileToSave, String savingFolder, String fileName) throws IOException,
            SecurityException {
        // Create folder if it does not exists
        createFolderIfNotExisting(savingFolder);

        byte[] bytes = fileToSave.getBytes();

        BufferedOutputStream stream = new BufferedOutputStream
                (
                        new FileOutputStream
                                (
                                        new File(savingFolder + fileName)
                                )
                );

        stream.write(bytes);
        stream.close();
        log.info("Saved file: " + savingFolder + fileName);
    }

    /**
     * TODO
     * @param deletionFolder
     * @param fileName
     */
    public void deleteFile(String deletionFolder, String fileName){
        File fileToDelete= new File(deletionFolder + fileName);
        fileToDelete.delete();
        log.info("Deleted file: " + deletionFolder + fileName);
    }

    /**
     * TODO
     * @param deletionFolder
     */
    public void deleteFolder(String deletionFolder){
        deleteFile(deletionFolder,"");
    }

    /**
     * TODO
     * @param savingFolder
     * @throws SecurityException
     */
    private void createFolderIfNotExisting(String savingFolder) throws SecurityException {
        File theDir = new File(savingFolder);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            log.info("Creating directory: " + savingFolder);

            boolean result = false;

            //Can throw SecurityException
            theDir.mkdir();
            result = true;

            if(result) {
                log.info("DIR created: " + savingFolder);
            }
        }
    }
}
