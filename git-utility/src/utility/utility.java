package utility;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;



//import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
//import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
//import org.apache.lucene.search.FieldCache.Bytes;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import utility.Debug.DEBUG_CONFIG;

import java.io.BufferedReader;

public class utility {

		public static void main(String[]  args) {
			utility util = new utility();
			String path = "..\\BEL_extractor\\test\\cbdgmlu_\\cbdgmlu_.pdf";
//			String s = util.readFromFile(path);
//			util.writeFile(path + ".txt", s, false);
		}

		
		
		public void writeFile(String path, String s, boolean append) {
			File log_f;
			log_f = new File(path);
			Writer out; 
			try {

				if(!log_f.exists()) {
					Path pathToFile = Paths.get(path);
					if(Files.createDirectories(pathToFile.getParent()) == null || Files.createFile(pathToFile) == null) {
						Debug.print("Failed to create file " + path, DEBUG_CONFIG.debug_error);
						return;
					}
				}
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(new File(path), append), "UTF-8"));
				//				out = new BufferedWriter(new FileWriter(new File(path), append));
				out.append(s);
				out.flush();
				out.close();

			}
			catch(Exception e) {
				Debug.print("Failed to create file " + path, DEBUG_CONFIG.debug_error);
				e.printStackTrace();

			}
		}
		
		String sortMap(Map<String, Integer> freqs) {
//					System.out.println(freqs);
			List<String> words  = null;
			List<Integer> freq = null;
			words = new ArrayList<String>(freqs.keySet());
			freq = new ArrayList<Integer>();
			utility util = new utility();
			util.sort(freqs, words, freq);
//			String s = "";
//			String t= "";
			String m = "";
			for(int i = 0; i < words.size(); i++) {
//				s = s + words.get(i) + ",";
//				t = t + freq.get(i) + ",";
				m = m + words.get(i) + "\t" + freq.get(i) + "\r\n";
				
			}
			
			System.out.println(words);
			System.out.println(freq);
			return m;
		}
		
		public void sort(Map<String, Integer> top_word, List<String> words, List<Integer> freq ) {
			{
				
				for(int j = 0; j < words.size(); j++) freq.add(top_word.get(words.get(j)));
				{//sort words and freq
					for(int t = 0; t < freq.size(); t++) {
						for(int j = t + 1; j < freq.size(); j++) {
							if(freq.get(t) < freq.get(j)) {
								int temp = freq.get(t); freq.set(t, freq.get(j)); freq.set(j, temp);
								String temps = words.get(t); words.set(t, words.get(j)); words.set(j, temps);
							}
						}
					}

				}
			}
		}
		
		 
		 public String readFromFile(String path) {
//			 FileInputStream inputStream;
			 BufferedReader br;
			 try {
//				 inputStream = new FileInputStream(path);
				
					 br= new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			 } catch (Exception e1) {
				 // TODO Auto-generated catch block
				 e1.printStackTrace();
				 return null;
			 }
			 try {
				 String everything = IOUtils.toString(br);
				 return everything;
			 }
			 catch(Exception e) {e.printStackTrace(); return null;}
			 finally {
				 try {
					br.close();
				 } catch (IOException e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 }
			 }

		 }
		 public String readFromFile(File file) {
//			 FileInputStream inputStream;
			 BufferedReader br;
			 try {
//				 inputStream = new FileInputStream(path);
				
					 br= new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			 } catch (Exception e1) {
				 // TODO Auto-generated catch block
				 e1.printStackTrace();
				 return null;
			 }
			 try {
				 String everything = IOUtils.toString(br);
				 return everything;
			 }
			 catch(Exception e) {e.printStackTrace(); return null;}
			 finally {
				 try {
					br.close();
				 } catch (IOException e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 }
			 }

		 }
		
		 
		 /**
		  * This is for special characters that PDFBox is not able to recognize. e.g. μ
		  * http://www.fileformat.info/info/charset/UTF-8/list.htm
		  * @param s
		  * @return
		  */
		 public String fixEncoding(String s) {
			 byte[] bytes;
			 try {
				 bytes = s.getBytes("UTF-8");
//				 String result = new String(bytes, "UTF-8");
//				 System.out.println("+++" + result);
				 List<Byte> newb = new ArrayList<Byte>();
				 for(int i = 0; i < bytes.length; i++) newb.add(bytes[i]);
				 for(int i = 0; i < newb.size(); i++) {
					 //common: 17, 65, 67, 1 // 13 is cr; 10 is line feed
					 if(newb.get(i) == 1) {
						 if(newb.get(i + 1) == 108 || newb.get(i+1) == 103 ) {// "l" or "g"; i.e. (char)bytes[i] + (char)bytes[i+1] is "μl" or "μg"
//							 bytes[i] = (byte) 117; // 52925 is "0x cebc" i.e. μ; 117 for u
							 //add two bytes which combined represents u 
							byte t1 = (byte) -50;
							byte t2 = (byte) -68;
							newb.set(i, t1);
							int size = newb.size();
							newb.add(newb.get(size - 1));
							for(int j = size -1; j > i + 1; j--) newb.set(j, newb.get(j - 1));
							newb.set(i + 1, t2);
							
							
//							 System.out.println((char) bytes[i]);
						 }
					 }
					 //				 String t = "" ;
					 //				 for(int j = 5; j > 0; j--) {
					 //					 t = t + (char) bytes[i - j]; 
					 //				 }
					 //				 for(int j = 0; j < 5; j++) {
					 //					 t = t + (char) bytes[i + j]; 
					 //				 }
					 //				 System.out.println(i + ";" + bytes[i] + "++" + t + "++");
//					 System.out.println(String.format("%02X ", test.getBytes("UTF-8")[1]));
				 }
//				 Bytes.toArray()
				byte[] newbytes = new byte[newb.size()];
				for(int i = 0; i < newb.size(); i++) newbytes[i]=newb.get(i);
				 String result = new String(newbytes, "UTF-8");
				 return result;
			 } catch (UnsupportedEncodingException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
				 return null;
			}
		 }
		 
		 public  String refineTitle(String title) {
					String prefix0 = "b\"[article] -";
					String 	prefix01 = "b\"[article]";
					String 	prefix1 = "b\'[article] - \"";
					String 	prefix2 = "b\'[article] -";
					String prefix3 = "b\'[article]";
					String prefix4 = "b\'[request]";
					String prefix5 = "b\'(request) ";
					String contain1 = "[request]";
					int count = 0;
					String x = title.toLowerCase();
					if(title.toLowerCase().startsWith(prefix0)) {
						x = title.substring(prefix0.length(), title.indexOf('\"',len(prefix0))).trim();
						if(x.endsWith("\'")) x = x.substring(0,len(x) -1);
					}
					else if(title.toLowerCase().startsWith(prefix01)) {
						x = title.substring(len(prefix01), title.indexOf("\"",len(prefix01))).trim();
						if(x.endsWith("\'"))
							x = x.substring(0,len(x) -1);
					}
					else if(title.toLowerCase().startsWith(prefix1)) {
						x =  title.substring(len(prefix1), title.indexOf("\"",len(prefix1))).trim();
						if(x.endsWith("\'"))
							x = x.substring(0,len(x) -1);
					}
					else if(title.toLowerCase().startsWith(prefix2)) {
						x = title.substring(len(prefix2)).trim();
						if(x.endsWith("\'"))
							x = x.substring(0,len(x) -1);
					}
					else if(title.toLowerCase().startsWith(prefix3)) {
						x = title.substring(len(prefix3)).trim();
						if(x.endsWith("\'"))
							x = x.substring(0,len(x) -1);
					}
					else if(title.toLowerCase().startsWith(prefix4)) {
						x = title.substring(len(prefix4)).trim();
						if(x.endsWith("\'"))
							x = x.substring(0,len(x) -1);
					}else if (title.toLowerCase().startsWith(prefix5)) {
						x = title.substring(len(prefix5)).trim();
						if(x.endsWith("\'"))
							x = x.substring(0,len(x) -1);
					}
					else if(title.toLowerCase().contains("request]")) {
						x = title.substring("request]".length());
					}
					else	if(title.toLowerCase().contains("article") || title.toLowerCase().contains("request")) {
							System.out.println("*" + title + "\t" + x);
							count = 1;
					}
					if(x.startsWith("b'")) x = x.substring(2);
					if(x.endsWith("'")) x = x.substring(0, len(x)-1);
//					System.out.println(count);
					if(count ==1) x = "count";
					return x;

		 }
		 
		 private int len(String s) {
			 return s.length();
		 }

		 public static Document stringToDom(String xmlSource) {
			 try {
		        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        return builder.parse(new InputSource(new StringReader(xmlSource)));
			 }
			 catch(Exception e) {
				 e.printStackTrace();
				 return null;
			 }
		    }

		 public static Document stringToDomWithoutDtd(String xmlSource) {
			 try {
		        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        factory.setNamespaceAware(false);
		        factory.setValidating(false);
		        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//		        factory.
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        return builder.parse(new InputSource(new StringReader(xmlSource)));
			 }
			 catch(Exception e) {
				 e.printStackTrace();
				 return null;
			 }
		    }
//		 public static Document stringToDomWithDtd(String xmlSource, String dtd) {
//			 try {
//		        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		        factory.setNamespaceAware(false);
//		        factory.setValidating(false);
//		        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
////		        factory.
//		        DocumentBuilder builder = factory.newDocumentBuilder();
//		        return builder.parse(new InputSource(new StringReader(xmlSource)));
//			 }
//			 catch(Exception e) {
//				 e.printStackTrace();
//				 return null;
//			 }
//		    }
		 public static  String extractURL(String text) {
//			 String s = text.substring(text.toLowerCase().indexOf("http"));
//			 s = s.substring(0, s.indexOf(' ') > 0 ? s.indexOf(' ') : s.length());
//			 s = s.substring(0, s.indexOf('\n') > 0 ? s.indexOf('\n') : s.length());
//			 return s;
			//Pull all links from the body for easy retrieval
			 ArrayList links = new ArrayList();
			  
			 String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
			 Pattern p = Pattern.compile(regex);
			 Matcher m = p.matcher(text);
			 while(m.find()) {
				 String urlStr = m.group();
				 if (urlStr.startsWith("(") && urlStr.endsWith(")"))
				 {
					 urlStr = urlStr.substring(1, urlStr.length() - 1);
				 }
				 links.add(urlStr);
			 }
			 String t = links.toString().replace(",", "; ").replace('[', ' ').replace(']',' ');
			 System.out.println(t + "\t" + text.replace("\n", "++"));
			 return t;
			 	 
		 }
		 
		 public static String readingFromURL(String url) {
			 URL oracle = null;
				try {
					oracle = new URL(url);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					return null;
				}
				String web = null;
				try {
					web = IOUtils.toString(oracle.openStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
				return web;
		 }
		 
//		 public static void unzip(String dirPath) throws IOException {
//			 File dir = new File(dirPath);
//			 File listDir[] = dir.listFiles();
//			 if (listDir.length!=0){
//			     for (File i:listDir){
//			         /*  Warning! this will try and extract all files in the directory
//			             if other files exist, a for loop needs to go here to check that
//			             the file (i) is an archive file before proceeding */
//			         if (i.isDirectory()){
//			             break;
//			         }
//			         String fileName = i.toString();
//			         String tarFileName = fileName +".tar";
//			         FileInputStream instream= new FileInputStream(fileName);
//			         GZIPInputStream ginstream =new GZIPInputStream(instream);
//			         FileOutputStream outstream = new FileOutputStream(tarFileName);
//			         byte[] buf = new byte[1024]; 
//			         int len;
//			         while ((len = ginstream.read(buf)) > 0) 
//			         {
//			             outstream.write(buf, 0, len);
//			         }
//			         ginstream.close();
//			         outstream.close();
//			         //There should now be tar files in the directory
//			         //extract specific files from tar
//			         TarArchiveInputStream myTarFile=new TarArchiveInputStream(new FileInputStream(tarFileName));
//			         TarArchiveEntry entry = null;
//			         int offset;
//			         FileOutputStream outputFile=null;
//			         //read every single entry in TAR file
//			         while ((entry = myTarFile.getNextTarEntry()) != null) {
//			             //the following two lines remove the .tar.gz extension for the folder name
//			              fileName = i.getName().substring(0, i.getName().lastIndexOf('.'));
//			             fileName = fileName.substring(0, fileName.lastIndexOf('.'));
//			             File outputDir =  new File(i.getParent() + "/" + fileName + "/" + entry.getName());
//			             if(! outputDir.getParentFile().exists()){ 
//			                 outputDir.getParentFile().mkdirs();
//			             }
//			             //if the entry in the tar is a directory, it needs to be created, only files can be extracted
//			             if(entry.isDirectory()){
//			                 outputDir.mkdirs();
//			             }else{
//			                 byte[] content = new byte[(int) entry.getSize()];
//			                 offset=0;
//			                 myTarFile.read(content, offset, content.length - offset);
//			                 outputFile=new FileOutputStream(outputDir);
//			                 IOUtils.write(content,outputFile);  
//			                 outputFile.close();
//			             }
//			         }
//			         //close and delete the tar files, leaving the original .tar.gz and the extracted folders
//			         myTarFile.close();
//			         File tarFile =  new File(tarFileName);
//			         tarFile.delete();
//			     }
//			 }
//		 }
//		 public static void unzipOneFile(String filePath) throws IOException {
//			 File i = new File(filePath);
//			 String fileName = i.toString();
//			 String tarFileName = fileName +".tar";
//			 FileInputStream instream= new FileInputStream(fileName);
//			 GZIPInputStream ginstream =new GZIPInputStream(instream);
//			 FileOutputStream outstream = new FileOutputStream(tarFileName);
//			 byte[] buf = new byte[1024]; 
//			 int len;
//			 while ((len = ginstream.read(buf)) > 0) 
//			 {
//				 outstream.write(buf, 0, len);
//			 }
//			 ginstream.close();
//			 outstream.close();
//			 //There should now be tar files in the directory
//			 //extract specific files from tar
//			 TarArchiveInputStream myTarFile=new TarArchiveInputStream(new FileInputStream(tarFileName));
//			 TarArchiveEntry entry = null;
//			 int offset;
//			 FileOutputStream outputFile=null;
//			 //read every single entry in TAR file
//			 while ((entry = myTarFile.getNextTarEntry()) != null) {
//				 //the following two lines remove the .tar.gz extension for the folder name
//				 fileName = i.getName().substring(0, i.getName().lastIndexOf('.'));
//				 fileName = fileName.substring(0, fileName.lastIndexOf('.'));
//				 File outputDir =  new File(i.getParent() + "/" + fileName + "/" + entry.getName());
//				 if(! outputDir.getParentFile().exists()){ 
//					 outputDir.getParentFile().mkdirs();
//				 }
//				 //if the entry in the tar is a directory, it needs to be created, only files can be extracted
//				 if(entry.isDirectory()){
//					 outputDir.mkdirs();
//				 }else{
//					 byte[] content = new byte[(int) entry.getSize()];
//					 offset=0;
//					 myTarFile.read(content, offset, content.length - offset);
//					 outputFile=new FileOutputStream(outputDir);
//					 IOUtils.write(content,outputFile);  
//					 outputFile.close();
//				 }
//			 }
//			 //close and delete the tar files, leaving the original .tar.gz and the extracted folders
//			 myTarFile.close();
//			 File tarFile =  new File(tarFileName);
//			 tarFile.delete();
//		 }

		 public static double round(double x, int decimal) {
			 String s = Double.toString(x);
			 int indexDot = -1;
			 for(int i = 0; i < s.length(); i++) {
				 if(s.charAt(i) == '.') {
					 indexDot = i; break;
				 }
			 }
			 s = s.substring(0, Math.min(indexDot + 3, s.length()));
			 return Double.parseDouble(s);
		 }
		 public void sortByStart(List<Span> input) {
//			 List<Span> result = new ArrayList<Span>();
			 for(int i = 0; i < input.size(); i++) {
				 for(int j = i + 1; j < input.size(); j++) {
					 if(input.get(i).getStart() > input.get(j).getStart()) {
						 Span temp = input.get(i);
						 input.set(i, input.get(j));
						 input.set(j, temp);
					 }
				 }
			 }
		 }
//		 public String getResourceAsString(String fileName) {
//			 ClassLoader classLoader = getClass().getClassLoader();
//			 //				File file = new File(classLoader.getResource("stopwords.txt").getFile());
//			 try {
//				 BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
//				 return IOUtils.toString(br);
//			 } catch (IOException e) {
//				 // TODO Auto-generated catch block
//				
//				 e.printStackTrace();
//				 System.out.println(fileName);
//				 return null;
//			 }
//		 }
//		 public BufferedReader getResourceAsBufReader(String fileName) {
//			 ClassLoader classLoader = getClass().getClassLoader();
//			 //				File file = new File(classLoader.getResource("stopwords.txt").getFile());
//			 BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
//			return br;
//		 }
//		 public InputStream getResourceAsInputStream(String fileName) {
//			return getClass().getClassLoader().getResourceAsStream(fileName);
//		 }
		 
		 public String xmlNodeToString(Document doc){
			try {
				DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
			 DocumentBuilder builder = domFact.newDocumentBuilder();
//			 Document doc = builder.parse(st);
			 DOMSource domSource = new DOMSource(doc);
			 StringWriter writer = new StringWriter();
			 StreamResult result = new StreamResult(writer);
			 TransformerFactory tf = TransformerFactory.newInstance();
			 Transformer transformer;
				transformer = tf.newTransformer();
			 transformer.transform(domSource, result);
//			 System.out.println("XML IN String format is: \n" + writer.toString());
			 return writer.toString();
			 } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		 }
		 
		 public String charToUnicode(char a) {
			 return "\\u" + Integer.toHexString(a | 0x10000).substring(1);
		 }
		 
		 public String toString(BufferedReader br) {
			 try {
				return IOUtils.toString(br);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		 }
	}



