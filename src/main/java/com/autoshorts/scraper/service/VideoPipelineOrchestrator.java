package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.AssetStatus;
import com.autoshorts.scraper.Model.Story;
import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.repository.StoryRepository;
import com.autoshorts.scraper.service.video.BackgroundAssetService;
import com.autoshorts.scraper.service.video.CaptionService;
import com.autoshorts.scraper.service.video.FfmpegAssemblyService;
import com.autoshorts.scraper.service.video.TtsService;
import com.autoshorts.scraper.service.video.VoiceResolver;
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
    private final VoiceResolver voiceResolver;

    public VideoPipelineOrchestrator(StoryRepository storyRepository,
                                     TtsService ttsService,
                                     CaptionService captionService,
                                     BackgroundAssetService backgroundAssetService,
                                     FfmpegAssemblyService ffmpegAssemblyService,
                                     AutoShortsProperties properties,
                                     VoiceResolver voiceResolver) {
        this.storyRepository = storyRepository;
        this.ttsService = ttsService;
        this.captionService = captionService;
        this.backgroundAssetService = backgroundAssetService;
        this.ffmpegAssemblyService = ffmpegAssemblyService;
        this.properties = properties;
        this.voiceResolver = voiceResolver;
    }

    public void processNextStoryForVideo() {
        processNextStoryForVideo(null);
    }

    public void processNextStoryForVideo(String voiceOverride) {
        Optional<Story> nextStory = storyRepository.findFirstByAssetStatusOrderByIdAsc(AssetStatus.PENDING);
        nextStory.ifPresentOrElse(
                story -> renderStory(story.getId(), voiceOverride),
                () -> System.out.println("No stories pending video generation.")
        );
    }

    public void renderStory(Long storyId) {
        renderStory(storyId, null);
    }

    public void renderStory(Long storyId, String voiceOverride) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found: " + storyId));

        if (story.getAssetStatus() == AssetStatus.COMPLETE) {
            System.out.println("Story " + storyId + " is already rendered.");
            return;
        }

        int claimed = storyRepository.claimForRendering(
                storyId, AssetStatus.RENDERING, AssetStatus.PENDING, AssetStatus.FAILED);
        if (claimed == 0) {
            System.out.println("Story " + storyId + " is already being rendered or is not renderable.");
            return;
        }

        story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found after claim: " + storyId));

        try {
            System.out.println("Rendering story: " + story.getRedditId() + " (id=" + storyId + ")");

            story.setAssetStatus(AssetStatus.RENDERING);
            story.setErrorMessage(null);
            storyRepository.save(story);

            String narrationScript = buildNarrationScript(story);
            String resolvedVoice = voiceResolver.resolveVoice(voiceOverride);
            Path audioPath = ttsService.synthesize(story.getRedditId(), narrationScript, resolvedVoice);
            story.setAudioPath(audioPath.toString());
            story.setTtsVoice(resolvedVoice);
            story.setAssetStatus(AssetStatus.AUDIO_READY);
            story.setErrorMessage(null);
            storyRepository.save(story);

            Path captionPath = captionService.generateCaptions(story.getRedditId(), audioPath, narrationScript);
            Path backgroundPath = backgroundAssetService.selectBackground();
            Path videoPath = properties.getAssets().videoPath(story.getRedditId());

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
