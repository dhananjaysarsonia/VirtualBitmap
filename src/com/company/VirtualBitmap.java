package com.company;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VirtualBitmap  {

    //let's go over to the algo for a moment
    //we will first hash the value of flow to get index and mod it with the range of virtual bitmap
    //then the index we got will be hashed again for a specific hash for the particular flow at index. and we will mod it with length of parent bitmap
    //thus we will get the index in the main parent counter

    //inputs
    //n = number of flows
    //String map of flows with Ip address as key and number of elements as values
    //l = length of virtual bitmap
    //for simplicity I am using Integer instead of bit array for counters

    int nFlows;
    int l;
    HashMap<String, Integer> ipCountMap;
    HashMap<String, int[]> ipValueMap;
    int[] internalVirtualHash;

    Random random;
    int m;
    int[] parentBitmap;
    int singleHash;
    public VirtualBitmap(int nFlows, int l, int m, HashMap<String, Integer> ipCountMap){
        this.nFlows = nFlows;
        this.l = l;
        this.m = m;
        this.ipCountMap = ipCountMap;


        initialize();
    }

    private void initialize() {
        random = new Random();
        parentBitmap = new int[m];
        singleHash = (int)(Math.random() * (Integer.MAX_VALUE - 1) + 1);
        internalVirtualHash = random.ints( 1, Integer.MAX_VALUE).distinct().limit(l).toArray();
        ipValueMap = new HashMap<>();
        for(Map.Entry<String, Integer> entry : ipCountMap.entrySet()){
            String ip = entry.getKey();
            int length = entry.getValue();
            int[] valueArray = random.ints( 1, Integer.MAX_VALUE).distinct().limit(length).toArray();
           // int[] valueArray = random.ints( 1, 2*l).limit(length).toArray();
            ipValueMap.put(ip,valueArray);

        }

    }

    //hash element with internal virtual hash which can be number of flows, for each flow we will have a unique hash

    private int getIndex(String ip, int value){
        int elementHash = (value ^ singleHash) % l;
        //System.out.print(elementHash+ " ");
//        int elementHash =  singleHash % l;
        int ipHash = Math.abs(ip.hashCode());
        return (ipHash ^ internalVirtualHash[elementHash]) % m;
    }
    public int getIndexForNoValue(String ip, int i){
        int ipHash = Math.abs(ip.hashCode());
        int elementHash = i;
        return (ipHash ^ internalVirtualHash[elementHash]) % m;
    }

    public void simulate(){
        for(Map.Entry<String, int[]> entry : ipValueMap.entrySet()){
            String ip = entry.getKey();
            int[] values =entry.getValue();
            for(int i : values){
                parentBitmap[getIndex(ip, i)] = 1;
            }
           // System.out.println();System.out.println();
        }
    }

    public void estimate(){
        try {
            int zeroes = 0;
            for(int i : parentBitmap){
                if(i == 0) zeroes++;
            }
            System.out.println("Following value will be estimates");
            FileWriter fileWriter = new FileWriter("outputNums.txt");
            for(Map.Entry<String, int[]> kv : ipValueMap.entrySet()){
                String ip = kv.getKey();
                System.out.println("For IP address: " + ip);
                double l = estimateForParentBitMap(ip, zeroes);
                double r = estimateForVirtualBitMap(ip);
                fileWriter.write(l - r + "," + kv.getValue().length + "\n");
                System.out.println("Estimated " + (l - r) + "    Actual: "+ kv.getValue().length); //as I have taken unique values for each ip
                System.out.println();
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("There is some problem with writing the output to textfile. Try commenting the writer from the function estimate if you do not have enough permissions");
            e.printStackTrace();
        }

    }

    private double estimateForVirtualBitMap(String ip){
        double denominator = l;
        double numerator = 0;
        for(int i = 0; i < l; i++){
           //System.out.print(getIndex(ip, v) + " ");
            if(parentBitmap[getIndexForNoValue(ip,i)] == 0){
               numerator++;
            }
        }
        return l * Math.log(numerator / denominator);
    }

    private double estimateForParentBitMap(String ip, int totalZeroes){
        double denominator = m;
        double numerator = totalZeroes;

        return l * Math.log(numerator / denominator);
    }





    public static void main(String[] args) {
        try {
            String path = "project4input.txt";
            int nFlows = 0;
            int l = 500;
            int m = 500000;


            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter name of file after placing it into the project directory or simply use the demo file in the project by entering project4input.txt");
            path = reader.readLine();
            System.out.println("Please enter the m Value. Demo value was 500000");
            m = Integer.parseInt(reader.readLine());
            System.out.println("Please enter the l Value. Demo value was 500");
            l = Integer.parseInt(reader.readLine());



            FileInputStream fileInputStream = new FileInputStream(path);
            BufferedReader br = new BufferedReader( new InputStreamReader(fileInputStream));
            boolean first = true;
            HashMap<String, Integer> map = new HashMap<>();
            String string;
            while((string = br.readLine()) != null){
                if(first){
                    first = false;
                    nFlows = Integer.parseInt(string);
                }else{
                    String[] ipValue = string.split("\\s+");
                    map.put(ipValue[0], Integer.parseInt(ipValue[1]));
                }
            }

            VirtualBitmap virtualBitmap = new VirtualBitmap(nFlows,l,m,map);
            virtualBitmap.simulate();
            virtualBitmap.estimate();





        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




}
