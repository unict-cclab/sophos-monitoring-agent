package it.unict.appgroup;

public class Channel {

    private final String peerAppName;

    private final double rps;

    private final double traffic;

    public Channel(String peerAppName, double rps, double traffic) {
        this.peerAppName = peerAppName;
        this.rps = rps;
        this.traffic = traffic;
    }

    public String getPeerAppName() {
        return peerAppName;
    }

    public double getRequestsPerSecond() {
        return rps;
    }

    public double getTraffic() {
        return traffic;
    }
}