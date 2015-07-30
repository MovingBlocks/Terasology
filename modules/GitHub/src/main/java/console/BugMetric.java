package console;

public class BugMetric {
	 private int bugs;
	 public BugMetric(int _bugs) {
		 this.bugs = _bugs;
	 }
	 public String getColor(){
		 if (bugs > 0) {
			 return "Red";
		 }
		 return "Green";
	 }

}
