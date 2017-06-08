package com.phunnylabs.assignmentloktra.models;

/**
 * Created by sachin on 06/06/17.
 */

public class Commit {

    private String authorName;
    private String authorURL;
    private String avatarURL;

    private String commitID;
    private String commitMessage;
    private String commitURL;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorURL() {
        return authorURL;
    }

    public void setAuthorURL(String authorURL) {
        this.authorURL = authorURL;
    }

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getCommitURL() {
        return commitURL;
    }

    public void setCommitURL(String commitURL) {
        this.commitURL = commitURL;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }
}
