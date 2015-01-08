/*
 *  Copyright (C) 2011 Claudius Teodorescu
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */

package org.expath.exist.pdf.metadata;

/**
 * Implements the pdf:get-text-fields() function for eXist.
 * 
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.input.CloseShieldInputStream;

import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.memtree.DocumentBuilderReceiver;
import org.exist.memtree.MemTreeBuilder;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.functions.map.MapType;
import org.exist.xquery.value.Base64BinaryValueType;
import org.exist.xquery.value.BinaryValue;
import org.exist.xquery.value.BinaryValueFromInputStream;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.exist.xquery.value.NodeValue;
import org.expath.exist.pdf.ExistExpathPdfModule;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ro.kuberam.libs.java.pdf.metadata.Metadata;

public class GetContentMetadataFunction extends BasicFunction {

	private final static Logger log = Logger.getLogger(GetContentMetadataFunction.class);

	public final static FunctionSignature signature = new FunctionSignature(new QName("get-content-metadata",
			ExistExpathPdfModule.NAMESPACE_URI, ExistExpathPdfModule.PREFIX),
			"Get the XMP metadata from a PDF contents.",
			new SequenceType[] { new FunctionParameterSequenceType("contents", Type.BASE64_BINARY,
					Cardinality.ZERO_OR_ONE, "PDF contents where to get the metadata from.") },
			new FunctionReturnSequenceType(Type.NODE, Cardinality.ZERO_OR_ONE,
					"an XML document containing XMP metadata."));

	public GetContentMetadataFunction(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	@Override
	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
		
		Sequence result = new ValueSequence();
		
		StreamResult resultAsStreamResult = null;
		
		try {
			byte[] binary = (byte[]) ((BinaryValue) args[0].itemAt(0)).toJavaObject(byte[].class);
			BinaryValue data = BinaryValueFromInputStream.getInstance(context, new Base64BinaryValueType(),
					new ByteArrayInputStream(binary));
			resultAsStreamResult = Metadata.run(data.getInputStream());
		} catch (Exception ex) {
			throw new XPathException(ex.getMessage());
		}
		
		ByteArrayInputStream resultDocAsInputStream = null;
		try {
			resultDocAsInputStream = new ByteArrayInputStream(resultAsStreamResult.getWriter().toString()
					.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new XPathException(ex.getMessage());
		}

		XMLReader reader = null;

		context.pushDocumentContext();
		try {
			InputSource src = new InputSource(new CloseShieldInputStream(resultDocAsInputStream));

			reader = context.getBroker().getBrokerPool().getParserPool().borrowXMLReader();
			MemTreeBuilder builder = context.getDocumentBuilder();
			DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(builder, true);
			reader.setContentHandler(receiver);
			reader.parse(src);
			Document doc = receiver.getDocument();

			result = (NodeValue) doc;
		} catch (SAXException saxe) {
			// do nothing, we will default to trying to return a string below
		} catch (IOException ioe) {
			// do nothing, we will default to trying to return a string below
		} finally {
			context.popDocumentContext();

			if (reader != null) {
				context.getBroker().getBrokerPool().getParserPool().returnXMLReader(reader);
			}
		}

		return result;
	}
}