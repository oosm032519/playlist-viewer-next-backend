package com.github.oosm032519.playlistviewernext.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePlaylistRequest {
    @NotEmpty
    private List<String> trackIds;
    private String playlistName;
}
