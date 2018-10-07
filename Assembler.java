import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Assembler{
    HashTable op;//Ū�JopCode
    HashTable sym = new HashTable(1024,"label","address");
    ArrayList<String> objectCode;
    ArrayList<String> wrongLabel = new ArrayList<String>();//�ϥ��٥��ŧi��Label�渹
    private int address;//�O�����m
    final int object_Length = 10;//objectProgram�C��Ӽ�
    private int thisLine;//�{�b����w�h��objectCode
    private boolean newLine = false;//�P�_�O�_�s�W���@��s��
    private int lineHead;//�{�b�o�@��bArrayList�O�ĴX��
    private String start = "0";//�_�l��m
    private String end;//������m
    private int j = 0;//�{�b������
    private int endline = 0;//�����渹
    public Assembler()throws IOException{
        op = new HashTable("opCode.txt",1000,"mnemonic","opCode");//Ū�JopCode
        objectCode = new ArrayList<String>();
    }
    //Ū�J�զX�y��
    public void read(String data)throws IOException{
        FileReader fr = new FileReader(data);
        BufferedReader br = new BufferedReader(fr);
        String line;
        boolean currentWrong = false;//�{�b�o�@��O�_����
        int wrong = 0;//����Ū�����h�ֿ�
        for(j=1;(line=br.readLine())!=null;j++){//�@��Ū�A����Ū���ɮ׳̫�
            line = line.replaceAll("[\\.].*$","");//�R������
            line = line.trim();//�R���e��ť�
            if(line.trim().length()<=0)//�����ťզ�
                continue;
            String CX = "";//�Ȯɦs��C'...'��X'...'
            Pattern pattern = Pattern.compile("[CX].*\'.*\'");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                CX = matcher.group();
                line = line.replace(CX,"*");//���NC'...'��X'...'���N��*
            }
            line = line.replaceAll(",\\s*X"," X");//�N,X���N��X
            if(!pattern.compile("^[A-Za-z0-9\\s\\*]+$").matcher(line).find()){//�P�_�O�_���S��r��
                System.out.println("���i���S��r��"+"......��"+j+"��");
                wrong++;
            }
            String[] arr = line.split("[^a-zA-Z0-9*]+");//�^�ƥH�~���r������TOKEN
            for(int i=0;i<arr.length;i++){
                if(arr[i].equals("*"))
                    arr[i] = CX;//�NC'...'��X'...'�s�^�h
            }
            if((objectCode.size()!=0||newLine==true)&&start=="0"){
                System.out.print("�Х��w�qSTART");
                System.out.println("......��"+(j-1)+"��");
                start = "-1";
                wrong++;
            }else if(end!=null){
                System.out.print("END����b�{������");
                System.out.println("......��"+endline+"��");
                wrong++;
                end = null;
            }else if(address-Integer.parseInt("FFFF",16)>0){
                System.out.println(Integer.toHexString(address).toUpperCase()+": �W�X�O����j�p......��"+j+"��");
                wrong++;
                end = "";
                break;
            }
            if((currentWrong=this.putSym(arr))==true){//�����~�T��
                if(currentWrong==true)
                    wrong++;
                System.out.println("......��"+j+"��");
            }
        }
        if(end==null){
            System.out.println("�Щw�qEND......��"+j+"��");
            wrong++;
        }
        for(int j=0;j<wrongLabel.size()/2;j++){
            if(wrongLabel.get(2*j)!=""){
                System.out.print(wrongLabel.get(2*j)+": ���w�q��Label");
                System.out.println("......��"+wrongLabel.get(2*j+1)+"��");
                wrong++;
            }
        }
        this.countLength();
        if(wrong==0)
            printObjectCode();
        else
            System.out.println("\n�`�@��"+wrong+"�ӿ�");
    }
    //�s�iSymbolTabl�B��XobjectCode
    public boolean putSym(String[] arr){
        if(op.search(arr[0])=="false"){//�Ĥ@�Ӥ��Omnemonic
            if(arr.length<2||arr.length>4){
                System.out.printf("�{������Label�Bmmnemonic��operand�զ�");
                return true;
            }else if((arr[0].equals("END")&&arr.length==2)||(arr[1].equals("END")&&arr.length==3)){
                endline = j;//����end�����渹
                String l = arr.length==2?arr[1]:arr[2];//�P�_END�᭱�O�_��label
                if(arr.length==3)if(storeLabel(arr[0])==true)return true;//�sLabel
                storeObject("E");
                newLine = false;
                this.thisLine = 0;
                end = Integer.toHexString(address);
                address = address - Integer.valueOf(start,16);
                if(sym.search(l)=="false"){
                    System.out.print(l+": �S����Label");
                    return true;
                }
                storeObject(addZero(sym.search(l),6));
                objectCode.set(3,addZero(Integer.toHexString(address).toUpperCase(),6));
            }else if(arr[0].equals("END")||arr[0].equals("START")){
                System.out.print(arr[0]+": �����label");
                return true;
            }else if(arr[0].equals("BYTE")||arr[0].equals("WORD")||arr[0].equals("RESB")||arr[0].equals("RESW")){
                System.out.print(arr[0]+"�e���ݦ�Label");
                return true;
            }else if(arr[1].equals("START")){
                if(start!="0"){
                    System.out.print("START����b�{������");
                    return true;
                }else if(arr.length!=3){
                    System.out.print(arr.length<3?"START�᭱���w�q�_�l��m":"START�᭱�u�঳�_�l��m");
                    return true;
                }else if(storeLabel(arr[0])==true)return true;//�sLabel
                else if(!arr[2].matches("[A-Fa-f0-9]+")){
                    System.out.print(arr[2]+": �Шϥ�16�i���");
                    start = "1";
                    return true;
                }else if(arr[2].compareTo("FFFF")>0){
                    start = "1";
                    System.out.print(arr[2]+": �_�l��m�W�L�O����j�p");
                    return true;
                }
                address = Integer.parseInt(arr[2],16);//16�i����10�i��A�r������
                storeObject("H");
                while(arr[0].length()<6){//�ɪť�
                    StringBuffer sb = new StringBuffer();
                    sb.append(arr[0]).append(" ");
                    arr[0] = sb.toString();
                }
                storeObject(arr[0]);
                storeObject("00"+arr[2]);storeObject("000000");
                newLine = true;
                start = arr[2];
            }else{
                //�P�_directives�᭱����[��L�F��
                if((arr[1].equals("BYTE")||arr[1].equals("WORD")||arr[1].equals("RESB")||arr[1].equals("RESW"))&&arr.length>3){
                    System.out.print(arr[1]+"�᭱�u�঳data");
                    return true;
                }else if(arr[1].equals("RESB")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//�sLabel
                    if(!arr[2].matches("[0-9]+")){
                        System.out.print(arr[2]+": �Шϥ�10�i���");
                        return true;
                    }
                    newLine = true;
                    address += Integer.parseInt(arr[2]);//�r������
                }else if(arr[1].equals("RESW")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//�sLabel
                    if(!arr[2].matches("[0-9]+")){
                        System.out.print(arr[2]+": �Шϥ�10�i���");
                        return true;
                    }
                    newLine = true;
                    address += 3*Integer.valueOf(arr[2]);//�r������
                }else if(arr[1].equals("WORD")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//�sLabel
                    if(!arr[2].matches("[0-9]+")){
                        System.out.print(arr[2]+": �Шϥ�10�i���");
                        return true;
                    }
                    storeObject(addZero(Integer.toHexString(Integer.valueOf(arr[2])),6));//�r�����ƦA��16�i��
                    address += 3;
                }else if(arr[1].equals("BYTE")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//�sLabel
                    if(!arr[2].matches("[CX]\\s*\'.*\'$")){
                        System.out.print("BYTE�榡���~");
                        return true;
                    }else if(arr[2].charAt(0)=='X'){//�P�_�OX'..'
                        if(arr[2].substring(arr[2].indexOf('\'')+1,arr[2].length()-1).contains(" ")){
                            System.out.print("X�����঳�ťզr��");
                            return true;
                        }else if(arr[2].length()==3){
                            System.out.print("X�̭����i���ŭ�");
                            return true;    
                        }else if(!arr[2].substring(arr[2].indexOf('\'')+1,arr[2].length()-1).matches("[A-Fa-f0-9]+")){
                            System.out.print(arr[2]+": �Шϥ�16�i���");
                            return true;
                        }
                        arr[2] = arr[2].replaceAll("\\s","");
                        storeObject(arr[2].substring(2,arr[2].length()-1));
                        if((arr[2].length()-3)%2!=0){//X���ݬ����ƭӦr��
                            System.out.print("X���Ф��ݬ����ƭӦr��");
                            return true;
                        }
                        address += (arr[2].length()-3)/2;
                    }else if(arr[2].charAt(0)=='C'&&arr[2].length()>2){//�٬OC'..'
                        if(arr[2].length()==3){
                            System.out.print("C�̭����i���ŭ�");
                            return true;    
                        }
                        int s = arr[2].indexOf('\'')+1;
                        storeObject(stringToASCII(arr[2].substring(s,arr[2].length()-1)));
                        address += arr[2].length()-s-1;
                    }
                }else if(op.search(arr[1])!="false"){//�Omnemonic�A�B�DRSUB
                    if(storeLabel(arr[0])==true)return true;//�sLabel
                    if(!arr[1].equals("RSUB")){
                        if(directOrIndexed(arr,1)==true)return true;//�P�_�O���ީw�}�Ϊ����w�}
                    }else if(arr[1].equals("RSUB")&&arr.length==1){
                        System.out.print("RSUB�᭱���঳operand");
                        return true;
                    }else if(arr[1].equals("RSUB"))
                        storeObject("4C0000");
                    address += 3;
                }else{//�����ŦX�A�{�����~
                    if(storeLabel(arr[0])==true)return true;//�sLabel
                    System.out.printf((arr.length<=2?arr[0]:arr[1])+": mnemonic��directive���~");
                    return true;
                }
            }
        }else{//�Ĥ@�ӬOmnemonic
            if(arr.length<4&&arr.length>1&&!arr[0].equals("RSUB")){//�P�_�DRSUB��Mnemonic
                if((!op.search(arr[1]).equals("false"))||arr[1].equals("BYTE")||arr[1].equals("WORD")||arr[1].equals("RESB")||arr[1].equals("RESW")){//�ĤG�Ӥ]�Omnemonic
                    System.out.print(arr[0]+": �������mnemonic��assembler directives�ۦP�W��");
                    return true;
                }
                if(directOrIndexed(arr,0)==true)return true;//�P�_�O���ީw�}�Ϊ����w�}
            }else if(arr.length==1&&arr[0].equals("RSUB")){
                storeObject("4C0000");
            }else if(arr[0].equals("RSUB")){
                System.out.print("RSUB �᭱���঳ operand");
                return true;
            }else{
                System.out.printf("�{������Label�Bmmnemonic��operand�զ�");
                return true;
            }
            address += 3;
        }
        return false;
    }
    public boolean directOrIndexed(String[] arr,int hasLabel){//�P�_�O���ީw�}�Ϊ����w�}
        if(arr.length==(hasLabel+3)&&arr[arr.length-1].equals("X"))//�O���ީw�}
            //�P�_�O�_�����label
            storeObject(op.search(arr[hasLabel])+(isLabelExit(arr[hasLabel+1],address)?indexedObject(arr[hasLabel+1]):"0000"));
        else if(arr.length==(hasLabel+2)&&!arr[arr.length-1].equals("X"))//�O�����w�}
            //�P�_�O�_�����label
            storeObject(op.search(arr[hasLabel])+(isLabelExit(arr[hasLabel+1],address)?sym.search(arr[hasLabel+1]):"0000"));
        else if(op.search(arr[arr.length-1])!="false"){
            System.out.print(arr[arr.length-1]+": operand�����mnemonic�P�W");
            return true;
        }else{
            System.out.print(arr[arr.length-1]+": indexed���~�A�Х�X�@������");
            return true;
       }
       return false;
    }
    //��m�O�_�W�LFFFF
    public boolean overMemory(int location){
        System.out.println(location);
        System.out.println(Integer.parseInt("FFFF",16));
        System.out.println(location-Integer.parseInt("FFFF",16));
        if((location-Integer.parseInt("FFFF",16))>0){
            System.out.print("�W�L�O����Ŷ�");
            return true;
        }else
            return false;
    }
    //�sLabel
    public boolean storeLabel(String label){
        if(!findLabel(label))//operand�O�_�s�L�o��label
            if(sym.insert(label,Integer.toHexString(address))==false)//10�i����16�i��
                return true;//�s������
        return false;//�s���\
    }
    //���operand�w�q��label
    public boolean findLabel(String a){
        if(sym.search(a).equals("****")){
            this.thisLine = 0;
            while(true){
                String location = sym.searchNoLabel(a,Integer.toHexString(address));
                if(location==null)
                    break;
                newLine = false;
                this.thisLine = 0;
                storeObject("T");
                storeObject(addZero(location,6));//�n�ק諸��}
                storeObject("00");//����
                storeObject(Integer.toHexString(address));//�{�b��m
                continue;
            }
            newLine = true;
            while(wrongLabel.indexOf(a)!=-1){//�p�G���Label�A�N�ϥΦ�Label����m�q�������R��
                wrongLabel.remove(wrongLabel.indexOf(a)+1);
                wrongLabel.remove(a);
            }
            return true;//operand�w�s�L
        }else
            return false;//operand�S���s�L
    }
    //�P�_�O�_���s�blabel
    public boolean isLabelExit(String operand,int location){
        if(sym.search(operand).equals("false")||sym.search(operand).equals("****")){//�S��label
            if(sym.search(operand).equals("false"))
                sym.insert(operand,"****");
            sym.storeLabel(operand,Integer.toHexString(location+1));
            wrongLabel.add(operand);//�NLabel�M��m�s�_��
            wrongLabel.add(Integer.toString(j));
            return false;//���w�q
        }else{//��label
            return true;//�w�w�q
        }
    }
    //����ީw�}��objectCode
    public String indexedObject(String object){
        int a = Integer.parseInt(sym.search(object),16);
        a += Integer.parseInt("8000",16);
        return Integer.toHexString(a);//10�i����16�i��
    }
    //�sobjectCode
    public void storeObject(String object){
        this.thisLine++;
        if(this.thisLine==object_Length||newLine == true){
            objectCode.add("T");
            objectCode.add(addZero(Integer.toHexString(address),6).toUpperCase());//10�i����16�i��
            objectCode.add("00");
            this.thisLine = 0;
            this.newLine = false;
        }
        objectCode.add(object.toUpperCase());
    }
    //�p��C�����
    public void countLength(){
        for(int i=0;i<objectCode.size();i++){
            if(objectCode.get(i)=="T"||objectCode.get(i)=="E"){//Ū��T��E��e�@�檺����
                int sum = 0;
                if(i!=4){//�Ĥ@�椣�κ�
                    for(int j=lineHead+1;j<i;j++)
                        sum += objectCode.get(j).length();
                    objectCode.set(lineHead,addZero(Integer.toHexString(sum/2),2).toUpperCase());
                }
                lineHead = i+2;
            }
        }
    }
    //�����0
    public String addZero(String str,int l){
        while(str.length()<l){
            StringBuffer sb = new StringBuffer();
            sb.append("0").append(str);
            str = sb.toString();
        }
        return str;
    }
    //�r����ASCII
    public String stringToASCII(String s){
        String b = "";
        for(int i=0;i<s.length();i++)
            b += Integer.toHexString((int)s.charAt(i));//10�i����16�i��
        return b;
    }
    //�Lobject program
    public void printObjectCode() throws IOException{
        FileWriter fw = new FileWriter("objectCode.txt");
        for(int i=0;i<objectCode.size();i++){
            if(objectCode.get(i)=="T"||objectCode.get(i)=="E"){
                System.out.println();
                fw.write("\r\n");
            }
            System.out.print(" "+objectCode.get(i));
            fw.write(" "+objectCode.get(i));
        }
        System.out.println();
        fw.close();
    }
    public static void main(String[] argv)throws IOException{
        Assembler as = new Assembler();
        as.read("code.s");
    }
}