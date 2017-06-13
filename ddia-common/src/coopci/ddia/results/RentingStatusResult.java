package coopci.ddia.results;

import coopci.ddia.Result;

public class RentingStatusResult  extends Result {
	public static class RentingStatus {
		public long pledge = 0;
		public String items[];
	}
	public RentingStatus data = new RentingStatus();
}
