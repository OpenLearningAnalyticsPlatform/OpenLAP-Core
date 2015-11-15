package org.rwthaachen.olap.analyticsmethods.service;

import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by lechip on 15/11/15.
 */
public class FileHandler {

    public void saveFile(MultipartFile fileToSave, String savingFolder, String fileName) throws IOException {
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
    }

    public void deleteFile(String analyticsMethodsRepository, String fileName){
        File fileToDelete= new File(analyticsMethodsRepository + fileName);
        fileToDelete.delete();
    }
}
