package org.opts.sols.solver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * This servlet demonstrates how to receive file uploaded from the client
 * without using third-party upload library such as Commons File Upload.
 * 
 * @author www.codejava.net
 */
@WebServlet("/UploadDownloadFileServlet")
public class UploadDownloadFileServlet extends HttpServlet {

	static final String SAVE_DIR = "D:/res/";
	static final int BUFFER_SIZE = 4096;
	private static final long serialVersionUID = 1L;
	private ServletFileUpload uploader = null;

	public void init() throws ServletException {
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		File filesDir = (File) getServletContext().getAttribute("FILES_DIR_FILE");
		fileFactory.setRepository(filesDir);
		this.uploader = new ServletFileUpload(fileFactory);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fileName = request.getParameter("fileName");
		if (fileName == null || fileName.equals("")) {
			throw new ServletException("File Name can't be null or empty");
		}
		File file = new File(request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileName);
		if (!file.exists()) {
			throw new ServletException("This File doesn't exists on server." + request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileName);
		}
		System.out.println("File location on server::" + file.getAbsolutePath());
		ServletContext ctx = getServletContext();
		InputStream fis = new FileInputStream(file);
		String mimeType = ctx.getMimeType(file.getAbsolutePath());
		response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		ServletOutputStream os = response.getOutputStream();
		byte[] bufferData = new byte[1024];
		int read = 0;
		while ((read = fis.read(bufferData)) != -1) {
			os.write(bufferData, 0, read);
		}
		os.flush();
		os.close();
		fis.close();
		System.out.println("File downloaded at client successfully");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Gets file name for HTTP header
		String fileName = request.getHeader("fileName");
		File saveFile = new File(request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileName);

		// prints out all header values
		System.out.println("===== Begin headers =====");
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String headerName = names.nextElement();
			System.out.println(headerName + " = " + request.getHeader(headerName));
		}
		System.out.println("===== End headers =====\n");

		// opens input stream of the request for reading data
		InputStream inputStream = request.getInputStream();

		// opens an output stream for writing file
		FileOutputStream outputStream = new FileOutputStream(saveFile);

		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;
		System.out.println("Receiving data...");

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}

		System.out.println("Data received.");
		outputStream.close();
		inputStream.close();

		System.out.println("File written to: " + saveFile.getAbsolutePath());

		// sends response to client
		response.getWriter().print("UPLOAD DONE TO : " +request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileName);
	}
}