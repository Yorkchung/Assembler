import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Assembler{
    HashTable op;//讀入opCode
    HashTable sym = new HashTable(1024,"label","address");
    ArrayList<String> objectCode;
    ArrayList<String> wrongLabel = new ArrayList<String>();//使用還未宣告的Label行號
    private int address;//Location
    final int object_Length = 10;//objectProgram長度
    private int line;//現在此行已多少objectCode
    private boolean newLine = false;//判斷是否新增為一行新的
    private int lineHead;//現在這一行在ArrayList是第幾個
    private String start = "0";//起始位置
    private String end;//結束位置
    private int j = 0;//現在執行行數
    public Assembler()throws IOException{
        op = new HashTable("opCode.txt",1000,"mnemonic","opCode");//讀入opCode
        objectCode = new ArrayList<String>();
    }
    //讀入組合語言
    public void read(String data)throws IOException{
        FileReader fr = new FileReader(data);
        BufferedReader br = new BufferedReader(fr);
        String line;
        boolean currentWrong = false;//現在這一行是否有錯
        int wrong = 0;//全部讀完有多少錯
        for(j=1;(line=br.readLine())!=null;j++){//一直讀，直到讀到檔案最後
            //System.out.printf("%d:%s\n",j,line);
            line = line.replaceAll("[\\.].*$","");//刪除註解
            line = line.trim();//刪除前後空白
            if(line.trim().length()<=0)//忽略空白行
                continue;
            String CX = "";//暫時存放C'...'及X'...'
            Pattern pattern = Pattern.compile("[CX].*\'.*\'");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                CX = matcher.group();
                line = line.replace(CX,"*");//先將C'...'及X'...'取代成*
            }
            line = line.replaceAll(",\\s*X"," X");//將,X取代為X
            if(!pattern.compile("^[A-Za-z0-9\\s\\*]+$").matcher(line).find()){//判斷是否有特殊字元
                System.out.println("不可有特殊字元"+"......第"+j+"行");
                wrong++;
            }
            String[] arr = line.split("[\\s]+");//用空白分割
            for(int i=0;i<arr.length;i++){
                if(arr[i].equals("*"))
                    arr[i] = CX;//將C'...'及X'...'存回去
            }
            if(j==2&&start=="0"){
                System.out.print("請先定義START");
                System.out.println("......第"+(j-1)+"行");
                wrong++;
            }else if(end!=null){
                System.out.print("END不能在程式中間");
                System.out.println("......第"+(j-1)+"行");
                wrong++;
                end = null;
            }else if(address-Integer.parseInt("FFFF",16)>0){
                System.out.println(Integer.toHexString(address).toUpperCase()+": 超出記憶體大小......第"+j+"行");
                wrong++;
                end = "";
                break;
            }
            if((currentWrong=this.putSym(arr))==true){//有錯誤訊息
                if(currentWrong==true)
                    wrong++;
                System.out.println("......第"+j+"行");
            }
        }
        if(end==null){
            System.out.println("請定義END");
            wrong++;
        }
        for(int j=0;j<wrongLabel.size()/2;j++){
            if(wrongLabel.get(2*j)!=""){
                System.out.print(wrongLabel.get(2*j)+": 未定義的Label");
                System.out.println("......第"+wrongLabel.get(2*j+1)+"行");
                wrong++;
            }
        }
        this.countLength();
        if(wrong==0)
            printObjectCode();
        else
            System.out.println("\n總共有"+wrong+"個錯");
    }
    //存進SymbolTabl、算出objectCode
    public boolean putSym(String[] arr){
        if(op.search(arr[0])=="false"){//第一個不是mnemonic
            if(arr.length<2||arr.length>4){
                System.out.printf("程式應由Label、mmnemonic跟operand組成");
                return true;
            }else if(arr[0].equals("END")&&arr.length==2){
                storeObject("E");
                end = Integer.toHexString(address);
                address = address - Integer.valueOf(start,16);
                if(sym.search(arr[1])=="false"){
                    System.out.print(arr[1]+": 沒有此Label");
                    return true;
                }
                storeObject(addZero(sym.search(arr[1]),6));
                objectCode.set(3,addZero(Integer.toHexString(address).toUpperCase(),6));
            }else if(arr[0].equals("END")||arr[0].equals("START")){
                System.out.print(arr[0]+": 不能當成label");
                return true;
            }else if(arr[0].equals("BYTE")||arr[0].equals("WORD")||arr[0].equals("RESB")||arr[0].equals("RESW")){
                System.out.print(arr[0]+"前面需有Label");
                return true;
            }else if(arr[1].equals("START")){
                if(start!="0"){
                    System.out.print("START不能在程式中間");
                    return true;
                }else if(arr.length!=3){
                    System.out.print(arr.length<3?"START後面應定義起始位置":"START後面只能有起始位置");
                    return true;
                }else if(storeLabel(arr[0])==true)return true;//存Label
                else if(!arr[2].matches("[A-Fa-f0-9]+")){
                    System.out.print(arr[2]+": 請使用16進位制");
                    start = "1";
                    return true;
                }else if(arr[2].compareTo("FFFF")>0){
                    start = "1";
                    System.out.print(arr[2]+": 起始位置超過記憶體大小");
                    return true;
                }
                address = Integer.parseInt(arr[2],16);//16進位轉10進位，字串轉整數
                storeObject("H");storeObject(arr[1]);storeObject("00"+arr[2]);storeObject("000000");
                newLine = true;
                start = arr[2];
            }else{
                if(arr[1].equals("RESB")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(!arr[2].matches("[0-9]+")){
                        System.out.print(arr[2]+": 請使用10進位制");
                        return true;
                    }
                    newLine = true;
                    address += Integer.parseInt(arr[2]);//字串轉整數
                }else if(arr[1].equals("RESW")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(!arr[2].matches("[0-9]+")){
                        System.out.print(arr[2]+": 請使用10進位制");
                        return true;
                    }
                    newLine = true;
                    address += 3*Integer.valueOf(arr[2]);//字串轉整數
                }else if(arr[1].equals("WORD")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(!arr[2].matches("[0-9]+")){
                        System.out.print(arr[2]+": 請使用10進位制");
                        return true;
                    }
                    storeObject(addZero(Integer.toHexString(Integer.valueOf(arr[2])),6));//字串轉整數再轉16進位
                    address += 3;
                }else if(arr[1].equals("BYTE")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(!arr[2].matches("[CX]\\s*\'.*\'$")){
                        System.out.print("BYTE格式錯誤");
                        return true;
                    }else if(arr[2].charAt(0)=='X'){//判斷是X'..'
                        if(arr[2].substring(arr[2].indexOf('\'')+1,arr[2].length()-1).contains(" ")){
                            System.out.print("X內不能有空白字元");
                            return true;
                        }else if(arr[2].length()==3){
                            System.out.print("X裡面不可為空值");
                            return true;    
                        }else if(!arr[2].substring(arr[2].indexOf('\'')+1,arr[2].length()-1).matches("[A-Za-z0-9]+")){
                            System.out.print(arr[2]+": 請使用16進位制");
                            return true;
                        }
                        arr[2] = arr[2].replaceAll("\\s","");
                        storeObject(arr[2].substring(2,arr[2].length()-1));
                        if((arr[2].length()-3)%2!=0){//X內需為偶數個字元
                            System.out.print("X指標內需為偶數個字元");
                            return true;
                        }
                        address += (arr[2].length()-3)/2;
                    }else if(arr[2].charAt(0)=='C'&&arr[2].length()>2){//還是C'..'
                        int s = arr[2].indexOf('\'')+1;
                        storeObject(stringToASCII(arr[2].substring(s,arr[2].length()-1)));
                        address += arr[2].length()-s-1;
                    }else{
                        System.out.println("請用C''或X''");
                        return true;
                    }
                }else if(op.search(arr[1])!="false"){//是mnemonic，且非RSUB
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(!arr[1].equals("RSUB")){
                        if(arr.length==4&&arr[arr.length-1].equals("X"))//是索引定址
                            //判斷是否有找到label
                            storeObject(op.search(arr[1])+(isLabelExit(arr[2],address)?indexedObject(arr[2]):"0000"));
                        else if(arr.length==3&&!arr[arr.length-1].equals("X"))//是直接定址
                            //判斷是否有找到label
                            storeObject(op.search(arr[1])+(isLabelExit(arr[2],address)?sym.search(arr[2]):"0000"));
                        else if(op.search(arr[arr.length-1])!="false"){
                            System.out.print(arr[arr.length-1]+": operand不能跟mnemonic同名");
                            return true;
                        }else{
                            System.out.print(arr[arr.length-1]+": indexed錯誤，請用X作為索引");
                            return true;
                        }
                    }else if(arr[1].equals("RSUB")&&arr.length>2){
                        System.out.print("RSUB後面不能有operand");
                        return true;
                    }
                    address += 3;
                }else{//都不符合，程式錯誤
                    if(storeLabel(arr[0])==true)return true;//存Label
                    System.out.printf("opCode錯誤");
                    return true;
                }
            }
        }else{//第一個是mnemonic
            if(arr.length<4&&arr.length>1&&!arr[0].equals("RSUB")){//判斷非RSUB的Mnemonic
                if((!op.search(arr[1]).equals("false"))||arr[1].equals("BYTE")||arr[1].equals("WORD")||arr[1].equals("RESB")||arr[1].equals("RESW")){//第二個也是mnemonic
                    System.out.print(arr[0]+": 不能取跟mnemonic相同名稱");
                    return true;
                }
                if(arr.length==3&&arr[arr.length-1].equals("X"))//是索引定址
                    //判斷是否有找到label
                    storeObject(op.search(arr[0])+(isLabelExit(arr[1],address)?indexedObject(arr[1]):"0000"));
                else if(arr.length==2&&!arr[arr.length-1].equals("X"))//是直接定址
                    //判斷是否有找到label
                    storeObject(op.search(arr[0])+(isLabelExit(arr[1],address)?sym.search(arr[1]):"0000"));
                else if(op.search(arr[arr.length-1])!="false"){
                    System.out.print(arr[arr.length-1]+": operand不能跟mnemonic同名");
                    return true;
                }else{
                    System.out.print(arr[arr.length-1]+": indexed錯誤，請用X作為索引");
                    return true;
                }
            }else if(arr.length==1&&arr[0].equals("RSUB")){
                storeObject("4C0000");
            }else if(arr[0].equals("RSUB")){
                System.out.print("RSUB 後面不能有 operand");
                return true;
            }else{
                System.out.printf("程式應由Label、mmnemonic跟operand組成");
                return true;
            }
            address += 3;
        }
        return false;
    }
    //位置是否超過FFFF
    public boolean overMemory(int location){
        System.out.println(location);
        System.out.println(Integer.parseInt("FFFF",16));
        System.out.println(location-Integer.parseInt("FFFF",16));
        if((location-Integer.parseInt("FFFF",16))>0){
            System.out.print("超過記憶體空間");
            return true;
        }else
            return false;
    }
    //存Label
    public boolean storeLabel(String label){
        if(!findLabel(label))//operand是否存過這個label
            if(sym.insert(label,Integer.toHexString(address))==false)//10進位轉16進位
                return true;//存取失敗
        return false;//存成功
    }
    //找到operand定義的label
    public boolean findLabel(String a){
        if(sym.search(a).equals("****")){
            this.line = 0;
            while(true){
                String location = sym.searchNoLabel(a,Integer.toHexString(address));
                if(location==null)
                    break;
                newLine = false;
                storeObject("T");
                storeObject(addZero(location,6));//要修改的位址
                storeObject("00");//長度
                storeObject(Integer.toHexString(address));//現在位置
                continue;
            }
            newLine = true;
            while(wrongLabel.indexOf(a)!=-1){//如果找到Label，將使用此Label的位置從紀錄中刪除
                wrongLabel.remove(wrongLabel.indexOf(a)+1);
                wrongLabel.remove(a);
            }
            return true;//operand已存過
        }else
            return false;//operand沒有存過
    }
    //判斷是否有存在label
    public boolean isLabelExit(String operand,int location){
        if(sym.search(operand).equals("false")||sym.search(operand).equals("****")){//沒有label
            if(sym.search(operand).equals("false"))
                sym.insert(operand,"****");
            sym.storeLabel(operand,Integer.toHexString(location+1));
            wrongLabel.add(operand);//將Label和位置存起來
            wrongLabel.add(Integer.toString(j));
            return false;//未定義
        }else{//有label
            return true;//已定義
        }
    }
    //算索引定址的objectCode
    public String indexedObject(String object){
        int a = Integer.parseInt(sym.search(object),16);
        a += Integer.parseInt("8000",16);
        return Integer.toHexString(a);//10進位轉16進位
    }
    //存objectCode
    public void storeObject(String object){
        this.line++;
        int sum = 0;
        if(this.line==10||newLine == true){
            objectCode.add("T");
            objectCode.add(addZero(Integer.toHexString(address),6).toUpperCase());//10進位轉16進位
            objectCode.add("00");
            this.line = 0;
            this.newLine = false;
        }
        objectCode.add(object.toUpperCase());
    }
    //計算每行長度
    public void countLength(){
        for(int i=0;i<objectCode.size();i++){
            if(objectCode.get(i)=="T"||objectCode.get(i)=="E"){
                int sum = 0;
                if(i!=4){
                    for(int j=lineHead+1;j<i;j++)
                        sum += objectCode.get(j).length();
                    objectCode.set(lineHead,addZero(Integer.toHexString(sum/2),2).toUpperCase());
                }
                lineHead = i+2;
            }
        }
    }
    //左邊補0
    public String addZero(String str,int l){
        while(str.length()<l){
            StringBuffer sb = new StringBuffer();
            sb.append("0").append(str);
            str = sb.toString();
        }
        return str;
    }
    //字串轉ASCII
    public String stringToASCII(String s){
        String b = "";
        for(int i=0;i<s.length();i++)
            b += Integer.toHexString((int)s.charAt(i));//10進位轉16進位
        return b;
    }
    //印object program
    public void printObjectCode(){
        for(int i=0;i<objectCode.size();i++){
            if(objectCode.get(i)=="T"||objectCode.get(i)=="E")
                System.out.println();
            System.out.print(objectCode.get(i)+" ");
        }
    }
    public static void main(String[] argv)throws IOException{
        Assembler as = new Assembler();
        as.read("one.txt");
    }
}
