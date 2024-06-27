package BE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.HttpEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import com.google.api.client.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.javalin.http.ContentType;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterInteractionTool {
	private WebDriver driver;

	static String apiKey = "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";
	static String assisstanId = "asst_F9dMi8lMVWCBhkSzIjqW4W5H";
	static String instructions = "Respond with a sales-oriented approach, like a customer service advisor. Keep your answers concise, easy to understand, and closely aligned with the conversation's contentAnswer the exact question and do not give redundant answersI want the answer to the point. No other content appears";
	static String vectorStoreId = "vs_Vti4nUl8CKiC1yIT35MEyxuA";

	public TwitterInteractionTool() {
		// Khởi tạo WebDriver
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.get("https://twitter.com/home");
	}

	public void start(String link, String textDefau) throws InterruptedException {
		// TODO Auto-generated method stub
		String textInput1 = "";
		driver.get(link);
		try {
			Thread.sleep(5000);
			WebElement textTweet = driver.findElement(By.cssSelector("div[data-testid='tweetText']"));
			textInput1 = textTweet.getText();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<WebElement> listCmt = driver.findElements(By.cssSelector("article[role='article']"));
		for (int i = 1; i < listCmt.size(); i++) {
			WebElement buttonCmt = listCmt.get(i).findElement(By.cssSelector("button[data-testid='reply']"));
			WebElement textCMT = listCmt.get(i).findElement(By.cssSelector("div[data-testid='tweetText']"));
			String textIpnut2 = textCMT.getText();
			buttonCmt.click();
			Thread.sleep(1000);
			Actions ac = new Actions(driver);
			ac.sendKeys(textDefau + ". " + returnContentGPT(textInput1, textIpnut2)).perform();
			WebElement submit = driver.findElement(By.cssSelector("button[data-testid='tweetButton']"));
			submit.click();
			Thread.sleep(5000);
		}

	}

// thêm mô tả yêu cầu câu trả lời
	public static String returnContentGPT(String input1, String input2) {
		String textPost = "Based on the following content and the knowledge you possess. "
				+ input1.replaceAll("[^a-zA-Z0-9 ]", "") + " Please provide an answer to the question. "
				+ input2.replaceAll("[^a-zA-Z0-9 ]", "");
		;
		System.out.println(textPost);
		return genThread(textPost);

	}

// tạo chủ đề mới
	private static String genThread(String messages) {
		// TODO Auto-generated method stub
		String threadId = "";
		String curl = "https://api.openai.com/v1/threads";
		OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder().url(curl).post(body).addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("OpenAI-Beta", "assistants=v1").build();
		try {
			Response response = client.newCall(request).execute();
			byte[] responseBodyBytes = response.body().bytes();
			String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

			JSONObject json = new JSONObject(responseBody);
			System.out.println(json);
			threadId = json.getString("id");
			return addMessagesToThread(threadId, messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

// thêm tin nhắn vào chủ đề
	private static String addMessagesToThread(String threadId, String message) {
		// TODO Auto-generated method stub
		String curl = "https://api.openai.com/v1/threads/" + threadId + "/messages";

		OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\"role\": \"user\", \"content\":\"" + message + "\"}");
		Request request = new Request.Builder().url(curl).post(body).addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("OpenAI-Beta", "assistants=v1").build();

		try {
			Response response = client.newCall(request).execute();
			byte[] responseBodyBytes = response.body().bytes();
			String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

			JSONObject json = new JSONObject(responseBody);
			System.out.println(json);
		} catch (Exception e) {
			// TODO: handle exception
		}
		// run thread
		runThread(threadId);
		return getMessagesReturn(threadId);
	}

// chạy chủ đề, tiến hành hỏi assistant
	private static void runThread(String threadId) {
		// TODO Auto-generated method stub
		MediaType mediaTypeRun = MediaType.parse("application/json");
		OkHttpClient client = new OkHttpClient();
		RequestBody bodyRun = RequestBody.create(mediaTypeRun,
				"{\"assistant_id\": \"asst_F9dMi8lMVWCBhkSzIjqW4W5H\", \"instructions\":\"" + instructions + "\"}");

		Request requestRun = new Request.Builder().url("https://api.openai.com/v1/threads/" + threadId + "/runs")
				.post(bodyRun).addHeader("Authorization", "Bearer " + apiKey).addHeader("OpenAI-Beta", "assistants=v1")
				.build();
		try {
			Response response2 = client.newCall(requestRun).execute();
			byte[] responseBodyBytes = response2.body().bytes();
			String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

			JSONObject json = new JSONObject(responseBody);
			System.out.println(json);
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

// lấy dữ liệu tin nhắn assistant trả về
	private static String getMessagesReturn(String threadId) {
		// TODO Auto-generated method stub
		String curl = "https://api.openai.com/v1/threads/" + threadId + "/messages";
		OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder().url(curl).get().addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("OpenAI-Beta", "assistants=v1").build();
		try {
			Response response = client.newCall(request).execute();
			byte[] responseBodyBytes = response.body().bytes();
			String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

			JSONObject json = new JSONObject(responseBody);

			JSONArray dataArray = json.getJSONArray("data");
			JSONObject message = dataArray.getJSONObject(0);
			JSONArray contentArray = message.getJSONArray("content");
			System.out.println(contentArray);
			// Lấy mảng "content"

			JSONObject jsonObject = contentArray.getJSONObject(0);
			JSONObject textObject = jsonObject.getJSONObject("text");
			System.out.println("---------------------------------");
			return textObject.getString("value");

		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}

	public void close() {
		// Đóng trình duyệt
		driver.quit();
	}

//tải file lên assistant
	public static void upLoadFile(String filePath) {
		String openaiApiKey = apiKey;
		File fileToUpload = new File(convertFile(filePath)); // Đường dẫn đến tệp cần tải lên
		OkHttpClient client = new OkHttpClient();
		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("purpose", "assistants").addFormDataPart("file", fileToUpload.getName(),
						RequestBody.create(MediaType.parse("application/octet-stream"), fileToUpload))
				.build();

		Request request = new Request.Builder().url("https://api.openai.com/v1/files").post(requestBody)
				.addHeader("Authorization", "Bearer " + openaiApiKey).build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);

			byte[] responseBodyBytes = response.body().bytes();
			String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

			JSONObject json = new JSONObject(responseBody);
			System.out.println(json);
			String file_id = json.getString("id");
			System.out.println(file_id);
			addFileToVector(file_id);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

// thêm file vào vectorstore
	public static void addFileToVector(String file_id) {

		OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody requestBody = RequestBody.create(mediaType, "{\"file_ids\": [\"" + file_id + "\"]}");

		Request request = new Request.Builder()
				.url("https://api.openai.com/v1/vector_stores/" + vectorStoreId + "/file_batches").post(requestBody)
				.addHeader("Authorization", "Bearer " + apiKey).addHeader("Content-Type", "application/json")
				.addHeader("OpenAI-Beta", "assistants=v2").build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);

			System.out.println(response.body().string());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// tạo file và chuyển đổi file excel thành file json
	public static String convertFile(String fileInputPath) {
		String excelFilePath = fileInputPath;
		String jsonFilePath = generateJsonFilePath();

		try (FileInputStream fis = new FileInputStream(excelFilePath); Workbook workbook = new XSSFWorkbook(fis)) {

			List<Map<String, Object>> sheetData = new ArrayList<>();
			Sheet sheet = workbook.getSheetAt(0);
			Row headerRow = sheet.getRow(0);

			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null)
					continue; // Skip null rows
				Map<String, Object> rowData = new HashMap<>();
				for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
					Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String columnName = headerRow.getCell(colIndex).getStringCellValue();
					rowData.put(columnName, getCellValue(cell));
				}
				sheetData.add(rowData);
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileOutputStream fos = new FileOutputStream(jsonFilePath)) {
				fos.write(gson.toJson(sheetData).getBytes());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonFilePath;
	}

	private static Object getCellValue(Cell cell) {

		switch (cell.getCellType()) {
		case 1:
			return cell.getStringCellValue();
		case 0:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				return cell.getNumericCellValue();
			}
		case 4:
			return cell.getBooleanCellValue();
		case 2:
			return cell.getCellFormula();
		case 3:
			return "";
		default:
			return "";
		}

	}

// khởi tạo file mới
	private static String generateJsonFilePath() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timestamp = sdf.format(new Date());
		return ".\\file_input_" + timestamp + ".json"; // Đường dẫn tới tệp JSON mới
	}

	
}