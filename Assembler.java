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
    private int address;//Location
    final int object_Length = 10;//objectProgram長度
    private int line;//現在此行已多少objectCode
    private boolean newLine = false;//判斷是否新增為一行新的
    private int lineHead;//現在這一行在ArrayList是第幾個
    private String start = "0";//起始位置
    private String end;//結束位置
    public Assembler()throws IOException{
        op = new HashTable("opCode.txt",1000,"mnemonic","opCode");//讀入opCode
        objectCode = new ArrayList<String>();
    }
    //讀入組合語言
    public void read(String data)throws IOException{
        FileReader fr = new FileReader(data);
        BufferedReader br = new BufferedReader(fr);
        String line;
        int j = 0;
        boolean currentWrong = false;//現在這一行是否有錯
        boolean wrong = false;//全部讀完是否有錯
        for(j=1;(line=br.readLine())!=null;j++){//一直讀，直到讀到檔案最後
            //System.out.printf("%d:%s\n",j,line);
            line = line.replaceAll("[\\.].*$","");//刪除註解
            line = line.trim();//刪除前後空白
            if(line.trim().length()<=0)//忽略空白行
                continue;
            String CX = "";//暫時存放C'...'及X'...'
            Pattern pattern = Pattern.compile("[CX]\\s*\'.*\'");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                CX = matcher.group();
                System.out.println(CX);
                line = line.replaceAll(CX,"*");//先將C'...'及X'...'取代成*
            }
            String[] arr = line.split("[,\\s]+");//用空白及,分割
            for(int i=0;i<arr.length;i++){
                if(arr[i].equals("*"))
                    arr[i] = CX;//將C'...'及X'...'存回去
            }
            if(j==2&&start==null){
                System.out.print("請先定義START");
                System.out.println("......第"+(j-1)+"行");
                wrong = true;
            }else if(end!=null){
                System.out.print("END不能在程式中間");
                System.out.println("......第"+(j-1)+"行");
                wrong = true;
                end = null;
            }else if((currentWrong=this.putSym(arr))==true){//有錯誤訊息，不繼續讀
                if(currentWrong==true)
                    wrong = true;
                System.out.println("......第"+j+"行");
            }
            //System.out.println();
        }
        if(end==null){
            System.out.print("請定義END");
            wrong = true;
        }
        this.countLength();
        if(wrong==false)
            printObjectCode();
    }
    //存進SymbolTabl、算出objectCode，順便印出Label、mnemonic...
    public boolean putSym(String[] arr){
        if(op.search(arr[0])=="false"){//第一個不是mnemonic
            if(arr.length<2||arr.length>4){
                System.out.printf("程式應由Label、mmnemonic跟operand組成");
                return true;
            }else if(arr[0].equals("END")&&arr.length==2){
                //System.out.println("end of the program");
                storeObject("E");
                storeObject(addZero(sym.search(arr[1]),6));
                end = Integer.toHexString(address);
                address = address - Integer.valueOf(start,16);
                objectCode.set(3,addZero(Integer.toHexString(address).toUpperCase(),6));
            }else if(arr[0].equals("END")||arr[0].equals("START")||arr[0].equals("BYTE")||arr[0].equals("WORD")||arr[0].equals("RESB")||arr[0].equals("RESW")){
                System.out.print(arr[0]+": 不能當成label");
                return true;
            }else if(arr[1].equals("START")&&arr.length==3){
                address = Integer.parseInt(arr[2],16);//16進位轉10進位，字串轉整數
                if(storeLabel(arr[0])==true)return true;//存Label
                //System.out.printf("Program name is %s\nstart from this line\n",arr[0]);
                storeObject("H");
                storeObject(arr[1]);
                storeObject("00"+arr[2]);
                storeObject("000000");
                newLine = true;
                start = arr[2];
            }else{
                if(arr[1].equals("RESB")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    //System.out.println(arr[1]+" is pesudo instruction code");
                    newLine = true;
                    address += Integer.parseInt(arr[2]);
                }else if(arr[1].equals("RESW")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    //System.out.println(arr[1]+" is pesudo instruction code");
                    newLine = true;
                    address += 3*Integer.parseInt(arr[2],16);
                }else if(arr[1].equals("WORD")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    storeObject(addZero(Integer.toHexString(Integer.valueOf(arr[2])),6));
                    address += 3;
                }else if(arr[1].equals("BYTE")&&arr.length==3){
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(arr[2].charAt(0)=='X'){//判斷是X'..'
                        if(arr[2].substring(arr[2].indexOf('\'')+1,arr[2].length()-1).contains(" ")){
                            System.out.print("X內不能有空白字元");
                            return true;
                        }
                        arr[2] = arr[2].replaceAll("\\s","");
                        storeObject(arr[2].substring(2,arr[2].length()-1));
                        if((arr[2].length()-3)%2!=0){//X內需為偶數個字元
                            System.out.print("X指標內需為偶數個字元");
                            return true;
                        }
                        address += (arr[2].length()-3)/2;
                    }else if(arr[2].charAt(0)=='C'){//還是C'..'
                        int s = arr[2].indexOf('\'')+1;
                        storeObject(stringToASCII(arr[2].substring(s,arr[2].length()-1)));
                        address += arr[2].length()-s-1;
                    }else
                        System.out.println("程式錯誤!");
                }else if(op.search(arr[1])!="false"){//是mnemonic，且非RSUB
                    if(storeLabel(arr[0])==true)return true;//存Label
                    if(!arr[1].equals("RSUB")){
                        if(arr.length==4&&arr[arr.length-1].equals("X"))//是索引定址
                            //判斷是否有找到label
                            storeObject(op.search(arr[1])+(isLabelExit(arr[2],address)?indexedObject(arr[2]):"0000"));
                        else if(arr.length==3&&!arr[arr.length-1].equals("X"))//是直接定址
                            //判斷是否有找到label
                            storeObject(op.search(arr[1])+(isLabelExit(arr[2],address)?sym.search(arr[2]):"0000"));
                        else{
                            System.out.print(arr[arr.length-1]+": indexed錯誤，請用X作為索引");
                            return true;
                        }
                        //System.out.printf("Label: %s\t Mnemonic: %s\t Operand: %s\n",arr[0],arr[1],arr[2]);
                    }else if(arr[1].equals("RSUB")&&arr.length>2){
                        System.out.print("RSUB後面不能有operand");
                        return true;
                    }
                    address += 3;
                }else{//都不符合，程式錯誤
                    System.out.printf(arr[1]+":  opCode錯誤");
                    return true;
                }
            }
        }else{//第一個是mnemonic
            if(arr.length<4&&arr.length>1&&!arr[0].equals("RSUB")){//判斷非RSUB的Mnemonic
                if(!op.search(arr[1]).equals("false")||arr[1].equals("BYTE")||arr[1].equals("WORD")||arr[1].equals("RESB")||arr[1].equals("RESW")){//第二個也是mnemonic
                    System.out.print(arr[0]+": 不能取跟mnemonic相同名稱");
                    return true;
                }
                if(arr.length==3&&arr[arr.length-1].equals("X"))//是索引定址
                    //判斷是否有找到label
                    storeObject(op.search(arr[0])+(isLabelExit(arr[1],address)?indexedObject(arr[1]):"0000"));
                else if(arr.length==2&&!arr[arr.length-1].equals("X"))//是直接定址
                    //判斷是否有找到label
                    storeObject(op.search(arr[0])+(isLabelExit(arr[1],address)?sym.search(arr[1]):"0000"));
                else{
                    System.out.print(arr[arr.length-1]+": indexed錯誤，請用X作為索引");
                    return true;
                }
                //System.out.printf("Label: \t Mnemonic: %s\t Operand: %s\n",arr[0],arr[1]);
            }else if(arr.length==1&&arr[0].equals("RSUB")){
                //System.out.printf("Label: \t Mnemonic: %s\t Operand: \n",arr[0]);
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
    /*public boolean countObjectCode(String[] arr){
        
    }*/
    //存Label
    public boolean storeLabel(String label){
        if(!findLabel(label))//operand是否存過這個label
            if(sym.insert(label,Integer.toHexString(address))==false)//10進位轉16進位
                return true;//存取失敗
        return false;//存成功
    }
    //operand是否存過這個label
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
