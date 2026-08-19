package com.cosmosdomain.service;

import com.cosmosdomain.config.TmdbProperties;
import com.cosmosdomain.entity.LocalMedia;
import com.cosmosdomain.entity.Subtitle;
import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.LocalMediaRepository;
import com.cosmosdomain.repository.SubtitleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalMediaService {

    private final LocalMediaRepository localMediaRepository;
    private final SubtitleRepository subtitleRepository;
    private final SubtitleService subtitleService;
    private final TmdbProperties props;

    public LocalMediaService(LocalMediaRepository localMediaRepository,
                             SubtitleRepository subtitleRepository,
                             SubtitleService subtitleService,
                             TmdbProperties props) {
         this.localMediaRepository = localMediaRepository;
        this.subtitleRepository = subtitleRepository;
        this.subtitleService = subtitleService;
        this.props = props;
    }

    public java.util.List<LocalMedia> getUserMedia(User user) {
        return localMediaRepository.findByUserIdOrderByAddedAtDesc(user.getId());
    }

    public LocalMedia getById(Long id, User user) {
        return localMediaRepository.findById(id)
            .filter(m -> m.getUser().getId().equals(user.getId()))
            .orElse(null);
    }

    public LocalMedia addMedia(User user, String title,
                               LocalMedia.MediaType mediaType,
                               String filePath, String thumbnailPath) {
        LocalMedia media = new LocalMedia();
        media.setUser(user);
        media.setTitle(title);
        media.setMediaType(mediaType);
        media.setFilePath(filePath);
        media.setThumbnailPath(thumbnailPath);
        try {
            media.setFileSize(Files.size(Paths.get(filePath)));
        } catch (IOException e) {
            media.setFileSize(0L);
        }
        return localMediaRepository.save(media);
    }

    public void updatePlaybackPosition(Long mediaId, int position) {
        localMediaRepository.findById(mediaId).ifPresent(m -> {
            m.setPlaybackPosition(position);
            localMediaRepository.save(m);
        });
    }

    public Subtitle addSubtitle(Long mediaId, MultipartFile file,
                                String language, String label) throws IOException {
        LocalMedia media = localMediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path mediaDir = Paths.get(props.getMediaDir(), "subtitles");
        Files.createDirectories(mediaDir);
        Path outputPath = mediaDir.resolve(filename);
        Files.copy(file.getInputStream(), outputPath);

        Subtitle subtitle = new Subtitle();
        subtitle.setLocalMedia(media);
        subtitle.setLanguage(language);
        subtitle.setLabel(label);
        subtitle.setFilePath(outputPath.toString());
        subtitle.setOriginalFilename(file.getOriginalFilename());
        subtitle.setStatus(Subtitle.Status.UPLOADED);

        if (subtitleService.isSrt(file.getOriginalFilename())) {
            Path vttPath = mediaDir.resolve(filename.replace(".srt", ".vtt"));
            if (subtitleService.convertSrtToVtt(outputPath, vttPath)) {
                subtitle.setFilePath(vttPath.toString());
                subtitle.setStatus(Subtitle.Status.CONVERTED);
            }
        }

        return subtitleRepository.save(subtitle);
    }
}