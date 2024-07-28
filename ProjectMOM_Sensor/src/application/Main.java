package application;
	
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

import javax.jms.JMSException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Main extends Application {
	
	private TopicRequester topicRequester = new TopicRequester(this);
	
	private ParameterMessageSender parameterMessageSender = new ParameterMessageSender(this);
	
	private MeasuredValuesSender measuredValuesSender = new MeasuredValuesSender(this);
	
	private ObservableList<String> availableParameters = FXCollections.observableArrayList();
	
	public String selectedParameter;
	
	public String minValue;
	
	public String maxValue;
	
	public String measuredValue;
	
	// Serviço responsável por executar uma thread mais de uma vez
	final ExecutorService service = Executors.newCachedThreadPool();
	
	@FXML ComboBox chooseParameter = new ComboBox();
	
	// Thread responsável por consumir os parâmetros enviados pelo Broker
	final class ParametersConsumer implements Runnable {
	    @Override
	    public void run() {
        	try {
				topicRequester.requestAvailableParameters(Main.this);
			} catch (JMSException e) {
				e.printStackTrace();
			}	

	    }
	}; 
	
	public void callParametersConsumer() {
		service.submit(new ParametersConsumer());
	}
	
	// Função que cria a página do Sensor
	private Parent createSensorPage() {
		
		Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#E27D60"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(566, 444);
    	
    	Label sensorName = new Label("SENSOR");
    	sensorName.setFont(new Font("Monaco",36));
    	sensorName.setLayoutX(200);
    	sensorName.setLayoutY(80);
    	
    	Label parameterLabel = new Label("Parâmetro Monitorado  :");
    	parameterLabel.setFont(new Font("Arial",13));
    	parameterLabel.setLayoutX(55);
    	parameterLabel.setLayoutY(205);
    	
    	chooseParameter.setItems(availableParameters);
    	chooseParameter.setLayoutX(200);
    	chooseParameter.setLayoutY(200);
    	chooseParameter.setMinWidth(220);
    	
    	// Já apto para receber os parâmetros não monitorados
    	callParametersConsumer();
    	
    	// Criar o filtro para os inputs do sensor aceitarem apenas números decimais
        UnaryOperator<TextFormatter.Change> filter = new UnaryOperator<TextFormatter.Change>() {

            @Override
            public TextFormatter.Change apply(TextFormatter.Change change) {

            	String newText = change.getControlNewText();
                // Se a alteração proposta resultar em um valor válido, retorne a alteração como está
                if (newText.matches("[-]?([1-9][0-9]*)?[.]?([1-9][0-9]*)?")) { 
                    return change;
                } else if ("-".equals(change.getText()) ) {

                    // Se o usuário digitar ou colar um "-" no meio do texto atual,
                    // alternar sinal do valor

                    if (change.getControlText().startsWith("-")) {
                        // Se atualmente começa com um "-", remova o primeiro caractere
                        change.setText("");
                        change.setRange(0, 1);
                        // Já que se esta excluindo um caractere em vez de adicionar um,
                        // a posição do cursor precisa recuar um, em vez de 
                        // avançar. Então realiza-se a alteração proposta para
                        // mover o cursor dois lugares antes da alteração proposta
                        change.setCaretPosition(change.getCaretPosition()-2);
                        change.setAnchor(change.getAnchor()-2);
                    } else {
                        // Caso contrário, basta inserir no início do texto
                        change.setRange(0, 0);
                    }
                    return change ;
                }
                // Alteração inválida, vete-a retornando null:
                return null;
            }
        };
        
        
    	Label minValueParameter = new Label("Valor mínimo do parâmetro:");
    	minValueParameter.setFont(new Font("Arial",13));
    	minValueParameter.setLayoutX(32);
    	minValueParameter.setLayoutY(245);
    	
        
    	TextField minValueParameterInput = new TextField();      
    	minValueParameterInput.setTextFormatter(new TextFormatter<>(filter));
    	minValueParameterInput.setLayoutX(200);
    	minValueParameterInput.setLayoutY(240);
    	minValueParameterInput.setMinWidth(220);
    	
    	Label maxValueParameter = new Label("Valor máximo do parâmetro:");
    	maxValueParameter.setFont(new Font("Arial",13));
    	maxValueParameter.setLayoutX(31);
    	maxValueParameter.setLayoutY(285);
    	
        
    	TextField maxValueParameterInput = new TextField();      
    	maxValueParameterInput.setTextFormatter(new TextFormatter<>(filter));
    	maxValueParameterInput.setLayoutX(200);
    	maxValueParameterInput.setLayoutY(280);
    	maxValueParameterInput.setMinWidth(220);
    	
    	Label measuredValueParameter = new Label("Valor medido do parâmetro:");
    	measuredValueParameter.setFont(new Font("Arial",13));
    	measuredValueParameter.setLayoutX(34);
    	measuredValueParameter.setLayoutY(325);
    	
        
    	TextField measuredValueParameterInput = new TextField();      
    	measuredValueParameterInput.setTextFormatter(new TextFormatter<>(filter));
    	measuredValueParameterInput.setLayoutX(200);
    	measuredValueParameterInput.setLayoutY(320);
    	measuredValueParameterInput.setMinWidth(220);
    	
    	Button sendValuesButton = new Button("Registrar valor medido");
    	sendValuesButton.setLayoutX(190);
    	sendValuesButton.setLayoutY(370);
    	sendValuesButton.setMinWidth(150);
    	sendValuesButton.setOnAction(event -> {
    		
    		minValue = minValueParameterInput.getText();
    		maxValue = maxValueParameterInput.getText();
    		measuredValue = measuredValueParameterInput.getText();
    		
    		// Registrar qual o parâmetro está sendo utilizado e quais não estão sendo utilizados
    		ArrayList<String> parametersNotUsed = new ArrayList<String>();
    		chooseParameter.getItems().forEach( (item) -> 
    					{
    					   if(chooseParameter.getValue().equals((item))) {
    						   selectedParameter = (String) item;
     					   }
    					   
    					   if(!chooseParameter.getValue().equals((item)) && !parametersNotUsed.contains((String) item)) {
    						   parametersNotUsed.add((String) item);
    					   }
    						
    					});
    		
    		// Enviando os valores medidos pelo Sensor ao Broker
    		try {
				this.measuredValuesSender.sendMeasuredValuesAndMessage();
			} catch (JMSException e) {
				e.printStackTrace();
			}
    		
    		// Se o usuário tiver escolhido um parâmetro para o sensor e a lista de parâmetros disponíveis
    		// não é exatamente igual a lista mandada anteriormente, atualiza o Broker com a nova lista de
    		// parâmetros disponíveis
    		if(chooseParameter.getValue() != null && !parametersNotUsed.containsAll(new ArrayList<String>(Arrays.asList(this.parameterMessageSender.getParameterNotUsed())))) {
        		this.parameterMessageSender.setParameterNotUsed(parametersNotUsed.toArray(new String[0]));
        		try {
    				this.parameterMessageSender.sendParametersnotUsed();
    			} catch (NumberFormatException e) {
    				e.printStackTrace();
    			} catch (JMSException e) {
    				e.printStackTrace();
    			}
    		}
        });
    	
    	root.getChildren().addAll(sensorName, parameterLabel, chooseParameter, minValueParameter, minValueParameterInput, maxValueParameter, maxValueParameterInput,
    			measuredValueParameter, measuredValueParameterInput, sendValuesButton);
    	
    	return root;
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Scene sensorPage = new Scene(createSensorPage());
			primaryStage.setScene(sensorPage);
			primaryStage.setTitle("Sensor");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		launch(args);
	}
}
