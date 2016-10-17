//File: HuffmanCompression.java
//Author: Jonathan Carpenter
//Date: 4/20/2016
//Description: Implements the Huffman Compression and Decompression algorithms
//             described in the book:  Pu, Ida M. Fundamental data compression.
//             Oxford Burlington, MA: Butterworth-Heinemann, 2006. Print.
//             Section 4.1


import java.util.*;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class HuffmanCompression
{
    private HashMap<String, Node> map;
    private TreeMap<String, Node> treemap;
    private HashMap<String, String> huffmanCodes;
    private HashMap<String, String> huffmanDecodes;
    private BinaryStdOut Bout;
    private BinaryStdIn Bin;
    private Node huffmanTree;
    private ByteArrayOutputStream output;
    private String data;
	
    public HuffmanCompression() {

	this.map = null;
	this.treemap = null;
	this.huffmanCodes = null;
	this.huffmanDecodes = null;
	this.huffmanTree = null;
	this.Bin = null;
	this.Bout = null;
	
    }

    //Compress data retrieved from the BufferedInputStream and writes out to BufferedOutputStream
    public String compress(String input) {
	this.Bin = new BinaryStdIn(new BufferedInputStream(new ByteArrayInputStream(input.getBytes())));
	this.huffmanCodes = new HashMap<String, String>();
	this.output = new ByteArrayOutputStream();
	this.Bout = new BinaryStdOut(new BufferedOutputStream(output));
	getFrequencies();
	sortMapByValue();
	this.huffmanTree = buildHuffmanTree();
	createHuffmanEncodeMap(huffmanTree, "");
	writeTree(huffmanTree);
	encode();

	return output.toString();
    }

    //Decompress data retrieved from the BufferedInputStream and writes out to BufferedOutputstream
    public String decompress(String input) {

	this.Bin = new BinaryStdIn(new BufferedInputStream(new ByteArrayInputStream(input.getBytes())));
	this.huffmanDecodes = new HashMap<String, String>();
	this.output = new ByteArrayOutputStream();
	this.Bout = new BinaryStdOut(new BufferedOutputStream(output));
	this.huffmanTree = readTree();
	createHuffmanDecodeMap(huffmanTree, "");
	decode();

	return output.toString();

    }

    //Encodes huffman tree into BufferedOutputStream
    //Parameter: Node - Root of huffman tree
    private void writeTree(Node node) {

	if(node.leftChild == null && node.rightChild == null) {

	    Bout.write(true);
	    Bout.write(node.string.charAt(0), 8);

	}
	else {

	    Bout.write(false);
	    writeTree(node.leftChild);
	    writeTree(node.rightChild);

	}
    }

    
    //Reads encoded tree from BufferedInputStream and builds huffman tree
    //Returns: Returns the root node of the huffman tree
    private Node readTree() {

	if(Bin.readBoolean()) {

	    char c = Bin.readChar();
	    return new Node(Character.toString(c), 0);

	}
	else {
	    return new Node(readTree(), readTree());
	}
    }

    //Encodes compressed data into the BufferedOutputStream
    private void encode() {
	
	long count = 0;

	for(int i=0;i<this.data.length();i++){
	    String code = huffmanCodes.get(Character.toString(this.data.charAt(i)));
	    count += code.length();
	}

	Bout.write(count);

	for(int i=0;i<this.data.length();i++) {

	    String code = huffmanCodes.get(Character.toString(this.data.charAt(i)));

	    for(int j=0;j<code.length();j++) {

		if(code.charAt(j) == '0'){
		    Bout.write(false);
		}
		else {
		    Bout.write(true);
		}
	    }

	}
	Bout.close();
    }

    //Reads in compressed data from BufferedInputStream
    private void decode() {

	long numBits = Bin.readLong();
	String path = "";

	for(long i=0;i < numBits;i++) {

	    if(Bin.readBoolean()){
		path += "1";
	    }
	    else {
		path += "0";
	    }

	    if(huffmanDecodes.containsKey(path)) {

		String c = huffmanDecodes.get(path);
		Bout.write(c.charAt(0));
		path = "";

	    }
	}

	Bout.close();

    }

    //Recursivley builds a mapping of characters to huffman codes
    private void createHuffmanEncodeMap(Node currentNode, String path) {
	if(currentNode == null) {

	    return;

	}
	else {

	    if(currentNode.leftChild == null && currentNode.rightChild == null) {
		this.huffmanCodes.put(currentNode.string, path);
		System.out.println(currentNode.string + " " + path);
	    }

	    createHuffmanEncodeMap(currentNode.leftChild, path + "0");
	    createHuffmanEncodeMap(currentNode.rightChild, path + "1");
	    
	}
    }

    //Recursivley builds a mapping of huffman codes to characters
    private void createHuffmanDecodeMap(Node currentNode, String path) {
	if(currentNode == null) {

	    return;

	}
	else {

	    if(currentNode.leftChild == null && currentNode.rightChild == null) {
		this.huffmanDecodes.put(path, currentNode.string);
	    }

	    createHuffmanDecodeMap(currentNode.leftChild, path + "0");
	    createHuffmanDecodeMap(currentNode.rightChild, path + "1");
	    
	}
    }


    //Builds a huffman tree
    //Returns the root of the huffman tree
    private Node buildHuffmanTree() {

	Node node = null;

	if(map.size() == 1) {
	    
	    Map.Entry<String, Node> entry1 = treemap.pollFirstEntry();
	    map.remove(entry1.getKey());
	    node = new Node(entry1.getValue());
	    node.frequency = entry1.getValue().frequency;
	    node.string = entry1.getKey();
	    map.put(node.string, node);
	    treemap.put(node.string, node);
	    
	}
	
	while(map.size() != 1) {

	    Map.Entry<String, Node> entry1 = treemap.pollFirstEntry();
	    Map.Entry<String, Node> entry2 = treemap.pollFirstEntry();
	    map.remove(entry1.getKey());
	    map.remove(entry2.getKey());

	    node = new Node(entry2.getValue(), entry1.getValue());
	    node.frequency = entry2.getValue().frequency + entry2.getValue().frequency;
	    node.string = entry2.getKey() + entry1.getKey();

	    map.put(node.string, node);
	    treemap.put(node.string, node);

	}

	return node;
    }


    //Sorts TreeMap by frequencies
    private void sortMapByValue() {

	Comparator<String> comparator = new ValueComparator(this.map);
	treemap = new TreeMap<String, Node>(comparator);
	treemap.putAll(map);

    }


    //Gets the frequencies of characters in BufferedInputStream
    private void getFrequencies() {

	this.map = new HashMap<String, Node>();
	this.data = Bin.readString();
	
	for(int i=0;i<data.length();i++) {

	    String c = Character.toString(data.charAt(i));
	    if (map.containsKey(c)) {

		Node node = map.get(c);
		node.frequency++;
		map.put(c, node);		

	    }
	    else {
		map.put(c, new Node(c, 1));
	    }
	}
    }

    public static void main(String[] args) {
	String inputStr = "This is a test!";
	
	HuffmanCompression huffman = new HuffmanCompression();
	String c = huffman.compress(inputStr);
	String d = huffman.decompress(c);
	
	System.out.println(c);
	System.out.println(d);

    }
}

class ValueComparator implements Comparator<String>
{
    
    HashMap<String, Node> map;

    public ValueComparator(HashMap<String, Node> map) {
	this.map = map;
    }

    public int compare(String a, String b) {

	if (map.get(a).frequency <= map.get(b).frequency) {
	    return -1;
	}
	else {
	    return 1;
	}
    }
}

class Node 
{
    int frequency;
    String string;
    Node leftChild;
    Node rightChild;

    public Node(String string, int frequency) {
	this.frequency = frequency;
	this.string = string;
	this.leftChild = null;
	this.rightChild = null;

    }

    public Node(Node lc, Node rc) {
	this.frequency = 0;
	this.string = "";
	this.leftChild = lc;
	this.rightChild = rc;
    }

    public Node(Node lc) {
	this.frequency = 0;
	this.string = "";
	this.leftChild = lc;
	this.rightChild = null;
    }
}

