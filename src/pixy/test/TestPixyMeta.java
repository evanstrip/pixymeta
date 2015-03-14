package pixy.test;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import pixy.image.jpeg.JPEGMeta;
import pixy.meta.Metadata;
import pixy.meta.MetadataType;
import pixy.meta.adobe.PhotoshopIPTC;
import pixy.meta.adobe.XMP;
import pixy.meta.adobe._8BIM;
import pixy.meta.exif.Exif;
import pixy.meta.exif.ExifTag;
import pixy.meta.exif.JpegExif;
import pixy.meta.exif.TiffExif;
import pixy.meta.iptc.IPTCApplicationTag;
import pixy.meta.iptc.IPTCDataSet;
import pixy.meta.iptc.IPTCRecord;
import cafe.image.tiff.FieldType;
import cafe.image.tiff.TiffTag;
import cafe.image.util.IMGUtils;
import cafe.string.XMLUtils;

public class TestPixyMeta {

	public static void main(String[] args) throws IOException {
		Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(args[0]);
		System.out.println("Start of metadata information:");
		System.out.println("Total number of metadata entries: " + metadataMap.size());
		int i = 0;
		for(Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
			System.out.println("Metadata entry " + i + " - " + entry.getKey());
			entry.getValue().showMetadata();
			i++;
			System.out.println("-----------------------------------------");
		}
		System.out.println("End of metadata information.");
	
		FileInputStream fin = null;
		FileOutputStream fout = null;
		
		if(metadataMap.get(MetadataType.XMP) != null) {
			XMP xmp = (XMP)metadataMap.get(MetadataType.XMP);
			Document xmpDoc = xmp.getXmpDocument();
			fin = new FileInputStream("images/1.jpg");
			fout = new FileOutputStream("1-xmp-inserted.jpg");
			if(!xmp.hasExtendedXmp())
				Metadata.insertXMP(fin, fout, XMLUtils.serializeToStringLS(xmpDoc, xmpDoc.getDocumentElement()));
			else {
				Document extendedXmpDoc = xmp.getExtendedXmpDocument();
				JPEGMeta.insertXMP(fin, fout, XMLUtils.serializeToStringLS(xmpDoc, xmpDoc.getDocumentElement()), XMLUtils.serializeToStringLS(extendedXmpDoc));
			}
			fin.close();
			fout.close();
		}
		
		Metadata.extractThumbnails("images/iptc-envelope.tif", "iptc-envelope");
	
		fin = new FileInputStream("images/iptc-envelope.tif");
		fout = new FileOutputStream("iptc-envelope-iptc-inserted.tif");
			
		Metadata.insertIPTC(fin, fout, createIPTCDataSet(), true);
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/wizard.jpg");
		fout = new FileOutputStream("wizard-iptc-inserted.jpg");
		
		Metadata.insertIPTC(fin, fout, createIPTCDataSet(), true);
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/1.jpg");
		fout = new FileOutputStream("1-irbthumbnail-inserted.jpg");
		
		Metadata.insertIRBThumbnail(fin, fout, createThumbnail("images/1.jpg"));
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/f1.tif");
		fout = new FileOutputStream("f1-irbthumbnail-inserted.tif");
		
		Metadata.insertIRBThumbnail(fin, fout, createThumbnail("images/f1.tif"));
		
		fin.close();
		fout.close();		

		fin = new FileInputStream("images/exif.tif");
		fout = new FileOutputStream("exif-exif-inserted.tif");
		
		Metadata.insertExif(fin, fout, populateExif(TiffExif.class), true);
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/12.jpg");
		fout = new FileOutputStream("12-exif-inserted.jpg");

		Metadata.insertExif(fin, fout, populateExif(JpegExif.class), true);
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/table.jpg");
		fout = new FileOutputStream("table-metadata-removed.jpg");
		
		Metadata.removeMetadata(fin, fout, MetadataType.IPTC, MetadataType.PHOTOSHOP, MetadataType.ICC_PROFILE, MetadataType.XMP, MetadataType.EXIF);
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/12.jpg");
		fout = new FileOutputStream("12-photoshop-iptc-inserted.jpg");
		
		Metadata.insertIRB(fin, fout, createPhotoshopIPTC(), true);
		
		fin.close();
		fout.close();
		
		fin = new FileInputStream("images/table.jpg");
		JPEGMeta.extractDepthMap(fin, "table");
		
		fin.close();
	}
	
	private static List<IPTCDataSet> createIPTCDataSet() {
		List<IPTCDataSet> iptcs = new ArrayList<IPTCDataSet>();
		iptcs.add(new IPTCDataSet(IPTCRecord.APPLICATION, IPTCApplicationTag.COPYRIGHT_NOTICE.getTag(), "Copyright 2014-2015, yuwen_66@yahoo.com"));
		iptcs.add(new IPTCDataSet(IPTCApplicationTag.CATEGORY.getTag(), "ICAFE"));
		iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS.getTag(), "Welcome 'icafe' user!"));
		
		return iptcs;
	}
	
	private static List<_8BIM> createPhotoshopIPTC() {
		PhotoshopIPTC iptc = new PhotoshopIPTC();
		iptc.addDataSet(new IPTCDataSet(IPTCRecord.APPLICATION, IPTCApplicationTag.COPYRIGHT_NOTICE.getTag(), "Copyright 2014-2015, yuwen_66@yahoo.com"));
		iptc.addDataSet(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS.getTag(), "Welcome 'icafe' user!"));
		iptc.addDataSet(new IPTCDataSet(IPTCApplicationTag.CATEGORY.getTag(), "ICAFE"));
		
		return new ArrayList<_8BIM>(Arrays.asList(iptc));
	}
	
	private static BufferedImage createThumbnail(String filePath) throws IOException {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		BufferedImage thumbnail = IMGUtils.createThumbnail(fin);
		
		fin.close();
		
		return thumbnail;
	}
	
	// This method is for testing only
	private static Exif populateExif(Class<?> exifClass) throws IOException {
		// Create an EXIF wrapper
		Exif exif = exifClass == (TiffExif.class)?new TiffExif() : new JpegExif();
		exif.addImageField(TiffTag.WINDOWS_XP_AUTHOR, FieldType.WINDOWSXP, "Author");
		exif.addImageField(TiffTag.WINDOWS_XP_KEYWORDS, FieldType.WINDOWSXP, "Copyright;Author");
		DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		exif.addExifField(ExifTag.EXPOSURE_TIME, FieldType.RATIONAL, new int[] {10, 600});
		exif.addExifField(ExifTag.FNUMBER, FieldType.RATIONAL, new int[] {49, 10});
		exif.addExifField(ExifTag.ISO_SPEED_RATINGS, FieldType.SHORT, new short[]{273});
		//All four bytes should be interpreted as ASCII values - represents [0220] - new byte[]{48, 50, 50, 48}
		exif.addExifField(ExifTag.EXIF_VERSION, FieldType.UNDEFINED, "0220".getBytes());
		exif.addExifField(ExifTag.DATE_TIME_ORIGINAL, FieldType.ASCII, formatter.format(new Date()));
		exif.addExifField(ExifTag.DATE_TIME_DIGITIZED, FieldType.ASCII, formatter.format(new Date()));
		exif.addExifField(ExifTag.FOCAL_LENGTH, FieldType.RATIONAL, new int[] {240, 10});		
		// Insert ThumbNailIFD
		// Since we don't provide thumbnail image, it will be created later from the input stream
		exif.addThumbnail(null);
		
		return exif;
	}
}