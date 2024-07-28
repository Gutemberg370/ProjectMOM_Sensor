package application;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

// Classe utilizada para mandar os parâmetros não medidos pelo sensor
public class ParameterMessageSender {

	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    
	private static String queueName = "BrokerLine";
	
	public String[] parametersNotUsed = new String[3];
	
	public Main main;
	
	public void setParameterNotUsed(String[] parametersNotUsed) {
		this.parametersNotUsed = parametersNotUsed;
	}
	
	public String[] getParameterNotUsed() {
		return this.parametersNotUsed;
	}
	
	public ParameterMessageSender(Main main) {
		this.main = main;
	}

	// Enviar os parâmetros não utilizados pelo sensor
	public void sendParametersnotUsed() throws JMSException {

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Destination destination = session.createQueue(queueName);

        MessageProducer producer = session.createProducer(destination);
        
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(parametersNotUsed);
        
        producer.send(objectMessage);
        
        //this.main.callParametersConsumer();
        //System.out.println("Sent message from PARAMETERMESSAGESENDER");

        connection.close();
    }
}
