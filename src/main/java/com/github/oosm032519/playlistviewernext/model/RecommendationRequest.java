package com.github.oosm032519.playlistviewernext.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RecommendationRequest {
    private List<String> seedArtists;
    private Map<String, Float> maxAudioFeatures;
    private Map<String, Float> minAudioFeatures;
}
