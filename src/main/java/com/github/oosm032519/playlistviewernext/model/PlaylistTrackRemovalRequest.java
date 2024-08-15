package com.github.oosm032519.playlistviewernext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlaylistTrackRemovalRequest {
    @NotBlank
    private String playlistId;
    @NotBlank
    private String trackId;
}
