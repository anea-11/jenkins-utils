package utils
import java.io.Serializable

class Version implements Serializable {
    int major
    int minor
    int patch
    String branchName = ""
    String buildId = ""

    public Version(String versionString, String branchName = "", String buildId = "") {
        def parts = versionString.split("\\.")
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Invalid versionString format: ${versionString}")
        }

        this.major = parts[0].toInteger()
        this.minor = parts[1].toInteger()
        this.patch = parts[2].toInteger()
        this.branchName = branchName
        this.buildId = buildId
    }

    @Override @NonCPS
    public String toString() {
        if (branchName.isEmpty() && buildId.isEmpty()) {
            // Release version
            return "${major}.${minor}.${patch}"
        } else {
            // Snapshot version
            return "${major}.${minor}.${patch}-${branchName}-b${buildId}"
        }
    }

    @NonCPS
    public void bumpVersion() {
        this.patch++
    }
}