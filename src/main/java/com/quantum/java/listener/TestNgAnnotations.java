package com.quantum.java.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.qmetry.qaf.automation.core.ConfigurationManager;


public class TestNgAnnotations implements ISuiteListener{

	@Override
	public void onFinish(ISuite arg0) {	
		try {
			postExecution();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart(ISuite arg0) {	
		ConfigurationManager.getBundle().setProperty("Driver.startDate", System.currentTimeMillis());
	}

	private void postExecution() throws IOException {
		sendEmailPostExecution();
	}

	private void sendEmailPostExecution() throws IOException {
		if(ConfigurationManager.getBundle().getString("perfecto.email.send").equalsIgnoreCase("true")) {
			String file = null;
			String body = null;
			file = getSuitePDFReport();
			body = getBody(file);

			String id;
			try {
				HttpClient httpClient = HttpClientBuilder.create().build();
				HttpResponse getCommandsResponse = getStartExecution(httpClient);

				if (HttpStatus.SC_OK == getCommandsResponse.getStatusLine().getStatusCode()) {
					id = getId(getCommandsResponse);
					sendEmail(body, id);
					endConnection(id, httpClient);
				} else {
					String errorMsg = IOUtils.toString(getCommandsResponse.getEntity().getContent(), Charset.defaultCharset());
					System.err.println("Error downloading file. Status: " + getCommandsResponse.getStatusLine() + ".\nInfo: " + errorMsg);
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	/**
	 * Gets the execution id
	 * @param getCommandsResponse
	 * @return
	 * @throws IOException
	 */
	protected String getId(HttpResponse getCommandsResponse) throws IOException {
		String id;
		try (InputStreamReader inputStreamReader = new InputStreamReader(getCommandsResponse.getEntity().getContent())) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonObject commands = gson.fromJson(IOUtils.toString(inputStreamReader), JsonObject.class);
			id = commands.get("executionId").toString();					 
		}
		id = id.toString().replaceAll("\"", "");
		return id;
	}

	protected HttpResponse getStartExecution(HttpClient httpClient) throws IOException, ClientProtocolException {
		HttpResponse getCommandsResponse = httpClient.execute(new HttpGet("https://" 
				+ ConfigurationManager.getBundle().getString("perfecto.email.cloud") 
				+ "/services/executions?operation=start&user=" 
				+ ConfigurationManager.getBundle().getString("perfecto.capabilities.user") 
				+ "&password=" + ConfigurationManager.getBundle().getString("perfecto.capabilities.password")));
		return getCommandsResponse;
	}

	/**
	 * Gets the email body contents
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected String getBody(String file) throws IOException {

		String body;

		String pdfText = getPDFtoText(file);
		pdfText = pdfText.replaceAll("View report", "")
				.replaceAll("View detailed list in new Reporting", "==============================")
				.replaceAll("", ":").replaceAll("General", "==============================")
				.replaceAll("Name Status", "Name : Status")
				.replaceAll("Execution Summary", "\n====Execution Summary====\n");
		String[] text = pdfText.split("==============================");

		pdfText = text[0]	
				.replaceAll("\n", "")
				.replaceAll("Summary====", "Summary====\n")				
				.replaceAll("TESTS", " : TESTS\n")
				.replaceAll("FAILED", " : FAILED\n")
				.replaceAll("PASSED", " : PASSED\n")
				.replaceAll("UNKNOWN", " : UNKNOWN")
				+ "\n\n====Individual Results====" 
				+ text[2]
						.replaceAll("\n:\n", " : ")
						.replaceAll("\n\n", " : ");

		if( System.getProperties().getProperty("reportium-job-name") != null) {
			body =  "Perfecto Suite report: "
					+ "https://" 
					+ ConfigurationManager.getBundle().getString("perfecto.email.reporting") 
					+ "/?jobName[0]="
					+ System.getProperties().getProperty("reportium-job-name") 
					+ "&jobNumber[0]="
					+ System.getProperties().getProperty("reportium-job-number") 
					+ ""
					+ "\n\n"
					+ pdfText;
		}else{
			body=pdfText;			
		}

		try {
			body=java.net.URLEncoder.encode(body,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return body;
	}
	
	/**
	 * Sends email
	 * @param body
	 * @param id
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	protected void sendEmail(String body, String id) throws MalformedURLException, IOException, ProtocolException {
		String emailList = ConfigurationManager.getBundle().getString("perfecto.email.to");
		String[] email = emailList.split(",");		
		for(int i = 0; i < email.length;i++){
			System.out.println("Mail to:" + email[i].toString());
			String url = "https://"
					+ ConfigurationManager.getBundle().getString("perfecto.email.cloud")
					+ "/services/executions/" 
					+ id 
					+ "?operation=command&user=" + ConfigurationManager.getBundle().getString("perfecto.capabilities.user") 
					+ "&password=" + ConfigurationManager.getBundle().getString("perfecto.capabilities.password") 
					+ "&command=gateway&subcommand=email&param.to.address=" + email[i]
							+ "&param.subject=" + ConfigurationManager.getBundle().getString("perfecto.email.subject") 
							+ "&param.body=" + body;
			url = url.replace(" ", "%20");
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			System.out.println("Response Message : " + con.getResponseMessage());

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			System.out.println("Response String:" + response.toString());
		}
	}

	/**
	 * Ends connection
	 * @param id
	 * @param httpClient
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	protected void endConnection(String id, HttpClient httpClient) throws IOException, ClientProtocolException {
		HttpResponse getCommandsResponse;
		getCommandsResponse = httpClient.execute(new HttpGet("https://" 
				+ ConfigurationManager.getBundle().getString("perfecto.email.cloud") 
				+ "/services/executions/"
				+ id
				+ "?operation=end&user=" 
				+ ConfigurationManager.getBundle().getString("perfecto.capabilities.user") 
				+ "&password=" + ConfigurationManager.getBundle().getString("perfecto.capabilities.password")));

		try (InputStreamReader inputStreamReader = new InputStreamReader(getCommandsResponse.getEntity().getContent())) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonObject commands = gson.fromJson(IOUtils.toString(inputStreamReader), JsonObject.class);
			System.out.println("\nList of commands response:\n" + gson.toJson(commands));
		}
		System.out.println("Command response string: " + getCommandsResponse.toString());
	}

	/**
	 * Gets the PDF contents to text
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected String getPDFtoText(String file) throws IOException, FileNotFoundException {
		PdfReader reader = new PdfReader(file);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		PrintWriter out = new PrintWriter(new FileOutputStream("C://Temp//text.txt"));
		TextExtractionStrategy strategy = null;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			strategy = parser.processContent(i, new SimpleTextExtractionStrategy());			
		}
		reader.close();
		out.flush();
		out.close();
		System.out.println("PDF to Text output: \n" + strategy.getResultantText());
		return strategy.getResultantText().toString();
	}

	/**
	 * Gets the suite PDF Report from Perfecto
	 * @return
	 * @throws IOException
	 */
	public String getSuitePDFReport() throws IOException{
		String filePath="";
		boolean downloadComplete = false;
		for (int attempt = 1; attempt <= 12 && !downloadComplete; attempt++) {
			HttpGet httpGet = new HttpGet(new TestNGListener().generatePDF().toString());	 
			httpGet.addHeader("PERFECTO_AUTHORIZATION", ConfigurationManager.getBundle().getString("perfecto.token"));

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = httpClient.execute(httpGet);
			FileOutputStream fileOutputStream = null;
			downloadComplete = false;
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				if (HttpStatus.SC_OK == statusCode) {
					File file = new File(System.getProperty("user.dir")+ "\\target\\Suite_Report.pdf");
					try{
						file.delete();
					}catch(Exception e){}
					fileOutputStream = new FileOutputStream(file);
					IOUtils.copy(response.getEntity().getContent(), fileOutputStream);
					filePath = file.getAbsolutePath().toString();
					System.out.println("\nSaved " + "Execution report" + " to: " + file.getAbsolutePath());
					downloadComplete = true;
				} else if (HttpStatus.SC_NO_CONTENT == statusCode) {

					// if the execution is being processed, the server will respond with empty response and status code 204
					System.out.println("\nThe server responded with 204 (no content). " +
							"The execution is still being processed. Attempting again in 5 sec ");
					Thread.sleep(5000);
				} else {
					String errorMsg = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
					System.err.println("Error downloading file. Status: " + response.getStatusLine() + ".\nInfo: " + errorMsg);
					downloadComplete = true;
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				EntityUtils.consumeQuietly(response.getEntity());
				IOUtils.closeQuietly(fileOutputStream);
			}
		}
		if (!downloadComplete) {
			System.err.println("The execution is still being processed. No more download attempts");
		}		
		return filePath;
	}

}

