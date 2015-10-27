package io.analytica.api;

public enum KMeasureType {
	RESPONSE_LENGTH("RESPONSE_LENGTH"),
	USER_ERROR("USER_ERROR"),
	OTHER_ERROR("OTHER_ERROR"),
	CPU_TIME("CPU_TIME"),
	HTML_SIZE("HTML_SIZE");

	private final String measureType;

	private KMeasureType(final String measureType) {
		this.measureType = measureType;
	}

	@Override
	public String toString() {
		return measureType;
	}

}
