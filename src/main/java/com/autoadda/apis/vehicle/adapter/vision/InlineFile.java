package com.autoadda.apis.vehicle.adapter.vision;

/** A file to send inline to a vision model: raw bytes plus its MIME type (image/jpeg, application/pdf, ...). */
public record InlineFile(byte[] data, String mimeType) {
}
