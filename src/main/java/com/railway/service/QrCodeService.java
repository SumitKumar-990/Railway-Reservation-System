package com.railway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.railway.model.Booking;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    public String generateQrCodeDataUri(Booking booking) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("pnr", booking.getPnr());
            data.put("trainNumber", booking.getTrainRun().getTrain().getTrainNumber());
            data.put("trainName", booking.getTrainRun().getTrain().getName());
            data.put("from", booking.getFromStation().getCode());
            data.put("to", booking.getToStation().getCode());
            data.put("date", booking.getTrainRun().getRunDate().toString());
            data.put("seatClass", booking.getSeatClass().getCode());
            data.put("status", booking.getStatus().name());
            data.put("passengerCount", booking.getPassengers().size());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    json, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
