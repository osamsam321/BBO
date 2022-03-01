package application;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
	

enum CommandType
{
	SELECT, UPDATE
}
enum HBoxLayerId
{
	RADIO_BTN_HBOX, SUBMIT_BTN_HBOX, RCN_HBOX;

}
enum MainContentInputId
{
	EXCEL_BTN("EXCEL_BTN"), RCN_INPUT("RCN_INPUT"), RADIO_SELECT("RADIO_SELECT"), RADIO_UPDATE("RADIO_UPDATE"),
	SUBMIT_BTN("SUBMIT_BTN");
	private String id;
    private MainContentInputId(String id) {
        this.id = id;
    }
   
    @Override
    public String toString(){
        return id;
    }


}
public class ExcelSqlExecuterApp extends Application{
	  
	Queue<String> errorQueue = new LinkedList<>();
	FileChooser fileChooser;
	TextField type;
	TextField RCN;
	volatile StringBuilder finalSql = null;
	volatile File resultFile = null;
	final int winH = 300;
	final int winW = 500;
	File excelFile = null;
	VBox topPane;
	VBox centerPane;
	BorderPane root;
	Scene scene;
	@Override
	public void start(@SuppressWarnings("exports") Stage primaryStage) {
		try {
			 root = new BorderPane();
			  fileChooser = new FileChooser();
		      fileChooser.setTitle("open excel spread sheet");
		      fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
			Text title = new Text("Bluesheet generator");
			Image img = new Image((getClass().getResourceAsStream("loading.gif")));
			ImageView iv = new ImageView(img);
			iv.setFitHeight(20);
			iv.setFitWidth(20);
			iv.setVisible(false);
			title.setStyle("-fx-font-size: 18px;");
			HBox titlePane = new HBox(iv, title);
			titlePane.setAlignment(Pos.CENTER);
			 topPane = new VBox(titlePane,genErrorMessageLayer("Please fill out all fields correctly"), genFileDialogBTN("File", Color.AZURE));
//			topPane.setStyle("-fx-background-color: rgb(100,100,100);");
			Button button = new Button("Start");
			button.setStyle("-fx-background-color:rgb(180,230,230);");
			button.setPrefSize(100, 20);
			HBox ButtonConPane = new HBox(button);
			ButtonConPane.setAlignment(Pos.CENTER);
			 centerPane = new VBox( 	 
				genTFLayer("Enter RCN:"),  genDefaultToggleLayer(List.of("update", "select")),
				button) ;
			centerPane.setAlignment(Pos.TOP_CENTER);
			
			
			root.setTop(topPane);
			root.setCenter(centerPane);
			root.setStyle("-fx-background-color: rgb(160, 180, 180);");
			 scene = new Scene(root,winW,winH);
			scene.setFill(Color.BLACK);
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("win_icon.png")));
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
			
			
			button.setOnAction(e -> 
			{
			   if(digitMatch())
			   {
				   try {
					   if(isUpdateSelected())
					   {
							   if(excelFile != null)
							   {
								   changeMessageLayer("Processing", Color.LAWNGREEN);
								   XLSXExecuter(excelFile,  Integer.parseInt(getRCN()), CommandType.UPDATE);
							   }
							   
							   else
							   {
								   changeMessageLayer("Please provide an excel file", Color.RED);
								   throw new Exception("please provide an excel file");
							   }
							  
					   }
					  
					   else if(isSelectSelected())
					   {
						   	if(excelFile != null)
						   	{
						   		    changeMessageLayer("Processing", Color.LAWNGREEN);
								   XLSXExecuter(excelFile, Integer.parseInt(getRCN()), CommandType.SELECT);	
						   	}
						   	else
						    {
						   		   changeMessageLayer("Please provide an excel file", Color.RED);
								   throw new Exception("please provide an excel file");
								   
							 }		
										  						   						  
					   }
					   
					   else
					   {
						   changeMessageLayer("Please add the correct values", Color.RED);
					   }
					
				} 
				   catch (FileNotFoundException f)
				   {
					   changeMessageLayer("Please provide the correct path", Color.RED);
				   }
				   catch (Exception e1) {
					e1.printStackTrace();
				}
				  
			   }
			   else
			   {
				   changeMessageLayer("Please add the correct values", Color.RED);
			   }
		    }
			);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("exports")
	public HBox genDefaultHBox()
	{
		HBox hBox = new HBox();
		@SuppressWarnings("unused")
		int marginHorz = 20;
		hBox.setPrefSize(winW, 60);
		hBox.setAlignment(Pos.CENTER);
		return hBox;
	}
	public String getRCN()
	{
		TextField tf = (TextField) scene.lookup("#" + MainContentInputId.RCN_INPUT.toString());
		return  tf.getText();
	}
	public void genColor(@SuppressWarnings("exports") Node node, @SuppressWarnings("exports") Color color)
	{
		String c = color.toString().substring(1).replace('x', '#');
		node.setStyle("-fx-background-color: " + c + ";" );
	}
	public void genColor(@SuppressWarnings("exports") Node node, String hex)
	{
		node.setStyle("-fx-background-color: " + hex + ";" );
	}
	@SuppressWarnings("exports")
	public HBox genErrorMessageLayer(String message)
	{
		Text text = new Text(message);
		text.setStyle("-fx-font-size: 15px;");

		HBox messagePane = new HBox(text);
		messagePane.setAlignment(Pos.CENTER);
		return messagePane;
	}
	@SuppressWarnings("exports")
	public HBox genDefaultToggleLayer(List<String> labels)
	{
		HBox h = genDefaultHBox();
		ToggleGroup tg = new ToggleGroup();
		for(String s: labels)
		{
			RadioButton r= new RadioButton(s);
			if(s.toLowerCase().contentEquals("select"))
			{				
				r.setId(MainContentInputId.RADIO_SELECT.toString());
			}
			else
			{
				r.setId(MainContentInputId.RADIO_UPDATE.toString());
			}
			
			r.setPrefSize(100, 50);
			r.setStyle("-fx-font-size: 15px");
			r.setToggleGroup(tg);			
			h.getChildren().add(r);
		}	
		
		return h;
	}
	@SuppressWarnings("exports")
	public HBox genFileDialogBTN(String message, Color color)
	{
			
	    
	      Button button = new Button(message);
	      button.setPrefSize(70,35);
	      genColor(button,color);
	      button.setOnMouseClicked(event -> { excelFile = fileChooser.showOpenDialog(new Stage()); }) ;
	      button.setOnKeyPressed(event -> { if(event.getCode() == KeyCode.ENTER)
	    		  {excelFile = fileChooser.showOpenDialog(new Stage());}});
	      
	      HBox h = genDefaultHBox();
	      h.setAlignment(Pos.TOP_LEFT);
	      h.getChildren().add(button);
	      return h;
	}
	@SuppressWarnings("exports")
	public HBox genTFLayer(String labelMessage)
	{
		HBox hb = genDefaultHBox();
		Label label = new Label(labelMessage);
		label.setPadding(new Insets(0, 15,0,0));
		label.setStyle("-fx-font-size: 14px; -fx-text-fill: rgb(30, 50,60);");
		TextField tf = new TextField();
		tf.setPrefSize(150, 20);
		tf.setId(MainContentInputId.RCN_INPUT.toString());
		hb.getChildren().addAll(label, tf);
		
		return hb;
	}
	public void changeMessageLayer(String message, @SuppressWarnings("exports") Color color)
	{
		HBox h = (HBox) topPane.getChildren().get(1);
		Text text = (Text) h.getChildren().get(0);
		text.setFill(color);
		text.setText(message);
	}
	public static void main(String[] args) {
		launch(args);
	}
	public boolean digitMatch()
	{
		return getRCN().matches("[0-9]+");
	}

	public boolean isUpdateSelected()
	{
		RadioButton r = (RadioButton) scene.lookup("#" + MainContentInputId.RADIO_UPDATE.toString());
		return r.isSelected();
	}
	public boolean isSelectSelected()
	{
		RadioButton r = (RadioButton) scene.lookup("#" + MainContentInputId.RADIO_SELECT.toString());
		return r.isSelected();
	}
	public String getNumField()
	{
		HBox h = (HBox) centerPane.getChildren().get(2);
		TextField tf = (TextField) h.getChildren().get(1);
		return tf.getText();
	}

	public void showLoadingImg(boolean visable)
	{
			VBox v = (VBox) root.getTop();
			HBox h = (HBox) v.getChildren().get(0);	
			ImageView iv = (ImageView) h.getChildren().get(0);
			iv.setVisible(visable);
	}

	public void cleanUp()
	{
		changeMessageLayer("Fill the form to read an excel sheet", Color.BLACK);
		showLoadingImg(false);
		
	}

	public void XLSXExecuter(File file, Integer rcn, @SuppressWarnings("exports") CommandType ct) throws Exception
	{
		final int CELL_NUM_WITH_VALUE = 0;
		final int SQL_RCN_ROW_LENGTH = 5;
		final int NUM_PER_SQL_STATEMENT = 100;
		
		//.xlsx & .xls file
//		File file = new File("C:\\demo\\employee.xlsx");   //creating a new file instance 
//	     file = createTestSheet(); //test
		showLoadingImg(true);
		 Task<Void> task = new Task<Void>() {
			
		    @Override 
		    public  Void call() {
		    	
		    	try
				{
		    		
		    		Thread.sleep(1000);
					System.out.println("inside of task thread");
					int sqlCount = 0;
					FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file  
					String sqlSelectHeader =  "\n" + "SELECT COUNT (*) FROM ETL.BLUESHEETS_COMPREHENSIVE "
							+ "\n" + "WHERE EDW_CASE_ID_NUM in" 
							+ "(";
					String sqlSelectFooter = "\n" + "AND EDW_SOURCE_SYSTEM = 'SEC' AND "
							+ "SBMTG_NSCC_ID = " + "'" + rcn + "' "
							+ "AND EDW_DELETE_FLAG = 'NO';"
							+ "\n";
					String sqlUpdateHeader = "\n" + "UPDATE ETL.BLUESHEETS_COMPREHENSIVE SET EDW_DELETE_FLAG ="
							+ "\n" + " 'YES', EDW_LASTMODIFIEDFLAG = 'U', EDW_LASTMODIFIEDDATE = CURRENT_DATE "
							+ "WHERE EDW_CASE_ID_NUM in"
							+ "(";
					String sqlUpdateFooter ="\n" + "AND EDW_SOURCE_SYSTEM = 'SEC' AND "
							+ "SBMTG_NSCC_ID = " + "'" + rcn + "' "
							+ "AND EDW_DELETE_FLAG = 'NO';"
							+ "\n";
					//creating Workbook instance that refers to .xlsx file  
					XSSFWorkbook wb = new XSSFWorkbook(fis);   
					XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object  
					Iterator<Row> rowItr = sheet.iterator();    //iter
					ArrayList<String> list = new ArrayList<String>();
					Cell cell;
					 finalSql = new StringBuilder();
					finalSql.append("\n" + ("---------- " + sqlCount + "-----------") + "\n");
					finalSql.append(sqlSelectHeader);
				
					while(rowItr.hasNext())
					{
						Row row = rowItr.next();
						cell  = row.getCell(CELL_NUM_WITH_VALUE);
						list.add(cell.getStringCellValue());
					}
					
				
					for(int i = 0; i < list.size();i++)
					{
						if(i < list.size() - 1)
						{
							if(i % SQL_RCN_ROW_LENGTH == 0)
							{
								finalSql.append("\n");

							}
							
							finalSql.append(list.get(i) + ", ");					
						}		
					
						else
						{
							finalSql.append(list.get(i));
						}
						if((i + 1) % NUM_PER_SQL_STATEMENT == 0)
						{
							sqlCount++;
							if(ct == CommandType.SELECT)
							{
								finalSql.append(sqlSelectFooter);
								finalSql.append("\n" + "---------- " + sqlCount + "-----------" + "\n");					
								finalSql.append(sqlSelectHeader);
							}
							else if(ct == CommandType.UPDATE)
							{
								finalSql.append(sqlUpdateFooter);
								finalSql.append("\n" + "---------- " + sqlCount + "-----------" + "\n");					
								finalSql.append(sqlUpdateHeader);
							}
							
						}
						
					}
				
					wb.close();
					
				}
				catch(Exception e)
				{
				
				}
		    	System.out.println("result sql: " + finalSql.toString());
		    	
		    	
		    	System.out.println("final call before returning");
		    	
		    	return null;
		     
		    }
		    
		}; 
		  task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent event) {
	                openSaveDialog();
	            }
	        });
		 Thread backgroundThread = new Thread(task);
		 backgroundThread.start();
			
		 	
	
	}
	public void openSaveDialog()
	{
		System.out.println(Thread.activeCount());
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("Text (*.txt)", "*.txt"));
		fc.setInitialFileName("\selection_results.txt");
		fc.setTitle("Save Result file");
		 resultFile = fc.showSaveDialog(new Stage());
		addNewTxtResultFile(resultFile.getAbsolutePath() , finalSql.toString());
		cleanUp();
		System.out.println("RCN values: " + "\n" + finalSql);
		showLoadingImg(false);
	}
	public void addNewTxtResultFile(String path, String dataResult)
	{
		
		try
		{
			FileWriter fw = new FileWriter(path);
			fw.write(dataResult);
			fw.close();
		}
		catch(Exception e)
		{
			changeMessageLayer("Error within internal System", Color.RED);
		}
	}
	public void executeUpdateParserProcedure()
	{
		
	}
	public boolean checkSpreadSheetValuesAreCorrectlyOriented() 
	{
		return true;
	}
	public void addErrorMessage(String errorMsg)
	{
		errorQueue.add(errorMsg);
	}
	public String pollErrorMessage()
	{
		return errorQueue.poll();
	}
//	public File createTestSheet() throws Exception
//	{
//		final int EXCEL_MAX_COL_SIZE = 1048575;
//		int size = 10000;
//		System.out.println("inside of Create test sheet method");
//		XSSFWorkbook workbook = new XSSFWorkbook();
//		  
//        // spreadsheet object
//        XSSFSheet spreadsheet
//            = workbook.createSheet(" RCN ");
//  
//        // creating a row object
//        XSSFRow row;
//        XSSFCell cell;
//        
//       
//        // This data needs to be written (Object[])
//
//        for(int i = 0; i < size; i++)
//        {
//        	row = spreadsheet.createRow(i);
//        	cell = row.createCell(0);
//        	cell.setCellValue("'" + ((int)(Math.random()* 90000) + 10000) + "'" );
//        	
//        }
//        File file = new File("C:/Users/osams/OneDrive/Documents/tmp/temp.xlsx");
//        FileOutputStream out = new FileOutputStream(
//                file);
//      
//            workbook.write(out);
//            out.close();
//        // writing the data into the sheets...
//          return file;
//	}
//

}
