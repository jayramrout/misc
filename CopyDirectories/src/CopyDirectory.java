import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyDirectory {
	public static String sourceFolder;
	public static String destinationFolder;

	public static void main(String arg[]) {
		if(arg.length != 2) {
			System.out.println("Run Program with the Source and Destination Folder as Arguments");
			System.exit(0);
		}
		sourceFolder = arg[0];
		destinationFolder = arg[1];
		try {
			System.out.println("Unzipping Starts...");
			unZipFolder(destinationFolder);
			System.out.println("Unzipping Ends...");

			System.out.println("Copying folders from "+sourceFolder + " to "+destinationFolder+" ...");
			copyDirectory(new File(sourceFolder), new File(destinationFolder));
			System.out.println("Copying done...");

			System.out.println("Zipping Started...");
			zipFolder(destinationFolder);
			System.out.println("Zipping done...");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void zipFolder(String directoryToZip) throws IOException {
		Zip.zip(directoryToZip);
	}

	public static void unZipFolder(String directoryToZip) throws IOException {
		UnZip.unZip(directoryToZip);
	}

	public static void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			if (targetLocation.exists())
				return;
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
