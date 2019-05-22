package de.delusionsoftware.shuttercontroller.network.shutterControlProtocol;

public final class NetworkCommands {

	public enum Commands {
		DIRECTION_DOWN("secure-control", "control-down"), DIRECTION_STATUS_ONLY("secure-control", "control-status"),
		DIRECTION_STOP("secure-control", "control-stop"), DIRECTION_UP("secure-control", "control-up"),
		FETCH_NVCN("secure-control", "security-fetch-nvcn"), SET_PWD("secure-control", "security-pw-change");
		public final String endPoint;
		public final String messageType;

		private Commands(final String endPoint, final String controlString) {
			this.endPoint = endPoint;
			messageType = controlString;
		}
	}

}
