package com.crackdress.wordgrab.kernel;


public class RecorderConfig {

    int outputFormat;
    int audioSource;
    int audioEncoder;

    String fileExt;


    public int getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(int outputFormat) {
        this.outputFormat = outputFormat;
    }

    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    public int getAudioEncoder() {
        return audioEncoder;
    }

    public void setAudioEncoder(int audioEncoder) {
        this.audioEncoder = audioEncoder;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    @Override
    public String toString() {
        return "RecorderConfig{" +
                "outputFormat=" + outputFormat +
                ", audioSource=" + audioSource +
                ", audioEncoder=" + audioEncoder +
                ", fileExt='" + fileExt + '\'' +
                '}';
    }
}
