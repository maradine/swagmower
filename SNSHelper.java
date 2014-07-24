import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;

public class SNSHelper {


	public static void publish(String accessKey, String secretKey, String SNSARN, String message) {

		AmazonSNSClient service = new AmazonSNSClient(new BasicAWSCredentials(accessKey, secretKey));
		PublishRequest publishReq = new PublishRequest().withTopicArn(SNSARN).withMessage(message);
		service.publish(publishReq);

	}

	public static void subscribe(String accessKey, String secretKey, String SNSARN, String number) {

		AmazonSNSClient service = new AmazonSNSClient(new BasicAWSCredentials(accessKey, secretKey));
		SubscribeRequest subRequest = new SubscribeRequest(SNSARN, "sms", number);
		service.subscribe(subRequest);

	}

}
