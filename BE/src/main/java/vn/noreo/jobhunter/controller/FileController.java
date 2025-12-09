package vn.noreo.jobhunter.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import vn.noreo.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.noreo.jobhunter.service.FileService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.UploadFileException;

@Tag(name = "File", description = "File management APIs")
@RestController
@RequestMapping("/api/v1")
public class FileController {

    @Value("${upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> uploadFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder)
            throws URISyntaxException, IOException, UploadFileException {

        // Validate
        if (file == null || file.isEmpty()) {
            throw new UploadFileException("File is empty, please select a file to upload.");
        }

        // Check file extension (file type)
        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValidExtension = allowedExtensions.stream().anyMatch(ext -> fileName.toLowerCase().endsWith(ext));
        if (!isValidExtension) {
            throw new UploadFileException(
                    "Invalid file type. Allowed types are: " + String.join(", ", allowedExtensions));
        }

        // Create the folder (directory) if it does not exist
        this.fileService.createDirectory(baseURI + folder);

        // Save the file to the folder
        String uploadFile = this.fileService.saveFile(file, folder, baseURI);

        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO(uploadFile, Instant.now());
        return ResponseEntity.ok().body(resUploadFileDTO);
    }

    @GetMapping("/files")
    @ApiMessage("Download single file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws UploadFileException, URISyntaxException, FileNotFoundException {
        if (fileName == null || folder == null) {
            throw new UploadFileException("File name is required for download.");
        }

        long fileLength = this.fileService.getFileLength(fileName, folder, baseURI);
        if (fileLength == 0) {
            throw new UploadFileException("File not found or is empty.");
        }

        // Download the file
        InputStreamResource resource = this.fileService.getResource(fileName, folder, baseURI);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
