package backend;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class Jenkins {
	
	static // Credentials
    String username = "user";
    static String password = "pw";

    // Jenkins url
    static String jenkinsUrl = "http://localhost:8080/jenkins";

    // Build name
    static String jobName = "BO4CO";

    // Build token
    static String buildToken = "token";
    
    // Parameters - not required
	static String s = "test";
	static Boolean b = true;
	
	// Create your httpclient
    private static DefaultHttpClient client = new DefaultHttpClient();
    private static BasicHttpContext context;
	
	private static void authenticate() {

	    // Then provide the right credentials
	    client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
	            new UsernamePasswordCredentials(username, password));

	    // Generate BASIC scheme object and stick it to the execution context
	    BasicScheme basicAuth = new BasicScheme();
	    context = new BasicHttpContext();
	    context.setAttribute("preemptive-auth", basicAuth);

	    // Add as the first (because of the zero) request interceptor
	    // It will first intercept the request and preemptively initialize the authentication scheme if there is not
	    client.addRequestInterceptor(new Jenkins().new PreemptiveAuth(), 0);
		
	}
	
	private static String csrf() {
	    // get csrf token
	    StringBuilder sb = new StringBuilder(jenkinsUrl);
	    sb.append("/crumbIssuer/api/json");
	    String csrfUrl = sb.toString();
	    HttpGet getcsrf = new HttpGet(csrfUrl);
	    String csrf = null;
	    try {
	        // Execute your request with the given context
	        HttpResponse response = client.execute(getcsrf, context);
	        System.out.println(response.getStatusLine());
	        HttpEntity entity = response.getEntity();
	        String token = EntityUtils.toString(entity);
	        int crumbindex = token.indexOf("\"crumb\":");
	        csrf = token.substring(crumbindex+9, crumbindex+41);
	        EntityUtils.consume(entity);
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    
	    System.out.println("csrf token: " + csrf);
	    return csrf;
		
	}
    
    public static void request(File file) {
    	
    	authenticate();
    	String csrf = csrf();

	    // You post request that will start the build
	    StringBuilder sb = new StringBuilder(jenkinsUrl);
	    sb.append("/job/").append(jobName).append("/build?token=").append(buildToken);
	    sb.append("&Jenkins-Crumb=").append(csrf);
	    String postUrl = sb.toString();
		System.out.println(postUrl);
	    HttpPost post = new HttpPost(postUrl);
	    
		sb = new StringBuilder("{ \"parameter\": [");
		sb.append("{\"name\":\"expconfig.yaml\",\"file\":\"file0\"}, ");
		sb.append("{\"name\":\"string\", \"value\":\"" + s +"\"},");
		String bool = b? "TRUE" : "FALSE";
		sb.append("{\"name\":\"bool\", \"value\":\"" + bool + "\"}");
		String payLoad = sb.append("  ] }").toString();
		
		HttpEntity postentity = MultipartEntityBuilder.create()
	    		
	            .addTextBody("json", payLoad, ContentType.TEXT_PLAIN)
	            .addBinaryBody("file0", file)
	            .build();
	    post.setEntity(postentity);
	
		
		System.out.println("posted");
	
	    try {
	        // Execute your request with the given context
	        HttpResponse response = client.execute(post, context);
	        HttpEntity result = response.getEntity();
	        result.writeTo(System.out);
	        //System.out.println(response.toString());
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
    
    }
    
    public static String[] checkOutput() {
    	
    	authenticate();
    	
    	StringBuilder sb = new StringBuilder(jenkinsUrl);
    	sb.append("/job/").append(jobName).append("/lastSuccessfulBuild/consoleText");
    	
    	HttpGet get = new HttpGet(sb.toString());
    	
    	StringWriter writer = new StringWriter();
    	
    	try {
    		HttpResponse response = client.execute(get, context);
    		HttpEntity result = response.getEntity();
    		IOUtils.copy(result.getContent(), writer, "UTF-8");
    	} catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
    	
    	String response = writer.toString();
    	String[] lines = response.split("\\R");
    	ArrayDeque<String> outputs = new ArrayDeque<String>();
    	for (String line : lines) {
    		if (line.startsWith("- [")) {
    			outputs.add(line.substring(3, line.length()-1));
    		}    			
    	}
    	return outputs.toArray(new String[outputs.size()]);
    }
    
    public static boolean checkFinished() {
    	
    	authenticate();
    	
    	StringBuilder sb = new StringBuilder(jenkinsUrl);
    	sb.append("/job/").append(jobName).append("/lastBuild/api/json");
    	
    	HttpGet get = new HttpGet(sb.toString());
    	
    	StringWriter writer = new StringWriter();
    	
    	try {
    		HttpResponse response = client.execute(get, context);
    		HttpEntity result = response.getEntity();
    		IOUtils.copy(result.getContent(), writer, "UTF-8");
    	} catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
    	
    	String response = writer.toString();
    	return response.contains("result\":null");
    }
    
    class PreemptiveAuth implements HttpRequestInterceptor {
    	 
        /*
         * (non-Javadoc)
         *
         * @see org.apache.http.HttpRequestInterceptor#process(org.apache.http.HttpRequest,
         * org.apache.http.protocol.HttpContext)
         */
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            // Get the AuthState
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
 
            // If no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context
                        .getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost
                            .getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
 
        }
 
    }

}
