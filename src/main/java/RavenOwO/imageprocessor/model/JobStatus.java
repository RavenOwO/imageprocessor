package RavenOwO.imageprocessor.model;

public class JobStatus {

    public enum JobState {
        RECEIVED,
        PROCESSING,
        COMPLETED,
        FAILED
    } //RECEIVED, PROCESSING, COMPLETED, FAILED

    private JobState state;
    private String resultPath;

    public JobStatus(JobState state, String resultPath) {
        this.state = state;
        this.resultPath = resultPath;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }
}