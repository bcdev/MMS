package com.bc.fiduceo.reader.slstr;

import org.esa.s3tbx.dataio.util.XPathHelper;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.w3c.dom.*;

import javax.xml.xpath.XPathFactory;

class Manifest {

    private final Document doc;
    private final XPathHelper xPathHelper;

    private MetadataElement manifestElement;

    Manifest(Document manifestDocument) {
        doc = manifestDocument;
        xPathHelper = new XPathHelper(XPathFactory.newInstance().newXPath());
        manifestElement = null;
    }

    MetadataElement getMetadata() {
        if (manifestElement == null) {
            manifestElement = new MetadataElement("Manifest");
            Node node = xPathHelper.getNode("//metadataSection", doc);
            manifestElement.addElement(convertNodeToMetadataElement(node, new MetadataElement(node.getNodeName())));
        }
        return manifestElement;
    }


    String getFileName(String variableName) {
        String fileName = null;

        final NodeList nodeList = xPathHelper.getNodeList("/XFDU/dataObjectSection/dataObject", doc);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node item = nodeList.item(i);
            final NamedNodeMap attributes = item.getAttributes();
            if (attributes != null) {
                final Attr attr = (Attr) (attributes.getNamedItem("ID"));
                final String id = (attr == null) ? "" : attr.getValue();
                if (id.equals(variableName)) {
                    fileName = xPathHelper.getString("./byteStream/fileLocation/@href", item);
                    if (fileName.startsWith("./")) {
                        fileName = fileName.substring(2, fileName.length());
                    }
                    break;
                }
            }
        }



        return fileName;
    }

    private MetadataElement convertNodeToMetadataElement(Node rootNode, MetadataElement rootMetadata) {
        NodeList childNodes = rootNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().contains(":")) {
                    String nodeName = removeNamespace(node.getNodeName());
                    if (hasElementChildNodes(node)) {
                        MetadataElement element = new MetadataElement(nodeName);
                        rootMetadata.addElement(element);
                        addAttributesToElement(node, element);
                        convertNodeToMetadataElement(node, element);
                    } else if (hasAttributeChildNodes(node)) {
                        MetadataElement element = new MetadataElement(nodeName);
                        rootMetadata.addElement(element);
                        final String textContent = node.getTextContent().trim();
                        if (!textContent.equals("")) {
                            element.setAttributeString(nodeName, textContent);
                        }
                        addAttributesToElement(node, element);
                    } else {
                        String nodevalue = node.getTextContent().trim();
                        ProductData textContent = ProductData.createInstance(nodevalue);
                        rootMetadata.addAttribute(new MetadataAttribute(nodeName, textContent, true));
                    }
                } else {
                    convertNodeToMetadataElement(node, rootMetadata);
                }
            }
        }
        return rootMetadata;
    }

    // @todo make package local and add test tb 2020-10-23
    private static String removeNamespace(String withNamespace) {
        if (!withNamespace.contains(":")) {
            return withNamespace;
        }
        return withNamespace.split(":")[1];
    }

    // @todo make package local and add test tb 2020-10-23
    private static boolean hasElementChildNodes(Node rootNode) {
        NodeList childNodes = rootNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    // @todo make static and package local and add test tb 2020-10-23
    private void addAttributesToElement(Node node, MetadataElement element) {
        final NamedNodeMap attributes = node.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) {
            final Node nodeAttribute = attributes.item(j);
            String nodeAttributeValue = nodeAttribute.getTextContent();
            ProductData attributeTextContent = ProductData.createInstance(nodeAttributeValue);
            String attributeNodeName = removeNamespace(nodeAttribute.getNodeName());
            final MetadataAttribute attribute = new MetadataAttribute(attributeNodeName,
                    attributeTextContent, true);
            element.addAttribute(attribute);
        }
    }

    // @todo make package local and add test tb 2020-10-23
    private static boolean hasAttributeChildNodes(Node rootNode) {
        final NamedNodeMap attributeNodes = rootNode.getAttributes();
        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Node node = attributeNodes.item(i);
            if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                return true;
            }
        }
        return false;
    }
}
