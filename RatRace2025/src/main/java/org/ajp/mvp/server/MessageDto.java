package org.ajp.mvp.server;

/**
 * Stub implementation of MessageDto for development.
 * Replace with actual MVP server JAR when available.
 */
public class MessageDto {
    private String type;
    private String content;
    private String namespace;

    public String getType() {
        return type;
    }

    public MessageDto setType(String type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MessageDto setContent(String content) {
        this.content = content;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public MessageDto setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MessageDto instance = new MessageDto();

        public Builder type(String type) {
            instance.setType(type);
            return this;
        }

        public Builder content(String content) {
            instance.setContent(content);
            return this;
        }

        public Builder namespace(String namespace) {
            instance.setNamespace(namespace);
            return this;
        }

        public MessageDto build() {
            return instance;
        }
    }
}