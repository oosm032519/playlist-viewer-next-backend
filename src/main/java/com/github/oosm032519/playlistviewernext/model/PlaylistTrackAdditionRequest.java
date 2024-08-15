package com.github.oosm032519.playlistviewernext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistTrackAdditionRequest {
    @NotBlank
    private String playlistId;
    @NotBlank
    private String trackId;
}
