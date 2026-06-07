package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.AssetStatus;
import com.autoshorts.scraper.Model.Story;
import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.repository.StoryRepository;
import com.autoshorts.scraper.service.video.BackgroundAssetService;
import com.autoshorts.scraper.service.video.CaptionService;
import com.autoshorts.scraper.service.video.FfmpegAssemblyService;
import com.autoshorts.scraper.service.video.TtsService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class VideoPipelineOrchestrator {

    private final StoryRepository storyRepository;
    private final TtsService ttsService;
    private final CaptionService captionService;
    private final BackgroundAssetService backgroundAssetService;
    private final FfmpegAssemblyService ffmpegAssemblyService;
    private final AutoShortsProperties properties;

    public VideoPipelineOrchestrator(StoryRepository storyRepository,
                                     TtsService ttsService,
                                     CaptionService captionService,
                                     BackgroundAssetService backgroundAssetService,
                                     FfmpegAssemblyService ffmpegAssemblyService,
                                     AutoShortsProperties properties) {
        this.storyRepository = storyRepository;
        this.ttsService = ttsService;
        this.captionService = captionService;
        this.backgroundAssetService = backgroundAssetService;
        this.ffmpegAssemblyService = ffmpegAssemblyService;
        this.properties = properties;
    }

    public void processNextStoryForVideo() {
        Optional<Story> nextStory = storyRepository.findFirstByAssetStatusOrderByIdAsc(AssetStatus.PENDING);
        nextStory.ifPresentOrElse(
                story -> renderStory(story.getId()),
                () -> System.out.println("No stories pending video generation.")
        );
    }

    public void renderStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found: " + storyId));

        if (story.getAssetStatus() == AssetStatus.COMPLETE) {
            System.out.println("Story " + storyId + " is already rendered.");
            return;
        }

        try {
            System.out.println("Rendering story: " + story.getRedditId() + " (id=" + storyId + ")");

            String narrationScript = buildNarrationScript(story);
            Path audioPath = ttsService.synthesize(story.getRedditId(), narrationScript);
            story.setAudioPath(audioPath.toString());
            story.setAssetStatus(AssetStatus.AUDIO_READY);
            story.setErrorMessage(null);
            storyRepository.save(story);

            Path captionPath = captionService.generateCaptions(story.getRedditId(), audioPath);
            Path backgroundPath = backgroundAssetService.selectBackground();
            Path videoPath = properties.getAssets().videoPath(story.getRedditId());

            story.setAssetStatus(AssetStatus.RENDERING);
            storyRepository.save(story);

            Path renderedVideo = ffmpegAssemblyService.assembleVideo(
                    backgroundPath, audioPath, captionPath, videoPath);

            story.setVideoPath(renderedVideo.toString());
            story.setAssetStatus(AssetStatus.COMPLETE);
            story.setErrorMessage(null);
            storyRepository.save(story);

            System.out.println("VIDEO COMPLETE: " + renderedVideo);

        } catch (Exception e) {
            story.setAssetStatus(AssetStatus.FAILED);
            story.setErrorMessage(e.getMessage());
            storyRepository.save(story);
            System.err.println("VIDEO FAILED for story " + storyId + ": " + e.getMessage());
        }
    }

    private String buildNarrationScript(Story story) {
        return story.getTitle() + ". " + story.getContent();
    }
}
