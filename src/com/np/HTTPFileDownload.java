package com.np;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class HTTPFileDownload {

	public static final int EOF = -1;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	public static void main(String[] args) {
		String fileURL = "http://i21.indiworlds.com:182/d/wuibobvq7db4joaxqiceb7foz5sxlaknpbbjjhsm2eovwo7znfu75cqm/World4uFRee.pw_Srt100Mar.mkv";
		String saveDir = "D:/Download";
		try {
			HTTPFileDownload.downloadFile(fileURL, saveDir);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void downloadFile(String fileURL, String saveDir) throws IOException {
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setConnectTimeout(6000);
		httpConn.setReadTimeout(60000);
		int responseCode = httpConn.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {

			String fileName = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");
			String contentType = httpConn.getContentType();
			int contentLength = httpConn.getContentLength();

			/*
			 * if (disposition != null) { // extracts file name from header
			 * field int index = disposition.indexOf("filename="); if (index >
			 * 0) { fileName = disposition.substring(index + 10,
			 * disposition.length() - 1); } } else {
			 */
			// extracts file name from URL
			fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
			// }

			System.out.println("Content-Type = " + contentType);
			System.out.println("Content-Disposition = " + disposition);
			System.out.println("Content-Length = " + contentLength);
			System.out.println("fileName = " + fileName);

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			String saveFilePath = saveDir + File.separator + fileName;

			File destination = new File(saveFilePath);
			// opens an output stream to save into file
			FileOutputStream outputStream = openOutputStream(destination);
			long dounloadSize = 0;
			try {

				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				dounloadSize = copyLarge(inputStream, outputStream, buffer);
				outputStream.close();
			} catch (IOException ex) {
				closeQuietly(outputStream);
				if (dounloadSize < contentLength) {
					forceDelete(destination);
				}
			} finally {
				closeQuietly(outputStream);
			}
			inputStream.close();

			System.out.println("File downloaded");
		} else {
			System.out.println("No file to download. Server replied HTTP code: " + responseCode);
		}
		httpConn.disconnect();
	}

	public static void forceDelete(final File file) throws IOException {
		if (file.isDirectory()) {
		} else {
			
			final boolean filePresent = file.exists();
			try{
			Files.delete(file.toPath());
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
	}

	public static FileOutputStream openOutputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			final File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file);
	}

	public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
			throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static void copyURLToFile(URL source, File destination) throws IOException {
		// does destination directory exist ?
		if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
			destination.getParentFile().mkdirs();
		}

		// make sure we can write to destination
		if (destination.exists() && !destination.canWrite()) {
			String message = "Unable to open file " + destination + " for writing.";
			throw new IOException(message);
		}

		InputStream input = source.openStream();
		try {
			FileOutputStream output = new FileOutputStream(destination);
			try {
				copy(input, output);
			} finally {
				closeQuietly(output);
			}
		} finally {
			closeQuietly(input);
		}
	}

	public static void closeQuietly(OutputStream output) {
		try {
			if (output != null) {
				output.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
