package com.cosmosdomain.controller;

import com.cosmosdomain.config.TmdbProperties;
import com.cosmosdomain.entity.LocalMedia;
import com.cosmosdomain.entity.Subtitle;
import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.UserRepository;
import com.cosmosdomain.service.LocalMediaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/player")
public class MediaPlayerController {

    private final LocalMediaService localMediaService;
    private final UserRepository userRepository;
    private final TmdbProperties props;

    public MediaPlayerController(LocalMediaService localMediaService,
                                 UserRepository userRepository,
                                 TmdbProperties props) {
        this.localMediaService = localMediaService;
        this.userRepository = userRepository;
        this.props = props;
    }

    @GetMapping("/{id}")
    public String play(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        Model model) {

        User user = findUser(userDetails);
        LocalMedia media = localMediaService.getById(id, user);
        if (media == null) {
            return "redirect:/library";
        }

        model.addAttribute("media", media);
        model.addAttribute("subtitles", media.getSubtitles());
        return "player";
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> streamMedia(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        @RequestHeader(value = HttpHeaders.RANGE, defaultValue = "") String rangeHeader) {

        User user = findUser(userDetails);
        LocalMedia media = localMediaService.getById(id, user);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(media.getFilePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            long fileSize = Files.size(filePath);
            return buildRangeResponse(filePath, fileSize, rangeHeader);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    private ResponseEntity<Resource> buildRangeResponse(
        Path filePath, long fileSize, String rangeHeader) throws IOException {

        long start = 0;
        long end = fileSize - 1;

        if (!rangeHeader.isEmpty() && rangeHeader.startsWith("bytes=")) {
            String range = rangeHeader.substring(6);
            String[] parts = range.split("-");
            start = Long.parseLong(parts[0]);
            if (parts.length > 1 && !parts[1].isEmpty()) {
                end = Long.parseLong(parts[1]);
            }
            if (end >= fileSize) end = fileSize - 1;
        }

        long contentLength = end - start + 1;
        Resource resource = new FileSystemResource(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "video/mp4");
        headers.set("Content-Length", String.valueOf(contentLength));
        headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        headers.set("Accept-Ranges", "bytes");
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

        return ResponseEntity.status(206).headers(headers).body(resource);
    }

    @GetMapping("/subtitle/{id}")
    public ResponseEntity<Resource> streamSubtitle(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id) {

        User user = findUser(userDetails);
        LocalMedia media = localMediaService.getById(id, user);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }

        List<Subtitle> subtitles = (List<Subtitle>) media.getSubtitles();
        if (subtitles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path subPath = Paths.get(subtitles.get(0).getFilePath());
        if (!Files.exists(subPath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new FileSystemResource(subPath);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "text/vtt");
            headers.set("Content-Disposition", "inline");
            headers.setContentLength(Files.size(subPath));
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    private User findUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}