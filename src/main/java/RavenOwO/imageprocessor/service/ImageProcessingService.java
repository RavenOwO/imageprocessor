package RavenOwO.imageprocessor.service;

import RavenOwO.imageprocessor.util.ImageUtils;
import RavenOwO.imageprocessor.model.JobStatus;
import RavenOwO.imageprocessor.model.JobStatus.JobState;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Service
public class ImageProcessingService {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();
    private final File outputDir = new File(System.getProperty("java.io.tmpdir"), "processed-images");

    public ImageProcessingService() {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    public String processImage(MultipartFile file, String operation,
                               Integer width, Integer height, Integer angle) {
        String jobId = UUID.randomUUID().toString();
        jobStatusMap.put(jobId, new JobStatus(JobState.RECEIVED, null));

        executor.submit(() -> {
            jobStatusMap.get(jobId).setState(JobState.PROCESSING);
            try {
                BufferedImage inputImage = ImageIO.read(file.getInputStream());

                BufferedImage resultImage = switch (operation.toLowerCase()) {
                    case "grayscale" -> ImageUtils.toGrayscale(inputImage);
                    case "resize" -> ImageUtils.resize(inputImage, width != null ? width : 200, height != null ? height : 200);
                    case "rotate" -> ImageUtils.rotate(inputImage, angle != null ? angle : 90);
                    case "flip" -> ImageUtils.flipHorizontal(inputImage);
                    case "invert" -> ImageUtils.invertColors(inputImage);
                    case "blur" -> ImageUtils.blur(inputImage);
                    case "sharpen" -> ImageUtils.sharpen(inputImage);
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                };

                File outputFile = new File(outputDir, jobId + ".jpg");
                ImageIO.write(resultImage, "jpg", outputFile);
                jobStatusMap.put(jobId, new JobStatus(JobState.COMPLETED, outputFile.getAbsolutePath()));

            } catch (Exception e) {
                jobStatusMap.put(jobId, new JobStatus(JobState.FAILED, null));
                e.printStackTrace();
            }
        });

        return jobId;
    }

    public JobStatus getJobStatus(String jobId) {
        return jobStatusMap.get(jobId);
    }

    public ResponseEntity<?> getProcessedFile(String jobId) {
        JobStatus status = jobStatusMap.get(jobId);
        if (status != null && status.getState() == JobState.COMPLETED) {
            File file = new File(status.getResultPath());
            if (file.exists()) {
                try {
                    byte[] content = Files.readAllBytes(file.toPath());
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                            .body(content);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body("Error reading result file.");
                }
            }
        }
        return ResponseEntity.badRequest().body("Result not available.");
    }
}