package com.sjtu.sw.webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static java.nio.file.StandardCopyOption.*;

import java.io.File;
import java.nio.file.Files;

@Controller
@RequestMapping("api/file")
public class FileController {

    @Value("${file.upload.path}")
    private String path;

    @GetMapping("/")
    public String filePage() {
        return "upload";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String uploadFile(@RequestPart MultipartFile file) throws Exception{
        String fileName = file.getOriginalFilename();
        String filePath = path + fileName;

        File dest = new File(filePath);
        Files.copy(file.getInputStream(), dest.toPath(), REPLACE_EXISTING);
        return "Upload file success : " + dest.getAbsolutePath();

    }
}
