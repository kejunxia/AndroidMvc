package com.shipdream.lib.android.mvc.view.injection.service;

public interface StorageService {
    class Storage {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    String getContent();

    void setContent(String content);
}
