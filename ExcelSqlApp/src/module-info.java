module ExcelSqlApp {
	   requires javafx.controls;
	    requires javafx.fxml;
	    requires javafx.media;
		requires javafx.graphics;
	
		requires java.desktop;
		requires javafx.base;
		    requires org.apache.poi.ooxml;
			requires org.apache.poi.poi;
	
//	opens application to javafx.graphics, javafx.fxml;
	opens application to javafx.fxml;
    exports application;
}
