package application;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

// Classe responsável por enviar os valores determinados no sensor
public class MeasuredValuesSender {
	
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	
	private static String topicName = "MeasuredValue";
    
    public String[] valuesAndMessage = new String[6];
    
    public Main main;
    
    public MeasuredValuesSender(Main main) {
    	this.main = main;
    }

    // Enviar os dados coletados
    public void sendMeasuredValuesAndMessage() throws JMSException {

    	// Só enviar uma mensagem se algum parâmetro tiver sido escolhido
    	if(this.main.selectedParameter != null && !this.main.selectedParameter.isEmpty()) {
    		
	        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
	        Connection connection = connectionFactory.createConnection();
	        connection.start();
	
	        Session session = connection.createSession(false,
	                Session.AUTO_ACKNOWLEDGE);
	        
	        valuesAndMessage[0] = this.main.selectedParameter;
	        valuesAndMessage[1] = this.main.minValue;
	        valuesAndMessage[2] = this.main.maxValue;
	        valuesAndMessage[3] = this.main.measuredValue;
	        valuesAndMessage[4] = "Sensor " + this.main.selectedParameter;
	        
	        if( Float.parseFloat(valuesAndMessage[3]) < Float.parseFloat(valuesAndMessage[1])) {
	        	valuesAndMessage[5] = "Alerta! Valor medido é menor \n que o mínimo estabelecido.";
	        }else if(Float.parseFloat(valuesAndMessage[3]) > Float.parseFloat(valuesAndMessage[2])){
	        	valuesAndMessage[5] = "Alerta! Valor medido é maior \n que o máximo estabelecido.";
	        }else {
	        	valuesAndMessage[5] = "Valor medido está dentro dos \n limites estabelecidos.";
	        }
	        
	        Topic topic = session.createTopic(topicName);
	
	        MessageProducer producer = session.createProducer(topic);
	        
	        ObjectMessage objectMessage = session.createObjectMessage();
	        objectMessage.setObject(valuesAndMessage);
	        
	        producer.send(objectMessage);
	        
	        //System.out.println("Sent message from MeasuredValuesSender");
	
	        connection.close();
    	}else {
    		return;
    	}
    }

}
