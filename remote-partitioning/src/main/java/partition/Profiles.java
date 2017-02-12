package partition;

public class Profiles {
	public static final String WORKER_PROFILE = "worker"; // <1>
	public static final String LEADER_PROFILE = "!" + WORKER_PROFILE; // <2>
}
