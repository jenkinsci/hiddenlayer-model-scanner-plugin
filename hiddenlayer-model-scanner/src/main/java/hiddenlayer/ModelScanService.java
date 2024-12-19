package hiddenlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiddenlayer.sdk.ApiClient;
import hiddenlayer.sdk.ApiException;
import hiddenlayer.sdk.rest.ModelScanApi;
import hiddenlayer.sdk.rest.ModelSupplyChainApi;
import hiddenlayer.sdk.rest.SensorApi;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.openapitools.client.model.*;

public class ModelScanService {
    private Config config;
    private ModelSupplyChainApi modelSupplyChainApi;
    private SensorApi sensorApi;
    private ModelScanApi modelScanApi;

    public ModelScanService(Config config) throws java.lang.Exception {
        this.config = config;
        ApiClient client = new ApiClient();
        String token = this.getJwt();
        client.setRequestInterceptor((request) -> {
            request.header("Authorization", "Bearer " + token);
        });
        this.modelSupplyChainApi = new ModelSupplyChainApi(client);
        this.sensorApi = new SensorApi(client);
        this.modelScanApi = new ModelScanApi(client);
    }

    public ScanReportV3 scanFile(String modelName, String modelPath)
            throws ApiException, IOException, URISyntaxException, InterruptedException, java.lang.Exception {
        this.submitFileToModelScanner(modelPath, modelName);

        ScanReportV3 scanReport = this.getScanResults(modelName);

        Double baseDelay = 100.0; // milliseconds
        Integer retries = 0;

        while (scanReport == null
                || (scanReport.getStatus() != ScanReportV3.StatusEnum.DONE
                        && scanReport.getStatus() != ScanReportV3.StatusEnum.FAILED)) {
            retries += 1;
            Double delay = baseDelay * Math.pow(2, retries) + Math.random();
            try {
                Thread.sleep(delay.longValue());
            } catch (InterruptedException e) {
                return null;
            }

            scanReport = this.getScanResults(modelName);
        }

        return scanReport;
    }

    public ScanReportV3 scanFolder(String modelName, String folderPath)
            throws ApiException, IOException, URISyntaxException, InterruptedException, java.lang.Exception {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new java.lang.Exception("Folder path is not a directory");
        }
        File tempFile = File.createTempFile("temp", ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
        File[] files = folder.listFiles();
        String msg = "";
        for (File file : files) {
            if (file.isFile()) {
                FileInputStream in = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(file.getName());
                out.putNextEntry(entry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        }
        out.finish();
        out.flush();
        out.close();

        return this.scanFile(modelName, tempFile.getAbsolutePath());
    }

    private Model submitFileToModelScanner(String modelPath, String modelName)
            throws ApiException, IOException, FileNotFoundException, URISyntaxException, InterruptedException,
                    java.lang.Exception {
        File file = new File(modelPath);

        Model model = this.sensorApi.createSensor(
                new CreateSensorRequest().plaintextName(modelName).adhoc(true));
        GetMultipartUploadResponse upload =
                this.sensorApi.beginMultipartUpload(model.getSensorId(), new BigDecimal(file.length()));

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            for (int i = 0; i < upload.getParts().size(); i++) {

                MultipartUploadPart part = upload.getParts().get(i);
                BigDecimal size = part.getEndOffset().subtract(part.getStartOffset());
                byte[] buffer = inputStream.readNBytes(size.intValue());

                if (part.getUploadUrl() != null) {
                    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                            .uri(new URI(part.getUploadUrl()))
                            .header("Content-Type", "application/octet-stream")
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(buffer));
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = requestBuilder.build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        throw new java.lang.Exception("Failed to upload part " + part.getPartNumber());
                    }
                } else {
                    this.sensorApi.uploadModelPart(
                            model.getSensorId(), upload.getUploadId(), part.getPartNumber(), buffer);
                }
            }

            this.sensorApi.completeMultipartUpload(model.getSensorId(), upload.getUploadId());
            this.modelScanApi.scanModel(model.getSensorId(), null);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return model;
    }

    private ScanReportV3 getScanResults(String modelName) throws ApiException {
        SensorSORModelCardQueryResponse response =
                this.sensorApi.sensorSorApiV3ModelCardsQueryGet(modelName, null, 1, null, null);
        UUID modelId = response.getResults().get(0).getModelId();
        ArrayList<String> modelIdList = new ArrayList<String>();
        modelIdList.add(modelId.toString());
        ModelScanApiV3ScanQuery200Response scans = this.modelSupplyChainApi.modelScanApiV3ScanQuery(
                null, modelIdList, null, null, null, null, null, null, null, true);
        if (scans.getTotal().intValue() == 0 || scans.getItems() == null) {
            return null;
        }

        ScanReportV3 scan = scans.getItems().get(0);
        UUID scanUuid = UUID.fromString(scan.getScanId());
        ScanReportV3 scanReport = this.modelSupplyChainApi.modelScanApiV3ScanModelVersionIdGet(scanUuid, null);
        return scanReport;
    }

    private String getJwt() throws java.lang.Exception, IOException, URISyntaxException {
        String tokenUrl = "https://auth.hiddenlayer.ai/oauth2/token?grant_type=client_credentials";
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(tokenUrl))
                .header(
                        "Authorization",
                        "Basic "
                                + Base64.getEncoder()
                                        .encodeToString(
                                                (this.config.getHlClientId() + ":" + this.config.getHlClientSecret())
                                                        .getBytes()))
                .POST(HttpRequest.BodyPublishers.noBody());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new java.lang.Exception("Failed to get JWT token: " + response.statusCode() + ": " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        HashMap responseMap = mapper.readValue(response.body(), HashMap.class);

        return responseMap.get("access_token").toString();
    }
}
