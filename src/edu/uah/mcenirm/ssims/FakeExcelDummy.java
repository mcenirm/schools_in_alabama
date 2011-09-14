package edu.uah.mcenirm.ssims;


import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tools.ant.filters.TokenFilter;
import org.apache.tools.ant.filters.TokenFilter.ReplaceString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class FakeExcelDummy extends DefaultHandler {

	/**
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws IOException, SAXException,
			ParserConfigurationException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		for (String arg : args) {
			// args
			FileReader fileReader = new FileReader(arg);
			PrintWriter out = new PrintWriter(arg + ".csv");
			FakeExcelDummy fakeExcelDummy = new FakeExcelDummy(out);
			// magically fix badly formed HTML
			TokenFilter tokenFilter = new TokenFilter(fileReader);
			ReplaceString replaceString = new ReplaceString();
			replaceString.setFrom("& ");
			replaceString.setTo("&amp; ");
			tokenFilter.addReplaceString(replaceString);
			// parsing
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			xmlReader.setContentHandler(fakeExcelDummy);
			xmlReader.setErrorHandler(fakeExcelDummy);
			InputSource inputSource = new InputSource(tokenFilter);
			xmlReader.parse(inputSource);
		}
	}

	private PrintWriter out;

	private FakeExcelDummy(PrintWriter out) {
		this.out = out;
	}

	private boolean isInCell = false;
	private StringBuffer text = new StringBuffer();

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (localName.equalsIgnoreCase("th")
				|| localName.equalsIgnoreCase("td")) {
			if (!isInCell) {
				isInCell = true;
				text.setLength(0);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equalsIgnoreCase("th")
				|| localName.equalsIgnoreCase("td")) {
			if (isInCell) {
				isInCell = false;
				out.print(StringEscapeUtils.escapeCsv(text.toString()));
				out.print(',');
			}
		}
		if (localName.equalsIgnoreCase("tr")) {
			out.println();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (isInCell) {
			text.append(ch, start, length);
		}
	}
}
