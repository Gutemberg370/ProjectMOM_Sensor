package application;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// Consumidor dos tópicos recebidos pelo Broker
public class TopicRequester implements MessageListener{

    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    private static String topicOfInterest = "SendAvailableParameters";
    
    public ObservableList<String> availableParameters = FXCollections.observableArrayList();
    
    public Main main;
    
    public TopicRequester(Main main) {
    	this.main = main;
    }

	public static void requestAvailableParameters(Main main) throws JMSException {
    	new TopicRequester(main).go();
    }

	// Função que criará um consumidor para as mensagens produzidas pelo Broker relacionados
	// a atualização dos parâmetros disponíveis
    public void go() throws JMSException {

    	try {
    		
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(topicOfInterest);

            MessageConsumer consumer = session.createConsumer(topic);

            consumer.setMessageListener(this);
            
    	}catch(Exception e) {
    		e.printStackTrace();
    	}

        
    }

	@Override
	public void onMessage(Message message) {
		// Quando a mensagem é recebida, o comboBox do sensor é atualizado com as opções recebidas
		if (message instanceof ObjectMessage) {
			Object object;
			try {
				object = ((ObjectMessage) message).getObject();
				String[] request = (String[]) object;
				ObservableList<String> listOfParameters = FXCollections.observableArrayList(request);
				// O sensor possui seu próprio parâmetro ja medido e os demais disponíveis como opções
				if(this.main.selectedParameter != null && !this.main.selectedParameter.isEmpty() && !listOfParameters.contains(this.main.selectedParameter)) {
					listOfParameters.add(this.main.selectedParameter);
				}
				availableParameters = listOfParameters;
		        Runnable updateComboBox = () -> {
		            Platform.runLater(() -> {
		            	this.main.chooseParameter.setItems(listOfParameters);	
		            });
		        };
		        Thread updateComboBoxThread = new Thread(updateComboBox);
		        updateComboBoxThread.setDaemon(true);
		        updateComboBoxThread.start();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}    