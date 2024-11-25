package com.example.blog.service.impl;

import com.example.blog.config.StorageProperties;
import com.example.blog.exception.BusinessException;
import com.example.blog.exception.InstructorCode;
import com.example.blog.service.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
@Service
public class FileSystemStorageServiceImpl implements StorageService {
    private final Path rootLocation;
    @Override
    public String getStoredFileName(MultipartFile file, String id){
        String ext= FilenameUtils.getExtension(file.getOriginalFilename());
        return "p"+id+"."+ext;
    }
    public FileSystemStorageServiceImpl(StorageProperties properties){
        this.rootLocation= Paths.get(properties.getLocation());
    }
    @Override
    public void store(MultipartFile file, String storedFilename){
        try{
            if(file.isEmpty()){
                throw new BusinessException(InstructorCode.EMPTY_FILE);
            }
            Path destinationFile=this.rootLocation.resolve(Paths.get(storedFilename))
                    .normalize().toAbsolutePath();
            if(!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())){
                throw new BusinessException(InstructorCode.INVALID_PATH);
            }
            try(InputStream inputStream=file.getInputStream()){
                Files.copy(inputStream,destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }catch(Exception e){

        }
    }
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new BusinessException(InstructorCode.FILE_NOT_READABLE);
        } catch (Exception e) {
            throw new BusinessException(InstructorCode.FILE_LOAD_ERROR);
        }
    }


    @Override
    public Path load(String filename){
        return rootLocation.resolve(filename);
    }
    @Override
    public void delete(String storedFilename) throws IOException {
        Path destinationFile=rootLocation.resolve(Paths.get(storedFilename)).normalize().toAbsolutePath();
        Files.delete(destinationFile);
    }
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Storage initialized at: " + rootLocation);
        } catch (IOException e) {
            throw new BusinessException(InstructorCode.STORAGE_INIT_ERROR);
        }
    }

}
