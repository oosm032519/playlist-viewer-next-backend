// RemoveTrackRequest.java

package com.github.oosm032519.playlistviewernext.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RemoveTrackRequest {
    private String playlistId;
    private String trackId;
}
