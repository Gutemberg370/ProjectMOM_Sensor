module ProjectMOM_Sensor {
	requires javafx.controls;
	requires activemq.all;
	requires javafx.fxml;
	
	opens application to javafx.graphics, javafx.fxml;
}
