package org.factpub.factify.test;

import java.util.ArrayList;
import java.util.List;

import org.factpub.factify.nlp.Sequence;
import org.factpub.factify.nlp.StanfordNLPLight;
import org.factpub.factify.utility.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Test_NLP {

	public static void main(String[] args) {
		testXMLPaper();
		if(true) return;

		// TODO Auto-generated method stub
		List<Sequence> results = StanfordNLPLight.INSTANCE.textToSequence("The rate grows by 2.1 percent.", false);
		for(Sequence seq : results) {
//			System.out.println(seq.getw);
//			System.out.println(seq.stems);
//			System.out.println(seq.POSTags);
		}
	}
	
	public static void testXMLPaper(){
		
		String path = "C:\\Users\\huangxc\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\5266ax06.grant-zotero\\zotero\\cbdgmlu_text_withoutspace.xml";
		String xmlContent = Utility.readFromFile(path);
		xmlContent = xmlContent.replace("\n", " ").replace("\r", "");
		Document xmlDoc = Utility.stringToDomWithoutDtd(xmlContent);
		if(xmlDoc == null) return ;
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		System.out.println("loading models:" + (end-start));
		List<List<Sequence>> allSequences = new ArrayList<List<Sequence>>();
		
		{
			NodeList paras = xmlDoc.getElementsByTagName("p");
			for(int i = 0; i < paras.getLength(); i++) {
				Node para = paras.item(i);
				String para_text = para.getTextContent().replace("&lt;", "<").replace("&amp;", "&").replace("&gt;", ">");
				List<Sequence> seqOfPara =  StanfordNLPLight.INSTANCE.textToSequence(para_text, true);
				allSequences.add(seqOfPara);
			}
		}
		
		end = System.currentTimeMillis();
		System.out.println("Time cost: " + (end-start)); 
		
		String output = "";
		for(List<Sequence> l : allSequences) {
//		for(Sequence s : l) {
//			output += "**\t"+s.getSourceString() + "\r\n";
//			output += "++\t";
//			for(String stem : s.stems) output += stem + "\t";
//			output += "++\r\n";
//			output += "--\t";
//			for(String tag : s.POSTags) output += tag + "\t";
//			output += "--\r\n\rn";
//		}
			output += "--------------------------------------------------\r\n";
		}
		Utility.writeFile("D:\\GitHub\\grant-zotero\\test on nlp_compromise\\stanford_cbdgmlu_text_withoutspace.xml", output, false);
	}

}
