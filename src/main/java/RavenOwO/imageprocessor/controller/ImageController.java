package RavenOwO.imageprocessor.controller;

import RavenOwO.imageprocessor.model.JobStatus;
import RavenOwO.imageprocessor.service.ImageProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ImageController {

    @Autowired
    private ImageProcessingService imageProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file,
                                              @RequestParam("operation") String operation,
                                              @RequestParam(required = false) Integer width,
                                              @RequestParam(required = false) Integer height,
                                              @RequestParam(required = false) Integer angle) {
        String jobId = imageProcessingService.processImage(file, operation, width, height, angle);
        return ResponseEntity.ok("Job submitted. Job ID: " + jobId);
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatus> getStatus(@PathVariable String jobId) {
        JobStatus status = imageProcessingService.getJobStatus(jobId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/result/{jobId}")
    public ResponseEntity<?> getResult(@PathVariable String jobId) {
        return imageProcessingService.getProcessedFile(jobId);
    }
}