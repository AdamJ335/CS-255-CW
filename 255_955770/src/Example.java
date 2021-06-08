import javafx.application.Application;
import javafx.beans.value.ChangeListener; 
import javafx.beans.value.ObservableValue; 
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class Example extends Application {
	short cthead[][][]; //store the 3D volume data set
	short min, max; //min/max value in the 3D volume data set
	int[][] histogram;
	
	
	
    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");
		

		ReadData();

		// initialising top-down view
		int widthZ = 256;
        int heightZ = 256;
		WritableImage medical_image_Z = new WritableImage(widthZ, heightZ);
		ImageView imageViewZ = new ImageView(medical_image_Z);
		
		// initialising front-back view
		int widthY = 256;
        int heightY = 113;
		WritableImage medical_image_Y = new WritableImage(widthY, heightY);
		ImageView imageViewY = new ImageView(medical_image_Y);
		
		// initialising side-side view
		int widthX = 256;
        int heightX = 113;
		WritableImage medical_image_X = new WritableImage(widthX, heightX);
		ImageView imageViewX = new ImageView(medical_image_X);
		 
		Label slicerZ_label = new Label("Z Slider");
		Label slicerX_label = new Label("X Slider");
		Label slicerY_label = new Label("Y Slider");
		Label newWidth_label = new Label("Resize on Width");
		Label newHeight_label = new Label("Resize on Height");
		
		//Button NN_resize_buttonZ = new Button("NN Resize Z");
		//Button NN_resize_buttonX = new Button("NN Resize X");
		//Button NN_resize_buttonY = new Button("NN Resize Y");
		Button resize_buttonZ = new Button("Resize Z");
		Button resize_buttonX = new Button("Resize X");
		Button resize_buttonY = new Button("Resize Y");
		Button mip_button= new Button("MIP");
		Button histogram_button= new Button("Histogram");
		Button cumulative_histogram_button = new Button("Cumulative Distribution");
		
		CheckBox equalisation_check = new CheckBox("Equalise");
		
		Slider zslider = new Slider(0, 112, 0);
		Slider yslider = new Slider(0, 255, 0);
		Slider xslider = new Slider(0, 255, 0);
		Slider ImageSliderX = new Slider(10, 600, 0);
		Slider ImageSliderY = new Slider(10, 600, 0);
		
		resize_buttonZ.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	System.out.println("Resizing with X= "+(int)ImageSliderX.getValue()+" Y= "+(int)ImageSliderY.getValue());
				resizePopUp(medical_image_Z, (int)ImageSliderX.getValue(), (int)ImageSliderY.getValue());
				
            }
        });
		resize_buttonY.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	System.out.println("Resizing with X= "+(int)ImageSliderX.getValue()+" Y= "+(int)ImageSliderY.getValue());
				resizePopUp(medical_image_Y, (int)ImageSliderX.getValue(), (int)ImageSliderY.getValue());
				
            }
        });
		resize_buttonX.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	//not needed but good for debugging
            	System.out.println("Resizing with X= "+(int)ImageSliderX.getValue()+" Y= "+(int)ImageSliderY.getValue());
				resizePopUp(medical_image_X, (int)ImageSliderX.getValue(), (int)ImageSliderY.getValue());
				
            }
        });
		histogram_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	drawHistogram(Histogram());
            }
        });
		
		cumulative_histogram_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	drawHistogram(CDHist());
            }
        });
		
		mip_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				MIP(medical_image_Z, 'Z');
				MIP(medical_image_Y, 'Y');
				MIP(medical_image_X, 'X');
            }
        });
		
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
			  
            public void handle(ActionEvent e) 
            { 
                if (equalisation_check.isSelected()) {
                	System.out.println("Equalising!");
                    Equalise(); 
                }
                else {
                	System.out.println("Reverting!");
                    try {
						ReadData(); // calling this function overrides the equalised data
					} catch (IOException e1) {
						System.out.println("Error on reverting!");
						e1.printStackTrace();
					} 
                } 
            }

        }; 
        equalisation_check.setOnAction(event);
        
		
		zslider.valueProperty().addListener( 
            new ChangeListener<Number>() { 
				public void changed(ObservableValue <? extends Number >  
					observable, Number oldValue, Number newValue) 
            { 
                System.out.println("z"+newValue.intValue());
                SlicerZ(medical_image_Z, zslider);
                //used to continuously press the Slicer button when there is a change in the slider value
               
            } 
        }); 
		
		yslider.valueProperty().addListener( 
	            new ChangeListener<Number>() { 
					public void changed(ObservableValue <? extends Number >  
						observable, Number oldValue, Number newValue) 
	            { 
	                System.out.println("y"+newValue.intValue());
	                SlicerY(medical_image_Y, yslider);
	                //used to continuously press the Slicer button when there is a change in the slider value
	            } 
	        }); 
		
		xslider.valueProperty().addListener( 
	            new ChangeListener<Number>() { 
					public void changed(ObservableValue <? extends Number >  
						observable, Number oldValue, Number newValue) 
	            { 
	                System.out.println("x"+newValue.intValue());
	                SlicerX(medical_image_X, xslider);
	                //used to continuously press the Slicer button when there is a change in the slider value
	            } 
	        }); 
		
		ImageSliderX.valueProperty().addListener( 
	            new ChangeListener<Number>() { 
					public void changed(ObservableValue <? extends Number >  
						observable, Number oldValue, Number newValue) 
	            { System.out.println(newValue.intValue());
	             } 
	        }); 
		ImageSliderY.valueProperty().addListener( 
	            new ChangeListener<Number>() { 
					public void changed(ObservableValue <? extends Number >  
						observable, Number oldValue, Number newValue) 
	            { System.out.println(newValue.intValue());
	              } 
	        }); 
		
		
		BorderPane root = new BorderPane();
		HBox horizon = new HBox();
		horizon.setSpacing(10);
		horizon.setAlignment(Pos.CENTER);
		horizon.getChildren().addAll(imageViewZ, imageViewY, imageViewX);
		
		VBox buttons = new VBox();
		buttons.setAlignment(Pos.CENTER_LEFT);
		buttons.setSpacing(10);
		buttons.getChildren().addAll(mip_button,resize_buttonZ, resize_buttonY, resize_buttonX, 
				histogram_button, cumulative_histogram_button, equalisation_check);
		
		HBox sliders = new HBox();
		sliders.setSpacing(10);
		sliders.getChildren().addAll(slicerZ_label, zslider, slicerY_label, yslider, slicerX_label, 
				xslider, newWidth_label, ImageSliderX, newHeight_label, ImageSliderY);
		
		root.setPadding(new Insets(10, 10, 10, 10));
		root.setStyle("-fx-background-color: #9EA2BA;");
		root.setTop(sliders);
        root.setLeft(buttons);
        root.setCenter(horizon);
//https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/

		//root.getChildren().addAll(imageViewZ, imageViewY, imageViewX, mip_button, zslider, yslider, xslider);
		
		//Parent root = FXMLLoader.load(getClass().getResource("cthead.fxml"));
        Scene scene = new Scene(root, 1150, 640);
        stage.setScene(scene);
        stage.show();
    }
	
	//Function to read in the cthead data set
	public void ReadData() throws IOException {
		//File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
		File file = new File("CThead.raw");
		//Read the data quickly via a buffer 
		//(in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
		@SuppressWarnings("resource")
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		
		//int levels = 3366;
		/*
		short index;
		int t_i = 0;
		int data_size = (113*256*256); // total number of pixels
		histogram = new int[levels];
		mapping = new float[data_size];
		*/
		int i, j, k; //loop through the 3D data set
		
		min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
		short read; //value read in
		int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around
		
		cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
		//loop through the data reading it in
		for (k=0; k<113; k++) {
			for (j=0; j<256; j++) {
				for (i=0; i<256; i++) {
					//because the Endianess is wrong, it needs to be read byte at a time and swapped
					b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
					b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
					read=(short)((b2<<8) | b1); //and swizzle the bytes around
					if (read<min) min=read; //update the minimum
					if (read>max) max=read; //update the maximum
					cthead[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
					
				}
			}
		}
		System.out.println(min+" "+max); //diagnostic - for CThead this should be -1117, 2248
		//(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
		//therefore histogram equalization would be a good thing
		
	}
	/*
	 * The getDepth function helps generalise the MIP so that there is only need for 1 MIP function.
	 * This checks the width and height and makes sure that the depth can go up to either 113 or 256.
	 */
	public int getDepth(int w, int h) {
		if(w==h) {
			return 113;
		}else {
			return 256;
		}
	}
	
	/*
	 * Creating one function with a character indicating which axis the image is going to check the MIP on
	 * Although more inefficient, it reduces having 3 separate MIP functions
	 */
	public void MIP(WritableImage image, char axis) {
        int w=(int) image.getWidth(), h=(int) image.getHeight(), i, j, k;
        int d= getDepth(w,h);
        PixelWriter image_writer = image.getPixelWriter();
		float col, maximum;
		short datum;
		//loops through height, width and depth
		//checks for every loop what is the maximum intensity value and then projects onto screen
        for (j=0; j<h; j++) {
                for (i=0; i<w; i++) {
                	maximum = -1117;
                	for(k=0; k<d; k++) {
                		if(axis == 'Z') {
                			datum = cthead[k][j][i];
                		}else if(axis == 'X') {
                			datum = cthead[j][i][k];
                		}else {
                			datum = cthead[j][k][i];
                		}
                		
                        if(datum >=maximum) {
                        	if(axis == 'Z') {
                    			maximum = cthead[k][j][i];
                    		}else if(axis == 'X') {
                    			maximum = cthead[j][i][k];
                    		}else {
                    			maximum = cthead[j][k][i];
                    		}
                        	col=(((float)maximum-(float)min)/((float)(max-min)));
							image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
                        }
                }
			}
		}
	}
    public void SlicerZ(WritableImage image, Slider zslider) {//a slider has to be passed through to use the correct image slice
            //Get image dimensions, and declare loop variables
            int w=(int) image.getWidth(), h=(int) image.getHeight(), i, j;
            PixelWriter image_writer = image.getPixelWriter();
			float col;
			short datum;
            //Shows how to loop through each pixel and colour
            //Try to always use j for loops in y, and i for loops in x
            //as this makes the code more readable
            for (j=0; j<h; j++) {
                    for (i=0; i<w; i++) {
							//at this point (i,j) is a single pixel in the image
							//here you would need to do something to (i,j) if the image size
							//does not match the slice size (e.g. during an image resizing operation
							//If you don't do this, your j,i could be outside the array bounds
							//In the framework, the image is 256x256 and the data set slices are 256x256
							//so I don't do anything - this also leaves you something to do for the assignment
							datum=cthead[(int) zslider.getValue()][j][i]; //get values from the slice of the number the slider is on
							//calculate the colour by performing a mapping from [min,max] -> [0,255]
							col=(((float)datum-(float)min)/((float)(max-min)));
							image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
		    				
                    } // column loop
            } // row loop
    }
    public void SlicerX(WritableImage image, Slider xslider) {//a slider has to be passed through to use the correct image slice
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight(), j, k;
        PixelWriter image_writer = image.getPixelWriter();
		float col;
		short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j=0; j<h; j++) {
                for (k=0; k<w; k++) {
						//at this point (i,j) is a single pixel in the image
						//here you would need to do something to (i,j) if the image size
						//does not match the slice size (e.g. during an image resizing operation
						//If you don't do this, your j,i could be outside the array bounds
						//In the framework, the image is 256x256 and the data set slices are 256x256
						//so I don't do anything - this also leaves you something to do for the assignment
						datum=cthead[j][k][(int) xslider.getValue()]; //get values from the slice of the number the slider is on
						//calculate the colour by performing a mapping from [min,max] -> [0,255]
						col=(((float)datum-(float)min)/((float)(max-min)));
						image_writer.setColor(k, j, Color.color(col,col,col, 1.0));
						
                } // column loop
        } // row loop
}
    public void SlicerY(WritableImage image, Slider yslider) {//a slider has to be passed through to use the correct image slice
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight(), i, k;
        PixelWriter image_writer = image.getPixelWriter();
        
		float col;
		short datum;
		//byte[] data = null;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (i=0; i<h; i++) {
                for (k=0; k<w; k++) {
						//at this point (i,j) is a single pixel in the image
						//here you would need to do something to (i,j) if the image size
						//does not match the slice size (e.g. during an image resizing operation
						//If you don't do this, your j,i could be outside the array bounds
						//In the framework, the image is 256x256 and the data set slices are 256x256
						//so I don't do anything - this also leaves you something to do for the assignment
						datum=cthead[i][(int) yslider.getValue()][k]; //get values from the slice of the number the slider is on
						//calculate the colour by performing a mapping from [min,max] -> [0,255]
						col=(((float)datum-(float)min)/((float)(max-min)));
                        image_writer.setColor(k, i, Color.color(col,col,col, 1.0));
						
                } // column loop
        } // row loop
}
    // Resizing algorithm with Nearest Neighbour
    public WritableImage NNResize(WritableImage image, int newWidth, int newHeight) {
    	int oldWidth = (int) image.getWidth();
    	int oldHeight = (int) image.getHeight();
    	WritableImage outputImage = new WritableImage(newWidth, newHeight);
    	PixelReader pixelReader = image.getPixelReader();
    	PixelWriter pixelWriter = outputImage.getPixelWriter();
    	int y, x;
    	for(int j=0; j<newHeight; j++) {
    		for(int i=0; i<newWidth; i++) {
    			for(int c=0; c<3; c++) {
    				x = (i*oldWidth/newWidth);
    				y = (j*oldHeight/newHeight);
    				Color temp = pixelReader.getColor(x, y);
    				pixelWriter.setColor(i, j, temp);
    			}
    		}
    	}
    	return outputImage;
    }
   
    //Resizing Image using Bilinear Interpolation
    public WritableImage BilResize(WritableImage image, double newWidth, double newHeight) {
    	double oldWidth = image.getWidth();
    	double oldHeight = image.getHeight();
    	WritableImage outputImage = new WritableImage((int)newWidth, (int)newHeight);
    	PixelReader pixelReader = image.getPixelReader();
    	PixelWriter pixelWriter = outputImage.getPixelWriter();
    	double x;
    	double y;
    	double y1;
    	double y2;
    	double x1;
    	double x2;
    	// setting everything to a double to prevent any integer division and making the resize more pixelated as if it is Nearest Neighbour
    	for(int j=0; j<newHeight-1; j++) {
    		for(int i=0; i<newWidth-1; i++) {
    				//System.out.println("i= "+i+" j= "+j+" x = "+x);
    				x = i*oldWidth/newWidth;
    				y = j*oldHeight/newHeight;
    				x1 = Math.floor(x);
    				y1 = Math.floor(y);
    				x2 = x1+1; // ceiling creates errors
    				y2 = y1+1;
    				double c1;
    				double c2;
    				double c3;
    				
    				if(x2>= image.getWidth() || y2>=image.getHeight()) {
    					Color newcol = new Color(0,0,0,0); // when the bounds are exceeded, there is a line of pixels on the side and bottom.
    					// This makes those pixels black to not be stuck out
    					pixelWriter.setColor(i, j, newcol);
    					//System.out.println("Image out of bounds1");
    				}else if(x1 > image.getWidth() || y1 > image.getHeight()) {
    					//System.out.println("Image out of bounds2");
    				}else {
    					Color v1 = pixelReader.getColor((int)x1, (int)y1);
        				Color v2 = pixelReader.getColor((int)x2, (int)y1);
        				double v1_double = v1.getBlue();
        				double v2_double = v2.getBlue();
        				// c is colour, v is value
        				
        				c1 = v1_double+(v2_double-v1_double)*((x-x1)/(x2-x1));
        				c2 = v1_double+(v2_double-v1_double)*((x-x1)/(x2-x1));
        				c3 = c1+(c2-c1)*((y-y1)/(y2-y1));
        				//System.out.println(c3);
        				Color colour = new Color(c3,c3,c3, 1);// all RGB values are the same in the data
        				//System.out.println(colour.getRed());
        				pixelWriter.setColor(i, j, colour);
        				
    				}
    				
    			
    		}
    	}
    	//System.out.println("Bilinear");
    	return outputImage;
    	
    }
    
    //generates a separate window to project the resized image
    public void resizePopUp(WritableImage image, int newWidth, int newHeight) {
    	Stage window = new Stage();
    	ImageView resizedImage = new ImageView(BilResize(image,newWidth,newHeight));
    	window.setTitle("Resized with Bilinear");
    	window.setTitle("Resized");
    	FlowPane root = new FlowPane();
		root.getChildren().addAll(resizedImage);
    	Scene sizeScene = new Scene(root,newWidth-1, newHeight-1);// to remove the extra pixel on the right and bottom
    	window.setScene(sizeScene);
    	window.show();
    }
    
    public int[] Histogram() {
    	int[] histogram=new int[3366];// shows the maximum value a pixel can be
    	int w=256;
    	int h=256;
    	int d = getDepth(w,h);
    	int index;
    	int k;
    	int i;
    	int j;
    	for (j=0; j<h; j++) {
    		for (i=0; i<w; i++) {
    			for(k=0; k<d; k++) {
    				index = cthead[k][i][j]-min;
    				histogram[index]++;
    			}//loop z
        	}//loop x
    	}// loop y
    		
        		//System.out.println(i+" intensity ="+histogram[i]);
        	
    	
    	return histogram;
    }

    //cumulative distribution
	public int[] CDHist() {
    	int[] histogram = Histogram();
    	int[] t = new int[max-min+1];
    	t[0] = histogram[0]; // defining base case
    	for(int i=1; i<=max-min; i++) {
    		t[i] = t[i-1]+histogram[i];
    	}
    	
    	return t;
    }
    
	//generates the histogram graph. Is used with passing both the regular histogram and the cumulative distribution
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void drawHistogram(int[] histogram) {
    	Stage histWindow = new Stage();
    	histWindow.setTitle("Histogram");
    	BorderPane root = new BorderPane();
    	CategoryAxis xAxis = new CategoryAxis();
    	NumberAxis yAxis = new NumberAxis();
    	xAxis.setLabel("Brightness Value");
    	yAxis.setLabel("Number of pixels");
    	BarChart<String, Number> chart = new BarChart<String,Number>(xAxis,yAxis);
    	chart.setTitle("Histogram");
    	root.setCenter(chart);
    	XYChart.Series pixelSeries = new XYChart.Series<>();
    	for(int i=0; i<histogram.length; i++) {
    		String brightnessValue = String.valueOf(i);
    		pixelSeries.getData().add(new XYChart.Data(brightnessValue,histogram[i])); // loops through adding all data in the array into a series
    	}
    	
    	Scene histScene = new Scene(root,500,500);
    	histWindow.setScene(histScene);
    	chart.getData().addAll(pixelSeries);// adds series to map onto the graph
    	histWindow.setScene(histScene);
    	histWindow.show();
    }
    
   
    public int[] mapping() {
    	int[] t = CDHist();
    	int[] mapping = new int[max-min+1];
    	double size = t[max-min-1];
    	
    	for(int i=0; i<max-min; i++) {
    		mapping[i] = (int)(( (double)max-(double)min)*(double)((double)t[i]/(double)size)+(double)min);// double casting looks overkill but most accurate reading as possible
    		
    	}
    	return mapping;
    }

	public void Equalise() {
		int w = 256;
		int h = 256;
		int d = getDepth(w,h);
    	int col;
    	short datum;
    	int[] mapping = mapping();
    	for (int j=0; j<h; j++) {
    		for (int i=0; i<w; i++) {
    			for(int k=0; k<d; k++) {
    				datum = cthead[k][j][i];
    				col = mapping[datum-min];
    				cthead[k][j][i]= (short) col;
    				//System.out.println(cthead[k][j][i]);
    			}//loop z
    		} // loop x
    	} // loop y
	
    	System.out.println("Equalised!");
    	System.out.println(min+" "+max);
	}
    public static void main(String[] args) {
        launch();
    }
    

}