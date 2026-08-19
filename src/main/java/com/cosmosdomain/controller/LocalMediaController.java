package com.cosmosdomain.controller;

import com.cosmosdomain.entity.LocalMedia;
import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.UserRepository;
import com.cosmosdomain.service.LocalMediaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/library")
public class LocalMediaController {

    private final LocalMediaService localMediaService;
    private final UserRepository userRepository;
    private final String mediaDir;

    public LocalMediaController(LocalMediaService localMediaService,
                                UserRepository userRepository,
                                @org.springframework.beans.factory.annotation.Value("${cosmosdomain.api.media-dir:./media}") String mediaDir) {
        this.localMediaService = localMediaService;
        this.userRepository = userRepository;
        this.mediaDir = mediaDir;
    }

    @GetMapping
    public String library(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

        User user = findUser(userDetails);
        List<LocalMedia> mediaList = localMediaService.getUserMedia(user);
        model.addAttribute("mediaList", mediaList);
        return "library";
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addMedia(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam String title,
        @RequestParam LocalMedia.MediaType mediaType,
        @RequestParam("mediaFile") MultipartFile mediaFile,
        @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
        Model model) {

        User user = findUser(userDetails);
        try {
            // Save media file
            Path mediaDirPath = Paths.get(mediaDir);
            Files.createDirectories(mediaDirPath);

            String mediaFilename = UUID.randomUUID() + "_" + mediaFile.getOriginalFilename();
            Path mediaPath = mediaDirPath.resolve(mediaFilename);
            Files.copy(mediaFile.getInputStream(), mediaPath);

            // Save thumbnail if provided
            String thumbnailPath = null;
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String thumbFilename = UUID.randomUUID() + "_thumb_" + thumbnailFile.getOriginalFilename();
                Path thumbPath = mediaDirPath.resolve(thumbFilename);
                Files.copy(thumbnailFile.getInputStream(), thumbPath);
                thumbnailPath = thumbPath.toString();
            }

            // Save to database
            localMediaService.addMedia(user, title, mediaType, mediaPath.toString(), thumbnailPath);
            return "redirect:/library?success=Media added successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add media: " + e.getMessage());
            return "library";
        }
    }

    @PostMapping("/{id}/subtitles")
    public String uploadSubtitle(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        @RequestParam MultipartFile file,
        @RequestParam String language,
        @RequestParam String label,
        Model model) {

        try {
            localMediaService.addSubtitle(id, file, language, label);
            return "redirect:/player/" + id + "?success=Subtitle uploaded";
        } catch (Exception e) {
            return "redirect:/player/" + id + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}/position")
    @ResponseBody
    public void updatePosition(
        @PathVariable Long id,
        @RequestParam int position) {
        localMediaService.updatePlaybackPosition(id, position);
    }

    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> serveThumbnail(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id) {

        User user = findUser(userDetails);
        LocalMedia media = localMediaService.getById(id, user);
        if (media == null || media.getThumbnailPath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path thumbPath = Paths.get(media.getThumbnailPath());
        if (!Files.exists(thumbPath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new FileSystemResource(thumbPath);
            String contentType = Files.probeContentType(thumbPath);
            if (contentType == null) contentType = "image/jpeg";

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    private User findUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}