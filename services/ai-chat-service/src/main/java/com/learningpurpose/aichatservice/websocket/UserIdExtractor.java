package com.learningpurpose.aichatservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class UserIdExtractor {

    private UserIdExtractor(){}

    public static Long extract(WebSocketSession session){
        var handShake = session.getHandshakeInfo();
        String fromHeader = handShake.getHeaders().getFirst("X-User-Id");
        if (fromHeader != null && !fromHeader.isBlank()){
            try {
                return Long.parseLong(fromHeader.trim());
            }catch (NumberFormatException ignored){}
        }

        var uri = handShake.getUri();
        String q = uri.getQuery();
        if (q != null && q.contains("userId=")){
            var params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            String v = params.getFirst("userId");
            if (v != null) {
                try { return Long.parseLong(v.trim()); }
                catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }
}
