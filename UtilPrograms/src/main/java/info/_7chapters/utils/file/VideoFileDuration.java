package info._7chapters.utils.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.xuggle.xuggler.IContainer;

public class VideoFileDuration {

	public static void main(String[] args) throws IOException {
		
		IContainer container = IContainer.make();
		List<String> list = new ArrayList<String>();
		File directory = new File("F:\\MyDocuments\\Studies\\H2K\\Batch22-March29-2016");
		File [] files = directory.listFiles((dir, name) -> {
            return name.toLowerCase().endsWith(".wmv");
        });
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
//	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS",Locale.US);
	    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
	    
	    long totalMinutes = 0;
	    for(File file : files) {
			container.open(file.getAbsolutePath(), IContainer.Type.READ, null);
			long timeInMinutes = TimeUnit.MICROSECONDS.toMinutes(container.getDuration());//(duration / (60 * 1000))/1000;
			
			Path path = Paths.get(file.getAbsolutePath());
			BasicFileAttributes view = Files.readAttributes(path, BasicFileAttributes.class);
		    calendar.setTimeInMillis(view.creationTime().toMillis());
		    list.add(sdf.format(calendar.getTime()) + "\t"+timeInMinutes+"\t" + file.getName());
		    totalMinutes += timeInMinutes;
		}
		
		for(String detail : list){
			System.out.println(detail);
		}
		
		long hours = totalMinutes/60;
		long minutes = totalMinutes % 60;
		System.out.println("\tTotal Time in Hours:minutes======");
		System.out.printf("\t%d:%02d",hours,minutes);
	}
}