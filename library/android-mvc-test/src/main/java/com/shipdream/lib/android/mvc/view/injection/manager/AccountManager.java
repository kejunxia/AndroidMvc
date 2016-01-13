package com.shipdream.lib.android.mvc.view.injection.manager;

public interface AccountManager {
    class Session {
        private long userId;

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }
    }

    void setUserId(long id);

    long getUserId();

    String getContent();

    void setContent(String content);
}
